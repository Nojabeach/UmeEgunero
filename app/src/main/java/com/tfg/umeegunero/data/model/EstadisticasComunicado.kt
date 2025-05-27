package com.tfg.umeegunero.data.model

/**
 * Modelo para almacenar estadísticas de comunicados
 */
data class EstadisticasComunicado(
    val totalDestinatarios: Int = 0,
    val totalLeidos: Int = 0,
    val totalConfirmados: Int = 0,
    val porcentajeLeido: Float = 0f,
    val porcentajeConfirmado: Float = 0f,
    val usuariosLeidos: List<String> = emptyList(),
    val usuariosConfirmados: List<String> = emptyList(),
    val usuariosPendientes: List<String> = emptyList()
) 