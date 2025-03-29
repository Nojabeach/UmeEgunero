package com.tfg.umeegunero.feature.common.mensajeria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de conversaciones
 */
data class ConversacionesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversaciones: List<ConversacionResumen> = emptyList(),
    val conversacionesFiltradas: List<ConversacionResumen> = emptyList(),
    val usuarioActual: Usuario? = null,
    val esFamiliar: Boolean = false
)

/**
 * ViewModel para la pantalla que lista todas las conversaciones
 */
@HiltViewModel
class ConversacionesViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConversacionesUiState())
    val uiState: StateFlow<ConversacionesUiState> = _uiState.asStateFlow()
    
    init {
        cargarUsuarioActual()
    }
    
    /**
     * Carga la información del usuario actual
     */
    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val usuarioId = usuarioRepository.getUsuarioActualId()
                
                if (usuarioId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo obtener la información del usuario",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val usuarioResult = usuarioRepository.getUsuarioById(usuarioId)
                
                when (usuarioResult) {
                    is Result.Success -> {
                        val usuario = usuarioResult.data
                        val esFamiliar = usuario.perfiles.any { it.tipo == TipoUsuario.FAMILIAR }
                        
                        _uiState.update { 
                            it.copy(
                                usuarioActual = usuario,
                                esFamiliar = esFamiliar
                            )
                        }
                        
                        // Cargar conversaciones
                        cargarConversaciones(usuarioId, esFamiliar)
                    }
                    
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar información del usuario: ${usuarioResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(usuarioResult.exception, "Error al cargar usuario")
                    }
                    
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar usuario actual")
            }
        }
    }
    
    /**
     * Carga todas las conversaciones del usuario
     */
    private fun cargarConversaciones(usuarioId: String, esFamiliar: Boolean) {
        viewModelScope.launch {
            try {
                val mensajesResult = usuarioRepository.getMensajesUsuario(usuarioId)
                
                when (mensajesResult) {
                    is Result.Success -> {
                        val mensajes = mensajesResult.data
                        
                        // Agrupar mensajes por conversación
                        val conversacionesMap = mutableMapOf<String, MutableList<Mensaje>>()
                        
                        mensajes.forEach { mensaje ->
                            val otroUsuarioId = if (mensaje.emisorId == usuarioId) 
                                mensaje.receptorId 
                            else 
                                mensaje.emisorId
                            
                            // Usar el alumnoId como parte de la clave si existe
                            val claveConversacion = if (mensaje.alumnoId.isNotBlank()) 
                                "$otroUsuarioId:${mensaje.alumnoId}" 
                            else 
                                otroUsuarioId
                            
                            if (!conversacionesMap.containsKey(claveConversacion)) {
                                conversacionesMap[claveConversacion] = mutableListOf()
                            }
                            
                            conversacionesMap[claveConversacion]?.add(mensaje)
                        }
                        
                        // Convertir mapa a lista de resúmenes de conversación
                        val conversacionesResumen = mutableListOf<ConversacionResumen>()
                        
                        for ((clave, mensajesConversacion) in conversacionesMap) {
                            // Ordenar mensajes por fecha
                            val mensajesOrdenados = mensajesConversacion.sortedByDescending { it.timestamp }
                            val ultimoMensaje = mensajesOrdenados.firstOrNull() ?: continue
                            
                            // Extraer IDs
                            val partes = clave.split(":")
                            val participanteId = partes[0]
                            val alumnoId = if (partes.size > 1) partes[1] else null
                            
                            // Cargar información del participante
                            val participanteResult = usuarioRepository.getUsuarioPorDni(participanteId)
                            val participante = when (participanteResult) {
                                is Result.Success -> participanteResult.data
                                else -> continue
                            }
                            
                            // Cargar información del alumno si existe
                            var alumnoNombre: String? = null
                            if (alumnoId != null) {
                                val alumnoResult = usuarioRepository.getAlumnoPorDni(alumnoId)
                                alumnoNombre = when (alumnoResult) {
                                    is Result.Success -> "${alumnoResult.data.nombre} ${alumnoResult.data.apellidos}"
                                    else -> null
                                }
                            }
                            
                            // Contar mensajes no leídos
                            val mensajesNoLeidos = mensajesOrdenados.count { 
                                it.receptorId == usuarioId && !it.leido 
                            }
                            
                            // Crear objeto resumen
                            conversacionesResumen.add(
                                ConversacionResumen(
                                    id = clave,
                                    conversacionId = clave,
                                    participanteId = participanteId,
                                    participanteNombre = "${participante.nombre} ${participante.apellidos}",
                                    participanteTipo = if (esFamiliar) TipoUsuario.PROFESOR else TipoUsuario.FAMILIAR,
                                    ultimoMensaje = ultimoMensaje.texto,
                                    fechaUltimoMensaje = ultimoMensaje.timestamp,
                                    mensajesNoLeidos = mensajesNoLeidos,
                                    alumnoId = alumnoId,
                                    alumnoNombre = alumnoNombre
                                )
                            )
                        }
                        
                        // Ordenar por fecha del último mensaje
                        val conversacionesOrdenadas = conversacionesResumen.sortedByDescending { 
                            it.fechaUltimoMensaje 
                        }
                        
                        _uiState.update { 
                            it.copy(
                                conversaciones = conversacionesOrdenadas,
                                conversacionesFiltradas = conversacionesOrdenadas,
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar conversaciones: ${mensajesResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(mensajesResult.exception, "Error al cargar conversaciones")
                    }
                    
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar conversaciones: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar conversaciones")
            }
        }
    }
    
    /**
     * Filtra la lista de conversaciones según el texto de búsqueda
     */
    fun filtrarConversaciones(textoBusqueda: String) {
        if (textoBusqueda.isBlank()) {
            _uiState.update { 
                it.copy(conversacionesFiltradas = it.conversaciones) 
            }
            return
        }
        
        val busquedaLowerCase = textoBusqueda.lowercase()
        
        val conversacionesFiltradas = _uiState.value.conversaciones.filter { conversacion ->
            conversacion.participanteNombre.lowercase().contains(busquedaLowerCase) ||
            conversacion.alumnoNombre?.lowercase()?.contains(busquedaLowerCase) == true ||
            conversacion.ultimoMensaje.lowercase().contains(busquedaLowerCase)
        }
        
        _uiState.update { 
            it.copy(conversacionesFiltradas = conversacionesFiltradas) 
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 