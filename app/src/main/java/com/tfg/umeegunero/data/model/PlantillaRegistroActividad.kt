package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Modelo que representa una plantilla predefinida para registros de actividad
 * 
 * Las plantillas permiten a los profesores reutilizar configuraciones comunes
 * para registros de actividades diarias, facilitando y agilizando el proceso
 * de documentación de las actividades de los alumnos.
 * 
 * @property id Identificador único de la plantilla
 * @property nombre Nombre descriptivo de la plantilla
 * @property descripcion Descripción detallada del propósito de la plantilla
 * @property profesorId ID del profesor que creó la plantilla (null si es global)
 * @property centroId ID del centro educativo (null si es una plantilla del sistema)
 * @property comidas Configuración predefinida de comidas
 * @property siesta Configuración predefinida de siesta
 * @property necesidadesFisiologicas Configuración predefinida para control de necesidades
 * @property observaciones Lista de observaciones predefinidas
 * @property tipoActividad Tipo de actividad al que aplica esta plantilla
 * @property etiquetas Lista de etiquetas para facilitar la búsqueda
 * @property esPredeterminada Indica si es la plantilla por defecto
 * @property ultimaModificacion Fecha de última modificación
 * @property creadaPor ID del usuario que creó la plantilla
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@IgnoreExtraProperties
data class PlantillaRegistroActividad(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val profesorId: String? = null,
    val centroId: String? = null,
    val comidas: Comidas = Comidas(),
    val siesta: Siesta? = null,
    val necesidadesFisiologicas: CacaControl = CacaControl(),
    val observaciones: List<Observacion> = emptyList(),
    val tipoActividad: TipoActividad = TipoActividad.GENERAL,
    val etiquetas: List<String> = emptyList(),
    val esPredeterminada: Boolean = false,
    val ultimaModificacion: Timestamp = Timestamp.now(),
    val creadaPor: String = ""
)

/**
 * Tipos de actividades que pueden tener plantillas predefinidas
 */
enum class TipoActividad {
    GENERAL,
    DESAYUNO,
    COMIDA,
    MERIENDA,
    SIESTA,
    JUEGO,
    APRENDIZAJE,
    HIGIENE,
    OTRO
} 