package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
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
 * Estado UI para la pantalla de detalle de clase
 * 
 * Este estado contiene toda la información necesaria para representar
 * la pantalla de detalle de una clase, incluyendo la información de la clase,
 * los alumnos asignados, el profesor titular y los profesores auxiliares.
 * 
 * @property clase Datos de la clase
 * @property alumnos Lista de alumnos asignados a la clase
 * @property profesorTitular Profesor responsable principal de la clase
 * @property profesoresAuxiliares Lista de profesores auxiliares asignados a la clase
 * @property isLoading Indica si se están cargando datos
 * @property error Mensaje de error, si existe
 */
data class DetalleClaseUiState(
    val clase: Clase? = null,
    val alumnos: List<Alumno> = emptyList(),
    val profesorTitular: Usuario? = null,
    val profesoresAuxiliares: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val capacidadActual: Int = 0,
    val capacidadMaxima: Int = 0,
    val porcentajeOcupacion: Int = 0,
    val plazasDisponibles: Int = 0
)

/**
 * ViewModel para la pantalla de detalle de clase
 * 
 * Este ViewModel se encarga de cargar y gestionar todos los datos
 * relacionados con una clase específica, incluyendo sus alumnos y profesores.
 * 
 * @param savedStateHandle Handle para acceder a los argumentos de navegación
 * @param claseRepository Repositorio para operaciones con clases
 * @param alumnoRepository Repositorio para operaciones con alumnos
 * @param usuarioRepository Repositorio para operaciones con usuarios
 */
