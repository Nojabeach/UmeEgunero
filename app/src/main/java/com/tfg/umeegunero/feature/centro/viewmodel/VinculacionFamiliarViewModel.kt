package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.model.Result
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
 * Estado UI para la pantalla de vinculación familiar
 */
data class VinculacionFamiliarUiState(
    // Datos y filtros
    val alumnos: List<Usuario> = emptyList(),
    val familiares: List<Usuario> = emptyList(),
    val familiaresDelAlumno: List<Usuario> = emptyList(),
    val alumnosDelFamiliar: List<Usuario> = emptyList(),
    val soloActivos: Boolean = true,
    val searchText: String = "",
    val selectedTab: Int = 0,
    val isFamiliarDropdownExpanded: Boolean = false,
    
    // Estados UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    
    // Centro
    val centroId: String = ""
) {
    // Alumnos filtrados según los criterios actuales
    val alumnosFiltrados: List<Usuario>
        get() {
            val filtradosPorActivo = if (soloActivos) {
                alumnos.filter { it.activo }
            } else {
                alumnos
            }
            
            return if (searchText.isBlank()) {
                filtradosPorActivo
            } else {
                filtradosPorActivo.filter {
                    it.nombre.contains(searchText, ignoreCase = true) ||
                    it.apellidos.contains(searchText, ignoreCase = true) ||
                    it.dni.contains(searchText, ignoreCase = true)
                }
            }
        }
    
    // Familiares filtrados según los criterios actuales
    val familiaresFiltrados: List<Usuario>
        get() {
            val filtradosPorActivo = if (soloActivos) {
                familiares.filter { it.activo }
            } else {
                familiares
            }
            
            return if (searchText.isBlank()) {
                filtradosPorActivo
            } else {
                filtradosPorActivo.filter {
                    it.nombre.contains(searchText, ignoreCase = true) ||
                    it.apellidos.contains(searchText, ignoreCase = true) ||
                    it.dni.contains(searchText, ignoreCase = true)
                }
            }
        }
}

@HiltViewModel
class VinculacionFamiliarViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VinculacionFamiliarUiState())
    val uiState: StateFlow<VinculacionFamiliarUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el ID del centro del administrador actual
        getCentroIdFromCurrentUser()
    }
    
    /**
     * Obtiene el ID del centro del usuario administrador actual
     */
    private fun getCentroIdFromCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                
                if (currentUser != null) {
                    // Obtener el perfil completo del usuario
                    val usuarioResult = usuarioRepository.getUsuarioByEmail(currentUser.email)
                    
                    if (usuarioResult is Result.Success) {
                        val usuario = usuarioResult.data
                        
                        // Obtener el centroId del primer perfil de tipo ADMIN_CENTRO
                        val centroId = usuario?.perfiles
                            ?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                            ?.centroId
                        
                        centroId?.let {
                            _uiState.update { state -> state.copy(centroId = it) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener centro del usuario actual")
            }
        }
    }
    
    /**
     * Carga todos los alumnos del centro
     */
    fun cargarAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val centroId = _uiState.value.centroId
                
                if (centroId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo determinar el centro del administrador",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Obtener alumnos por centro
                val result = usuarioRepository.obtenerAlumnosPorCentro(centroId)
                
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                alumnos = result.data as List<Usuario>,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar alumnos: ${result.exception.message}",
                                isLoading = false
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
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }
    
    /**
     * Carga todos los familiares del centro
     */
    fun cargarFamiliares() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val centroId = _uiState.value.centroId
                
                if (centroId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo determinar el centro del administrador",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Obtener familiares por centro
                val result = usuarioRepository.obtenerFamiliaresPorCentro(centroId)
                
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                familiares = result.data as List<Usuario>,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar familiares: ${result.exception.message}",
                                isLoading = false
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
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar familiares")
            }
        }
    }
    
    /**
     * Carga los familiares vinculados a un alumno específico
     */
    fun cargarFamiliaresPorAlumno(alumnoDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener los familiares vinculados al alumno
                val result = usuarioRepository.obtenerFamiliaresPorAlumno(alumnoDni)
                
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                familiaresDelAlumno = result.data as List<Usuario>,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                familiaresDelAlumno = emptyList(),
                                error = "Error al cargar familiares del alumno: ${result.exception.message}",
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
                        familiaresDelAlumno = emptyList(),
                        error = "Error inesperado al cargar familiares del alumno: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar familiares del alumno")
            }
        }
    }
    
    /**
     * Carga los alumnos vinculados a un familiar específico
     */
    fun cargarAlumnosPorFamiliar(familiarDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener los alumnos vinculados al familiar
                val result = usuarioRepository.obtenerAlumnosPorFamiliar(familiarDni)
                
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                alumnosDelFamiliar = result.data as List<Usuario>,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                alumnosDelFamiliar = emptyList(),
                                error = "Error al cargar alumnos del familiar: ${result.exception.message}",
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
                                error = "Error al vincular familiar: ${result.exception.message}",
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
                                error = "Error al desvincular familiar: ${result.exception.message}",
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
    fun toggleSoloActivos() {
        _uiState.update { it.copy(soloActivos = !it.soloActivos) }
    }
    
    fun updateSearchText(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }
    
    fun resetFiltros() {
        _uiState.update { 
            it.copy(
                searchText = "",
                soloActivos = true
            )
        }
    }
    
    // Funciones para manejar pestañas y dropdowns
    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun toggleFamiliarDropdown() {
        _uiState.update { it.copy(isFamiliarDropdownExpanded = !it.isFamiliarDropdownExpanded) }
    }
    
    // Funciones para manejar mensajes
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 