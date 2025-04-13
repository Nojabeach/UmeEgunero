package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.TipoDestinatario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.MensajeRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado para la pantalla de componer mensaje
 */
data class ComponerMensajeUiState(
    val asunto: String = "",
    val contenido: String = "",
    val destinatario: Usuario? = null,
    val destinatarios: List<Usuario> = emptyList(),
    val tipoDestinatario: TipoDestinatario = TipoDestinatario.INDIVIDUAL,
    val adjuntos: List<String> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val cargando: Boolean = false,
    val enviando: Boolean = false,
    val enviado: Boolean = false,
    val error: String? = null,
    val textoFiltroBusqueda: String = "",
    val usuariosFiltrados: List<Usuario> = emptyList(),
    val mensajeRespuesta: Mensaje? = null
)

/**
 * ViewModel para la pantalla de componer mensaje
 */
@HiltViewModel
class ComponerMensajeViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ComponerMensajeUiState())
    val uiState: StateFlow<ComponerMensajeUiState> = _uiState.asStateFlow()
    
    private var usuarioActual: Usuario? = null
    
    init {
        cargarUsuarios()
    }
    
    /**
     * Carga un destinatario por su ID
     */
    fun cargarDestinatario(destinatarioId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(cargando = true) }
                
                // Buscar usuario por ID
                when (val result = usuarioRepository.obtenerUsuarioPorId(destinatarioId)) {
                    is Result.Success<Usuario> -> {
                        val usuario = result.data
                        _uiState.update { 
                            it.copy(
                                destinatario = usuario,
                                tipoDestinatario = TipoDestinatario.INDIVIDUAL,
                                cargando = false
                            ) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "No se pudo cargar el destinatario: ${result.exception?.message}",
                                cargando = false
                            ) 
                        }
                    }
                    is Result.Loading<*> -> {
                        // Estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar destinatario")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar destinatario: ${e.message}",
                        cargando = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Carga la lista de usuarios disponibles para enviar mensajes
     */
    private fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            try {
                // Obtener usuario actual
                usuarioActual = usuarioRepository.obtenerUsuarioActual()
                
                if (usuarioActual == null) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo identificar al usuario actual",
                            cargando = false
                        ) 
                    }
                    return@launch
                }
                
                // Obtener usuarios del mismo centro
                val centroId = usuarioActual!!.perfiles.firstOrNull()?.centroId
                if (centroId == null) {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo determinar el centro del usuario",
                            cargando = false
                        )
                    }
                    return@launch
                }
                
                val usuarios = usuarioRepository.obtenerUsuariosPorCentro(centroId)
                    .filter { it.dni != usuarioActual!!.dni } // Excluir al usuario actual
                
                _uiState.update { 
                    it.copy(
                        usuarios = usuarios,
                        usuariosFiltrados = usuarios,
                        cargando = false
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar usuarios")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar usuarios: ${e.message}",
                        cargando = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Actualiza el filtro de búsqueda de usuarios
     */
    fun actualizarFiltroBusqueda(texto: String) {
        val filtrados = if (texto.isBlank()) {
            _uiState.value.usuarios
        } else {
            _uiState.value.usuarios.filter { usuario ->
                usuario.nombre.contains(texto, ignoreCase = true) ||
                usuario.email.contains(texto, ignoreCase = true)
            }
        }
        
        _uiState.update { 
            it.copy(
                textoFiltroBusqueda = texto,
                usuariosFiltrados = filtrados
            ) 
        }
    }
    
    /**
     * Selecciona un destinatario para el mensaje (modo individual)
     */
    fun seleccionarDestinatario(usuario: Usuario) {
        _uiState.update { 
            it.copy(
                destinatario = usuario,
                tipoDestinatario = TipoDestinatario.INDIVIDUAL
            ) 
        }
    }
    
    /**
     * Añade un usuario a la lista de destinatarios (modo grupo)
     */
    fun agregarDestinatario(usuario: Usuario) {
        val nuevosDestinatarios = _uiState.value.destinatarios.toMutableList()
        
        if (!nuevosDestinatarios.contains(usuario)) {
            nuevosDestinatarios.add(usuario)
        }
        
        _uiState.update { 
            it.copy(
                destinatarios = nuevosDestinatarios,
                tipoDestinatario = TipoDestinatario.GRUPO
            ) 
        }
    }
    
    /**
     * Elimina un usuario de la lista de destinatarios
     */
    fun eliminarDestinatario(usuario: Usuario) {
        val nuevosDestinatarios = _uiState.value.destinatarios.toMutableList()
        nuevosDestinatarios.remove(usuario)
        
        _uiState.update { 
            it.copy(destinatarios = nuevosDestinatarios) 
        }
    }
    
    /**
     * Actualiza el tipo de destinatario
     */
    fun actualizarTipoDestinatario(tipo: TipoDestinatario) {
        _uiState.update { 
            it.copy(tipoDestinatario = tipo) 
        }
    }
    
    /**
     * Actualiza el asunto del mensaje
     */
    fun actualizarAsunto(asunto: String) {
        _uiState.update { 
            it.copy(asunto = asunto) 
        }
    }
    
    /**
     * Actualiza el contenido del mensaje
     */
    fun actualizarContenido(contenido: String) {
        _uiState.update { 
            it.copy(contenido = contenido) 
        }
    }
    
    /**
     * Añade un archivo adjunto a la lista
     */
    fun agregarAdjunto(url: String) {
        val nuevosAdjuntos = _uiState.value.adjuntos.toMutableList()
        nuevosAdjuntos.add(url)
        
        _uiState.update { 
            it.copy(adjuntos = nuevosAdjuntos) 
        }
    }
    
    /**
     * Elimina un archivo adjunto de la lista
     */
    fun eliminarAdjunto(url: String) {
        val nuevosAdjuntos = _uiState.value.adjuntos.toMutableList()
        nuevosAdjuntos.remove(url)
        
        _uiState.update { 
            it.copy(adjuntos = nuevosAdjuntos) 
        }
    }
    
    /**
     * Configura el mensaje como respuesta a otro
     */
    fun configurarComoRespuesta(mensaje: Mensaje) {
        _uiState.update { 
            it.copy(
                mensajeRespuesta = mensaje,
                asunto = if (!mensaje.asunto.startsWith("RE:")) "RE: ${mensaje.asunto}" else mensaje.asunto,
                destinatario = _uiState.value.usuarios.firstOrNull { usuario -> usuario.dni == mensaje.remitente }
            ) 
        }
    }
    
    /**
     * Envía el mensaje
     */
    fun enviarMensaje() {
        if (usuarioActual == null) {
            _uiState.update { it.copy(error = "No se pudo identificar al usuario actual") }
            return
        }
        
        val state = _uiState.value
        
        // Validaciones
        if (state.asunto.isBlank()) {
            _uiState.update { it.copy(error = "El asunto no puede estar vacío") }
            return
        }
        
        if (state.contenido.isBlank()) {
            _uiState.update { it.copy(error = "El contenido no puede estar vacío") }
            return
        }
        
        when (state.tipoDestinatario) {
            TipoDestinatario.INDIVIDUAL -> {
                if (state.destinatario == null) {
                    _uiState.update { it.copy(error = "Debes seleccionar un destinatario") }
                    return
                }
            }
            TipoDestinatario.GRUPO -> {
                if (state.destinatarios.isEmpty()) {
                    _uiState.update { it.copy(error = "Debes seleccionar al menos un destinatario") }
                    return
                }
            }
            else -> {}
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true) }
            
            try {
                when (state.tipoDestinatario) {
                    TipoDestinatario.INDIVIDUAL -> {
                        val destinatario = state.destinatario!!
                        val mensaje = Mensaje(
                            remitente = usuarioActual!!.dni,
                            remitenteNombre = usuarioActual!!.nombre,
                            destinatarioId = destinatario.dni,
                            destinatarioNombre = destinatario.nombre,
                            asunto = state.asunto,
                            contenido = state.contenido,
                            fechaEnvio = Timestamp.now(),
                            adjuntos = state.adjuntos,
                            tipoDestinatario = tipoDestinatarioToString(TipoDestinatario.INDIVIDUAL),
                            respuestaA = state.mensajeRespuesta?.id
                        )
                        
                        mensajeRepository.enviarMensaje(mensaje)
                    }
                    TipoDestinatario.GRUPO -> {
                        // Enviar un mensaje a cada destinatario
                        for (destinatario in state.destinatarios) {
                            val mensaje = Mensaje(
                                remitente = usuarioActual!!.dni,
                                remitenteNombre = usuarioActual!!.nombre,
                                destinatarioId = destinatario.dni,
                                destinatarioNombre = destinatario.nombre,
                                asunto = state.asunto,
                                contenido = state.contenido,
                                fechaEnvio = Timestamp.now(),
                                adjuntos = state.adjuntos,
                                tipoDestinatario = tipoDestinatarioToString(TipoDestinatario.GRUPO),
                                respuestaA = state.mensajeRespuesta?.id
                            )
                            
                            mensajeRepository.enviarMensaje(mensaje)
                        }
                    }
                    TipoDestinatario.CLASE, TipoDestinatario.CENTRO -> {
                        // Funcionalidad para mensajes a clases o centros completos
                        // (a implementar según necesidades específicas)
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        enviando = false,
                        enviado = true
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar mensaje")
                _uiState.update { 
                    it.copy(
                        enviando = false,
                        error = "Error al enviar mensaje: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Reinicia el formulario
     */
    fun reiniciarFormulario() {
        _uiState.update { 
            it.copy(
                asunto = "",
                contenido = "",
                destinatario = null,
                destinatarios = emptyList(),
                adjuntos = emptyList(),
                tipoDestinatario = TipoDestinatario.INDIVIDUAL,
                enviado = false,
                mensajeRespuesta = null
            ) 
        }
    }

    // Función para convertir TipoDestinatario a String
    private fun tipoDestinatarioToString(tipo: TipoDestinatario): String {
        return when (tipo) {
            TipoDestinatario.INDIVIDUAL -> "INDIVIDUAL"
            TipoDestinatario.GRUPO -> "GRUPO"
            TipoDestinatario.CLASE -> "CLASE"
            TipoDestinatario.CENTRO -> "CENTRO"
        }
    }
} 