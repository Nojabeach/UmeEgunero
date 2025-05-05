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
 * Repositorio para gestionar eventos en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * eventos en el contexto educativo, permitiendo a diferentes tipos de usuarios
 * (profesores, administradores, familiares) interactuar con el calendario
 * y la programación de actividades.
 *
 * Características principales:
 * - Creación de eventos educativos
 * - Gestión de calendarios por centro, curso y clase
 * - Soporte para diferentes tipos de eventos
 * - Control de permisos de visualización y edición
 * - Sincronización de eventos entre usuarios
 *
 * El repositorio permite:
 * - Programar actividades académicas
 * - Registrar eventos escolares
 * - Gestionar horarios de clases
 * - Compartir eventos con usuarios autorizados
 * - Notificar sobre próximos eventos
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property centroRepository Repositorio de centros para obtener información contextual
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
                    Evento.fromMap(doc.data ?: mapOf(), doc.id)
                }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos por centro")
            emptyList()
        }
    }

    /**
     * Obtiene los eventos próximos para un usuario
     * @param usuarioId ID del usuario
     * @return Lista de eventos próximos ordenados por fecha
     */
    suspend fun obtenerEventosProximos(usuarioId: String): List<Evento> {
        val ahora = Timestamp.now()
        val unaSemanaFutura = Timestamp(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        
        return try {
            // Obtener eventos públicos o donde el usuario es destinatario
            val eventosSnapshot = firestore.collection(COLLECTION_EVENTOS)
                .whereGreaterThanOrEqualTo("fecha", ahora)
                .whereLessThanOrEqualTo("fecha", unaSemanaFutura)
                .get()
                .await()
            
            val eventos = mutableListOf<Evento>()
            
            for (doc in eventosSnapshot.documents) {
                val data = doc.data ?: continue
                val esPublico = data["publico"] as? Boolean ?: true
                
                // Si es público o el usuario está en la lista de destinatarios
                if (esPublico) {
                    Evento.fromMap(data, doc.id)?.let { eventos.add(it) }
                } else {
                    val destinatarios = data["destinatarios"] as? List<*> ?: emptyList<String>()
                    if (destinatarios.contains(usuarioId)) {
                        Evento.fromMap(data, doc.id)?.let { eventos.add(it) }
                    }
                }
            }
            
            // Ordenar eventos por fecha (más próximos primero)
            eventos.sortedBy { it.fecha.seconds }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos próximos para el usuario $usuarioId")
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
                    Evento.fromMap(doc.data ?: mapOf(), doc.id)
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
            val docRef = firestore.collection(COLLECTION_EVENTOS).add(evento.toMap()).await()
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
            firestore.collection(COLLECTION_EVENTOS)
                .document(evento.id)
                .update(evento.toMap())
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

    /**
     * Actualiza la caché local de eventos desde Firestore
     * @param centroId ID del centro educativo
     */
    suspend fun actualizarEventosLocales(centroId: String) {
        try {
            Timber.d("Sincronizando eventos para el centro $centroId")
            val eventos = obtenerEventosPorCentro(centroId)
            Timber.d("Se sincronizaron ${eventos.size} eventos")
            // Aquí se podría implementar una caché local con Room
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar eventos")
        }
    }
} 