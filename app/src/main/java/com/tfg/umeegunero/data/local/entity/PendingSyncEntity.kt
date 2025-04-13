package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * Entidad que representa una operación pendiente de sincronización
 */
@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey
    val id: String,
    val operationType: String, // CREATE, UPDATE, DELETE
    val entityType: String, // COMUNICADO, EVENTO, etc.
    val entityId: String,
    val data: String, // JSON string con los datos a sincronizar
    val status: String = "PENDING", // PENDING, COMPLETED, ERROR
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Timestamp = Timestamp.now()
) {
    /**
     * Enumeración que define los tipos de operaciones para sincronización
     */
    enum class OperationType {
        CREATE,
        UPDATE,
        DELETE
    }
    
    /**
     * Enumeración que define los estados de sincronización
     */
    enum class Status {
        PENDING,
        COMPLETED,
        ERROR
    }
} 