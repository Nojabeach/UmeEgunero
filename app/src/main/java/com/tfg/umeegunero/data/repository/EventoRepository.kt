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
            
            Timber.d("DEBUG-EVENTOS-REPO: Creando evento. Destinatarios: ${evento.destinatarios.size}")
            
            // Comprobar si el creadorId es un UID de Firebase o un DNI
            val creadorId = if (evento.creadorId.length > 20) {
                // Probablemente es un UID de Firebase, buscar el DNI correspondiente
                val dni = getDniByFirebaseUid(evento.creadorId)
                if (dni.isNullOrBlank()) {
                    // Si no se encontró el DNI, usamos el UID como fallback
                    Timber.w("DEBUG-EVENTOS-REPO: No se encontró DNI para UID: ${evento.creadorId}, usando UID como fallback")
                    evento.creadorId
                } else {
                    Timber.d("DEBUG-EVENTOS-REPO: Convertido UID a DNI: $dni")
                    dni
                }
            } else {
                // Es probable que ya sea un DNI, lo usamos directamente
                Timber.d("DEBUG-EVENTOS-REPO: Usando creadorId (DNI): ${evento.creadorId}")
                evento.creadorId
            }
            
            // Crear mapa con los datos del evento, pero usando el DNI como creadorId
            val eventoMap = evento.toMap().toMutableMap()
            eventoMap["creadorId"] = creadorId
            
            // Verificar que los destinatarios estén presentes en el mapa y no sean sobrescritos
            if (!eventoMap.containsKey("destinatarios") || (eventoMap["destinatarios"] as? List<*>)?.isEmpty() == true) {
                Timber.d("DEBUG-EVENTOS-REPO: No se encontraron destinatarios en el mapa, agregándolos desde el objeto evento")
                val destinatarios = ArrayList(evento.destinatarios)
                eventoMap["destinatarios"] = destinatarios
            }
            
            // Lo mismo para destinatariosIds
            if (!eventoMap.containsKey("destinatariosIds") || (eventoMap["destinatariosIds"] as? List<*>)?.isEmpty() == true) {
                Timber.d("DEBUG-EVENTOS-REPO: No se encontraron destinatariosIds en el mapa, agregándolos desde el objeto evento")
                val destinatarios = ArrayList(evento.destinatarios)
                eventoMap["destinatariosIds"] = destinatarios
            }
            
            Timber.d("DEBUG-EVENTOS-REPO: Guardando evento con ${(eventoMap["destinatarios"] as? List<*>)?.size ?: 0} destinatarios")
            Timber.d("DEBUG-EVENTOS-REPO: Lista de destinatarios: ${eventoMap["destinatarios"]}")
            Timber.d("DEBUG-EVENTOS-REPO: Lista de destinatariosIds: ${eventoMap["destinatariosIds"]}")
            
            // Guardar el evento usando los datos del map actualizado
            nuevoEventoRef.set(eventoMap).await()
            
            Timber.d("DEBUG-EVENTOS-REPO: Evento creado con ID: $eventoId, creador (DNI): $creadorId")
            Result.Success(eventoId)
        } catch (e: Exception) {
            Timber.e(e, "DEBUG-EVENTOS-REPO: Error al crear evento: ${e.message}")
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

    /**
     * Obtiene todos los eventos destinados a un usuario específico
     * @param usuarioId ID del usuario (DNI)
     * @param centroId ID del centro educativo
     * @return Lista de eventos destinados al usuario
     */
    suspend fun obtenerEventosParaUsuario(usuarioId: String, centroId: String): List<Evento> {
        return try {
            Timber.d("EventoRepository: Obteniendo eventos para usuario $usuarioId en centro $centroId")
            
            // Obtener todos los eventos del centro
            val eventosSnapshot = firestore.collection(COLLECTION_EVENTOS)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            val eventos = mutableListOf<Evento>()
            
            for (doc in eventosSnapshot.documents) {
                val data = doc.data ?: continue
                val evento = Evento.fromMap(data, doc.id) ?: continue
                
                // Extraer y registrar la fecha del evento para depuración
                val fechaStr = if (evento.fecha is com.google.firebase.Timestamp) {
                    val ts = evento.fecha as com.google.firebase.Timestamp
                    val date = java.util.Date(ts.seconds * 1000)
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
                } else {
                    "Formato desconocido: ${evento.fecha}"
                }
                
                // Verificar si el usuario es destinatario
                val esDestinatario = evento.destinatarios.contains(usuarioId)
                
                // Incluir el evento si:
                // 1. Es público
                // 2. El usuario es destinatario
                // 3. El usuario es el creador
                val incluir = evento.publico || esDestinatario || evento.creadorId == usuarioId
                
                Timber.d("EventoRepository: Evento ID=${evento.id}, Título=${evento.titulo}, Fecha=$fechaStr")
                Timber.d("EventoRepository: - Público=${evento.publico}, Usuario es destinatario=$esDestinatario, Usuario es creador=${evento.creadorId == usuarioId}")
                Timber.d("EventoRepository: - Destinatarios=${evento.destinatarios}")
                Timber.d("EventoRepository: - ¿Incluir? $incluir")
                
                if (incluir) {
                    eventos.add(evento)
                }
            }
            
            Timber.d("EventoRepository: Recuperados ${eventos.size} eventos para usuario $usuarioId")
            
            // Ordenar eventos por fecha
            eventos.sortedBy { it.fecha.seconds }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos para usuario $usuarioId en centro $centroId")
            emptyList()
        }
    }

    /**
     * Obtiene todos los eventos destinados a un usuario específico sin necesidad de centroId
     * @param usuarioId ID del usuario (DNI)
     * @return Lista de eventos destinados al usuario
     */
    suspend fun obtenerEventosDestinadosAUsuario(usuarioId: String): List<Evento> {
        return try {
            Timber.d("EventoRepository: Obteniendo eventos destinados al usuario $usuarioId")
            
            // Buscar eventos donde el usuario está en la lista de destinatarios
            val eventosSnapshot = firestore.collection(COLLECTION_EVENTOS)
                .whereArrayContains("destinatarios", usuarioId)
                .get()
                .await()
            
            val eventos = eventosSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val evento = Evento.fromMap(data, doc.id) ?: return@mapNotNull null
                
                // Para depuración, mostrar información del evento
                val fechaStr = if (evento.fecha is com.google.firebase.Timestamp) {
                    val ts = evento.fecha as com.google.firebase.Timestamp
                    val date = java.util.Date(ts.seconds * 1000)
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
                } else {
                    "Formato desconocido: ${evento.fecha}"
                }
                
                Timber.d("EventoRepository: Evento destinado al usuario - ID=${evento.id}, Título=${evento.titulo}, Fecha=$fechaStr")
                evento
            }
            
            Timber.d("EventoRepository: Recuperados ${eventos.size} eventos destinados al usuario $usuarioId")
            eventos.sortedBy { it.fecha.seconds }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos destinados al usuario $usuarioId: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtiene todos los eventos públicos
     * @return Lista de eventos públicos
     */
    suspend fun obtenerEventosPublicos(): List<Evento> {
        return try {
            Timber.d("EventoRepository: Obteniendo eventos públicos")
            
            // Buscar eventos públicos
            val eventosSnapshot = firestore.collection(COLLECTION_EVENTOS)
                .whereEqualTo("publico", true)
                .get()
                .await()
            
            val eventos = eventosSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val evento = Evento.fromMap(data, doc.id) ?: return@mapNotNull null
                
                // Para depuración, mostrar información del evento
                val fechaStr = if (evento.fecha is com.google.firebase.Timestamp) {
                    val ts = evento.fecha as com.google.firebase.Timestamp
                    val date = java.util.Date(ts.seconds * 1000)
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
                } else {
                    "Formato desconocido: ${evento.fecha}"
                }
                
                Timber.d("EventoRepository: Evento público - ID=${evento.id}, Título=${evento.titulo}, Fecha=$fechaStr")
                evento
            }
            
            Timber.d("EventoRepository: Recuperados ${eventos.size} eventos públicos")
            eventos.sortedBy { it.fecha.seconds }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener eventos públicos: ${e.message}")
            emptyList()
        }
    }
} 