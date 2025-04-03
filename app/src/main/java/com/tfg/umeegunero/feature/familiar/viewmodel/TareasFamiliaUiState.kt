package com.tfg.umeegunero.feature.familiar.viewmodel

import com.tfg.umeegunero.data.model.Tarea

/**
 * Modelo de datos para la información básica de un alumno en la pantalla de tareas
 */
data class AlumnoInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val cursoNombre: String,
    val claseNombre: String
) {
    val nombreCompleto: String
        get() = "$nombre $apellidos"
        
    val ubicacion: String
        get() = if (cursoNombre.isNotEmpty() && claseNombre.isNotEmpty()) {
            "$cursoNombre - $claseNombre"
        } else if (cursoNombre.isNotEmpty()) {
            cursoNombre
        } else if (claseNombre.isNotEmpty()) {
            claseNombre
        } else {
            ""
        }
}

/**
 * Enum que define los diferentes filtros que se pueden aplicar a las tareas
 */
enum class FiltroTarea {
    TODAS,
    PENDIENTES,
    EN_PROGRESO,
    COMPLETADAS,
    RETRASADAS
}

/**
 * Estado de la UI para la pantalla de tareas de familia
 */
data class TareasFamiliaUiState(
    // Datos de usuario
    val familiarId: String = "",
    val alumnos: List<AlumnoInfo> = emptyList(),
    val alumnoSeleccionadoId: String = "",
    
    // Datos de tareas
    val tareas: List<Tarea> = emptyList(),
    val tareasFiltradas: List<Tarea> = emptyList(),
    val filtroSeleccionado: FiltroTarea = FiltroTarea.TODAS,
    
    // Tarea seleccionada
    val tareaSeleccionadaId: String = "",
    
    // Estados de UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) 