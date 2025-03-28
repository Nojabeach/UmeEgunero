package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo para representar un mensaje entre usuarios
 */
data class Mensaje(
    @DocumentId val id: String = "",
    val emisorId: String = "",
    val receptorId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val texto: String = "",
    val leido: Boolean = false,
    val fechaLeido: Timestamp? = null,
    val alumnoId: String = "",
    val adjuntos: List<String> = emptyList()
) 