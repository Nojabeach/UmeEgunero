package com.tfg.umeegunero.feature.admin.model

import com.tfg.umeegunero.data.model.Comunicado

/**
 * Estado de la UI para la pantalla de comunicados
 *
 * @property comunicados Lista de comunicados a mostrar
 * @property isLoading Estado de carga
 * @property error Mensaje de error si existe
 * @property success Mensaje de éxito si existe
 * @property mostrarFormulario Indica si se debe mostrar el formulario para crear nuevo comunicado
 * @property titulo Título del nuevo comunicado
 * @property mensaje Mensaje del nuevo comunicado
 * @property tituloError Error en el título si existe
 * @property mensajeError Error en el mensaje si existe
 * @property enviarATodos Indica si se debe enviar a todos los usuarios
 * @property enviarACentros Indica si se debe enviar a los centros
 * @property enviarAProfesores Indica si se debe enviar a los profesores
 * @property enviarAFamiliares Indica si se debe enviar a los familiares
 * @property isEnviando Indica si se está enviando un comunicado
 * @property enviado Indica si el comunicado fue enviado correctamente
 * @property fecha Fecha del comunicado
 * @property remitente Remitente del comunicado
 */
data class ComunicadosUiState(
    val comunicados: List<Comunicado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
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
    val enviado: Boolean = false,
    val fecha: String = "",
    val remitente: String = "Administrador del Sistema"
) 