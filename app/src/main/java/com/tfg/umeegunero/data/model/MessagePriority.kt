package com.tfg.umeegunero.data.model

/**
 * Niveles de prioridad para los mensajes
 */
enum class MessagePriority(val value: Int) {
    LOW(0),      // Prioridad baja
    NORMAL(1),   // Prioridad normal
    HIGH(2),     // Prioridad alta
    URGENT(3);   // Prioridad urgente
    
    companion object {
        /**
         * Obtiene la prioridad a partir de un valor entero
         */
        fun fromInt(value: Int): MessagePriority {
            return values().find { it.value == value } ?: NORMAL
        }
    }
} 