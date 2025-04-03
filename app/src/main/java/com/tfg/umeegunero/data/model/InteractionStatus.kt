package com.tfg.umeegunero.data.model

/**
 * Enum que representa los diferentes estados de interacción con un mensaje.
 */
enum class InteractionStatus {
    NONE,           // Sin interacción
    READING,        // Leyendo activamente el mensaje
    VIEWED,         // Solo visto
    INTERACTION,    // Interactuando con el mensaje (por ejemplo, con archivos adjuntos)
    DOWNLOADING     // Descargando un archivo
} 