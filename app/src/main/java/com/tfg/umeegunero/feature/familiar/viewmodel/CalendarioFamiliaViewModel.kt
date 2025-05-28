package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Información del usuario actual
 */
data class UserInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val tipoUsuario: TipoUsuario,
    val centroId: String
)

/**
 * Estado UI para la pantalla de calendario
 */
data class CalendarioUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val eventos: List<Evento> = emptyList(),
    val fechaSeleccionada: Calendar = Calendar.getInstance()
)

/**
 * ViewModel para la pantalla de calendario
 */
@HiltViewModel
class CalendarioFamiliaViewModel @Inject constructor(
    private val calendarioRepository: CalendarioRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()
    
    init {
        cargarEventos()
    }
    
    /**
     * Carga los eventos desde Firestore
     */
    fun cargarEventos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener la información del usuario",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener el centro del familiar desde el primer perfil
                val perfil = usuario.perfiles.firstOrNull()
                val centroId = perfil?.centroId ?: ""
                val familiarId = usuario.dni
                
                // Usar el nuevo método para obtener eventos específicos para el usuario
                val eventos = if (centroId.isNotEmpty() && familiarId.isNotEmpty()) {
                    eventoRepository.obtenerEventosParaUsuario(familiarId, centroId)
                } else {
                    emptyList()
                }
                
                _uiState.update { it.copy(
                    eventos = eventos,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Limpia la lista de eventos
     */
    fun clearEvents() {
        _uiState.update { it.copy(eventos = emptyList()) }
    }
    
    /**
     * Obtiene la información del usuario actual
     * Este método debe usarse con precaución ya que no es asíncrono
     * @return Información del usuario
     */
    suspend fun getUserInfo(): UserInfo {
        val currentUser = authRepository.getCurrentUser()
        // Asumimos que usaremos el primer perfil que se encuentre
        val perfil = currentUser?.perfiles?.firstOrNull()
        return UserInfo(
            id = currentUser?.dni ?: "",
            nombre = currentUser?.nombre ?: "",
            apellidos = currentUser?.apellidos ?: "",
            email = currentUser?.email ?: "",
            tipoUsuario = perfil?.tipo ?: TipoUsuario.FAMILIAR,
            centroId = perfil?.centroId ?: ""
        )
    }
    
    /**
     * Carga eventos específicos de un centro educativo
     * @param centroId ID del centro educativo
     */
    fun loadEventosByCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val eventos = calendarioRepository.getEventosByCentro(centroId)
                _uiState.update { it.copy(eventos = it.eventos + eventos) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos del centro $centroId")
            }
        }
    }
    
    /**
     * Carga eventos específicos de un usuario
     * @param usuarioId ID del usuario
     */
    fun loadEventosByUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val eventos = calendarioRepository.getEventosByUsuarioId(usuarioId)
                _uiState.update { it.copy(eventos = it.eventos + eventos) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos del usuario $usuarioId")
            }
        }
    }
    
    /**
     * Carga eventos específicos para los hijos de un familiar
     * Útil para mostrar eventos específicos de los alumnos del familiar
     */
    fun loadEventosByHijos() {
        viewModelScope.launch {
            try {
                // Obtener IDs de los hijos del familiar actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    Timber.d("Usuario no encontrado")
                    return@launch
                }
                
                // Comprobar si el usuario tiene un perfil de tipo FAMILIAR
                val perfilFamiliar = usuario.perfiles.find { it.tipo == TipoUsuario.FAMILIAR }
                if (perfilFamiliar == null) {
                    Timber.d("No se encontró un perfil de tipo FAMILIAR")
                    return@launch
                }
                
                val familiarId = usuario.dni
                if (familiarId.isEmpty()) {
                    Timber.e("ID del familiar está vacío")
                    return@launch
                }
                
                try {
                    val hijos = usuarioRepository.getHijosByFamiliarId(familiarId)
                    
                    // Para cada hijo, cargar sus eventos específicos
                    for (hijo in hijos) {
                        val eventosHijo = calendarioRepository.getEventosByAlumnoId(hijo.id)
                        _uiState.update { it.copy(eventos = it.eventos + eventosHijo) }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener hijos del familiar: ${e.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos de los hijos: ${e.message}")
            }
        }
    }
    
    /**
     * Carga eventos creados por profesores del centro
     * @param centroId ID del centro educativo
     */
    fun loadEventosByProfesores(centroId: String) {
        viewModelScope.launch {
            try {
                // Primero obtenemos los profesores del centro
                val resultadoProfesores = usuarioRepository.getProfesoresByCentroId(centroId)
                
                when (resultadoProfesores) {
                    is Result.Success -> {
                        val profesores = resultadoProfesores.data
                        
                        // Para cada profesor, cargamos sus eventos
                        for (profesor in profesores) {
                            val eventosProfesores = calendarioRepository.getEventosByUsuarioId(profesor.dni)
                            _uiState.update { it.copy(eventos = it.eventos + eventosProfesores) }
                        }
                        
                        Timber.d("Cargados eventos de ${profesores.size} profesores")
                    }
                    is Result.Error -> {
                        Timber.e(resultadoProfesores.exception, "Error al obtener profesores del centro $centroId")
                    }
                    is Result.Loading -> {
                        Timber.d("Cargando profesores del centro $centroId")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos de profesores: ${e.message}")
            }
        }
    }
    
    /**
     * Carga eventos generales que aplican a todos los alumnos
     */
    fun loadEventosGenerales() {
        viewModelScope.launch {
            try {
                val eventosGenerales = calendarioRepository.getEventosGenerales()
                _uiState.update { it.copy(eventos = it.eventos + eventosGenerales) }
                Timber.d("Cargados ${eventosGenerales.size} eventos generales")
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos generales: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza la fecha seleccionada
     */
    fun seleccionarFecha(fecha: Calendar) {
        _uiState.update { it.copy(fechaSeleccionada = fecha) }
    }
    
    /**
     * Cambia al mes anterior
     */
    fun mesAnterior() {
        val nuevaFecha = _uiState.value.fechaSeleccionada.clone() as Calendar
        nuevaFecha.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha) }
    }
    
    /**
     * Cambia al mes siguiente
     */
    fun mesSiguiente() {
        val nuevaFecha = _uiState.value.fechaSeleccionada.clone() as Calendar
        nuevaFecha.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha) }
    }
} 