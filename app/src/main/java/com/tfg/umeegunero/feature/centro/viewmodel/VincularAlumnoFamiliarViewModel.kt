package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de vinculación de alumnos a familiares
 */
data class VincularAlumnoFamiliarUiState(
    val alumnos: List<Usuario> = emptyList(),
    val familiares: List<Usuario> = emptyList(),
    val alumnosFiltrados: List<Usuario> = emptyList(),
    val familiaresFiltrados: List<Usuario> = emptyList(),
    val familiaresDelAlumno: List<Usuario> = emptyList(),
    val alumnosDelFamiliar: List<Usuario> = emptyList(),
    val filtroAlumnos: String = "",
    val filtroFamiliares: String = "",
    val mensaje: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isFamiliarDropdownOpen: Boolean = false,
    val isAlumnoDropdownOpen: Boolean = false,
    val centroId: String = ""
)

/**
 * ViewModel para la pantalla de vinculación de alumnos a familiares
 */
@HiltViewModel
class VincularAlumnoFamiliarViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VincularAlumnoFamiliarUiState())
    val uiState: StateFlow<VincularAlumnoFamiliarUiState> = _uiState.asStateFlow()
    
    private var alumnosJob: Job? = null
    private var familiaresJob: Job? = null
    
    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales necesarios para la pantalla
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Obtener el centro seleccionado por el usuario actual (admin centro)
            val centroId = obtenerCentroSeleccionado()
            
            if (centroId.isNullOrEmpty()) {
                _uiState.update { 
                    it.copy(
                        error = "No se ha podido determinar el centro actual. Por favor, seleccione un centro.",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            _uiState.update { it.copy(centroId = centroId) }
            
            // Cargar alumnos y familiares del centro
            cargarAlumnosPorCentro(centroId)
            cargarFamiliaresPorCentro(centroId)
        }
    }
    
    /**
     * Obtiene el ID del centro seleccionado actualmente
     */
    private suspend fun obtenerCentroSeleccionado(): String? {
        return try {
            authRepository.getCurrentCentroId()
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro del usuario actual")
            null
        }
    }
    
    /**
     * Carga la lista de alumnos del centro
     */
    private fun cargarAlumnosPorCentro(centroId: String) {
        // Cancelar job anterior si existe
        alumnosJob?.cancel()
        
        alumnosJob = viewModelScope.launch {
            try {
                val result = usuarioRepository.obtenerAlumnosPorCentro(centroId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                alumnos = result.data,
                                alumnosFiltrados = result.data,
                                isLoading = familiaresJob?.isActive ?: false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar alumnos: ${result.exception?.message}",
                                isLoading = familiaresJob?.isActive ?: false
                            )
                        }
                        Timber.e(result.exception, "Error al cargar alumnos")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar alumnos: ${e.message}",
                        isLoading = familiaresJob?.isActive ?: false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }
    
    /**
     * Carga la lista de familiares del centro
     */
    private fun cargarFamiliaresPorCentro(centroId: String) {
        // Cancelar job anterior si existe
        familiaresJob?.cancel()
        
        familiaresJob = viewModelScope.launch {
            try {
                val result = usuarioRepository.obtenerFamiliaresPorCentro(centroId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                familiares = result.data,
                                familiaresFiltrados = result.data,
                                isLoading = alumnosJob?.isActive ?: false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar familiares: ${result.exception?.message}",
                                isLoading = alumnosJob?.isActive ?: false
                            )
                        }
                        Timber.e(result.exception, "Error al cargar familiares")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar familiares: ${e.message}",
                        isLoading = alumnosJob?.isActive ?: false
                    )
                }
                Timber.e(e, "Error inesperado al cargar familiares")
            }
        }
    }
    
    /**
     * Carga los familiares vinculados a un alumno
     */
    fun cargarFamiliaresPorAlumno(alumnoDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = usuarioRepository.obtenerFamiliaresPorAlumno(alumnoDni)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                familiaresDelAlumno = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar familiares del alumno: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al cargar familiares del alumno")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar familiares del alumno: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar familiares del alumno")
            }
        }
    }
    
    /**
     * Carga los alumnos vinculados a un familiar
     */
    fun cargarAlumnosPorFamiliar(familiarDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = usuarioRepository.obtenerAlumnosPorFamiliar(familiarDni)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                alumnosDelFamiliar = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar alumnos del familiar: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al cargar alumnos del familiar")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        alumnosDelFamiliar = emptyList(),
                        error = "Error inesperado al cargar alumnos del familiar: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos del familiar")
            }
        }
    }
    
    /**
     * Vincula un familiar con un alumno
     */
    fun vincularFamiliar(alumnoDni: String, familiarDni: String, parentesco: SubtipoFamiliar) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Vincular el familiar al alumno en la base de datos
                val result = usuarioRepository.vincularFamiliarAlumno(
                    alumnoDni = alumnoDni,
                    familiarDni = familiarDni,
                    parentesco = parentesco
                )
                
                when (result) {
                    is Result.Success<*> -> {
                        // Recargar los familiares del alumno
                        cargarFamiliaresPorAlumno(alumnoDni)
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Vinculación exitosa",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al vincular familiar: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al vincular familiar")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al vincular familiar: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al vincular familiar")
            }
        }
    }
    
    /**
     * Desvincula un familiar de un alumno
     */
    fun desvincularFamiliar(alumnoDni: String, familiarDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Desvincular el familiar del alumno en la base de datos
                val result = usuarioRepository.desvincularFamiliarAlumno(
                    alumnoDni = alumnoDni,
                    familiarDni = familiarDni
                )
                
                when (result) {
                    is Result.Success<*> -> {
                        // Recargar los familiares del alumno
                        cargarFamiliaresPorAlumno(alumnoDni)
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Familiar desvinculado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al desvincular familiar: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al desvincular familiar")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al desvincular familiar: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al desvincular familiar")
            }
        }
    }
    
    // Funciones para manejar filtros
    
    /**
     * Actualiza el filtro para la lista de alumnos
     */
    fun updateFiltroAlumnos(filtro: String) {
        _uiState.update { currentState ->
            val alumnosFiltrados = if (filtro.isBlank()) {
                currentState.alumnos
            } else {
                currentState.alumnos.filter { alumno ->
                    alumno.nombre.contains(filtro, ignoreCase = true) ||
                    alumno.apellidos.contains(filtro, ignoreCase = true) ||
                    alumno.dni.contains(filtro, ignoreCase = true)
                }
            }
            
            currentState.copy(
                filtroAlumnos = filtro,
                alumnosFiltrados = alumnosFiltrados
            )
        }
    }
    
    /**
     * Actualiza el filtro para la lista de familiares
     */
    fun updateFiltroFamiliares(filtro: String) {
        _uiState.update { currentState ->
            val familiaresFiltrados = if (filtro.isBlank()) {
                currentState.familiares
            } else {
                currentState.familiares.filter { familiar ->
                    familiar.nombre.contains(filtro, ignoreCase = true) ||
                    familiar.apellidos.contains(filtro, ignoreCase = true) ||
                    familiar.dni.contains(filtro, ignoreCase = true)
                }
            }
            
            currentState.copy(
                filtroFamiliares = filtro,
                familiaresFiltrados = familiaresFiltrados
            )
        }
    }
    
    /**
     * Alterna el estado del dropdown de familiares
     */
    fun toggleFamiliarDropdown() {
        _uiState.update { it.copy(isFamiliarDropdownOpen = !it.isFamiliarDropdownOpen) }
    }
    
    /**
     * Alterna el estado del dropdown de alumnos
     */
    fun toggleAlumnoDropdown() {
        _uiState.update { it.copy(isAlumnoDropdownOpen = !it.isAlumnoDropdownOpen) }
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Recarga los datos principales (alumnos y familiares)
     */
    fun recargarDatos() {
        viewModelScope.launch {
            val centroId = _uiState.value.centroId
            if (centroId.isNotEmpty()) {
                cargarAlumnosPorCentro(centroId)
                cargarFamiliaresPorCentro(centroId)
            }
        }
    }
} 