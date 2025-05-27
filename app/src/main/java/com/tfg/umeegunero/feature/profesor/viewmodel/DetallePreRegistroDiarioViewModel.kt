package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.RegistroDiario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de detalle de registro diario
 */
data class DetallePreRegistroDiarioUiState(
    val registro: RegistroDiario? = null,
    val nombreAlumno: String = "",
    val nombreClase: String = "",
    val fechaFormateada: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de detalle de registro diario
 */
@HiltViewModel
class DetallePreRegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetallePreRegistroDiarioUiState())
    val uiState: StateFlow<DetallePreRegistroDiarioUiState> = _uiState.asStateFlow()
    
    /**
     * Carga los datos del registro
     */
    fun cargarRegistro(registroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener el registro
                val registroResult = registroDiarioRepository.getRegistroDiario(registroId)
                
                if (registroResult !is Result.Success) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No se pudo cargar el registro: ${(registroResult as? Result.Error)?.exception?.message ?: "Error desconocido"}"
                    ) }
                    return@launch
                }
                
                val registro = registroResult.data
                
                // Obtener datos del alumno
                val alumnoResult = alumnoRepository.getAlumnoById(registro.alumnoId)
                val nombreAlumno = if (alumnoResult is Result.Success) {
                    "${alumnoResult.data.nombre} ${alumnoResult.data.apellidos}"
                } else {
                    "Alumno no encontrado"
                }
                
                // Obtener datos de la clase
                val claseResult = claseRepository.getClaseById(registro.claseId)
                val nombreClase = if (claseResult is Result.Success) {
                    claseResult.data.nombre
                } else {
                    "Clase no encontrada"
                }
                
                // Formatear la fecha
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaFormateada = dateFormat.format(registro.fecha.toDate())
                
                // Actualizar el estado
                _uiState.update { it.copy(
                    isLoading = false,
                    registro = registro,
                    nombreAlumno = nombreAlumno,
                    nombreClase = nombreClase,
                    fechaFormateada = fechaFormateada
                ) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registro diario: $registroId")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                ) }
            }
        }
    }
} 