package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa la relación entre un familiar y un alumno en el sistema UmeEgunero.
 * 
 * Esta clase se utiliza para gestionar y almacenar las vinculaciones entre alumnos y sus
 * familiares, especificando el tipo de relación que existe entre ellos (padre, madre, tutor, etc.)
 * y manteniendo información sobre cuándo se estableció dicha relación.
 *
 * @property id Identificador único de la relación, normalmente compuesto por el ID del familiar
 *           y el ID del alumno, separados por un guion bajo.
 * @property familiarId Identificador único del familiar (DNI).
 * @property alumnoId Identificador único del alumno (DNI).
 * @property parentesco Tipo de relación entre el familiar y el alumno, representado como
 *           un valor del enum [SubtipoFamiliar].
 * @property fechaCreacion Timestamp que indica cuándo se estableció la relación.
 * @property activo Indica si la relación está actualmente activa o ha sido desactivada.
 * @property centroId ID del centro educativo donde se establece la relación.
 *
 * @see Alumno
 * @see Usuario
 * @see SubtipoFamiliar
 */
data class RelacionFamiliar(
    @DocumentId val id: String = "",
    val familiarId: String = "",
    val alumnoId: String = "",
    val parentesco: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val activo: Boolean = true,
    val centroId: String = ""
) 