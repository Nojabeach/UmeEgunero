package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.toLocalDate
import com.tfg.umeegunero.util.toTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Estado UI para la pantalla de Calendario
 */
data class CalendarioUiState(
    val mesSeleccionado: YearMonth = YearMonth.now(),
    val diaSeleccionado: LocalDate? = null,
    val eventos: List<Evento> = emptyList(),
    val eventosDiaSeleccionado: List<Evento> = emptyList(),
    val mostrarEventos: Boolean = false,
    val mostrarDialogoEvento: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para gestionar el calendario y eventos del profesor
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val familiarRepository: com.tfg.umeegunero.data.repository.FamiliarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()
    
    private var profesorId: String? = null
    private var centroId: String? = null

    init {
        // Obtener ID del profesor actual
        viewModelScope.launch {
            try {
                usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            (result.data as? Usuario)?.let { user ->
                                profesorId = user.dni
                                user.perfiles.find { it.tipo == TipoUsuario.PROFESOR }?.let { perfil ->
                                    centroId = perfil.centroId
                                    cargarEventos()
                                }
                            }
                        }
                        is Result.Error -> {
                            Timber.e(result.exception, "Error al obtener usuario actual")
                            _uiState.update { 
                                it.copy(error = "Error al cargar datos de usuario: ${result.exception?.message}")
                            }
                        }
                        is Result.Loading<*> -> {
                            // Esperando datos
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener usuario actual")
                _uiState.update { 
                    it.copy(error = "Error al cargar datos de usuario: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga los eventos del mes seleccionado
     */
    private fun cargarEventos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Usar el nuevo método para obtener eventos específicos para el usuario
                val eventos = if (centroId != null && profesorId != null) {
                    eventoRepository.obtenerEventosParaUsuario(profesorId!!, centroId!!)
                } else {
                    emptyList()
                }
                
                _uiState.update { 
                    it.copy(
                        eventos = eventos,
                        isLoading = false
                    )
                }
                
                actualizarEventosDiaSeleccionado()
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar eventos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Selecciona un día del calendario
     */
    fun seleccionarDia(dia: LocalDate) {
        _uiState.update { 
            it.copy(
                diaSeleccionado = dia,
                mostrarEventos = true
            )
        }
        actualizarEventosDiaSeleccionado()
    }

    /**
     * Actualiza la lista de eventos para el día seleccionado
     */
    private fun actualizarEventosDiaSeleccionado() {
        val diaSeleccionado = _uiState.value.diaSeleccionado ?: return
        
        val eventosDelDia = _uiState.value.eventos.filter { evento ->
            val fechaEvento = evento.fecha.toLocalDate()
            fechaEvento.isEqual(diaSeleccionado)
        }
        
        _uiState.update { 
            it.copy(eventosDiaSeleccionado = eventosDelDia)
        }
    }

    /**
     * Avanza al mes siguiente
     */
    fun mesSiguiente() {
        _uiState.update { 
            it.copy(
                mesSeleccionado = it.mesSeleccionado.plusMonths(1),
                diaSeleccionado = null,
                mostrarEventos = false
            )
        }
    }

    /**
     * Retrocede al mes anterior
     */
    fun mesAnterior() {
        _uiState.update { 
            it.copy(
                mesSeleccionado = it.mesSeleccionado.minusMonths(1),
                diaSeleccionado = null,
                mostrarEventos = false
            )
        }
    }

    /**
     * Muestra el diálogo para crear un evento
     */
    fun mostrarDialogoCrearEvento() {
        if (_uiState.value.diaSeleccionado == null) {
            _uiState.update { it.copy(error = "Selecciona un día primero") }
            return
        }
        
        _uiState.update { it.copy(mostrarDialogoEvento = true) }
    }

    /**
     * Oculta el diálogo para crear un evento
     */
    fun ocultarDialogoCrearEvento() {
        _uiState.update { it.copy(mostrarDialogoEvento = false) }
    }

    /**
     * Crea un nuevo evento
     */
    fun crearEvento(titulo: String, descripcion: String, tipoEvento: TipoEvento) {
        val diaSeleccionado = _uiState.value.diaSeleccionado
        val profesorId = this.profesorId
        val centroId = this.centroId
        
        if (diaSeleccionado == null) {
            _uiState.update { it.copy(error = "No se puede crear el evento: no hay día seleccionado") }
            return
        }
        
        // Validar que tenemos el ID del usuario y del centro
        if (profesorId.isNullOrEmpty() || centroId.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "No se puede crear el evento: no se pudo identificar al usuario o su centro") }
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, mostrarDialogoEvento = false) }
                
                // Obtener los familiares de los alumnos del profesor
                val destinatarios = obtenerFamiliaresDeAlumnos()
                
                Timber.d("Creando evento con ${destinatarios.size} destinatarios: $destinatarios")
                
                // Crear objeto evento
                val fechaHora = LocalDateTime.of(diaSeleccionado, LocalTime.of(8, 0))
                val timestamp = fechaHora.toTimestamp()
                val nuevoEvento = Evento(
                    id = "",  // Se asignará en el repositorio
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = timestamp,
                    tipo = tipoEvento,
                    creadorId = profesorId,
                    centroId = centroId,
                    publico = true,
                    destinatarios = destinatarios
                )
                
                eventoRepository.crearEvento(nuevoEvento)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Evento creado correctamente"
                    )
                }
                
                cargarEventos()
            } catch (e: Exception) {
                Timber.e(e, "Error al crear evento")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al crear evento: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Obtiene los IDs de los familiares de los alumnos del profesor
     */
    private suspend fun obtenerFamiliaresDeAlumnos(): List<String> {
        return try {
            val familiaresIds = mutableSetOf<String>()
            
            // Obtener el usuario actual (profesor)
            val usuarioActual = usuarioRepository.getUsuarioActual().first()
            if (usuarioActual is Result.Success) {
                val profesor = usuarioActual.data
                
                // Obtener las clases del profesor directamente desde el documento en Firestore
                // para asegurar que tenemos los datos más actualizados
                val firestore = FirebaseFirestore.getInstance()
                val profesorDoc = firestore.collection("usuarios")
                    .document(profesor.dni)
                    .get()
                    .await()
                
                // Obtenemos los IDs de las clases directamente del documento
                val clasesIds = profesorDoc.get("clasesIds") as? List<String> ?: emptyList()
                
                Timber.d("Profesor ${profesor.nombre} (${profesor.dni}) tiene ${clasesIds.size} clases: $clasesIds")
                
                // Si no hay clases, intentamos obtenerlas del perfil del profesor
                if (clasesIds.isEmpty()) {
                    Timber.w("No se encontraron clasesIds en el documento del profesor. Intentando obtener del perfil...")
                    val perfilProfesor = profesor.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                    if (perfilProfesor != null && perfilProfesor.clasesIds.isNotEmpty()) {
                        Timber.d("Usando clasesIds del perfil del profesor: ${perfilProfesor.clasesIds}")
                        // Procesar cada clase para obtener sus alumnos y familiares
                        procesarClasesParaFamiliares(perfilProfesor.clasesIds, familiaresIds)
                    } else {
                        Timber.w("El profesor no tiene clases asignadas.")
                    }
                } else {
                    // Procesar las clases obtenidas del documento
                    procesarClasesParaFamiliares(clasesIds, familiaresIds)
                }
            } else {
                Timber.e("No se pudo obtener el usuario actual")
            }
            
            Timber.d("Total de familiares encontrados: ${familiaresIds.size}, IDs: $familiaresIds")
            familiaresIds.toList()
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener familiares de alumnos")
            emptyList()
        }
    }
    
    /**
     * Procesa las clases para obtener los familiares de sus alumnos
     */
    private suspend fun procesarClasesParaFamiliares(clasesIds: List<String>, familiaresIds: MutableSet<String>) {
        // Para cada clase, obtener los alumnos
        for (claseId in clasesIds) {
            try {
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)
                if (alumnosResult is Result.Success) {
                    val alumnos = alumnosResult.data
                    
                    Timber.d("Clase $claseId tiene ${alumnos.size} alumnos")
                    
                    // Para cada alumno, obtener sus familiares
                    for (alumno in alumnos) {
                        try {
                            val familiaresResult = familiarRepository.getFamiliaresByAlumnoId(alumno.id)
                            if (familiaresResult is Result.Success) {
                                val familiares = familiaresResult.data
                                familiaresIds.addAll(familiares.map { it.dni })
                                
                                Timber.d("Alumno ${alumno.nombre} (ID: ${alumno.id}) tiene ${familiares.size} familiares: ${familiares.map { it.dni }}")
                            } else if (familiaresResult is Result.Error) {
                                Timber.e(familiaresResult.exception, "Error al obtener familiares para alumno ${alumno.id}")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al procesar familiares del alumno ${alumno.id}")
                        }
                    }
                } else if (alumnosResult is Result.Error) {
                    Timber.e(alumnosResult.exception, "Error al obtener alumnos para clase $claseId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al procesar la clase $claseId")
            }
        }
    }

    /**
     * Elimina un evento
     */
    fun eliminarEvento(evento: Evento) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                eventoRepository.eliminarEvento(evento.id)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Evento eliminado correctamente"
                    )
                }
                
                cargarEventos()
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar evento")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar evento: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 