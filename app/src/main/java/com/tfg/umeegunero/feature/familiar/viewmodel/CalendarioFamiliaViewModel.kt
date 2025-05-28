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
import com.google.firebase.Timestamp

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
                
                // Obtener el ID del familiar directamente
                val familiarId = usuario.dni
                Timber.d("CalendarioFamiliaViewModel: Cargando eventos para familiar $familiarId")
                
                try {
                    // Cargar eventos destinados directamente al familiar
                    val eventosDestinatario = eventoRepository.obtenerEventosDestinadosAUsuario(familiarId)
                    Timber.d("CalendarioFamiliaViewModel: Encontrados ${eventosDestinatario.size} eventos destinados al familiar $familiarId")
                    
                    // Obtener eventos públicos que puedan ser relevantes para el familiar
                    val eventosPublicos = eventoRepository.obtenerEventosPublicos()
                    Timber.d("CalendarioFamiliaViewModel: Encontrados ${eventosPublicos.size} eventos públicos")
                    
                    // Combinar todos los eventos
                    val todosEventos = (eventosDestinatario + eventosPublicos).distinctBy { it.id }
                    
                    // Registrar información sobre los eventos cargados
                    todosEventos.forEach { evento ->
                        val fechaStr = evento.fecha.toDate().let { date ->
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
                        }
                        
                        Timber.d("CalendarioFamiliaViewModel: Evento ID: ${evento.id}, Título: ${evento.titulo}")
                        Timber.d("CalendarioFamiliaViewModel: - Creador: ${evento.creadorId}")
                        Timber.d("CalendarioFamiliaViewModel: - Destinatarios: ${evento.destinatarios}")
                        Timber.d("CalendarioFamiliaViewModel: - Público: ${evento.publico}")
                        Timber.d("CalendarioFamiliaViewModel: - Fecha: $fechaStr")
                    }
                    
                    _uiState.update { it.copy(
                        eventos = todosEventos,
                        isLoading = false
                    ) }
                    
                    // Si aún no hay eventos, intentar cargar eventos para los hijos del familiar
                    if (todosEventos.isEmpty()) {
                        Timber.d("CalendarioFamiliaViewModel: No se encontraron eventos directos. Cargando eventos para hijos...")
                        loadEventosByHijos()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al cargar eventos para el familiar: ${e.message}")
                    _uiState.update { it.copy(
                        error = "Error al cargar eventos: ${e.message}",
                        isLoading = false
                    ) }
                }
                
                // Verificar eventos para la fecha seleccionada actual
                verificarEventosParaFechaSeleccionada()
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
     * Verifica qué eventos hay para la fecha seleccionada actualmente
     */
    private fun verificarEventosParaFechaSeleccionada() {
        viewModelScope.launch {
            val fecha = _uiState.value.fechaSeleccionada
            val fechaFormateada = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(fecha.time)
            Timber.d("CalendarioFamiliaViewModel: Verificando eventos para fecha actual: $fechaFormateada")
            
            val eventosEnFecha = _uiState.value.eventos.filter { evento ->
                try {
                    val fechaEvento = Calendar.getInstance()
                    fechaEvento.timeInMillis = evento.fecha.toDate().time
                    
                    val eventoYear = fechaEvento.get(Calendar.YEAR)
                    val eventoMonth = fechaEvento.get(Calendar.MONTH)
                    val eventoDay = fechaEvento.get(Calendar.DAY_OF_MONTH)
                    val selectedYear = fecha.get(Calendar.YEAR)
                    val selectedMonth = fecha.get(Calendar.MONTH)
                    val selectedDay = fecha.get(Calendar.DAY_OF_MONTH)
                    val mismaFecha = eventoYear == selectedYear && eventoMonth == selectedMonth && eventoDay == selectedDay
                    Timber.d("CalendarioFamiliaViewModel: Comparando evento '${evento.titulo}' - Fecha: $fechaFormateada")
                    Timber.d("CalendarioFamiliaViewModel: Evento (Y/M/D): $eventoYear/$eventoMonth/$eventoDay - Seleccionada: $selectedYear/$selectedMonth/$selectedDay - ¿Coincide? $mismaFecha")
                    mismaFecha
                } catch (e: Exception) {
                    Timber.e(e, "Error al comparar fechas para evento ${evento.id}")
                    false
                }
            }
            
            Timber.d("CalendarioFamiliaViewModel: Total de eventos para la fecha $fechaFormateada: ${eventosEnFecha.size}")
            eventosEnFecha.forEach { evento ->
                Timber.d("CalendarioFamiliaViewModel: Evento en fecha $fechaFormateada: ${evento.titulo}")
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
     * Carga eventos destinados a los hijos del familiar actual
     */
    private fun loadEventosByHijos() {
        viewModelScope.launch {
            try {
                val usuario = authRepository.getCurrentUser() ?: return@launch
                val familiarId = usuario.dni
                
                // Obtener los hijos asociados a este familiar
                val hijos = usuarioRepository.obtenerHijosDeFamiliar(familiarId)
                
                if (hijos.isEmpty()) {
                    Timber.d("CalendarioFamiliaViewModel: No se encontraron hijos para el familiar $familiarId")
                    return@launch
                }
                
                Timber.d("CalendarioFamiliaViewModel: Encontrados ${hijos.size} hijos para el familiar $familiarId")
                
                // Para cada hijo, cargar eventos destinados a él
                val eventosHijos = mutableListOf<Evento>()
                
                for (hijo in hijos) {
                    Timber.d("CalendarioFamiliaViewModel: Cargando eventos para el hijo ${hijo.nombre} (${hijo.dni})")
                    
                    val eventosHijo = eventoRepository.obtenerEventosDestinadosAUsuario(hijo.dni)
                    Timber.d("CalendarioFamiliaViewModel: Encontrados ${eventosHijo.size} eventos para el hijo ${hijo.nombre}")
                    
                    eventosHijos.addAll(eventosHijo)
                }
                
                // Actualizar el estado con los eventos de los hijos, combinándolos con los eventos actuales
                val eventosActuales = _uiState.value.eventos
                val todosEventos = (eventosActuales + eventosHijos).distinctBy { it.id }
                
                Timber.d("CalendarioFamiliaViewModel: Total de eventos después de agregar eventos de hijos: ${todosEventos.size}")
                
                _uiState.update { it.copy(
                    eventos = todosEventos
                ) }
                
                // Verificar eventos para la fecha seleccionada con los nuevos eventos añadidos
                verificarEventosParaFechaSeleccionada()
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
        Timber.d("CalendarioFamiliaViewModel: Seleccionada nueva fecha: ${fecha.time}")
        _uiState.update { it.copy(fechaSeleccionada = fecha) }
        
        // Al seleccionar un nuevo día, verificamos los eventos para esa fecha
        verificarEventosParaFechaSeleccionada()
    }
    
    /**
     * Cambia al mes anterior
     */
    fun mesAnterior() {
        val nuevaFecha = _uiState.value.fechaSeleccionada.clone() as Calendar
        nuevaFecha.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha) }
        
        // Recargar eventos al cambiar de mes
        cargarEventos()
    }
    
    /**
     * Cambia al mes siguiente
     */
    fun mesSiguiente() {
        val nuevaFecha = _uiState.value.fechaSeleccionada.clone() as Calendar
        nuevaFecha.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(fechaSeleccionada = nuevaFecha) }
        
        // Recargar eventos al cambiar de mes
        cargarEventos()
    }
} 