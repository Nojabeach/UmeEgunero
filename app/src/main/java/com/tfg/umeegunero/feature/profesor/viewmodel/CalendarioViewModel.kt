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
        // Obtener ID del usuario actual y su centro
        viewModelScope.launch {
            try {
                usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            (result.data as? Usuario)?.let { user ->
                                profesorId = user.dni
                                // Buscar perfil de profesor
                                user.perfiles.find { it.tipo == TipoUsuario.PROFESOR }?.let { perfil ->
                                    centroId = perfil.centroId
                                    cargarEventos()
                                } 
                                // Si no es profesor, verificar si es admin de centro
                                ?: user.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }?.let { perfil ->
                                    centroId = perfil.centroId
                                    Timber.d("Usuario es administrador de centro ${perfil.centroId}")
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
        
        // Actualizar estado de carga
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                // Obtener los IDs de los destinatarios (familiares o profesores según corresponda)
                val destinatarios = obtenerFamiliaresDeAlumnos()
                
                Timber.d("DEBUG-EVENTOS: Creando evento con ${destinatarios.size} destinatarios")
                Timber.d("DEBUG-EVENTOS: Lista de destinatarios: $destinatarios")
                
                // Crear fecha y hora a partir del día seleccionado (hora por defecto: 8:00)
                val fechaHora = LocalDateTime.of(
                    diaSeleccionado.year,
                    diaSeleccionado.month,
                    diaSeleccionado.dayOfMonth,
                    8, 0
                )
                
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
                    destinatarios = destinatarios // Esto ya se guarda como destinatariosIds en el modelo Evento
                )
                
                Timber.d("DEBUG-EVENTOS: Enviando evento a repository con ${nuevoEvento.destinatarios.size} destinatarios")
                val resultado = eventoRepository.crearEvento(nuevoEvento)
                
                when (resultado) {
                    is Result.Success -> {
                        Timber.d("DEBUG-EVENTOS: Evento creado con éxito, ID: ${resultado.data}")
                    }
                    is Result.Error -> {
                        Timber.e("DEBUG-EVENTOS: Error al crear evento: ${resultado.exception?.message}")
                    }
                    else -> {
                        Timber.d("DEBUG-EVENTOS: Respuesta inesperada del repository")
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Evento creado correctamente"
                    )
                }
                
                cargarEventos()
            } catch (e: Exception) {
                Timber.e(e, "DEBUG-EVENTOS: Error al crear evento: ${e.message}")
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
     * Obtiene los IDs de los destinatarios para un evento según el tipo de usuario
     * - Para profesor: los familiares de los alumnos del profesor
     * - Para admin de centro: todos los profesores del centro
     */
    private suspend fun obtenerFamiliaresDeAlumnos(): List<String> {
        return try {
            val destinatarios = mutableSetOf<String>()
            
            // Obtener el usuario actual
            val usuarioActual = usuarioRepository.getUsuarioActual().first()
            Timber.d("DEBUG-EVENTOS: Obteniendo destinatarios, usuario=${usuarioActual is Result.Success}")
            
            if (usuarioActual is Result.Success) {
                val usuario = usuarioActual.data
                Timber.d("DEBUG-EVENTOS: Usuario actual DNI=${usuario.dni}, nombre=${usuario.nombre}")
                
                // Verificar si el usuario es un administrador de centro
                val esAdminCentro = usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_CENTRO }
                val esProfesor = usuario.perfiles.any { it.tipo == TipoUsuario.PROFESOR }
                
                Timber.d("DEBUG-EVENTOS: Tipo de usuario - Admin:$esAdminCentro, Profesor:$esProfesor")
                
                if (esAdminCentro) {
                    Timber.d("DEBUG-EVENTOS: Usuario es administrador de centro, obteniendo profesores del centro")
                    // Obtener ID del centro desde el perfil de administrador
                    val perfilAdmin = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                    val centroDelAdmin = perfilAdmin?.centroId
                    
                    Timber.d("DEBUG-EVENTOS: Admin del centro: $centroDelAdmin")
                    
                    if (!centroDelAdmin.isNullOrEmpty()) {
                        // Obtener todos los profesores del centro
                        val profesores = usuarioRepository.getProfesoresByCentroId(centroDelAdmin)
                        Timber.d("DEBUG-EVENTOS: Resultado búsqueda profesores: ${profesores is Result.Success}")
                        
                        if (profesores is Result.Success) {
                            profesores.data.forEach { profesor ->
                                destinatarios.add(profesor.dni)
                                Timber.d("DEBUG-EVENTOS: Añadido profesor ${profesor.nombre} (DNI: ${profesor.dni}) como destinatario")
                            }
                            
                            Timber.d("DEBUG-EVENTOS: Total profesores añadidos como destinatarios: ${destinatarios.size}")
                        } else {
                            Timber.e("DEBUG-EVENTOS: Error al obtener profesores del centro: ${(profesores as? Result.Error)?.message}")
                        }
                    } else {
                        Timber.e("DEBUG-EVENTOS: No se pudo obtener el ID del centro del admin")
                    }
                } else if (esProfesor) {
                    // Si es profesor, obtener las clases del profesor
                    Timber.d("DEBUG-EVENTOS: Obteniendo clases del profesor ${usuario.dni}")
                    val firestore = FirebaseFirestore.getInstance()
                    val profesorDoc = firestore.collection("usuarios")
                        .document(usuario.dni)
                        .get()
                        .await()
                    
                    // Obtenemos los IDs de las clases directamente del documento
                    val clasesIds = profesorDoc.get("clasesIds") as? List<String> ?: emptyList()
                    
                    Timber.d("DEBUG-EVENTOS: Profesor ${usuario.nombre} (${usuario.dni}) tiene ${clasesIds.size} clases: $clasesIds")
                    
                    // Si no hay clases, intentamos obtenerlas del perfil del profesor
                    if (clasesIds.isEmpty()) {
                        Timber.w("DEBUG-EVENTOS: No se encontraron clasesIds en documento, buscando en perfil...")
                        val perfilProfesor = usuario.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                        if (perfilProfesor != null && perfilProfesor.clasesIds.isNotEmpty()) {
                            Timber.d("DEBUG-EVENTOS: Usando clasesIds del perfil: ${perfilProfesor.clasesIds}")
                            // Procesar cada clase para obtener sus alumnos y familiares
                            procesarClasesParaFamiliares(perfilProfesor.clasesIds, destinatarios)
                        } else {
                            Timber.w("DEBUG-EVENTOS: El profesor no tiene clases asignadas.")
                        }
                    } else {
                        // Procesar las clases obtenidas del documento
                        Timber.d("DEBUG-EVENTOS: Procesando ${clasesIds.size} clases para obtener familiares")
                        procesarClasesParaFamiliares(clasesIds, destinatarios)
                    }
                } else {
                    Timber.w("DEBUG-EVENTOS: El usuario no es ni profesor ni administrador de centro")
                }
            } else {
                Timber.e("DEBUG-EVENTOS: No se pudo obtener el usuario actual")
            }
            
            // Log final de los destinatarios encontrados
            Timber.d("DEBUG-EVENTOS: Total de destinatarios encontrados: ${destinatarios.size}")
            Timber.d("DEBUG-EVENTOS: Lista de destinatarios: $destinatarios")
            
            // Si no se encontraron destinatarios, registrar un error
            if (destinatarios.isEmpty()) {
                Timber.w("DEBUG-EVENTOS: ¡ADVERTENCIA! No se encontraron destinatarios para el evento")
            }
            
            return destinatarios.toList()
        } catch (e: Exception) {
            Timber.e(e, "DEBUG-EVENTOS: Error al obtener destinatarios para el evento")
            emptyList()
        }
    }
    
    /**
     * Procesa las clases para obtener los familiares de sus alumnos
     */
    private suspend fun procesarClasesParaFamiliares(clasesIds: List<String>, familiaresIds: MutableSet<String>) {
        val firestore = FirebaseFirestore.getInstance()
        
        Timber.d("DEBUG-EVENTOS: Iniciando procesamiento de ${clasesIds.size} clases")
        
        // Para cada clase, obtener los alumnos
        for (claseId in clasesIds) {
            try {
                Timber.d("DEBUG-EVENTOS: Procesando clase $claseId")
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)
                
                if (alumnosResult is Result.Success) {
                    val alumnos = alumnosResult.data
                    
                    Timber.d("DEBUG-EVENTOS: Clase $claseId tiene ${alumnos.size} alumnos")
                    
                    // Para cada alumno, buscar sus familiares en vinculaciones_familiar_alumno
                    for (alumno in alumnos) {
                        try {
                            Timber.d("DEBUG-EVENTOS: Buscando familiares de alumno ${alumno.nombre} (DNI: ${alumno.dni})")
                            
                            // Estrategia 1: Buscar en la colección vinculaciones_familiar_alumno usando el DNI del alumno
                            val vinculacionesSnapshot = firestore.collection("vinculaciones_familiar_alumno")
                                .whereEqualTo("alumnoId", alumno.dni)
                                .get()
                                .await()
                            
                            Timber.d("DEBUG-EVENTOS: Encontradas ${vinculacionesSnapshot.size()} vinculaciones para alumno ${alumno.dni}")
                            
                            var familiaresEncontrados = 0
                            for (vinculacionDoc in vinculacionesSnapshot.documents) {
                                val familiarId = vinculacionDoc.getString("familiarId")
                                if (!familiarId.isNullOrEmpty()) {
                                    familiaresIds.add(familiarId)
                                    familiaresEncontrados++
                                    Timber.d("DEBUG-EVENTOS: Añadido familiar ID: $familiarId para alumno ${alumno.nombre}")
                                } else {
                                    Timber.w("DEBUG-EVENTOS: Vinculación sin familiarId válido para alumno ${alumno.dni}")
                                }
                            }
                            
                            // Estrategia 2: Si no se encontraron familiares con la primera estrategia, intentar directamente en el alumno
                            if (familiaresEncontrados == 0) {
                                Timber.d("DEBUG-EVENTOS: Intentando estrategia alternativa para alumno ${alumno.dni}")
                                
                                // Algunos alumnos pueden tener directamente un campo familiaresIds
                                val alumnoDoc = firestore.collection("usuarios")
                                    .document(alumno.dni)
                                    .get()
                                    .await()
                                
                                val familiaresDirectos = alumnoDoc.get("familiaresIds") as? List<String> ?: emptyList()
                                
                                if (familiaresDirectos.isNotEmpty()) {
                                    familiaresDirectos.forEach { familiarId ->
                                        if (!familiarId.isNullOrEmpty()) {
                                            familiaresIds.add(familiarId)
                                            Timber.d("DEBUG-EVENTOS: Añadido familiar ID (estrategia alternativa): $familiarId para alumno ${alumno.nombre}")
                                        }
                                    }
                                    Timber.d("DEBUG-EVENTOS: Encontrados ${familiaresDirectos.size} familiares con estrategia alternativa para alumno ${alumno.dni}")
                                } else {
                                    Timber.w("DEBUG-EVENTOS: No se encontraron familiares con ninguna estrategia para alumno ${alumno.dni}")
                                }
                            }
                            
                        } catch (e: Exception) {
                            Timber.e(e, "DEBUG-EVENTOS: Error al procesar familiares del alumno ${alumno.dni}")
                        }
                    }
                } else if (alumnosResult is Result.Error) {
                    Timber.e(alumnosResult.exception, "DEBUG-EVENTOS: Error al obtener alumnos para clase $claseId")
                } else {
                    Timber.d("DEBUG-EVENTOS: Resultado inesperado al obtener alumnos para clase $claseId")
                }
            } catch (e: Exception) {
                Timber.e(e, "DEBUG-EVENTOS: Error al procesar la clase $claseId")
            }
        }
        
        Timber.d("DEBUG-EVENTOS: Finalizado procesamiento de clases. Total familiares: ${familiaresIds.size}")
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