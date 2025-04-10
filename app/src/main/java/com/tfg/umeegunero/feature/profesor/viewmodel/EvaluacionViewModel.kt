package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.AlumnoRepositoryImpl
import com.tfg.umeegunero.feature.profesor.models.Criterio
import com.tfg.umeegunero.feature.profesor.models.EvaluacionRubrica
import com.tfg.umeegunero.feature.profesor.models.OpcionCriterio
import com.tfg.umeegunero.feature.profesor.models.Rubrica
import com.tfg.umeegunero.feature.profesor.models.TipoCriterio
import com.tfg.umeegunero.feature.profesor.repository.RubricaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * Estados posibles de la UI en la pantalla de evaluación
 */
sealed class EvaluacionUiState {
    data object Loading : EvaluacionUiState()
    data class Success(
        val alumnos: List<Alumno> = emptyList(),
        val rubricas: List<Rubrica> = emptyList(),
        val rubricaSeleccionada: Rubrica? = null,
        val alumnoSeleccionado: Alumno? = null,
        val trimestreSeleccionado: Int = 1,
        val asignaturaSeleccionada: String = "",
        val evaluaciones: List<EvaluacionRubrica> = emptyList(),
        val criteriosEditando: Map<String, Float> = emptyMap(),
        val comentariosEditando: Map<String, String> = emptyMap(),
        val comentarioGeneral: String = "",
        val mostrarDialogoNuevaRubrica: Boolean = false,
        val mostrarDialogoEvaluacion: Boolean = false,
        val rubricaEnEdicion: Rubrica? = null,
        val criterioEnEdicion: Criterio? = null,
        val mostrarDialogoCriterio: Boolean = false,
        val asignaturas: List<String> = listOf(
            "Matemáticas", "Lengua", "Inglés", "Ciencias", 
            "Arte", "Música", "Educación Física"
        )
    ) : EvaluacionUiState()
    data class Error(val mensaje: String) : EvaluacionUiState()
}

/**
 * ViewModel para gestionar la pantalla de evaluación académica.
 * Maneja las operaciones relacionadas con rúbricas y evaluaciones de alumnos.
 */
