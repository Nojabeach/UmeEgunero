package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Enumeración para el modo de visualización de alumnos
 */
enum class ModoVisualizacionAlumnos {
    TODOS,        // Mostrar todos los alumnos
    VINCULADOS,   // Mostrar solo los alumnos vinculados
    PENDIENTES    // Mostrar solo los alumnos pendientes de vincular
}

/**
 * Estado de la UI para la pantalla de vinculación de alumnos a clases
 */
data class VincularAlumnoClaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val alumnos: List<Alumno> = emptyList(), // Todos los alumnos (para filtrar o añadir nuevos)
    val alumnosVinculados: List<Alumno> = emptyList(), // Alumnos ya vinculados a la clase
    val alumnosDisponibles: List<Alumno> = emptyList(), // Alumnos disponibles para vincular
    val clases: List<Clase> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val alumnoSeleccionado: Alumno? = null,
    val claseSeleccionada: Clase? = null,
    val cursoSeleccionado: Curso? = null,
    val centroSeleccionado: Centro? = null,
    val centroId: String = "",
    val showAsignarDialog: Boolean = false,
    val showConfirmarDesasignacionDialog: Boolean = false,
    val showCrearAlumnoDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isAdminApp: Boolean = false,
    val textoFiltroAlumnos: String = "",
    val modoVisualizacion: ModoVisualizacionAlumnos = ModoVisualizacionAlumnos.TODOS,
    val nuevoAlumno: NuevoAlumnoData = NuevoAlumnoData(),
    val capacidadClase: Int = 0,
    val alumnosEnClase: Int = 0
)

/**
 * Datos para el formulario de nuevo alumno
 */
data class NuevoAlumnoData(
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val fechaNacimiento: String = "",
    val errorNombre: String? = null,
    val errorApellidos: String? = null,
    val errorDni: String? = null,
    val errorFechaNacimiento: String? = null
)

/**
 * ViewModel para la pantalla de vinculación de alumnos a clases
 */
