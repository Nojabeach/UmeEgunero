package com.tfg.umeegunero.data.model

/**
 * Modelo que representa una dirección postal completa en el sistema UmeEgunero.
 * 
 * Esta clase define una estructura de datos estandarizada para almacenar
 * direcciones físicas utilizadas por diferentes entidades del sistema, como
 * centros educativos, domicilios de usuarios, etc. Proporciona un formato
 * consistente para el almacenamiento y validación de datos de localización.
 * 
 * El modelo incluye todos los componentes necesarios para una dirección postal
 * completa en el contexto español, facilitando la integración con servicios
 * de geolocalización y envío de correspondencia.
 * 
 * Se utiliza como componente dentro de otras entidades más complejas y puede
 * combinarse con coordenadas geográficas para funcionalidades de mapas.
 * 
 * @property calle Nombre de la vía (calle, avenida, plaza, etc.)
 * @property numero Número del edificio o portal
 * @property piso Planta, piso y/o puerta (opcional)
 * @property codigoPostal Código postal (CP) de la dirección
 * @property ciudad Localidad o municipio
 * @property provincia Provincia o región administrativa
 * 
 * @see Centro Entidad que utiliza este modelo para su dirección física
 * @see Usuario Entidad que puede utilizar este modelo para domicilio
 */
data class Direccion(
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = ""
) 