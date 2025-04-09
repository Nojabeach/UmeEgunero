package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Modelo que representa una etiqueta personalizada para categorizar actividades
 * 
 * Las etiquetas permiten a los profesores organizar y filtrar las actividades
 * según criterios personalizados, facilitando la búsqueda y organización.
 * 
 * @property id Identificador único de la etiqueta
 * @property nombre Nombre descriptivo y corto de la etiqueta
 * @property color Color de la etiqueta en formato hexadecimal
 * @property profesorId ID del profesor que creó la etiqueta (null si es global)
 * @property centroId ID del centro educativo (null si es una etiqueta del sistema)
 * @property descripcion Descripción opcional más detallada de la etiqueta
 * @property categoria Categoría a la que pertenece la etiqueta
 * @property contador Número de veces que se ha usado la etiqueta
 * @property fechaCreacion Fecha de creación de la etiqueta
 * @property activa Indica si la etiqueta está activa o ha sido archivada
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@IgnoreExtraProperties
data class EtiquetaActividad(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val color: String = "#03A9F4", // Color por defecto (azul)
    val profesorId: String? = null,
    val centroId: String? = null,
    val descripcion: String = "",
    val categoria: CategoriaEtiqueta = CategoriaEtiqueta.GENERAL,
    val contador: Int = 0,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val activa: Boolean = true
)

/**
 * Categorías predefinidas para las etiquetas
 */
enum class CategoriaEtiqueta {
    GENERAL,
    ACADEMICO,
    COMPORTAMIENTO,
    DESARROLLO,
    SOCIAL,
    SALUD,
    LOGRO,
    OTRO
} 