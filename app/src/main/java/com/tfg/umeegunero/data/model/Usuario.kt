package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa las preferencias de notificación de un usuario
 */
data class PreferenciasNotificacion(
    val permisoPendiente: Boolean = false,
    val notificacionesHabilitadas: Boolean = true,
    val ultimaActualizacion: Long = 0
)

/**
 * Representa un usuario en la aplicación UmeEgunero.
 *
 * Esta clase define la estructura de datos para los diferentes tipos de usuarios
 * que pueden interactuar con la aplicación, como administradores, profesores, 
 * familiares y alumnos.
 *
 * Cada usuario tiene un conjunto de propiedades que definen su perfil, rol y 
 * permisos dentro del sistema educativo.
 *
 * @property id Identificador único del usuario
 * @property nombre Nombre del usuario
 * @property apellidos Apellidos del usuario
 * @property email Correo electrónico del usuario
 * @property tipoUsuario Tipo de usuario (administrador, profesor, familiar, alumno)
 * @property centroId Identificador del centro educativo al que pertenece el usuario
 * @property activo Indica si el usuario está activo en el sistema
 * @property fechaRegistro Fecha de registro del usuario en la aplicación
 * @property ultimoAcceso Fecha del último acceso del usuario
 * @property imagenPerfil URL de la imagen de perfil del usuario
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
data class Usuario(
    val dni: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val fechaRegistro: Timestamp = Timestamp.now(),
    val ultimoAcceso: Timestamp? = null,
    val activo: Boolean = true,
    val perfiles: List<Perfil> = emptyList(),
    val direccion: Direccion? = null,
    val preferencias: Preferencias = Preferencias(),
    var avatarUrl: String? = null,
    val firebaseUid: String = "",
    val preferenciasNotificacion: PreferenciasNotificacion? = null,
    val nombreAlumno: String? = null,
    val estado: String? = null,
    val metadata: Map<String, Any>? = null,
    val clasesIds: List<String> = emptyList()
) {
    @field:DocumentId
    var documentId: String = dni
} 