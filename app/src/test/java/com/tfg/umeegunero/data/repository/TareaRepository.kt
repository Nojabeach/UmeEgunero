package com.tfg.umeegunero.data.repository

import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.util.Result

/**
 * Interfaz del repositorio de tareas para tests
 */
interface TareaRepository {
    suspend fun obtenerTarea(tareaId: String): Result<Tarea>
    suspend fun guardarEntrega(entrega: EntregaTarea): Result<String>
    suspend fun actualizarEstadoTarea(tareaId: String, estado: String): Result<Boolean>
} 