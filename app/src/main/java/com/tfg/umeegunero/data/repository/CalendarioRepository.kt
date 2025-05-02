package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las operaciones relacionadas con el calendario
 * Maneja la persistencia y recuperación de eventos en Firestore
 */
@Singleton
class CalendarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val eventosCollection = firestore.collection("eventos")

    /**
     * Obtiene los eventos de un mes específico
     * @param year Año del mes
     * @param month Mes (1-12)
     * @return Lista de eventos del mes
     */
    suspend fun getEventosByMonth(year: Int, month: Int): List<Evento> {
        return try {
            val startOfMonth = LocalDateTime.of(year, month, 1, 0, 0)
            val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)

            val snapshot = eventosCollection
                .whereGreaterThanOrEqualTo("fecha", startOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .whereLessThanOrEqualTo("fecha", endOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Evento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Guarda un nuevo evento
     * @param evento Evento a guardar
     * @return Resultado de la operación
     */
    suspend fun saveEvento(evento: Evento): Result<Evento> {
        return try {
            val docRef = eventosCollection.document()
            val eventoWithId = evento.copy(id = docRef.id)
            docRef.set(eventoWithId).await()
            Result.Success(eventoWithId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Elimina un evento
     * @param eventoId ID del evento a eliminar
     * @return Resultado de la operación
     */
    suspend fun deleteEvento(eventoId: String): Result<Unit> {
        return try {
            eventosCollection.document(eventoId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Actualiza un evento existente
     * @param evento Evento a actualizar
     * @return Resultado de la operación
     */
    suspend fun updateEvento(evento: Evento): Result<Evento> {
        return try {
            eventosCollection.document(evento.id).set(evento).await()
            Result.Success(evento)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario autenticado
     */
    fun obtenerUsuarioId(): String {
        // Por simplicidad, se utiliza un valor predeterminado
        // En una implementación real, se obtendría del servicio de autenticación
        return "profesor_1"
    }

    /**
     * Obtiene el ID del centro al que pertenece el usuario actual
     * @return ID del centro educativo
     */
    fun obtenerCentroId(): String {
        // Por simplicidad, se utiliza un valor predeterminado
        // En una implementación real, se obtendría de las propiedades del usuario
        return "centro_1"
    }

    /**
     * Verifica si una fecha específica es festiva
     * @param fecha Fecha a comprobar
     * @return Resultado que indica si es festivo o no
     */
    suspend fun esDiaFestivo(fecha: java.time.LocalDate): Result<Boolean> {
        return try {
            val startOfDay = fecha.atStartOfDay()
            val endOfDay = fecha.atTime(23, 59, 59)
            
            val snapshot = eventosCollection
                .whereGreaterThanOrEqualTo("fecha", startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .whereLessThanOrEqualTo("fecha", endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .whereEqualTo("tipo", TipoEvento.FESTIVO.toString())
                .get()
                .await()
                
            Result.Success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 