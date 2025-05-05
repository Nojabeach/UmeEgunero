package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Representa una clase en el contexto educativo de la aplicación UmeEgunero.
 *
 * Esta clase contiene información detallada sobre una clase específica, 
 * incluyendo su identificador, nombre, centro, curso, capacidad máxima, 
 * horario y otros detalles relevantes.
 *
 * @property id Identificador único de la clase
 * @property nombre Nombre descriptivo de la clase
 * @property centroId Identificador del centro al que pertenece la clase
 * @property cursoId Identificador del curso asociado a la clase
 * @property aula Número o identificador del aula donde se imparte la clase
 * @property capacidadMaxima Número máximo de alumnos permitidos en la clase
 * @property horario Rango de tiempo en el que se imparte la clase
 * @property profesorTitularId Identificador del profesor titular de la clase
 * @property profesoresAuxiliaresIds Lista de identificadores de profesores auxiliares
 * @property alumnosIds Lista de identificadores de alumnos matriculados
 * @property activo Indica si la clase está activa o no
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
data class Clase(
    @DocumentId val id: String = "",
    val cursoId: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "A", "B", "Mañana", "Tarde"
    val profesorId: String? = null, // Cambiado a nullable
    val profesorTitularId: String? = null, // Cambiado a nullable
    val profesoresAuxiliaresIds: List<String>? = null, // Cambiado a nullable
    val alumnosIds: List<String>? = null, // Cambiado a nullable
    val capacidadMaxima: Int? = null, // Cambiado a nullable
    val activo: Boolean = true,
    val horario: String = "", // Descripción del horario o referencia a un objeto Horario
    val aula: String = "" // Ubicación física dentro del centro
) 