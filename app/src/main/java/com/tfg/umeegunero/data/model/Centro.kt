package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Clase de datos que representa un centro educativo en la aplicación.
 * 
 * @property id Identificador único del centro
 * @property nombre Nombre del centro educativo
 * @property direccion Dirección física del centro (objeto completo)
 * @property contacto Información de contacto (teléfono, email)
 * @property latitud Coordenada de latitud para la ubicación geográfica
 * @property longitud Coordenada de longitud para la ubicación geográfica
 * @property activo Indica si el centro está activo en la plataforma
 * @property logo URL del logo del centro (opcional)
 * @property numProfesores Número de profesores asociados al centro
 * @property numClases Número de clases asociadas al centro
 * @property adminIds Lista de IDs de administradores del centro
 * @property profesorIds Lista de IDs de profesores asignados al centro
 */
data class Centro(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val activo: Boolean = true,
    val logo: String? = null,
    val numProfesores: Int = 0,
    val numClases: Int = 0,
    val contacto: String = "",
    val adminIds: List<String> = emptyList(),
    val profesorIds: List<String> = emptyList(),
    val direccionObj: Direccion = Direccion(),
    val contactoObj: Contacto = Contacto()
) {
    fun getDireccionCalle(): String = direccionObj.calle.ifEmpty { direccion }
    fun getDireccionNumero(): String = direccionObj.numero
    fun getDireccionCodigoPostal(): String = direccionObj.codigoPostal
    fun getDireccionCiudad(): String = direccionObj.ciudad
    fun getDireccionProvincia(): String = direccionObj.provincia
    
    fun obtenerTelefono(): String = contactoObj.telefono.ifEmpty { telefono }
    fun obtenerEmail(): String = contactoObj.email.ifEmpty { email }
} 