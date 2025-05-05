package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para el gestor académico.
 * 
 * Contiene toda la información necesaria para representar el estado actual
 * de la pantalla de gestión académica, incluyendo listas de centros, cursos,
 * clases, selecciones actuales y estados de carga.
 * 
 * @property centros Lista de centros educativos disponibles
 * @property cursos Lista de cursos del centro seleccionado
 * @property clases Lista de clases del curso seleccionado
 * @property selectedCentro Centro actualmente seleccionado
 * @property selectedCurso Curso actualmente seleccionado
 * @property isLoadingCentros Indica si se están cargando los centros
 * @property isLoadingCursos Indica si se están cargando los cursos
 * @property isLoadingClases Indica si se están cargando las clases
 * @property error Mensaje de error en caso de fallo, o null si no hay error
 * @property centroMenuExpanded Indica si el menú desplegable de centros está expandido
 * @property cursoMenuExpanded Indica si el menú desplegable de cursos está expandido
 */
data class GestorAcademicoUiState(
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val selectedCentro: Centro? = null,
    val selectedCurso: Curso? = null,
    val isLoadingCentros: Boolean = false,
    val isLoadingCursos: Boolean = false,
    val isLoadingClases: Boolean = false,
    val error: String? = null,
    val centroMenuExpanded: Boolean = false,
    val cursoMenuExpanded: Boolean = false
)

/**
 * ViewModel para la gestión académica de centros, cursos y clases.
 * 
 * Proporciona funcionalidad para cargar y gestionar la estructura académica
 * de la aplicación, permitiendo navegar entre centros, cursos y clases, y
 * gestionar las relaciones entre estos elementos.
 * 
 * @property centroRepository Repositorio para operaciones relacionadas con centros educativos
 * @property cursoRepository Repositorio para operaciones relacionadas con cursos
 * @property claseRepository Repositorio para operaciones relacionadas con clases
 */
