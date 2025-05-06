package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Mensaje para tests
 */
data class Mensaje(
    val id: String = "",
    val remitente: String = "",
    val destinatario: String = "",
    val texto: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val leido: Boolean = false,
    val adjuntos: List<String> = emptyList(),
    val tipo: String = "NORMAL"
) 