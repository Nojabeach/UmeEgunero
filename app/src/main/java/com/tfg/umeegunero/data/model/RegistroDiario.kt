package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo para el registro diario de actividades de un alumno
 */
data class RegistroDiario(
    @DocumentId val id: String = "",
    val alumnoId: String = "",
    val claseId: String = "",
    val profesorId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    
    // Comidas
    val primerPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val segundoPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val postre: EstadoComida = EstadoComida.NO_SERVIDO,
    val merienda: EstadoComida = EstadoComida.NO_SERVIDO,
    val observacionesComida: String = "",
    
    // Siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: Timestamp? = null,
    val horaFinSiesta: Timestamp? = null,
    val observacionesSiesta: String = "",
    
    // Deposiciones
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
    
    // Control de visualización
    val visualizadoPorFamiliar: Boolean = false,
    val fechaVisualizacion: Timestamp? = null,
    
    // Metadatos
    val ultimaModificacion: Timestamp = Timestamp.now(),
    val creadoPor: String = "",
    val modificadoPor: String = ""
)

/**
 * Estado posible para las comidas
 */
enum class EstadoComida {
    NO_SERVIDO,
    COMPLETO,
    PARCIAL,
    RECHAZADO,
    NO_APLICABLE
} 