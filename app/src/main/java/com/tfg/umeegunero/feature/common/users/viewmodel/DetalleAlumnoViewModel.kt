package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

data class DetalleAlumnoUiState(
    val alumno: Result<Alumno> = Result.Loading()
)

@HiltViewModel
class DetalleAlumnoViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleAlumnoUiState())
    val uiState: StateFlow<DetalleAlumnoUiState> = _uiState.asStateFlow()

    fun cargarDetallesAlumno(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(alumno = Result.Loading()) }
            try {
                Timber.d("DetalleAlumnoViewModel: Iniciando carga de detalles para alumno con DNI: '$dni'")
                
                if (dni.isBlank()) {
                    Timber.e("DetalleAlumnoViewModel: Error - DNI vacío")
                    _uiState.update { it.copy(alumno = Result.Error(Exception("El DNI proporcionado está vacío"))) }
                    return@launch
                }
                
                val alumnoResult = alumnoRepository.getAlumnoByDni(dni)
                
                when (alumnoResult) {
                    is Result.Success -> {
                        Timber.d("DetalleAlumnoViewModel: Alumno cargado exitosamente: ${alumnoResult.data.nombre} ${alumnoResult.data.apellidos}")
                        _uiState.update { it.copy(alumno = alumnoResult) }
                    }
                    is Result.Error -> {
                        Timber.e("DetalleAlumnoViewModel: Error al cargar alumno: ${alumnoResult.exception?.message}")
                        _uiState.update { it.copy(alumno = alumnoResult) }
                    }
                    else -> {
                        Timber.w("DetalleAlumnoViewModel: Resultado inesperado al cargar alumno: $alumnoResult")
                        _uiState.update { it.copy(alumno = alumnoResult) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "DetalleAlumnoViewModel: Excepción al cargar detalles de alumno con DNI: $dni")
                _uiState.update { it.copy(alumno = Result.Error(e)) }
            }
        }
    }
} 