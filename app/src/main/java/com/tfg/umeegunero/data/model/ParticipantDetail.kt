package com.tfg.umeegunero.data.model

/**
 * Modelo que representa un participante en una conversación
 */
data class ParticipantDetail(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val userType: String = ""
) 