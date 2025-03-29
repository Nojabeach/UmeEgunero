package com.tfg.umeegunero.data.model

/**
 * Modelo que representa las preferencias configurables de un usuario en el sistema UmeEgunero.
 * 
 * Esta clase centraliza todas las opciones personalizables que afectan a la experiencia
 * de usuario dentro de la aplicación. Las preferencias permiten adaptar el comportamiento
 * e interfaz a las necesidades específicas de cada usuario, mejorando así la usabilidad
 * y satisfacción.
 * 
 * El modelo incluye configuraciones relativas al idioma, las notificaciones y el tema
 * visual de la aplicación, con valores predeterminados que se aplican para nuevos usuarios.
 * 
 * @property idiomaApp Código ISO del idioma preferido por el usuario ('es' para español por defecto)
 * @property notificaciones Configuración detallada de las preferencias de notificaciones
 * @property tema Preferencia sobre el tema visual (claro, oscuro o según sistema)
 * 
 * @see Usuario Clase principal que contiene estas preferencias
 * @see Notificaciones Configuración detallada de las opciones de notificación
 * @see TemaPref Enumeración de opciones de tema visual
 */
data class Preferencias(
    val idiomaApp: String = "es",
    val notificaciones: Notificaciones = Notificaciones(),
    val tema: TemaPref = TemaPref.SYSTEM
)

/**
 * Modelo que representa las preferencias específicas de notificaciones de un usuario.
 * 
 * Esta clase permite configurar los canales de comunicación preferidos para recibir
 * alertas y notificaciones del sistema. El usuario puede elegir activar o desactivar
 * cada canal de forma independiente según sus necesidades.
 * 
 * Las notificaciones son un componente clave en la comunicación entre el centro educativo
 * y los familiares, permitiendo informar de manera oportuna sobre actividades, incidencias
 * o mensajes relevantes relacionados con los alumnos.
 * 
 * @property push Indica si las notificaciones push están habilitadas (notificaciones en dispositivo)
 * @property email Indica si las notificaciones por correo electrónico están habilitadas
 * 
 * @see Preferencias Clase que contiene estas configuraciones de notificaciones
 */
data class Notificaciones(
    val push: Boolean = true,
    val email: Boolean = true
) 