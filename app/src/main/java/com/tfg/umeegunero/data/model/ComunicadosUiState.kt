package com.tfg.umeegunero.data.model

import com.tfg.umeegunero.data.model.Comunicado

/**
 * Estado de la UI para la pantalla de gestión de comunicados.
 *
 * Esta clase encapsula todos los datos necesarios para la representación del estado
 * de la interfaz de usuario en la pantalla de gestión de comunicados. Contiene tanto
 * la lista de comunicados existentes como los estados del formulario para crear nuevos
 * comunicados y los estados de envío.
 *
 * @property comunicados Lista de comunicados a mostrar
 * @property isLoading Indica si se están cargando los datos desde el repositorio
 * @property error Mensaje de error en caso de que ocurra algún problema, o null si no hay errores
 * @property success Mensaje de éxito tras realizar una operación correctamente
 * @property mostrarFormulario Indica si se debe mostrar el formulario para crear un nuevo comunicado (alias de showNuevoComunicado)
 * @property showNuevoComunicado Igual que mostrarFormulario, mantenido por compatibilidad
 * @property titulo Título del nuevo comunicado que se está redactando
 * @property mensaje Contenido del mensaje del nuevo comunicado
 * @property tituloError Mensaje de error de validación para el título, o cadena vacía si es válido
 * @property mensajeError Mensaje de error de validación para el mensaje, o cadena vacía si es válido
 * @property enviarATodos Indica si se debe enviar el comunicado a todos los tipos de usuarios
 * @property enviarACentros Indica si se debe enviar el comunicado a los centros educativos
 * @property enviarAProfesores Indica si se debe enviar el comunicado a los profesores
 * @property enviarAFamiliares Indica si se debe enviar el comunicado a los familiares
 * @property isEnviando Indica si se está procesando el envío de un comunicado
 * @property enviado Indica si el comunicado ha sido enviado correctamente
 * @property fecha Fecha del comunicado en formato texto
 * @property remitente Nombre o cargo del remitente del comunicado
 * @property requiereConfirmacion Indica si el comunicado requiere confirmación de lectura
 * @property firmaDigital Firma digital en formato Base64 o null si no hay firma
 * @property estadisticasLectura Estadísticas de lectura del comunicado
 * @property mostrarEstadisticas Indica si se debe mostrar el panel de estadísticas
 * @property showEstadisticas Alias de mostrarEstadisticas, mantenido por compatibilidad
 * @property comunicadoSeleccionado Comunicado seleccionado actualmente para visualizar detalles
 * @property mostrarFirmaDigital Indica si se debe mostrar el panel de firma digital
 * @property showFirmaDigital Alias de mostrarFirmaDigital, mantenido por compatibilidad
 *
 * @see Comunicado
 * @see com.tfg.umeegunero.feature.admin.viewmodel.ComunicadosViewModel
 * @see com.tfg.umeegunero.feature.admin.screen.ComunicadosScreen
 */
data class ComunicadosUiState(
    // Estado de datos
    val comunicados: List<Comunicado> = emptyList(),
    val comunicadoSeleccionado: Comunicado? = null,
    
    // Estados de carga y mensajes
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val isEnviando: Boolean = false,
    val enviado: Boolean = false,
    
    // Estado del formulario
    val mostrarFormulario: Boolean = false,
    val showNuevoComunicado: Boolean = false, // Alias de mostrarFormulario
    val titulo: String = "",
    val mensaje: String = "",
    val tituloError: String = "",
    val mensajeError: String = "",
    
    // Destinatarios
    val enviarATodos: Boolean = false,
    val enviarACentros: Boolean = false,
    val enviarAProfesores: Boolean = false,
    val enviarAFamiliares: Boolean = false,
    
    // Metadatos del comunicado
    val fecha: String = "",
    val remitente: String = "Administrador del Sistema",
    
    // Confirmación y firma
    val requiereConfirmacion: Boolean = false,
    val firmaDigital: String? = null,
    
    // Estadísticas
    val estadisticasLectura: Map<String, Any> = emptyMap(),
    val mostrarEstadisticas: Boolean = false,
    val showEstadisticas: Boolean = false, // Alias de mostrarEstadisticas
    
    // Firma digital
    val mostrarFirmaDigital: Boolean = false,
    val showFirmaDigital: Boolean = false // Alias de mostrarFirmaDigital
) 