package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.ResultadoUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

// Importar las clases desde el archivo TareasFamiliaUiState.kt
import com.tfg.umeegunero.feature.familiar.viewmodel.AlumnoInfo
import com.tfg.umeegunero.feature.familiar.viewmodel.FiltroTarea
import com.tfg.umeegunero.feature.familiar.viewmodel.TareasFamiliaUiState

// Las definiciones de FiltroTarea, AlumnoInfo y TareasFamiliaUiState se han movido al archivo TareasFamiliaUiState.kt

/**
 * ViewModel para la gestión de tareas desde la perspectiva del familiar
 */
@HiltViewModel
class TareasFamiliaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TareasFamiliaUiState())
    val uiState: StateFlow<TareasFamiliaUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos del usuario familiar
     */
    fun inicializar(familiarId: String) {
        _uiState.update { it.copy(isLoading = true, familiarId = familiarId) }
        
        viewModelScope.launch {
            try {
                // Cargar información de los hijos del familiar
                val result = usuarioRepository.obtenerUsuarioPorId(familiarId)
                if (result is Result.Success) {
                    val usuario = result.data
                    
                    // Buscar perfiles de tipo familiar
                    val usuarioId = usuario.id ?: usuario.dni
                    
                    // Cargar perfiles del usuario para obtener información de familiar
                    val perfilesResult = usuarioRepository.obtenerPerfilesUsuario(usuarioId)
                    if (perfilesResult is Result.Success) {
                        // Filtrar perfiles de tipo FAMILIAR
                        val perfilesFamiliar = perfilesResult.data.filter { perfil ->
                            perfil.tipo == TipoUsuario.FAMILIAR
                        }
                        
                        // Obtener IDs de alumnos asociados a este familiar
                        val alumnosIds = perfilesFamiliar.flatMap { it.alumnos }.distinct()
                        
                        if (alumnosIds.isNotEmpty()) {
                            val alumnosInfo = mutableListOf<AlumnoInfo>()
                            
                            // Para cada alumno, obtener su información básica
                            for (alumnoId in alumnosIds) {
                                val alumnoResultado = alumnoRepository.obtenerAlumnoPorId(alumnoId)
                                // Convertimos Resultado a Result para mantener consistencia
                                val alumnoResult = ResultadoUtil.convertirResultadoAResult(alumnoResultado)
                                
                                if (alumnoResult is Result.Success && alumnoResult.data != null) {
                                    val alumno = alumnoResult.data
                                    alumnosInfo.add(
                                        AlumnoInfo(
                                            id = alumno.id,
                                            nombre = alumno.nombre,
                                            apellidos = alumno.apellidos ?: "",
                                            cursoNombre = alumno.curso ?: "",
                                            claseNombre = alumno.clase ?: ""
                                        )
                                    )
                                }
                            }
                            
                            _uiState.update { 
                                it.copy(
                                    alumnos = alumnosInfo,
                                    alumnoSeleccionadoId = if (alumnosInfo.isNotEmpty()) alumnosInfo.first().id else ""
                                ) 
                            }
                            
                            // Cargar tareas del primer alumno seleccionado
                            if (alumnosInfo.isNotEmpty()) {
                                cargarTareas(alumnosInfo.first().id)
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    error = "No hay alumnos asociados a esta cuenta familiar", 
                                    isLoading = false
                                ) 
                            }
                        }
                    } else if (perfilesResult is Result.Error) {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar perfiles del usuario: ${perfilesResult.exception?.message}", 
                                isLoading = false
                            ) 
                        }
                    }
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar información del usuario: ${result.exception?.message}", 
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar TareasFamiliaViewModel")
                _uiState.update { it.copy(error = "Error al cargar datos: ${e.message}", isLoading = false) }
            }
        }
    }

    /**
     * Carga las tareas asociadas a un alumno específico
     */
    fun cargarTareas(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alumnoSeleccionadoId = alumnoId) }
            
            try {
                // Obtener tareas del alumno desde el repositorio
                val result = tareaRepository.obtenerTareasPorAlumno(alumnoId)
                
                if (result is Result.Success) {
                    val tareas = result.data
                    _uiState.update { 
                        it.copy(
                            tareas = tareas,
                            tareasFiltradas = filtrarTareas(tareas, it.filtroSeleccionado),
                            isLoading = false
                        ) 
                    }
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar tareas: ${result.exception?.message}", 
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar tareas del alumno")
                _uiState.update { it.copy(error = "Error al cargar tareas: ${e.message}", isLoading = false) }
            }
        }
    }

    /**
     * Cambia el filtro de tareas aplicado
     */
    fun cambiarFiltro(filtro: FiltroTarea) {
        _uiState.update { 
            val tareasFiltradas = filtrarTareas(it.tareas, filtro)
            it.copy(
                filtroSeleccionado = filtro,
                tareasFiltradas = tareasFiltradas
            )
        }
    }

    /**
     * Marca una tarea como revisada por el familiar
     */
    fun marcarTareaComoRevisada(tareaId: String, comentario: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = tareaRepository.marcarTareaComoRevisadaPorFamiliar(
                    tareaId = tareaId,
                    familiarId = _uiState.value.familiarId,
                    comentario = comentario
                )
                
                if (result is Result.Success) {
                    // Actualizar la tarea en la lista local
                    val tareas = _uiState.value.tareas.map { tarea ->
                        if (tarea.id == tareaId) {
                            tarea.copy(
                                revisadaPorFamiliar = true,
                                fechaRevision = Timestamp.now(),
                                comentariosFamiliar = comentario
                            )
                        } else {
                            tarea
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            tareas = tareas,
                            tareasFiltradas = filtrarTareas(tareas, it.filtroSeleccionado),
                            mensaje = "Tarea marcada como revisada",
                            isLoading = false
                        )
                    }
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            error = "Error al marcar la tarea como revisada: ${result.exception?.message}",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar tarea como revisada")
                _uiState.update { it.copy(error = "Error: ${e.message}", isLoading = false) }
            }
        }
    }

    /**
     * Filtra las tareas según el criterio seleccionado
     */
    private fun filtrarTareas(tareas: List<Tarea>, filtro: FiltroTarea): List<Tarea> {
        val ahora = Date()
        
        return tareas.filter { tarea ->
            when (filtro) {
                FiltroTarea.TODAS -> true
                FiltroTarea.PENDIENTES -> tarea.estado == EstadoTarea.PENDIENTE
                FiltroTarea.EN_PROGRESO -> tarea.estado == EstadoTarea.EN_PROGRESO
                FiltroTarea.COMPLETADAS -> tarea.estado == EstadoTarea.COMPLETADA
                FiltroTarea.RETRASADAS -> {
                    val fechaEntrega = tarea.fechaEntrega?.toDate()
                    fechaEntrega != null && fechaEntrega.before(ahora) && 
                    tarea.estado != EstadoTarea.COMPLETADA && tarea.estado != EstadoTarea.CANCELADA
                }
            }
        }
    }

    /**
     * Limpia los mensajes de error o éxito
     */
    fun limpiarMensajes() {
        _uiState.update { 
            it.copy(
                error = null,
                mensaje = null
            ) 
        }
    }
} 