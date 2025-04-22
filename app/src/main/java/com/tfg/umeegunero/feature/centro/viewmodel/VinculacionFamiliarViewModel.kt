package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VinculacionFamiliarUiState(
    val isLoading: Boolean = false,
    val alumnosFiltrados: List<Alumno> = emptyList(),
    val familiaresFiltrados: List<Usuario> = emptyList(),
    val filtroAlumnos: String = "",
    val filtroFamiliares: String = "",
    val alumnos: List<Alumno> = emptyList(),
    val familiares: List<Usuario> = emptyList(),
    val mensaje: String? = null,
    val error: String? = null,
    val familiaresDelAlumno: List<Usuario> = emptyList(),
    val alumnosDelFamiliar: List<Alumno> = emptyList()
)

@HiltViewModel
class VinculacionFamiliarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val alumnoRepository: AlumnoRepository,
    private val familiarRepository: FamiliarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VinculacionFamiliarUiState())
    val uiState: StateFlow<VinculacionFamiliarUiState> = _uiState.asStateFlow()

    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Obtener el ID del centro del usuario logueado
                val currentUser = authRepository.getCurrentUser()
                val userCentroId = currentUser?.perfiles?.firstOrNull()?.centroId ?: ""

                if (userCentroId.isNotEmpty()) {
                    // Cargar alumnos
                    cargarAlumnosPorCentro(userCentroId)
                    
                    // Cargar familiares
                    cargarFamiliaresPorCentro(userCentroId)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "No se ha podido determinar el centro del usuario"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar los datos: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun cargarAlumnosPorCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val result = alumnoRepository.getAlumnos()
                when (result) {
                    is Result.Success -> {
                        val listaAlumnos = result.data.filter { it.centroId == centroId }
                        _uiState.update { 
                            it.copy(
                                alumnos = listaAlumnos,
                                alumnosFiltrados = listaAlumnos
                            )
                        }
                        
                        if (_uiState.value.isLoading && _uiState.value.familiares.isNotEmpty()) {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Error al cargar los alumnos: ${result.exception?.message}"
                            ) 
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar los alumnos: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun cargarFamiliaresPorCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val result = familiarRepository.getFamiliares()
                when (result) {
                    is Result.Success -> {
                        val listaFamiliares = result.data.filter { usuario ->
                            usuario.perfiles.any { it.centroId == centroId }
                        }
                        _uiState.update { 
                            it.copy(
                                familiares = listaFamiliares,
                                familiaresFiltrados = listaFamiliares
                            )
                        }
                        
                        if (_uiState.value.isLoading && _uiState.value.alumnos.isNotEmpty()) {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Error al cargar los familiares: ${result.exception?.message}"
                            ) 
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar los familiares: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun cargarFamiliaresPorAlumno(alumnoDni: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val result = familiarRepository.getFamiliaresByAlumnoId(alumnoDni)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                familiaresDelAlumno = result.data
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar los familiares del alumno: ${result.exception?.message}"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar familiares del alumno: ${e.message}"
                    )
                }
            }
        }
    }

    fun vincularFamiliar(alumnoDni: String, familiarDni: String, tipoParentesco: SubtipoFamiliar) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val result = familiarRepository.vincularFamiliarAlumno(
                    familiarId = familiarDni,
                    alumnoId = alumnoDni,
                    parentesco = tipoParentesco.name
                )
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                mensaje = "Relación familiar creada con éxito"
                            )
                        }
                        // Recargar datos
                        cargarFamiliaresPorAlumno(alumnoDni)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "No se pudo crear la relación familiar: ${result.exception?.message}"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al vincular familiar: ${e.message}"
                    )
                }
            }
        }
    }

    fun desvincularFamiliar(alumnoId: String, familiarId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val result = familiarRepository.desvincularFamiliarAlumno(
                    familiarId = familiarId,
                    alumnoId = alumnoId
                )
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                mensaje = "Relación familiar eliminada con éxito"
                            )
                        }
                        // Recargar datos
                        cargarFamiliaresPorAlumno(alumnoId)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "No se pudo eliminar la relación familiar: ${result.exception?.message}"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al desvincular familiar: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateFiltroAlumnos(filtro: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val alumnosFiltrados = if (filtro.isEmpty()) {
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
    }

    fun updateFiltroFamiliares(filtro: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val familiaresFiltrados = if (filtro.isEmpty()) {
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
    }

    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 