@HiltViewModel
class DetalleClaseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleClaseUiState(isLoading = true))
    val uiState: StateFlow<DetalleClaseUiState> = _uiState.asStateFlow()

    private val claseId: String = savedStateHandle.get<String>("claseId") ?: ""

    init {
        if (claseId.isNotEmpty()) {
            cargarDetallesClase(claseId)
        } else {
            _uiState.update { it.copy(
                isLoading = false,
                error = "ID de clase no especificado"
            )}
        }
    }

    /**
     * Carga todos los detalles de la clase: datos de la clase, alumnos y profesores
     * 
     * @param claseId ID de la clase a cargar
     */
    private fun cargarDetallesClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Cargar información de la clase
            when (val resultClase = claseRepository.getClaseById(claseId)) {
                is Result.Success -> {
                    val clase = resultClase.data
                    _uiState.update { it.copy(clase = clase) }
                    
                    // 2. Cargar alumnos de la clase
                    cargarAlumnos(clase.alumnosIds)
                    
                    // 3. Cargar profesor titular (primero intentar profesorTitularId, luego profesorId)
                    if (!clase.profesorTitularId.isNullOrBlank()) {
                        // Si existe profesorTitularId, usarlo para cargar el profesor titular
                        cargarProfesorTitular(clase.profesorTitularId)
                    } else if (!clase.profesorId.isNullOrBlank()) {
                        // Si no hay profesorTitularId pero hay profesorId, usar ese 
                        Timber.d("Usando profesorId como profesor titular: ${clase.profesorId}")
                        cargarProfesorTitular(clase.profesorId)
                    } else {
                        Timber.d("La clase no tiene profesor asignado")
                    }
                    
                    // 4. Cargar profesores auxiliares
                    cargarProfesoresAuxiliares(clase.profesoresAuxiliaresIds)
                    
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    Timber.e(resultClase.exception, "Error al cargar la clase $claseId")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar la clase: ${resultClase.exception?.message ?: "Error desconocido"}"
                    )}
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Carga la información de los alumnos asignados a la clase
     * 
     * @param alumnosIds Lista de IDs de alumnos a cargar
     */
    private suspend fun cargarAlumnos(alumnosIds: List<String>?) {
        val idsValidas = alumnosIds?.filter { it.isNotBlank() } ?: emptyList()
        
        try {
            // Primero intentamos cargar los alumnos directamente por el ID de la clase
            // Esta es la forma más fiable de obtener todos los alumnos de una clase
            Timber.d("Intentando cargar alumnos por claseId: $claseId")
            val resultPorClase = alumnoRepository.getAlumnosByClaseId(claseId)
            
            if (resultPorClase is Result.Success && resultPorClase.data.isNotEmpty()) {
                Timber.d("Alumnos encontrados por claseId: ${resultPorClase.data.size}")
                _uiState.update { it.copy(alumnos = resultPorClase.data) }
                return // Si encontramos alumnos, terminamos aquí
            }
            
            // Si no hay resultados por claseId, intentamos con el método getAlumnosPorClase
            // que usa múltiples estrategias para encontrar alumnos
            Timber.d("Intentando método alternativo getAlumnosPorClase para: $claseId")
            val alumnosPorClase = alumnoRepository.getAlumnosPorClase(claseId)
            
            if (alumnosPorClase.isNotEmpty()) {
                Timber.d("Alumnos encontrados con getAlumnosPorClase: ${alumnosPorClase.size}")
                _uiState.update { it.copy(alumnos = alumnosPorClase) }
                return // Si encontramos alumnos, terminamos aquí
            }
            
            // Si aún no encontramos alumnos, intentamos cargar uno por uno por DNI/ID
            if (idsValidas.isNotEmpty()) {
                Timber.d("Intentando cargar ${idsValidas.size} alumnos con IDs individuales: $idsValidas")
                val alumnos = mutableListOf<Alumno>()
                
                for (alumnoId in idsValidas) {
                    try {
                        // Intentar primero obtener por DNI
                        val alumnoResult = alumnoRepository.getAlumnoByDni(alumnoId)
                        if (alumnoResult is Result.Success<Alumno>) {
                            // TODO: Implementar método para añadir alumno a clase
                            // Anteriormente: addAlumnoToClase(alumnoResult.data, claseId)
                            alumnos.add(alumnoResult.data)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al intentar cargar el alumno $alumnoId")
                    }
                }
                
                if (alumnos.isNotEmpty()) {
                    Timber.d("Total de alumnos cargados por IDs individuales: ${alumnos.size}")
                    _uiState.update { it.copy(alumnos = alumnos) }
                    return
                }
            }
            
            // Si no se ha encontrado ningún alumno, actualizar el estado con lista vacía
            Timber.w("No se encontraron alumnos por ningún método para la clase: $claseId")
            _uiState.update { it.copy(alumnos = emptyList()) }
            
        } catch (e: Exception) {
            Timber.e(e, "Error general al cargar los alumnos para la clase: $claseId")
            _uiState.update { it.copy(alumnos = emptyList()) }
        }
    }

    /**
     * Carga la información del profesor titular de la clase
     * 
     * @param profesorId ID del profesor titular
     */
    private suspend fun cargarProfesorTitular(profesorId: String?) {
        if (profesorId.isNullOrBlank()) return
        
        try {
            when (val result = usuarioRepository.obtenerUsuarioPorId(profesorId)) {
                is Result.Success -> {
                    result.data?.let { profesor ->
                        _uiState.update { it.copy(profesorTitular = profesor) }
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesor titular $profesorId")
                }
                is Result.Loading -> { /* No action needed */ }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar el profesor titular")
        }
    }

    /**
     * Carga la información de los profesores auxiliares de la clase
     * 
     * @param profesoresIds Lista de IDs de profesores auxiliares
     */
    private suspend fun cargarProfesoresAuxiliares(profesoresIds: List<String>?) {
        val idsValidas = profesoresIds?.filter { it.isNotBlank() } ?: emptyList()
        
        if (idsValidas.isEmpty()) return
        
        try {
            val profesores = mutableListOf<Usuario>()
            
            for (profesorId in idsValidas) {
                when (val result = usuarioRepository.obtenerUsuarioPorId(profesorId)) {
                    is Result.Success -> {
                        result.data?.let { profesores.add(it) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar profesor auxiliar $profesorId")
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
            
            _uiState.update { it.copy(profesoresAuxiliares = profesores) }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar los profesores auxiliares")
        }
    }

    // Método para calcular métricas de la clase con manejo correcto de nulos
    private fun calcularMetricasClase(clase: Clase): Map<String, Int> {
        // Usamos operadores de llamada segura para evitar NullPointerException
        val capacidadActual = clase.alumnosIds?.size ?: 0
        val capacidadMaxima = clase.capacidadMaxima ?: 0
        
        val porcentajeOcupacion = if (capacidadMaxima > 0) {
            (capacidadActual.toFloat() / capacidadMaxima * 100f).toInt()
        } else {
            0
        }
        
        val plazasDisponibles = capacidadMaxima - capacidadActual
        
        return mapOf(
            "capacidadActual" to capacidadActual,
            "capacidadMaxima" to capacidadMaxima,
            "porcentajeOcupacion" to porcentajeOcupacion,
            "plazasDisponibles" to plazasDisponibles
        )
    }

    // Modificar el método de inicialización para usar el nuevo método
    private fun inicializarDetalleClase(clase: Clase) {
        val metricas = calcularMetricasClase(clase)
        
        _uiState.update { currentState ->
            currentState.copy(
                clase = clase,
                // Otros campos existentes...
                capacidadActual = metricas["capacidadActual"] ?: 0,
                capacidadMaxima = metricas["capacidadMaxima"] ?: 0,
                porcentajeOcupacion = metricas["porcentajeOcupacion"] ?: 0,
                plazasDisponibles = metricas["plazasDisponibles"] ?: 0
            )
        }
    }
} 