package com.tfg.umeegunero.data.model

import java.util.UUID

/**
 * Clase que representa una acción disponible para un mensaje
 */
data class MessageAction(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val actionType: String = "",
    val data: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val confirmationMessage: String = ""
) {
    /**
     * Convierte la acción a un mapa para Firestore
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "label" to label,
        "actionType" to actionType,
        "data" to data,
        "requiresConfirmation" to requiresConfirmation,
        "confirmationMessage" to confirmationMessage
    )
    
    companion object {
        /**
         * Crea una acción a partir de un mapa de datos
         */
        fun fromMap(data: Map<String, Any?>): MessageAction? {
            return try {
                MessageAction(
                    id = data["id"] as? String ?: UUID.randomUUID().toString(),
                    label = data["label"] as? String ?: "",
                    actionType = data["actionType"] as? String ?: "",
                    data = getActionData(data["data"]),
                    requiresConfirmation = data["requiresConfirmation"] as? Boolean ?: false,
                    confirmationMessage = data["confirmationMessage"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Obtiene los datos de acción de forma segura, evitando el unchecked cast
         */
        private fun getActionData(rawData: Any?): Map<String, String> {
            return try {
                @Suppress("UNCHECKED_CAST")
                rawData as? Map<String, String> ?: emptyMap()
            } catch (e: Exception) {
                // Si la conversión falla, devolvemos un mapa vacío
                emptyMap()
            }
        }
    }
} 