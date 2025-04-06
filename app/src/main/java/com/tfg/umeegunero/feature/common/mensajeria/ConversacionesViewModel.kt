package com.tfg.umeegunero.feature.common.mensajeria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.MensajeRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

/**
 * Modelo de datos para representar un resumen de una conversación 
 * para mostrar en la lista de conversaciones
 */
data class ConversacionResumen(
    val id: String,
    val participanteId: String,
    val nombreParticipante: String,
    val participanteAvatar: String?,
    val alumnoId: String?,
    val nombreAlumno: String?,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: Long,
    val mensajesNoLeidos: Int
)

/**
 * Estado UI para la pantalla de conversaciones
 */
data class ConversacionesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val conversaciones: List<ConversacionResumen> = emptyList(),
    val busqueda: String = "",
    val usuario: Usuario? = null,
    val alumnoSeleccionadoId: String? = null
)

/**
 * ViewModel para la gestión de conversaciones
 */
@HiltViewModel
class ConversacionesViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConversacionesUiState())
    val uiState: StateFlow<ConversacionesUiState> = _uiState.asStateFlow()
    
    init {
        cargarUsuarioYConversaciones()
    }
    
    /**
     * Carga el usuario actual y sus conversaciones
     */
    private fun cargarUsuarioYConversaciones() {
        viewModelScope.launch {
            try {
                // Obtener usuario actual
                val usuario = _uiState.value.usuario ?: run {
                    val usuarioResultFlow = usuarioRepository.getUsuarioActual()
                    var usuarioActual: Usuario? = null
                    
                    // Obtener el valor actual del flow
                    usuarioResultFlow.collectLatest<Result<Usuario>> { result ->
                        when (result) {
                            is Result.Success<*> -> {
                                usuarioActual = result.data as Usuario
                            }
                            is Result.Error -> {
                                _uiState.update { state -> state.copy(
                                    isLoading = false,
                                    error = "Error al cargar el usuario: ${result.exception?.message}"
                                ) }
                            }
                            is Result.Loading<*> -> {
                                // Mantener estado de carga
                            }
                        }
                    }
                    
                    if (usuarioActual == null) {
                        return@launch
                    }
                    
                    _uiState.update { state -> state.copy(usuario = usuarioActual) }
                    usuarioActual
                }
                
                // Cargar conversaciones si el usuario está autenticado
                if (usuario != null) {
                    cargarConversaciones()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar el usuario: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Carga las conversaciones del usuario
     */
    private fun cargarConversaciones() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Obtener el usuario actual
                val usuario = _uiState.value.usuario ?: run {
                    val usuarioResultFlow = usuarioRepository.getUsuarioActual()
                    var usuarioActual: Usuario? = null
                    
                    // Obtener el valor actual del flow
                    usuarioResultFlow.collectLatest<Result<Usuario>> { result ->
                        when (result) {
                            is Result.Success<*> -> {
                                usuarioActual = result.data as Usuario
                            }
                            is Result.Error -> {
                                _uiState.update { state -> state.copy(
                                    isLoading = false,
                                    error = "Error al cargar el usuario: ${result.exception?.message}"
                                ) }
                            }
                            is Result.Loading<*> -> {
                                // Mantener estado de carga
                            }
                        }
                    }
                    
                    if (usuarioActual == null) {
                        return@launch
                    }
                    
                    _uiState.update { state -> state.copy(usuario = usuarioActual) }
                    usuarioActual
                }
                
                // Tipo de usuario para determinar comportamiento
                val tipoUsuario = usuario?.perfiles?.firstOrNull()?.tipo ?: TipoUsuario.DESCONOCIDO
                val esFamiliar = tipoUsuario == TipoUsuario.FAMILIAR
                
                // Cargar las conversaciones del usuario
                usuario?.dni?.let { usuarioDni ->
                    val conversacionesInfo = chatRepository.getConversacionesByUsuarioId(usuarioDni)
                    procesarConversaciones(conversacionesInfo, usuario)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar conversaciones")
                _uiState.update { state -> state.copy(
                    isLoading = false,
                    error = "Error al cargar las conversaciones: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Procesa las conversaciones y actualiza el estado
     */
    private suspend fun procesarConversaciones(
        conversacionesInfo: List<com.tfg.umeegunero.data.repository.ConversacionInfo>,
        usuario: Usuario
    ) {
        if (conversacionesInfo.isEmpty()) {
            _uiState.update { state -> state.copy(
                isLoading = false,
                conversaciones = emptyList(),
                error = "No tienes conversaciones activas"
            ) }
            return
        }
        
        val resumenList = mutableListOf<ConversacionResumen>()
        
        for (conversacion in conversacionesInfo) {
            try {
                // Obtener datos del participante
                val participanteResult = usuarioRepository.obtenerUsuarioPorId(conversacion.participanteId)
                val participante = when (participanteResult) {
                    is Result.Success<*> -> participanteResult.data as Usuario
                    else -> {
                        Timber.e("No se pudo obtener el participante ${conversacion.participanteId}")
                        continue // Saltar esta conversación si no podemos obtener el participante
                    }
                }
                
                // Nombre del participante
                val nombreParticipante = "${participante.nombre} ${participante.apellidos}"
                
                // Nombre del alumno para contexto
                var nombreAlumno: String? = null
                if (conversacion.alumnoId != null) {
                    val alumnoResult = alumnoRepository.obtenerAlumnoPorId(conversacion.alumnoId)
                    nombreAlumno = when (alumnoResult) {
                        is Result.Success<*> -> {
                            val alumno = alumnoResult.data
                            // Asumimos que alumno puede ser null o tener los campos asignados desde un Map
                            val nombre = if (alumno is Map<*, *>) alumno["nombre"] as? String ?: "" else ""
                            val apellidos = if (alumno is Map<*, *>) alumno["apellidos"] as? String ?: "" else ""
                            "$nombre $apellidos"
                        }
                        else -> "Alumno"
                    }
                }
                
                // Crear resumen
                val resumen = ConversacionResumen(
                    id = conversacion.conversacionId,
                    participanteId = conversacion.participanteId,
                    nombreParticipante = nombreParticipante,
                    participanteAvatar = null,
                    alumnoId = conversacion.alumnoId,
                    nombreAlumno = nombreAlumno,
                    ultimoMensaje = conversacion.ultimoMensaje,
                    fechaUltimoMensaje = conversacion.fechaUltimoMensaje,
                    mensajesNoLeidos = conversacion.mensajesNoLeidos
                )
                
                resumenList.add(resumen)
            } catch (e: Exception) {
                Timber.e(e, "Error procesando conversación ${conversacion.conversacionId}")
                // Continuamos con otras conversaciones
            }
        }
        
        // Ordenar por fecha más reciente
        val listaOrdenada = resumenList.sortedByDescending { 
            it.fechaUltimoMensaje
        }
        
        _uiState.update { state -> state.copy(
            isLoading = false,
            conversaciones = listaOrdenada,
            error = null
        ) }
    }
    
    /**
     * Actualiza el texto de búsqueda y filtra las conversaciones
     */
    fun actualizarBusqueda(busqueda: String) {
        _uiState.update { it.copy(busqueda = busqueda) }
        
        if (busqueda.isNotBlank()) {
            filtrarConversaciones(busqueda)
        } else {
            cargarConversaciones()
        }
    }
    
    /**
     * Filtra las conversaciones por el texto de búsqueda
     */
    private fun filtrarConversaciones(busqueda: String) {
        viewModelScope.launch {
            val busquedaLower = busqueda.lowercase()
            
            try {
                val conversacionesFiltradas = _uiState.value.conversaciones.filter { conversacion ->
                    conversacion.nombreParticipante.lowercase().contains(busquedaLower) ||
                    conversacion.ultimoMensaje.lowercase().contains(busquedaLower) ||
                    (conversacion.nombreAlumno?.lowercase()?.contains(busquedaLower) ?: false)
                }
                
                _uiState.update { it.copy(conversaciones = conversacionesFiltradas) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al filtrar conversaciones: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Actualiza una conversación cuando se ha leído
     */
    fun marcarConversacionComoLeida(conversacionId: String) {
        viewModelScope.launch {
            try {
                mensajeRepository.marcarConversacionComoLeida(conversacionId)
                
                // Actualizar estado local
                val conversacionesActualizadas = _uiState.value.conversaciones.map { conversacion ->
                    if (conversacion.id == conversacionId) {
                        conversacion.copy(mensajesNoLeidos = 0)
                    } else {
                        conversacion
                    }
                }
                
                _uiState.update { it.copy(conversaciones = conversacionesActualizadas) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al marcar conversación como leída: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Crear una nueva conversación
     */
    fun crearConversacion(participanteId: String, alumnoId: String? = null) {
        viewModelScope.launch {
            try {
                val usuario = _uiState.value.usuario ?: throw Exception("Usuario no disponible")
                
                val conversacionId = mensajeRepository.crearConversacion(
                    usuarioId = usuario.dni,
                    participanteId = participanteId,
                    alumnoId = alumnoId
                )
                
                // Recargar conversaciones para incluir la nueva
                cargarConversaciones()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al crear conversación: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Eliminar una conversación
     */
    fun eliminarConversacion(conversacionId: String) {
        viewModelScope.launch {
            try {
                mensajeRepository.eliminarConversacion(conversacionId)
                
                // Actualizar estado local
                val conversacionesActualizadas = _uiState.value.conversaciones.filter { 
                    it.id != conversacionId 
                }
                
                _uiState.update { it.copy(conversaciones = conversacionesActualizadas) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar conversación: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
} 