@HiltViewModel
class EvaluacionViewModel @Inject constructor(
    private val rubricaRepository: RubricaRepository,
    private val alumnoRepository: AlumnoRepositoryImpl
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<EvaluacionUiState>(EvaluacionUiState.Loading)
    val uiState: StateFlow<EvaluacionUiState> = _uiState.asStateFlow()
    
    init {
        cargarDatos()
    }
    
    /**
     * Carga los datos iniciales: alumnos y rúbricas.
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            // Primero cargamos las rúbricas
            rubricaRepository.getRubricas().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = EvaluacionUiState.Loading
                    }
                    is Result.Success -> {
                        val rubricas = result.data
                        
                        // Ahora cargamos los alumnos
                        val alumnosResult = alumnoRepository.getAlumnosByCentro("centro_prueba")
                        when (alumnosResult) {
                            is Result.Loading -> {
                                _uiState.value = EvaluacionUiState.Loading
                            }
                            is Result.Success -> {
                                val alumnos = alumnosResult.data
                                
                                _uiState.value = EvaluacionUiState.Success(
                                    alumnos = alumnos,
                                    rubricas = rubricas
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = EvaluacionUiState.Error(
                                    alumnosResult.exception?.message ?: "Error al cargar alumnos"
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = EvaluacionUiState.Error(
                            result.exception?.message ?: "Error al cargar rúbricas"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Selecciona una rúbrica para su uso en evaluaciones.
     * @param rubrica Rúbrica seleccionada
     */
    fun seleccionarRubrica(rubrica: Rubrica?) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { 
                state.copy(
                    rubricaSeleccionada = rubrica,
                    // Si hay una rúbrica seleccionada, inicializar los criterios con valores por defecto
                    criteriosEditando = rubrica?.criterios?.associate { it.id to 0f } ?: emptyMap(),
                    comentariosEditando = rubrica?.criterios?.associate { it.id to "" } ?: emptyMap()
                )
            }
        }
    }
    
    /**
     * Selecciona un alumno para su evaluación.
     * @param alumno Alumno seleccionado
     */
    fun seleccionarAlumno(alumno: Alumno?) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(alumnoSeleccionado = alumno) }
            
            if (alumno != null) {
                cargarEvaluacionesAlumno(alumno.dni)
            }
        }
    }
    
    /**
     * Selecciona un trimestre para filtrar evaluaciones.
     * @param trimestre Número de trimestre (1, 2 o 3)
     */
    fun seleccionarTrimestre(trimestre: Int) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(trimestreSeleccionado = trimestre) }
            
            val alumno = state.alumnoSeleccionado
            if (alumno != null) {
                cargarEvaluacionesAlumnoTrimestre(alumno.dni, trimestre)
            }
        }
    }
    
    /**
     * Selecciona una asignatura para filtrar rúbricas.
     * @param asignatura Nombre de la asignatura
     */
    fun seleccionarAsignatura(asignatura: String) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(asignaturaSeleccionada = asignatura) }
            
            if (asignatura.isNotEmpty()) {
                cargarRubricasPorAsignatura(asignatura)
            } else {
                cargarRubricas()
            }
        }
    }
    
    /**
     * Carga las evaluaciones de un alumno específico.
     * @param alumnoId ID del alumno
     */
    private fun cargarEvaluacionesAlumno(alumnoId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is EvaluacionUiState.Success) {
                rubricaRepository.getEvaluacionesPorAlumno(alumnoId).collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            // Mantenemos el estado actual pero mostramos loading de evaluaciones
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(evaluaciones = emptyList())
                                } else {
                                    it
                                }
                            }
                        }
                        is Result.Success -> {
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(evaluaciones = result.data)
                                } else {
                                    it
                                }
                            }
                        }
                        is Result.Error -> {
                            Timber.e("Error al cargar evaluaciones: ${result.exception}")
                            // No cambiamos el estado principal, solo mostramos un error en evaluaciones
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Carga las evaluaciones de un alumno para un trimestre específico.
     * @param alumnoId ID del alumno
     * @param trimestre Número de trimestre
     */
    private fun cargarEvaluacionesAlumnoTrimestre(alumnoId: String, trimestre: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is EvaluacionUiState.Success) {
                rubricaRepository.getEvaluacionesPorAlumnoYTrimestre(alumnoId, trimestre).collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(evaluaciones = emptyList())
                                } else {
                                    it
                                }
                            }
                        }
                        is Result.Success -> {
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(evaluaciones = result.data)
                                } else {
                                    it
                                }
                            }
                        }
                        is Result.Error -> {
                            Timber.e("Error al cargar evaluaciones por trimestre: ${result.exception}")
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Carga todas las rúbricas disponibles.
     */
    private fun cargarRubricas() {
        viewModelScope.launch {
            rubricaRepository.getRubricas().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        // No hacemos nada, mantenemos el estado actual
                    }
                    is Result.Success -> {
                        _uiState.update { 
                            if (it is EvaluacionUiState.Success) {
                                it.copy(rubricas = result.data)
                            } else {
                                it
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e("Error al cargar rúbricas: ${result.exception}")
                    }
                }
            }
        }
    }
    
    /**
     * Carga las rúbricas filtradas por asignatura.
     * @param asignatura Nombre de la asignatura
     */
    private fun cargarRubricasPorAsignatura(asignatura: String) {
        viewModelScope.launch {
            rubricaRepository.getRubricasPorAsignatura(asignatura).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        // No hacemos nada, mantenemos el estado actual
                    }
                    is Result.Success -> {
                        _uiState.update { 
                            if (it is EvaluacionUiState.Success) {
                                it.copy(rubricas = result.data)
                            } else {
                                it
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e("Error al cargar rúbricas por asignatura: ${result.exception}")
                    }
                }
            }
        }
    }
    
    /**
     * Actualiza el valor de un criterio en el proceso de evaluación.
     * @param criterioId ID del criterio
     * @param valor Valor asignado al criterio
     */
    fun actualizarValorCriterio(criterioId: String, valor: Float) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            val criteriosActualizados = state.criteriosEditando.toMutableMap().apply {
                this[criterioId] = valor
            }
            _uiState.update { state.copy(criteriosEditando = criteriosActualizados) }
        }
    }
    
    /**
     * Actualiza el comentario de un criterio en el proceso de evaluación.
     * @param criterioId ID del criterio
     * @param comentario Comentario sobre el criterio
     */
    fun actualizarComentarioCriterio(criterioId: String, comentario: String) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            val comentariosActualizados = state.comentariosEditando.toMutableMap().apply {
                this[criterioId] = comentario
            }
            _uiState.update { state.copy(comentariosEditando = comentariosActualizados) }
        }
    }
    
    /**
     * Actualiza el comentario general de la evaluación.
     * @param comentario Comentario general
     */
    fun actualizarComentarioGeneral(comentario: String) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(comentarioGeneral = comentario) }
        }
    }
    
    /**
     * Guarda una evaluación con los datos actuales.
     */
    fun guardarEvaluacion() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            val rubrica = state.rubricaSeleccionada
            val alumno = state.alumnoSeleccionado
            
            if (rubrica == null || alumno == null) {
                _uiState.update { 
                    if (it is EvaluacionUiState.Success) {
                        it.copy(
                            mostrarDialogoEvaluacion = false,
                            criteriosEditando = emptyMap(),
                            comentariosEditando = emptyMap(),
                            comentarioGeneral = ""
                        )
                    } else {
                        it
                    }
                }
                return
            }
            
            // Calcular la calificación final según los valores asignados a los criterios
            val calificacionFinal = rubrica.calcularCalificacion(state.criteriosEditando)
            
            val evaluacion = EvaluacionRubrica(
                rubricaId = rubrica.id,
                alumnoId = alumno.dni,
                trimestreId = state.trimestreSeleccionado,
                valoresCriterios = state.criteriosEditando,
                comentariosCriterios = state.comentariosEditando,
                comentariosGenerales = state.comentarioGeneral,
                calificacionFinal = calificacionFinal,
                fecha = Date()
            )
            
            viewModelScope.launch {
                rubricaRepository.guardarEvaluacion(evaluacion).collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            // No hacemos nada, mantenemos el estado actual
                        }
                        is Result.Success -> {
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(
                                        mostrarDialogoEvaluacion = false,
                                        criteriosEditando = emptyMap(),
                                        comentariosEditando = emptyMap(),
                                        comentarioGeneral = ""
                                    )
                                } else {
                                    it
                                }
                            }
                            
                            // Recargar las evaluaciones para reflejar los cambios
                            cargarEvaluacionesAlumnoTrimestre(alumno.dni, state.trimestreSeleccionado)
                        }
                        is Result.Error -> {
                            Timber.e("Error al guardar evaluación: ${result.exception}")
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(mostrarDialogoEvaluacion = false)
                                } else {
                                    it
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Muestra el diálogo para crear/editar una rúbrica.
     * @param rubrica Rúbrica a editar (null para crear una nueva)
     */
    fun mostrarDialogoRubrica(rubrica: Rubrica? = null) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { 
                state.copy(
                    mostrarDialogoNuevaRubrica = true,
                    rubricaEnEdicion = rubrica ?: Rubrica(
                        nombre = "",
                        descripcion = "",
                        asignatura = state.asignaturaSeleccionada
                    )
                )
            }
        }
    }
    
    /**
     * Oculta el diálogo de creación/edición de rúbrica.
     */
    fun ocultarDialogoRubrica() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { 
                state.copy(
                    mostrarDialogoNuevaRubrica = false,
                    rubricaEnEdicion = null,
                    criterioEnEdicion = null,
                    mostrarDialogoCriterio = false
                )
            }
        }
    }
    
    /**
     * Muestra el diálogo para crear/editar un criterio.
     * @param criterio Criterio a editar (null para crear uno nuevo)
     */
    fun mostrarDialogoCriterio(criterio: Criterio? = null) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { 
                state.copy(
                    mostrarDialogoCriterio = true,
                    criterioEnEdicion = criterio ?: Criterio(
                        nombre = "",
                        descripcion = "",
                        peso = 1.0f
                    )
                )
            }
        }
    }
    
    /**
     * Oculta el diálogo de creación/edición de criterio.
     */
    fun ocultarDialogoCriterio() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(mostrarDialogoCriterio = false, criterioEnEdicion = null) }
        }
    }
    
    /**
     * Actualiza los datos de la rúbrica en edición.
     */
    fun actualizarRubricaEnEdicion(
        nombre: String? = null,
        descripcion: String? = null,
        asignatura: String? = null
    ) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success && state.rubricaEnEdicion != null) {
            val rubricaActualizada = state.rubricaEnEdicion.copy(
                nombre = nombre ?: state.rubricaEnEdicion.nombre,
                descripcion = descripcion ?: state.rubricaEnEdicion.descripcion,
                asignatura = asignatura ?: state.rubricaEnEdicion.asignatura
            )
            _uiState.update { state.copy(rubricaEnEdicion = rubricaActualizada) }
        }
    }
    
    /**
     * Actualiza los datos del criterio en edición.
     */
    fun actualizarCriterioEnEdicion(
        nombre: String? = null,
        descripcion: String? = null,
        tipo: TipoCriterio? = null,
        peso: Float? = null,
        valorMaximo: Float? = null,
        opciones: List<OpcionCriterio>? = null
    ) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success && state.criterioEnEdicion != null) {
            val criterioActualizado = state.criterioEnEdicion.copy(
                nombre = nombre ?: state.criterioEnEdicion.nombre,
                descripcion = descripcion ?: state.criterioEnEdicion.descripcion,
                tipo = tipo ?: state.criterioEnEdicion.tipo,
                peso = peso ?: state.criterioEnEdicion.peso,
                valorMaximo = valorMaximo ?: state.criterioEnEdicion.valorMaximo,
                opciones = opciones ?: state.criterioEnEdicion.opciones
            )
            _uiState.update { state.copy(criterioEnEdicion = criterioActualizado) }
        }
    }
    
    /**
     * Añade el criterio en edición a la rúbrica en edición.
     */
    fun añadirCriterioARubrica() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success && 
            state.rubricaEnEdicion != null && 
            state.criterioEnEdicion != null) {
            
            val criterioActual = state.criterioEnEdicion
            
            // Si el criterio ya existe, lo reemplazamos
            val criteriosActualizados = state.rubricaEnEdicion.criterios.toMutableList()
            val indiceExistente = criteriosActualizados.indexOfFirst { it.id == criterioActual.id }
            
            if (indiceExistente >= 0) {
                criteriosActualizados[indiceExistente] = criterioActual
            } else {
                criteriosActualizados.add(criterioActual)
            }
            
            val rubricaActualizada = state.rubricaEnEdicion.copy(
                criterios = criteriosActualizados
            )
            
            _uiState.update { 
                state.copy(
                    rubricaEnEdicion = rubricaActualizada,
                    criterioEnEdicion = null,
                    mostrarDialogoCriterio = false
                )
            }
        }
    }
    
    /**
     * Elimina un criterio de la rúbrica en edición.
     * @param criterioId ID del criterio a eliminar
     */
    fun eliminarCriterioDeRubrica(criterioId: String) {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success && state.rubricaEnEdicion != null) {
            val criteriosActualizados = state.rubricaEnEdicion.criterios.filter { it.id != criterioId }
            
            val rubricaActualizada = state.rubricaEnEdicion.copy(
                criterios = criteriosActualizados
            )
            
            _uiState.update { state.copy(rubricaEnEdicion = rubricaActualizada) }
        }
    }
    
    /**
     * Guarda la rúbrica que está siendo editada.
     */
    fun guardarRubrica() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success && state.rubricaEnEdicion != null) {
            viewModelScope.launch {
                rubricaRepository.guardarRubrica(state.rubricaEnEdicion).collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            // No hacemos nada, mantenemos el estado actual
                        }
                        is Result.Success -> {
                            _uiState.update { 
                                if (it is EvaluacionUiState.Success) {
                                    it.copy(
                                        mostrarDialogoNuevaRubrica = false,
                                        rubricaEnEdicion = null
                                    )
                                } else {
                                    it
                                }
                            }
                            
                            // Recargar las rúbricas para reflejar los cambios
                            cargarRubricas()
                        }
                        is Result.Error -> {
                            Timber.e("Error al guardar rúbrica: ${result.exception}")
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Elimina una rúbrica del sistema.
     * @param rubricaId ID de la rúbrica a eliminar
     */
    fun eliminarRubrica(rubricaId: String) {
        viewModelScope.launch {
            rubricaRepository.eliminarRubrica(rubricaId).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        // No hacemos nada, mantenemos el estado actual
                    }
                    is Result.Success -> {
                        // Recargar las rúbricas para reflejar los cambios
                        cargarRubricas()
                    }
                    is Result.Error -> {
                        Timber.e("Error al eliminar rúbrica: ${result.exception}")
                    }
                }
            }
        }
    }
    
    /**
     * Muestra el diálogo de evaluación para crear una nueva evaluación.
     */
    fun mostrarDialogoEvaluacion() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { state.copy(mostrarDialogoEvaluacion = true) }
        }
    }
    
    /**
     * Oculta el diálogo de evaluación.
     */
    fun ocultarDialogoEvaluacion() {
        val state = _uiState.value
        if (state is EvaluacionUiState.Success) {
            _uiState.update { 
                state.copy(
                    mostrarDialogoEvaluacion = false,
                    criteriosEditando = emptyMap(),
                    comentariosEditando = emptyMap(),
                    comentarioGeneral = ""
                )
            }
        }
    }
} 