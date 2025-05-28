package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
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
        private const val COLLECTION_USUARIOS = "usuarios"
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
     * @param usuarioId ID del usuario (DNI)
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
     * @param profesorId ID del profesor (DNI)
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
     * Busca el DNI de un usuario por su UID de Firebase
     * @param firebaseUid UID de Firebase del usuario
     * @return DNI del usuario o null si no se encuentra
     */
    private suspend fun getDniByFirebaseUid(firebaseUid: String): String? {
        return try {
            val usuariosSnapshot = firestore.collection(COLLECTION_USUARIOS)
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .await()
            
            if (usuariosSnapshot.isEmpty) {
                Timber.w("No se encontró usuario con UID: $firebaseUid")
                null
            } else {
                val usuario = usuariosSnapshot.documents.first().toObject(Usuario::class.java)
                val dni = usuario?.dni
                if (dni.isNullOrBlank()) {
                    Timber.w("Usuario encontrado pero sin DNI: $firebaseUid")
                    null
                } else {
                    Timber.d("DNI encontrado: $dni para UID: $firebaseUid")
                    dni
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar DNI por UID: $firebaseUid")
            null
        }
    }

    /**
     * Crea un nuevo evento en Firestore
     * @param evento Objeto Evento a crear, puede tener firebaseUid o DNI como creadorId
     * @return Resultado con el ID del evento creado
     */
    suspend fun crearEvento(evento: Evento): Result<String> {
        return try {
            val nuevoEventoRef = firestore.collection(COLLECTION_EVENTOS).document()
            val eventoId = nuevoEventoRef.id
            
            // Comprobar si el creadorId es un UID de Firebase o un DNI
            val creadorId = if (evento.creadorId.length > 20) {
                // Probablemente es un UID de Firebase, buscar el DNI correspondiente
                val dni = getDniByFirebaseUid(evento.creadorId)
                if (dni.isNullOrBlank()) {
                    // Si no se encontró el DNI, usamos el UID como fallback
                    Timber.w("No se encontró DNI para UID: ${evento.creadorId}, usando UID como fallback")
                    evento.creadorId
                } else {
                    dni
                }
            } else {
                // Es probable que ya sea un DNI, lo usamos directamente
                evento.creadorId
            }
            
            // Crear mapa con los datos del evento, pero usando el DNI como creadorId
            val eventoMap = evento.toMap().toMutableMap()
            eventoMap["creadorId"] = creadorId
            
            // Guardar el evento usando los datos del map actualizado
            nuevoEventoRef.set(eventoMap).await()
            
            Timber.d("Evento creado con ID: $eventoId, creador (DNI): $creadorId")
            Result.Success(eventoId)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear evento")
            Result.Error(e)
        }
    }

    /**
     * Actualiza un evento existente
     * @param evento Objeto Evento con los datos actualizados
     */
    suspend fun actualizarEvento(evento: Evento) {
        try {
            // Comprobar si el creadorId es un UID de Firebase o un DNI
            val creadorId = if (evento.creadorId.length > 20) {
                // Probablemente es un UID de Firebase, buscar el DNI correspondiente
                val dni = getDniByFirebaseUid(evento.creadorId)
                if (dni.isNullOrBlank()) {
                    // Si no se encontró el DNI, usamos el UID como fallback
                    Timber.w("No se encontró DNI para UID: ${evento.creadorId}, usando UID como fallback")
                    evento.creadorId
                } else {
                    dni
                }
            } else {
                // Es probable que ya sea un DNI, lo usamos directamente
                evento.creadorId
            }
            
            // Crear mapa con los datos del evento, pero usando el DNI como creadorId
            val eventoMap = evento.toMap().toMutableMap()
            eventoMap["creadorId"] = creadorId
            
            firestore.collection(COLLECTION_EVENTOS)
                .document(evento.id)
                .update(eventoMap)
                .await()
                
            Timber.d("Evento actualizado: ${evento.id}, creador (DNI): $creadorId")
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar evento")
            throw e
        }
    }

    /**
     * Elimina un evento
     * @param eventoId ID del evento a eliminar
     * @return Result indicando éxito o error
     */
    suspend fun eliminarEvento(eventoId: String): Result<Unit> {
        return try {
            Timber.d("Intentando eliminar evento con ID: $eventoId")
            
            // Verificar primero que el evento existe
            val eventoExistente = firestore.collection(COLLECTION_EVENTOS)
                .document(eventoId)
                .get()
                .await()
                
            if (!eventoExistente.exists()) {
                Timber.w("Intento de eliminar un evento que no existe: $eventoId")
                return Result.Error(Exception("El evento no existe o ya fue eliminado"))
            }
            
            // Proceder con la eliminación
            firestore.collection(COLLECTION_EVENTOS)
                .document(eventoId)
                .delete()
                .await()
                
            Timber.d("Evento eliminado correctamente: $eventoId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar evento: $eventoId")
            Result.Error(e)
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