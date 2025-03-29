package com.tfg.umeegunero.data.model

/**
 * Modelo que representa la información de contacto en el sistema UmeEgunero.
 * 
 * Esta clase define una estructura de datos estandarizada para almacenar
 * información de contacto utilizada por diferentes entidades del sistema,
 * como centros educativos, usuarios, etc. Permite encapsular los datos
 * de comunicación básicos de forma consistente.
 * 
 * El modelo se utiliza como componente dentro de otras entidades más complejas
 * y facilita la organización y validación de los datos de contacto.
 * 
 * @property telefono Número de teléfono de contacto principal
 * @property email Dirección de correo electrónico para comunicaciones electrónicas
 * 
 * @see Centro Entidad que utiliza este modelo para su información de contacto
 * @see Usuario Entidad que puede utilizar este modelo para datos de contacto
 */
data class Contacto(
    val telefono: String = "",
    val email: String = ""
) 