package com.tfg.umeegunero.feature.familiar.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.Calendar
import javax.inject.Inject

data class ConsultaRegistroDiarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registros: List<RegistroActividad> = emptyList(),
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val registroSeleccionado: RegistroActividad? = null,
    val fechaSeleccionada: Date? = null
)

@HiltViewModel
class ConsultaRegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConsultaRegistroDiarioUiState())
    val uiState: StateFlow<ConsultaRegistroDiarioUiState> = _uiState.asStateFlow()
    
    /**
     * Carga los registros de un alumno
     */
    fun cargarRegistros(alumnoId: String) {
        _uiState.update { it.copy(isLoading = true, error = null, alumnoId = alumnoId) }
        
        // Cargar datos del alumno
        cargarDatosAlumno(alumnoId)
        
        viewModelScope.launch {
            try {
                registroDiarioRepository.obtenerRegistrosAlumno(alumnoId)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false, 
                                        registros = result.data.sortedByDescending { reg -> reg.fecha }
                                    ) 
                                }
                            }
                            is Result.Error -> {
                                Timber.e(result.exception, "Error al obtener registros del alumno $alumnoId")
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false, 
                                        error = result.exception?.message ?: "Error desconocido"
                                    ) 
                                }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoading = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros del alumno $alumnoId")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar registros"
                    ) 
                }
            }
        }
    }
    
    /**
     * Carga los datos básicos del alumno
     */
    private fun cargarDatosAlumno(alumnoId: String) {
        viewModelScope.launch {
            try {
                val result = alumnoRepository.getAlumnoById(alumnoId)
                if (result is Result.Success) {
                    val alumno = result.data
                    _uiState.update { 
                        it.copy(
                            alumnoNombre = "${alumno.nombre} ${alumno.apellidos}".trim()
                        )
                    }
                    Timber.d("Datos del alumno cargados: ${alumno.nombre} ${alumno.apellidos}")
                } else if (result is Result.Error) {
                    Timber.e(result.exception, "Error al cargar datos del alumno $alumnoId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos del alumno $alumnoId")
            }
        }
    }
    
    /**
     * Carga los registros por fecha específica
     */
    fun cargarRegistrosPorFecha(alumnoId: String, fecha: Date) {
        _uiState.update { 
            it.copy(
                isLoading = true, 
                error = null, 
                alumnoId = alumnoId, 
                fechaSeleccionada = fecha
            ) 
        }
        
        viewModelScope.launch {
            try {
                // Crear rango de fecha (inicio y fin del día)
                val calendar = Calendar.getInstance()
                calendar.time = fecha
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val inicioDia = calendar.time
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val finDia = calendar.time
                
                // Obtener registros en ese rango de fecha
                val result = registroDiarioRepository.obtenerRegistrosPorFecha(
                    alumnoId = alumnoId,
                    fechaInicio = Timestamp(inicioDia),
                    fechaFin = Timestamp(finDia)
                )
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                registros = result.data.sortedByDescending { reg -> reg.fecha }
                            ) 
                        }
                        Timber.d("Registros por fecha cargados: ${result.data.size} para $alumnoId en $fecha")
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registros por fecha")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.exception?.message ?: "Error desconocido"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros por fecha para $alumnoId")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar registros por fecha"
                    ) 
                }
            }
        }
    }
    
    /**
     * Carga un registro específico por ID
     */
    fun cargarRegistroPorId(registroId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        Timber.d("Iniciando carga de registro por ID: $registroId")
        
        viewModelScope.launch {
            try {
                val result = registroDiarioRepository.obtenerRegistroPorId(registroId)
                
                when (result) {
                    is Result.Success -> {
                        val registro = result.data
                        Timber.d("Registro cargado con éxito: $registroId (alumnoId: ${registro.alumnoId}, fecha: ${registro.fecha})")
                        
                        // Guardar el registro en el estado y también actualizar datos del alumno
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                registroSeleccionado = registro,
                                alumnoId = registro.alumnoId,
                                alumnoNombre = registro.alumnoNombre.ifEmpty { "Alumno" }
                            ) 
                        }
                        
                        // También cargamos los datos del alumno para asegurar que tenemos la información completa
                        if (registro.alumnoId.isNotEmpty()) {
                            cargarDatosAlumno(registro.alumnoId)
                        }
                        
                        // Marcar automáticamente como visto si no lo está ya
                        if (!registro.vistoPorFamiliar) {
                            marcarComoVisto(registroId)
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registro $registroId: ${result.exception?.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.exception?.message ?: "Error al cargar el registro"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al cargar registro $registroId: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar el registro"
                    ) 
                }
            }
        }
    }
    
    /**
     * Marca un registro como visto y leído por el familiar actual
     */
    fun marcarComoVisto(registroId: String) {
        viewModelScope.launch {
            try {
                // Obtener información del familiar actual
                val familiarActual = authRepository.getUsuarioActual()
                if (familiarActual == null) {
                    Timber.e("No hay usuario autenticado")
                    return@launch
                }
                
                val familiarId = familiarActual.dni
                val nombreFamiliar = "${familiarActual.nombre} ${familiarActual.apellidos}".trim()
                
                // Marcar como leído por el familiar específico
                val result = registroDiarioRepository.marcarRegistroComoLeidoPorFamiliar(
                    registroId = registroId,
                    familiarId = familiarId,
                    nombreFamiliar = nombreFamiliar
                )
                
                if (result is Result.Success) {
                    // Actualizar la lista de registros
                    _uiState.update { state ->
                        val registrosActualizados = state.registros.map { registro ->
                            if (registro.id == registroId) {
                                val lecturasPorFamiliar = registro.lecturasPorFamiliar.toMutableMap()
                                lecturasPorFamiliar[familiarId] = com.tfg.umeegunero.data.model.LecturaFamiliar(
                                    familiarId = familiarId,
                                    nombreFamiliar = nombreFamiliar,
                                    fechaLectura = com.google.firebase.Timestamp.now(),
                                    leido = true
                                )
                                
                                registro.copy(
                                    vistoPorFamiliar = true,
                                    lecturasPorFamiliar = lecturasPorFamiliar
                                )
                            } else {
                                registro
                            }
                        }
                        
                        // Actualizar también el registro seleccionado si es el mismo
                        val registroSeleccionadoActualizado = state.registroSeleccionado?.let {
                            if (it.id == registroId) {
                                val lecturasPorFamiliar = it.lecturasPorFamiliar.toMutableMap()
                                lecturasPorFamiliar[familiarId] = com.tfg.umeegunero.data.model.LecturaFamiliar(
                                    familiarId = familiarId,
                                    nombreFamiliar = nombreFamiliar,
                                    fechaLectura = com.google.firebase.Timestamp.now(),
                                    leido = true
                                )
                                
                                it.copy(
                                    vistoPorFamiliar = true,
                                    lecturasPorFamiliar = lecturasPorFamiliar
                                )
                            } else {
                                it
                            }
                        }
                        
                        state.copy(
                            registros = registrosActualizados,
                            registroSeleccionado = registroSeleccionadoActualizado
                        )
                    }
                    
                    Timber.d("Registro $registroId marcado como leído por $nombreFamiliar")
                } else if (result is Result.Error) {
                    Timber.e(result.exception, "Error al marcar registro como leído: $registroId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar registro como leído: $registroId")
            }
        }
    }
    
    /**
     * Obtiene los registros no visualizados
     */
    fun cargarRegistrosNoVisualizados(alumnosIds: List<String>) {
        if (alumnosIds.isEmpty()) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = registroDiarioRepository.obtenerRegistrosNoVisualizados(alumnosIds)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                registros = result.data.sortedByDescending { reg -> reg.fecha }
                            ) 
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registros no visualizados")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.exception?.message ?: "Error desconocido"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros no visualizados")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar registros"
                    ) 
                }
            }
        }
    }
} 