@HiltViewModel
class GestorAcademicoViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GestorAcademicoUiState())
    
    /**
     * Estado observable de la UI para la gestión académica.
     */
    val uiState: StateFlow<GestorAcademicoUiState> = _uiState.asStateFlow()

    init {
        cargarCentros()
    }

    /**
     * Carga la lista de centros educativos desde el repositorio.
     */
    fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCentros = true) }
            try {
                when (val centrosResult = centroRepository.getAllCentros()) {
                    is Result.Success -> {
                        _uiState.update { it.copy(centros = centrosResult.data, isLoadingCentros = false) }
                        centrosResult.data.firstOrNull()?.let { primerCentro ->
                            onCentroSelected(primerCentro) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar centros: ${centrosResult.exception?.message}", isLoadingCentros = false) }
                    }
                    else -> { /* Loading state is handled */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al cargar centros")
                _uiState.update { it.copy(error = "Error inesperado al cargar centros: ${e.message}", isLoadingCentros = false) }
            }
        }
    }

    /**
     * Actualiza el centro seleccionado y carga sus cursos.
     * 
     * @param centro Centro a seleccionar
     */
    fun onCentroSelected(centro: Centro) {
        _uiState.update { it.copy(selectedCentro = centro, selectedCurso = null, cursos = emptyList(), clases = emptyList()) }
        observarCursos(centro.id)
    }

    /**
     * Observa los cambios en los cursos asociados a un centro específico usando Flows.
     * 
     * @param centroId Identificador único del centro del que observar los cursos
     */
    private fun observarCursos(centroId: String) {
        _uiState.update { it.copy(isLoadingCursos = true, error = null) }
        
        cursoRepository.obtenerCursosPorCentroFlow(centroId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("Cursos actualizados para centro $centroId: ${result.data.size}")
                        _uiState.update { it.copy(cursos = result.data, isLoadingCursos = false) }
                        if (_uiState.value.selectedCurso == null && result.data.isNotEmpty()) {
                            onCursoSelected(result.data.first())
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al observar cursos del centro $centroId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar cursos", isLoadingCursos = false) }
                    }
                    is Result.Loading -> {
                         _uiState.update { it.copy(isLoadingCursos = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "Excepción en el Flow de cursos del centro $centroId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingCursos = false) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Actualiza el curso seleccionado y carga sus clases.
     * 
     * @param curso Curso a seleccionar
     */
    fun onCursoSelected(curso: Curso) {
        _uiState.update { it.copy(selectedCurso = curso, clases = emptyList()) }
        observarClases(curso.id)
    }

    /**
     * Inicializa el viewModel con un cursoId específico, cargando el curso y sus clases.
     * Útil cuando se llega a la pantalla directamente con un cursoId sin tener el objeto Curso.
     * 
     * @param cursoId ID del curso a seleccionar
     */
    fun inicializarConCursoId(cursoId: String) {
        viewModelScope.launch {
            try {
                Timber.d("🚀 Inicializando con cursoId: $cursoId")
                
                // Log de estado actual
                Timber.d("📊 Estado actual: cursos=${_uiState.value.cursos.size}, selectedCurso=${_uiState.value.selectedCurso}")
                
                // Primero comprobamos si ya tenemos el curso cargado
                val cursoActual = _uiState.value.cursos.find { it.id == cursoId }
                
                if (cursoActual != null) {
                    Timber.d("📚 Curso encontrado en lista actual: ${cursoActual.nombre}")
                    onCursoSelected(cursoActual)
                    return@launch
                }
                
                // Si no está en la lista, intentamos obtenerlo del repositorio
                Timber.d("🔍 Curso no encontrado en lista, consultando repositorio...")
                when (val result = cursoRepository.getCursoById(cursoId)) {
                    is Result.Success -> {
                        val curso = result.data
                        Timber.d("✅ Curso obtenido del repositorio: ${curso.nombre}")
                        onCursoSelected(curso)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌ Error al obtener curso por ID")
                        _uiState.update { it.copy(error = "Error al obtener curso: ${result.exception?.message}") }
                    }
                    else -> { Timber.d("⏳ Esperando resultado de consulta de curso...") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌❌ Excepción al inicializar con cursoId")
                _uiState.update { it.copy(error = "Error inesperado: ${e.message}") }
            }
        }
    }

    /**
     * Observa los cambios en las clases asociadas a un curso específico usando Flows.
     * 
     * @param cursoId Identificador único del curso del que observar las clases
     */
    private fun observarClases(cursoId: String) {
        _uiState.update { it.copy(isLoadingClases = true, error = null) }
        
        // Comprobación de seguridad - Asegurarse de que estamos utilizando el servicio correcto para obtener las clases
        Timber.d("🔍🔍 INICIANDO observación de clases para el curso ID: '$cursoId'")
        Timber.d("📊 Estado actual: clases=${_uiState.value.clases.size}, isLoadingClases=${_uiState.value.isLoadingClases}")
        
        // Verificar que el ID no esté vacío
        if (cursoId.isBlank()) {
            Timber.e("❌❌ ERROR: Se intentó observar clases con un cursoId vacío!")
            _uiState.update { it.copy(error = "ID de curso no válido", isLoadingClases = false) }
            return
        }
        
        // Añadir log adicional para verificar el curso actual
        val selectedCurso = _uiState.value.selectedCurso
        Timber.d("🔍 Curso seleccionado actual: ${selectedCurso?.id}, ${selectedCurso?.nombre}")
        
        claseRepository.obtenerClasesPorCursoFlow(cursoId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("✅✅ SUCCESS: Clases actualizadas para curso $cursoId: ${result.data.size}")
                        result.data.forEach { clase ->
                            Timber.d("📝 Clase encontrada: id=${clase.id}, nombre=${clase.nombre}, aula=${clase.aula}, cursoId=${clase.cursoId}")
                        }
                        _uiState.update { it.copy(clases = result.data, isLoadingClases = false) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌❌ ERROR: Error al observar clases del curso $cursoId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar clases", isLoadingClases = false) }
                    }
                    is Result.Loading -> {
                         Timber.d("⏳ LOADING: Cargando clases para curso $cursoId")
                         _uiState.update { it.copy(isLoadingClases = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "❌❌❌ CATCH: Excepción en el Flow de clases del curso $cursoId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingClases = false) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Actualiza el estado de expansión del menú de centros.
     * 
     * @param expanded Estado de expansión del menú
     */
    fun onCentroMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(centroMenuExpanded = expanded) }
    }

    /**
     * Actualiza el estado de expansión del menú de cursos.
     * 
     * @param expanded Estado de expansión del menú
     */
    fun onCursoMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(cursoMenuExpanded = expanded) }
    }

    /**
     * Elimina un curso específico del sistema.
     * 
     * @param cursoId Identificador único del curso a eliminar
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            try {
                when (val result = cursoRepository.deleteCurso(cursoId)) {
                    is Result.Success -> {
                         _uiState.update { it.copy(isLoadingCursos = false) }
                        Timber.d("Curso $cursoId eliminado correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar curso: ${result.exception?.message}", isLoadingCursos = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingCursos = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar curso")
                _uiState.update { it.copy(error = "Error inesperado al eliminar curso: ${e.message}", isLoadingCursos = false) }
            }
        }
    }

    /**
     * Elimina una clase específica del sistema.
     * 
     * @param claseId Identificador único de la clase a eliminar
     */
    fun eliminarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingClases = true) }
            try {
                when (val result = claseRepository.eliminarClase(claseId)) {
                    is Result.Success<*> -> {
                         _uiState.update { it.copy(isLoadingClases = false) }
                         Timber.d("Clase $claseId eliminada correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar clase: ${result.exception?.message}", isLoadingClases = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingClases = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar clase")
                _uiState.update { it.copy(error = "Error inesperado al eliminar clase: ${e.message}", isLoadingClases = false) }
            }
        }
    }
} 