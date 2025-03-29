package com.tfg.umeegunero.data.model

/**
 * Estado de la UI para la pantalla de comunicados
 */
data class ComunicadosUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val comunicados: List<Comunicado> = emptyList(),
    val mostrarFormulario: Boolean = false,
    val titulo: String = "",
    val mensaje: String = "",
    val tituloError: String = "",
    val mensajeError: String = "",
    val enviarATodos: Boolean = false,
    val enviarACentros: Boolean = false,
    val enviarAProfesores: Boolean = false,
    val enviarAFamiliares: Boolean = false,
    val isEnviando: Boolean = false,
    val enviado: Boolean = false
) 