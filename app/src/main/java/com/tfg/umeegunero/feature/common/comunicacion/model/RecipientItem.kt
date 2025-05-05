package com.tfg.umeegunero.feature.common.comunicacion.model

/**
 * Modelo de datos para representar un destinatario en la interfaz de usuarios
 * 
 * @property id Identificador único del destinatario
 * @property name Nombre a mostrar del destinatario
 * @property email Correo electrónico del destinatario (opcional)
 * @property avatarUrl URL de la imagen de avatar (opcional)
 * @property type Tipo de destinatario (ej: "USUARIO", "GRUPO", etc.)
 * @property isSelected Indica si el destinatario está seleccionado actualmente
 */
data class RecipientItem(
    val id: String,
    val name: String,
    val email: String = "",
    val avatarUrl: String? = null,
    val type: String = "USUARIO",
    val isSelected: Boolean = false
) 