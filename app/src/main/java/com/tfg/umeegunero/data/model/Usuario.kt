package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo principal que representa a un usuario en el sistema UmeEgunero.
 * 
 * Esta clase contiene toda la información personal y de gestión de los usuarios,
 * independientemente de su rol en el sistema (administrador, profesor, familiar).
 * La información específica según el rol se gestiona mediante la colección de perfiles.
 *
 * Los usuarios se almacenan en Firestore en la colección 'usuarios' donde el ID del
 * documento corresponde al DNI del usuario.
 *
 * @property dni Documento Nacional de Identidad del usuario (formato español). Sirve como
 *               identificador único en el sistema y clave primaria en la base de datos.
 * @property email Correo electrónico del usuario, utilizado para autenticación y comunicaciones.
 * @property nombre Nombre del usuario.
 * @property apellidos Apellidos del usuario.
 * @property telefono Número de teléfono de contacto.
 * @property fechaRegistro Fecha y hora en que el usuario se registró en el sistema.
 * @property ultimoAcceso Fecha y hora del último inicio de sesión del usuario. Puede ser null
 *                       si el usuario nunca ha iniciado sesión.
 * @property activo Estado de la cuenta (true = activa, false = desactivada).
 * @property perfiles Lista de perfiles asignados al usuario, que determinan sus roles y permisos.
 * @property direccion Información de domicilio del usuario (opcional).
 * @property preferencias Configuraciones personalizadas del usuario (tema, notificaciones, etc).
 * @property documentId Campo utilizado por Firestore para mapear el ID del documento.
 *                      Por defecto se establece igual al DNI.
 *
 * @see Perfil
 * @see Direccion
 * @see Preferencias
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
    val id: String = "",
    val avatar: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val centroId: String = "",
    val rol: Rol = Rol.USUARIO
) {
    @field:DocumentId
    var documentId: String = dni
}

/**
 * Roles de usuarios en la aplicación
 */
enum class Rol {
    ADMIN,
    PROFESOR,
    ALUMNO,
    PADRE,
    USUARIO
} 