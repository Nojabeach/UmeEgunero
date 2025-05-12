package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Modelo de datos para Registro de Actividad
 * 
 * Representa un registro diario de actividades y estado general
 * de un alumno, que incluye comidas, siesta, y otros aspectos.
 */
data class RegistroActividad(
    @DocumentId
    val id: String = "",
    
    // Datos de identificación
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val claseId: String = "",
    
    // Fecha del registro
    val fecha: Timestamp = Timestamp.now(),
    
    // Datos del profesor
    val profesorId: String = "",
    val profesorNombre: String? = null,
    
    // Datos de comidas - Estructura unificada
    val comidas: Comidas = Comidas(),
    val observacionesComida: String = "",
    
    // Siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: String = "",  // Formato "HH:mm"
    val horaFinSiesta: String = "",     // Formato "HH:mm"
    val observacionesSiesta: String = "",
    
    // Datos de deposiciones
    val haHechoCaca: Boolean = false,
    val numeroCacas: Int = 0,
    val observacionesCaca: String = "",
    
    // Material a reponer
    val necesitaPanales: Boolean = false,
    val necesitaToallitas: Boolean = false,
    val necesitaRopaCambio: Boolean = false,
    val otroMaterialNecesario: String = "",
    
    // Observaciones generales
    val observacionesGenerales: String = "",
    
    // Etiquetas
    val etiquetas: List<String> = emptyList(),
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null,
    val vistoPor: Map<String, Boolean> = emptyMap(),
    
    // Metadatos
    val ultimaModificacion: Timestamp = Timestamp.now(),
    val creadoPor: String = "",
    val modificadoPor: String = "",
    
    // Lista de actividades realizadas durante el día
    val actividades: List<String> = emptyList(),
    
    // ID de plantilla usada (si corresponde)
    val plantillaId: String? = null,
    
    // Bandera para marcar registros eliminados (borrado lógico)
    val eliminado: Boolean = false
) {
    /**
     * Determina si el registro pertenece a una fecha específica.
     * Útil para filtrar registros por día.
     * 
     * @param target Fecha a comparar (sin hora)
     * @return true si el registro pertenece a la fecha especificada
     */
    fun esDeEsteDia(target: Date): Boolean {
        val fechaRegistro = fecha.toDate()
        val calRegistro = java.util.Calendar.getInstance()
        calRegistro.time = fechaRegistro
        
        val calTarget = java.util.Calendar.getInstance()
        calTarget.time = target
        
        return calRegistro.get(java.util.Calendar.YEAR) == calTarget.get(java.util.Calendar.YEAR) &&
               calRegistro.get(java.util.Calendar.MONTH) == calTarget.get(java.util.Calendar.MONTH) &&
               calRegistro.get(java.util.Calendar.DAY_OF_MONTH) == calTarget.get(java.util.Calendar.DAY_OF_MONTH)
    }
} 