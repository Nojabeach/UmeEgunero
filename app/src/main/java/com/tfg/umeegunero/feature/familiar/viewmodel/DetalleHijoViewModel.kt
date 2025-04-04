package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
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
 * Estado UI para la pantalla de detalle de alumno
 */
data class DetalleHijoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val alumno: Alumno? = null,
    val centroNombre: String? = null,
    val profesores: List<Usuario> = emptyList(),
    val familiar: Usuario? = null  // Información del familiar asociado
)

/**
 * ViewModel para la pantalla de detalle de alumno/hijo
 */
@HiltViewModel
class DetalleHijoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleHijoUiState())
    val uiState: StateFlow<DetalleHijoUiState> = _uiState.asStateFlow()

    init {
        // Obtener el DNI del alumno de la navegación
        val alumnoDni = savedStateHandle.get<String>("alumnoDni")

        if (alumnoDni != null) {
            cargarAlumno(alumnoDni)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener el ID del alumno")
            }
        }
    }

    /**
     * Carga los datos del alumno
     */
    private fun cargarAlumno(alumnoDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val alumnoResult = usuarioRepository.getAlumnoPorDni(alumnoDni)

                when (alumnoResult) {
                    is Result.Success<Alumno> -> {
                        val alumno = alumnoResult.data
                        _uiState.update {
                            it.copy(
                                alumno = alumno,
                                isLoading = false
                            )
                        }

                        // Cargar información adicional
                        cargarInformacionCentro(alumno.centroId)
                        cargarProfesores(alumno.profesorIds ?: emptyList())
                        cargarFamiliar()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar el alumno: ${alumnoResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnoResult.exception, "Error al cargar alumno")
                    }
                    is Result.Loading -> { 
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumno")
            }
        }
    }

    /**
     * Carga la información del centro educativo
     */
    private fun cargarInformacionCentro(centroId: String) {
        // Aquí se implementaría la carga de datos del centro educativo
        // Por ahora, dejamos el centroNombre como null y se mostrará el ID
    }

    /**
     * Carga la información de los profesores
     */
    private fun cargarProfesores(profesorIds: List<String>) {
        if (profesorIds.isEmpty()) return

        viewModelScope.launch {
            try {
                val profesores = mutableListOf<Usuario>()

                for (profesorId in profesorIds) {
                    val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                    when (profesorResult) {
                        is Result.Success<Usuario> -> {
                            profesores.add(profesorResult.data)
                        }
                        is Result.Error -> {
                            Timber.e(profesorResult.exception, "Error al cargar profesor: $profesorId")
                        }
                        is Result.Loading -> {
                            // No actualizamos estado ya que es una operación secundaria
                        }
                    }
                }

                _uiState.update {
                    it.copy(profesores = profesores)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar profesores")
            }
        }
    }

    /**
     * Carga la información del familiar asociado al alumno
     */
    private fun cargarFamiliar() {
        viewModelScope.launch {
            try {
                // Obtener el familiar actual
                val familiarId = usuarioRepository.getUsuarioActualId()

                if (familiarId.isNotBlank()) {
                    val familiarResult = usuarioRepository.getUsuarioPorDni(familiarId)

                    when (familiarResult) {
                        is Result.Success<Usuario> -> {
                            _uiState.update {
                                it.copy(familiar = familiarResult.data)
                            }
                        }
                        is Result.Error -> {
                            Timber.e(familiarResult.exception, "Error al cargar familiar")
                        }
                        is Result.Loading -> {
                            // No actualizamos estado ya que es una operación secundaria
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar familiar")
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}