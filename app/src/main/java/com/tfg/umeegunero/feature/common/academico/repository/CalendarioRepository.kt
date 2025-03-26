package com.tfg.umeegunero.feature.common.academico.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val eventosCollection = firestore.collection("eventos")

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

    suspend fun saveEvento(evento: Evento): Result<Evento> {
        return try {
            val docRef = eventosCollection.document()
            val eventoWithId = evento.copy(id = docRef.id)
            docRef.set(eventoWithId).await()
            Result.success(eventoWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvento(eventoId: String): Result<Unit> {
        return try {
            eventosCollection.document(eventoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvento(evento: Evento): Result<Evento> {
        return try {
            eventosCollection.document(evento.id).set(evento).await()
            Result.success(evento)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 