@HiltViewModel
class VincularAlumnoClaseViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository,
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularAlumnoClaseUiState())
    val uiState: StateFlow<VincularAlumnoClaseUiState> = _uiState.asStateFlow()
    
    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales necesarios para la pantalla
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Verificar si es admin de app
                val currentUser = authRepository.getCurrentUser()
                val isAdminApp = currentUser?.perfiles?.any { 
                    it.tipo == TipoUsuario.ADMIN_APP 
                } ?: false

                _uiState.update { it.copy(isAdminApp = isAdminApp) }

                // Si es admin de app, cargar centros disponibles
                if (isAdminApp) {
                    centroRepository.getCentros().collect { centros ->
                        _uiState.update { it.copy(centros = centros) }
                    }
                } else {
                    // Si no es admin, intentar cargar su centro asignado
                    val centroId = currentUser?.perfiles?.find { 
                        it.tipo == TipoUsuario.ADMIN_CENTRO || it.tipo == TipoUsuario.PROFESOR 
                    }?.centroId ?: ""
                    
                    if (centroId.isNotEmpty()) {
                        seleccionarCentro(centroId)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales")
                _uiState.update { it.copy(error = "Error al cargar datos: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Selecciona un centro y carga sus cursos
     */
    fun seleccionarCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null,
                centroId = centroId
            ) }
            
            try {
                // Obtener el centro seleccionado
                val centroResult = centroRepository.getCentroById(centroId)
                if (centroResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    _uiState.update { it.copy(centroSeleccionado = centroResult.data as Centro) }
                }

                // Cargar cursos del centro
                val cursos = cursoRepository.obtenerCursosPorCentro(centroId, soloActivos = false)
                _uiState.update { it.copy(cursos = cursos) }
                
                // Cargar alumnos del centro - usamos getAlumnosByCentroId que devuelve lista directa
                try {
                    Timber.d("VincularAlumnoClaseViewModel: Intentando cargar alumnos para el centro $centroId")
                    val alumnos = alumnoRepository.getAlumnosByCentroId(centroId)
                    Timber.d("VincularAlumnoClaseViewModel: Cargados ${alumnos.size} alumnos para el centro $centroId")
                    _uiState.update { it.copy(alumnos = alumnos) }
                } catch (e: Exception) {
                    Timber.e(e, "Error al cargar alumnos por centroId")
                    
                    // Plan B: Intentar cargar usando getAlumnosByCentro
                    val alumnosResult = alumnoRepository.getAlumnosByCentro(centroId)
                    if (alumnosResult is Result.Success<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val alumnos = alumnosResult.data as List<Alumno>
                        Timber.d("VincularAlumnoClaseViewModel: Cargados ${alumnos.size} alumnos (método alternativo) para el centro $centroId")
                        _uiState.update { it.copy(alumnos = alumnos) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar centro")
                _uiState.update { it.copy(error = "Error al cargar datos del centro: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Selecciona un curso y carga sus clases
     */
    fun seleccionarCurso(curso: Curso) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                cursoSeleccionado = curso,
                claseSeleccionada = null,
                isLoading = true
            ) }
            
            try {
                // Cargar las clases del curso
                cargarClasesPorCurso(curso.id)
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar curso")
                _uiState.update { it.copy(error = "Error al cargar datos del curso: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                    is Result.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val clases = result.data as List<Clase>
                        _uiState.update { it.copy(clases = clases) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar clases: ${result.exception?.message}",
                            clases = emptyList()
                        ) }
                    }
                    is Result.Loading<*> -> {
                        // Estado de carga ya manejado
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar clases del curso")
                _uiState.update { it.copy(
                    error = "Error al cargar clases: ${e.message}",
                    clases = emptyList()
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Selecciona una clase y carga sus alumnos
     */
    fun seleccionarClase(clase: Clase) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                claseSeleccionada = clase,
                isLoading = true
            ) }
            
            try {
                Timber.d("VincularAlumnoClaseViewModel: Seleccionando clase: ${clase.nombre} (${clase.id})")
                
                // Primero asegurarnos que la lista de todos los alumnos está cargada
                if (_uiState.value.alumnos.isEmpty()) {
                    try {
                        val centroId = _uiState.value.centroId
                        if (centroId.isNotEmpty()) {
                            Timber.d("VincularAlumnoClaseViewModel: Cargando todos los alumnos del centro $centroId")
                            val alumnos = alumnoRepository.getAlumnosByCentroId(centroId)
                            Timber.d("VincularAlumnoClaseViewModel: Encontrados ${alumnos.size} alumnos en el centro")
                            _uiState.update { it.copy(alumnos = alumnos) }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al cargar alumnos del centro")
                    }
                }
                
                // Cargar los alumnos vinculados a esta clase
                val alumnosVinculados = alumnoRepository.getAlumnosPorClase(clase.id)
                Timber.d("VincularAlumnoClaseViewModel: Encontrados ${alumnosVinculados.size} alumnos vinculados a la clase ${clase.nombre}")
                
                // Mostrar DNIs de los alumnos para depuración
                if (alumnosVinculados.isNotEmpty()) {
                    Timber.d("VincularAlumnoClaseViewModel: DNIs de alumnos vinculados: ${alumnosVinculados.map { it.dni }}")
                }
                
                // Log de todos los alumnos disponibles
                Timber.d("VincularAlumnoClaseViewModel: Total de alumnos disponibles: ${_uiState.value.alumnos.size}")
                if (_uiState.value.alumnos.isNotEmpty()) {
                    Timber.d("VincularAlumnoClaseViewModel: DNIs de todos los alumnos: ${_uiState.value.alumnos.map { it.dni }}")
                }
                
                // Obtener los alumnos disponibles (excluyendo los ya vinculados)
                val alumnosDisponibles = _uiState.value.alumnos.filter { alumno ->
                    !alumnosVinculados.any { it.dni == alumno.dni }
                }
                
                Timber.d("VincularAlumnoClaseViewModel: ${alumnosDisponibles.size} alumnos disponibles (no vinculados) para la clase")

                // Actualizar capacidad de la clase
                val capacidadClase = clase.capacidadMaxima ?: 0
                
                _uiState.update { it.copy(
                    alumnosVinculados = alumnosVinculados,
                    alumnosDisponibles = alumnosDisponibles,
                    capacidadClase = capacidadClase,
                    alumnosEnClase = alumnosVinculados.size
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar clase")
                _uiState.update { it.copy(error = "Error al cargar datos de la clase: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Selecciona un alumno
     */
    fun seleccionarAlumno(alumno: Alumno) {
        _uiState.update { it.copy(alumnoSeleccionado = alumno) }
    }
    
    /**
     * Asigna un alumno a una clase
     */
    fun asignarAlumnoAClase() {
        val alumno = _uiState.value.alumnoSeleccionado ?: return
        val clase = _uiState.value.claseSeleccionada ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 1. Actualizar alumnoIds en la clase
                val resultClase = claseRepository.asignarAlumnoAClase(clase.id, alumno.dni)
                
                // 2. Actualizar la clase en el alumno
                val resultAlumno = if (resultClase is Result.Success) {
                    // Actualizar la clase en el alumno usando el nuevo método
                    alumnoRepository.asignarClaseAAlumno(alumno.dni, clase.id)
                } else {
                    resultClase
                }
                
                when (resultAlumno) {
                    is Result.Success -> {
                        _uiState.update { it.copy(
                            mensaje = "Alumno asignado correctamente a la clase",
                            showAsignarDialog = false,
                            showSuccessMessage = true
                        ) }
                        
                        // Recargar los datos de la clase
                        val claseActual = _uiState.value.claseSeleccionada
                        if (claseActual != null) {
                            seleccionarClase(claseActual)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = resultAlumno.message ?: "Error al asignar alumno a la clase",
                            showAsignarDialog = false
                        ) }
                    }
                    is Result.Loading<*> -> {
                        // Ya se está manejando el estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al asignar alumno a clase")
                _uiState.update { it.copy(
                    error = "Error al asignar alumno: ${e.message}",
                    showAsignarDialog = false
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Desasigna un alumno de una clase
     */
    fun desasignarAlumnoDeClase() {
        val alumnoId = _uiState.value.alumnoSeleccionado?.dni ?: return
        val claseId = _uiState.value.claseSeleccionada?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                Timber.d("ViewModel: Iniciando proceso de desasignación para alumno $alumnoId de clase $claseId")
                
                // 1. Primero desasignamos el alumno de la clase (actualizar alumnosIds en la clase)
                val resultClase = claseRepository.desasignarAlumnoDeClase(claseId, alumnoId)
                
                if (resultClase is Result.Error) {
                    Timber.e(resultClase.exception, "ViewModel: Error al desasignar de la clase: ${resultClase.message}")
                    _uiState.update { it.copy(
                        error = resultClase.message ?: "Error al desasignar alumno de la clase",
                        showConfirmarDesasignacionDialog = false,
                        isLoading = false
                    ) }
                    return@launch
                }
                
                Timber.d("ViewModel: Alumno correctamente desasignado de la clase, procediendo a actualizar alumno")
                
                // 2. Ahora eliminamos la referencia a la clase en el alumno
                val resultAlumno = alumnoRepository.desasignarClaseDeAlumno(alumnoId, claseId)
                
                when (resultAlumno) {
                    is Result.Success -> {
                        Timber.d("ViewModel: Desasignación completa exitosa para alumno $alumnoId de clase $claseId")
                        _uiState.update { it.copy(
                            mensaje = "Alumno desasignado correctamente de la clase",
                            showConfirmarDesasignacionDialog = false,
                            showSuccessMessage = true
                        ) }
                        
                        // Recargar los datos de la clase para actualizar las listas
                        // Eliminado delay innecesario - Firestore es consistente
                        val claseActual = _uiState.value.claseSeleccionada
                        if (claseActual != null) {
                            seleccionarClase(claseActual)
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultAlumno.exception, "ViewModel: Error al desasignar clase del alumno: ${resultAlumno.message}")
                        _uiState.update { it.copy(
                            error = resultAlumno.message ?: "Error al desasignar clase del alumno",
                            showConfirmarDesasignacionDialog = false
                        ) }
                    }
                    is Result.Loading<*> -> {
                        // Ya se está manejando el estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "ViewModel: Error general al desasignar alumno de clase")
                _uiState.update { it.copy(
                    error = "Error al desasignar alumno: ${e.message}",
                    showConfirmarDesasignacionDialog = false
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Crea un nuevo alumno
     */
    fun crearNuevoAlumno() {
        val nuevoAlumno = _uiState.value.nuevoAlumno
        val cursoId = _uiState.value.cursoSeleccionado?.id ?: ""
        
        // Validar campos
        if (nuevoAlumno.nombre.isEmpty() || nuevoAlumno.apellidos.isEmpty() || 
            nuevoAlumno.dni.isEmpty() || nuevoAlumno.fechaNacimiento.isEmpty()) {
            
            _uiState.update { it.copy(
                nuevoAlumno = it.nuevoAlumno.copy(
                    errorNombre = if (nuevoAlumno.nombre.isEmpty()) "El nombre es obligatorio" else null,
                    errorApellidos = if (nuevoAlumno.apellidos.isEmpty()) "Los apellidos son obligatorios" else null,
                    errorDni = if (nuevoAlumno.dni.isEmpty()) "El DNI es obligatorio" else null,
                    errorFechaNacimiento = if (nuevoAlumno.fechaNacimiento.isEmpty()) "La fecha de nacimiento es obligatoria" else null
                )
            ) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Crear el alumno en el repositorio
                val result = alumnoRepository.crearAlumno(
                    nombre = nuevoAlumno.nombre,
                    apellidos = nuevoAlumno.apellidos,
                    dni = nuevoAlumno.dni,
                    fechaNacimiento = nuevoAlumno.fechaNacimiento,
                    cursoId = cursoId
                )
                
                when (result) {
                    is Result.Success<*> -> {
                        Timber.d("Alumno creado exitosamente con DNI: ${nuevoAlumno.dni}")
                        
                        // Si hay una clase seleccionada, asignar automáticamente el alumno a esa clase
                        val claseSeleccionada = _uiState.value.claseSeleccionada
                        if (claseSeleccionada != null) {
                            Timber.d("Asignando automáticamente el alumno ${nuevoAlumno.dni} a la clase ${claseSeleccionada.nombre}")
                            
                            // 1. Asignar alumno a la clase (actualizar alumnosIds en la clase)
                            val resultAsignacionClase = claseRepository.asignarAlumnoAClase(claseSeleccionada.id, nuevoAlumno.dni)
                            
                            // 2. Asignar clase al alumno (actualizar aulaId en el alumno)
                            val resultAsignacionAlumno = if (resultAsignacionClase is Result.Success) {
                                alumnoRepository.asignarClaseAAlumno(nuevoAlumno.dni, claseSeleccionada.id)
                            } else {
                                resultAsignacionClase
                            }
                            
                            when (resultAsignacionAlumno) {
                                is Result.Success -> {
                                    Timber.d("Alumno ${nuevoAlumno.dni} asignado correctamente a la clase ${claseSeleccionada.nombre}")
                                    _uiState.update { it.copy(
                                        mensaje = "Alumno creado y asignado automáticamente a la clase ${claseSeleccionada.nombre}",
                                        showCrearAlumnoDialog = false,
                                        showSuccessMessage = true,
                                        nuevoAlumno = NuevoAlumnoData()
                                    ) }
                                }
                                is Result.Error -> {
                                    Timber.e("Error al asignar alumno a la clase: ${resultAsignacionAlumno.message}")
                                    _uiState.update { it.copy(
                                        mensaje = "Alumno creado correctamente, pero no se pudo asignar automáticamente a la clase. Puedes asignarlo manualmente.",
                                        showCrearAlumnoDialog = false,
                                        showSuccessMessage = true,
                                        nuevoAlumno = NuevoAlumnoData()
                                    ) }
                                }
                                is Result.Loading<*> -> {
                                    // Estado de carga ya manejado
                                }
                            }
                        } else {
                            // No hay clase seleccionada, solo crear el alumno
                            _uiState.update { it.copy(
                                mensaje = "Alumno creado correctamente",
                                showCrearAlumnoDialog = false,
                                showSuccessMessage = true,
                                nuevoAlumno = NuevoAlumnoData()
                            ) }
                        }
                        
                        // Recargar la lista de alumnos
                        val centroId = _uiState.value.centroId
                        if (centroId.isNotEmpty()) {
                            val alumnosResult = alumnoRepository.getAlumnosByCentro(centroId)
                            if (alumnosResult is Result.Success<*>) {
                                @Suppress("UNCHECKED_CAST")
                                val alumnos = alumnosResult.data as List<Alumno>
                                _uiState.update { it.copy(alumnos = alumnos) }
                            }
                        }
                        
                        // Recargar la clase si hay una seleccionada
                        val clase = _uiState.value.claseSeleccionada
                        if (clase != null) {
                            seleccionarClase(clase)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = result.message ?: "Error al crear el alumno",
                            showCrearAlumnoDialog = true // Mantener diálogo abierto para corregir errores
                        ) }
                    }
                    is Result.Loading<*> -> {
                        // Ya se está manejando el estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al crear nuevo alumno")
                _uiState.update { it.copy(
                    error = "Error al crear alumno: ${e.message}",
                    showCrearAlumnoDialog = true // Mantener diálogo abierto para corregir errores
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Actualiza el texto de filtro de alumnos
     */
    fun actualizarFiltroAlumnos(texto: String) {
        _uiState.update { it.copy(textoFiltroAlumnos = texto) }
    }
    
    /**
     * Cambia el modo de visualización de alumnos
     */
    fun cambiarModoVisualizacion(modo: ModoVisualizacionAlumnos) {
        _uiState.update { it.copy(modoVisualizacion = modo) }
    }
    
    /**
     * Refresca todos los alumnos del centro
     */
    fun refrescarAlumnosCentro() {
        viewModelScope.launch {
            val centroId = _uiState.value.centroId
            if (centroId.isBlank()) return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                Timber.d("Refrescando alumnos para el centro $centroId")
                
                // Intentar cargar los alumnos directamente
                val alumnos = alumnoRepository.getAlumnosByCentroId(centroId)
                Timber.d("Cargados ${alumnos.size} alumnos del centro $centroId")
                
                // Actualizar estado con los nuevos alumnos
                _uiState.update { it.copy(alumnos = alumnos) }
                
                // Si hay una clase seleccionada, actualizar los alumnos vinculados y disponibles
                val claseActual = _uiState.value.claseSeleccionada
                if (claseActual != null) {
                    seleccionarClase(claseActual)
                }
                
                mostrarMensaje("Alumnos actualizados correctamente")
            } catch (e: Exception) {
                Timber.e(e, "Error al refrescar alumnos del centro")
                _uiState.update { it.copy(error = "Error al refrescar alumnos: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Cambia el modo de visualización de alumnos
     */
    /**
     * Cicla entre los diferentes modos de visualización
     */
    fun ciclarModoVisualizacion() {
        val currentMode = _uiState.value.modoVisualizacion
        val nextMode = when (currentMode) {
            ModoVisualizacionAlumnos.TODOS -> ModoVisualizacionAlumnos.VINCULADOS
            ModoVisualizacionAlumnos.VINCULADOS -> ModoVisualizacionAlumnos.PENDIENTES
            ModoVisualizacionAlumnos.PENDIENTES -> ModoVisualizacionAlumnos.TODOS
        }
        _uiState.update { it.copy(modoVisualizacion = nextMode) }
    }
    
    /**
     * Actualiza los datos del formulario de nuevo alumno
     */
    fun actualizarNuevoAlumno(nuevoAlumno: NuevoAlumnoData) {
        _uiState.update { it.copy(nuevoAlumno = nuevoAlumno) }
    }
    
    /**
     * Muestra el diálogo para asignar un alumno a la clase
     */
    fun mostrarDialogoAsignar(alumno: Alumno? = null) {
        if (alumno != null) {
            _uiState.update { it.copy(alumnoSeleccionado = alumno) }
        }
        _uiState.update { it.copy(showAsignarDialog = true) }
    }
    
    /**
     * Oculta el diálogo para asignar un alumno a la clase
     */
    fun ocultarDialogoAsignar() {
        _uiState.update { it.copy(showAsignarDialog = false) }
    }
    
    /**
     * Muestra el diálogo para confirmar la desasignación de un alumno
     */
    fun mostrarDialogoConfirmarDesasignacion(alumno: Alumno? = null) {
        if (alumno != null) {
            _uiState.update { it.copy(alumnoSeleccionado = alumno) }
        }
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = true) }
    }
    
    /**
     * Oculta el diálogo para confirmar la desasignación
     */
    fun ocultarDialogoConfirmarDesasignacion() {
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = false) }
    }
    
    /**
     * Muestra el diálogo para crear un nuevo alumno
     */
    fun mostrarDialogoCrearAlumno() {
        _uiState.update { it.copy(showCrearAlumnoDialog = true) }
    }
    
    /**
     * Oculta el diálogo para crear un nuevo alumno
     */
    fun ocultarDialogoCrearAlumno() {
        _uiState.update { it.copy(
            showCrearAlumnoDialog = false,
            nuevoAlumno = NuevoAlumnoData() // Resetear el formulario
        ) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { it.copy(
            mensaje = null,
            showSuccessMessage = false
        ) }
    }
    
    /**
     * Limpia los errores
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Muestra un mensaje en la UI
     */
    fun mostrarMensaje(mensaje: String) {
        _uiState.update { it.copy(
            mensaje = mensaje,
            showSuccessMessage = true
        ) }
    }
    
    /**
     * Inicializa el ViewModel con una clase específica
     */
    fun inicializarConClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Cargar la información de la clase
                val claseResult = claseRepository.getClaseById(claseId)
                if (claseResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val clase = claseResult.data as Clase
                    
                    // Cargar el curso al que pertenece la clase
                    val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                    if (cursoResult is Result.Success<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val curso = cursoResult.data as Curso
                        
                        // Cargar el centro al que pertenece el curso
                        if (curso.centroId.isNotEmpty()) {
                            val centroResult = centroRepository.getCentroById(curso.centroId)
                            if (centroResult is Result.Success<*>) {
                                @Suppress("UNCHECKED_CAST")
                                val centro = centroResult.data as Centro
                                
                                // Actualizar estado con todos los datos cargados
                                _uiState.update { it.copy(
                                    centroId = centro.id,
                                    centroSeleccionado = centro,
                                    cursoSeleccionado = curso
                                ) }
                                
                                // Cargar cursos del centro
                                val cursos = cursoRepository.obtenerCursosPorCentro(centro.id, soloActivos = false)
                                _uiState.update { it.copy(cursos = cursos) }
                                
                                // Cargar clases del curso
                                cargarClasesPorCurso(curso.id)
                                
                                // Cargar alumnos del centro
                                val alumnosResult = alumnoRepository.getAlumnosByCentro(centro.id)
                                if (alumnosResult is Result.Success<*>) {
                                    @Suppress("UNCHECKED_CAST")
                                    val alumnos = alumnosResult.data as List<Alumno>
                                    _uiState.update { it.copy(alumnos = alumnos) }
                                }
                                
                                // Seleccionar la clase específica para cargar sus alumnos
                                seleccionarClase(clase)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar con clase")
                _uiState.update { it.copy(error = "Error al cargar la clase: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
} 