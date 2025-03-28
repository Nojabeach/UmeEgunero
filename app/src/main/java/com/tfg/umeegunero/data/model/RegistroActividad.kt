package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un registro de actividad diaria de un alumno
 */
data class RegistroActividad(
    @DocumentId val id: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val profesorId: String? = null,
    val profesorNombre: String? = null,
    val comida: Comida? = null,
    val siesta: Siesta? = null,
    val cacaControl: CacaControl? = null,
    val actividades: Actividad? = null,
    val comidas: Comidas = Comidas(),
    val necesidadesFisiologicas: NecesidadesFisiologicas = NecesidadesFisiologicas(),
    val observaciones: String? = null,
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null
) 