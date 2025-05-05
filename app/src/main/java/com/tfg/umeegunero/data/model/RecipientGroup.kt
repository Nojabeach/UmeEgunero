package com.tfg.umeegunero.data.model

/**
 * Estructura para representar un grupo de destinatarios
 */
data class RecipientGroup(
    val id: String,
    val name: String,
    val type: RecipientGroupType
) 