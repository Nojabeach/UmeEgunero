package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los eventos del calendario en Firestore
 */
@Singleton
class EventoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_EVENTOS = "eventos"
    }

    /**
     * Obtiene todos los eventos de un centro educativo
     * @param centroId ID del centro educativo
     * @return Lista de eventos del centro
     */
    suspend fun obtenerEventosPorCentro(centroId: String): List<Evento> {
        return try {
            firestore.collection(COLLECTION_EVENTOS)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val titulo = doc.getString("titulo") ?: ""
                        val descripcion = doc.getString("descripcion") ?: ""
                        val fechaTimestamp = doc.getTimestamp("fecha")
                        val tipoString = doc.getString("tipo") ?: TipoEvento.OTRO.name
                        val creadorId = doc.getString("creadorId") ?: ""
                        val centroId = doc.getString("centroId") ?: ""

                        val fecha = timestampToLocalDateTime(fechaTimestamp)
                        val tipo = TipoEvento.valueOf(tipoString)

                        Evento(id, titulo, descripcion, fecha, tipo, creadorId, centroId)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a Evento")
                        null
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos por centro")
            emptyList()
        }
    }

    /**
     * Obtiene los eventos creados por un profesor
     * @param profesorId ID del profesor
     * @return Lista de eventos creados por el profesor
     */
    suspend fun obtenerEventosPorProfesor(profesorId: String): List<Evento> {
        return try {
            firestore.collection(COLLECTION_EVENTOS)
                .whereEqualTo("creadorId", profesorId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val titulo = doc.getString("titulo") ?: ""
                        val descripcion = doc.getString("descripcion") ?: ""
                        val fechaTimestamp = doc.getTimestamp("fecha")
                        val tipoString = doc.getString("tipo") ?: TipoEvento.OTRO.name
                        val creadorId = doc.getString("creadorId") ?: ""
                        val centroId = doc.getString("centroId") ?: ""

                        val fecha = timestampToLocalDateTime(fechaTimestamp)
                        val tipo = TipoEvento.valueOf(tipoString)

                        Evento(id, titulo, descripcion, fecha, tipo, creadorId, centroId)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a Evento")
                        null
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos por profesor")
            emptyList()
        }
    }

    /**
     * Crea un nuevo evento en Firestore
     * @param evento Objeto Evento a crear
     * @return ID del evento creado
     */
    suspend fun crearEvento(evento: Evento): String {
        return try {
            val fechaTimestamp = localDateTimeToTimestamp(evento.fecha)
            
            val eventoMap = hashMapOf(
                "titulo" to evento.titulo,
                "descripcion" to evento.descripcion,
                "fecha" to fechaTimestamp,
                "tipo" to evento.tipo.name,
                "creadorId" to evento.creadorId,
                "centroId" to evento.centroId
            )
            
            val docRef = firestore.collection(COLLECTION_EVENTOS).add(eventoMap).await()
            docRef.id
        } catch (e: Exception) {
            Timber.e(e, "Error al crear evento")
            throw e
        }
    }

    /**
     * Actualiza un evento existente
     * @param evento Objeto Evento con los datos actualizados
     */
    suspend fun actualizarEvento(evento: Evento) {
        try {
            val fechaTimestamp = localDateTimeToTimestamp(evento.fecha)
            
            val eventoMap = hashMapOf(
                "titulo" to evento.titulo,
                "descripcion" to evento.descripcion,
                "fecha" to fechaTimestamp,
                "tipo" to evento.tipo.name,
                "creadorId" to evento.creadorId,
                "centroId" to evento.centroId
            )
            
            firestore.collection(COLLECTION_EVENTOS)
                .document(evento.id)
                .update(eventoMap as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar evento")
            throw e
        }
    }

    /**
     * Elimina un evento
     * @param eventoId ID del evento a eliminar
     */
    suspend fun eliminarEvento(eventoId: String) {
        try {
            firestore.collection(COLLECTION_EVENTOS)
                .document(eventoId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar evento")
            throw e
        }
    }

    /**
     * Convierte un Timestamp de Firebase a LocalDateTime
     */
    private fun timestampToLocalDateTime(timestamp: Timestamp?): LocalDateTime {
        return timestamp?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?: LocalDateTime.now()
    }

    /**
     * Convierte un LocalDateTime a Timestamp de Firebase
     */
    private fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Timestamp {
        val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(Date.from(instant))
    }
} 