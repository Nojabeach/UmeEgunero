package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Modelo que representa la lectura de un registro por parte de un familiar.
 * 
 * Esta clase almacena información sobre cuándo un familiar visualizó un registro
 * de actividad específico, permitiendo el seguimiento de las lecturas.
 * 
 * @property id Identificador único de la lectura (generado automáticamente)
 * @property familiarId ID del familiar que realizó la lectura
 * @property registroId ID del registro que fue leído
 * @property alumnoId ID del alumno al que pertenece el registro
 * @property fechaLectura Fecha y hora en que se realizó la lectura
 * @property dispositivo Información sobre el dispositivo desde el que se accedió (opcional)
 */
@IgnoreExtraProperties
data class LecturaFamiliar(
    @DocumentId
    val id: String = "",
    
    @PropertyName("familiarId")
    val familiarId: String = "",
    
    @PropertyName("registroId")
    val registroId: String = "",
    
    @PropertyName("alumnoId")
    val alumnoId: String = "",
    
    @PropertyName("fechaLectura")
    val fechaLectura: Timestamp = Timestamp.now(),
    
    @PropertyName("dispositivo")
    val dispositivo: String? = null
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", "", "", Timestamp.now())
    
    /**
     * Convierte la fecha de lectura a un objeto Date de Java.
     */
    fun getFechaLecturaAsDate(): Date {
        return fechaLectura.toDate()
    }
    
    /**
     * Crea un ID compuesto para la lectura basado en el ID del registro y del familiar.
     */
    fun generateLecturaId(): String {
        return "${registroId}_${familiarId}_${fechaLectura.seconds}"
    }
    
    override fun toString(): String {
        return "LecturaFamiliar(id='$id', familiarId='$familiarId', registroId='$registroId', " +
                "alumnoId='$alumnoId', fechaLectura=$fechaLectura)"
    }
} 