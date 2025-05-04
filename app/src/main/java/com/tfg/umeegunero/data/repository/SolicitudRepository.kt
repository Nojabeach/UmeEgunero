package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tfg.umeegunero.notification.AppNotificationManager
import com.tfg.umeegunero.data.service.NotificationService
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.service.EmailNotificationService

/**
 * Repositorio para gestionar las solicitudes de vinculación entre familiares y alumnos.
 * 
 * Este repositorio se encarga de todas las operaciones relacionadas con las solicitudes
 * de vinculación, como crear nuevas solicitudes, obtener solicitudes existentes,
 * y actualizar el estado de las solicitudes.
 * 
 * @property firestore Instancia de FirebaseFirestore para acceder a la base de datos
 */
@Singleton
class SolicitudRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationManager: AppNotificationManager,
    private val notificationService: NotificationService,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val emailNotificationService: EmailNotificationService
) {
    companion object {
        private const val COLLECTION_SOLICITUDES = "solicitudes_vinculacion"
    }
    
    /**
     * Crea una nueva solicitud de vinculación en la base de datos
     * 
     * @param solicitud Datos de la solicitud a crear
     * @return Resultado con la solicitud creada o error
     */
    suspend fun crearSolicitudVinculacion(solicitud: SolicitudVinculacion): Result<SolicitudVinculacion> {
        return try {
            val docRef = firestore.collection(COLLECTION_SOLICITUDES).document()
            val solicitudConId = solicitud.copy(id = docRef.id)
            
            docRef.set(solicitudConId).await()
            
            // Enviar notificación a los administradores del centro
            enviarNotificacionSolicitudPendiente(solicitudConId)
            
            Result.Success(solicitudConId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las solicitudes de vinculación asociadas a un familiar
     * 
     * @param familiarId ID del familiar
     * @return Lista de solicitudes o error
     */
    suspend fun getSolicitudesByFamiliarId(familiarId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("familiarId", familiarId)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Comprueba si el familiar tiene solicitudes pendientes de aprobación.
     *
     * @param familiarId ID del familiar
     * @return true si tiene solicitudes pendientes, false si no
     */
    suspend fun tieneSolicitudesPendientes(familiarId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("familiarId", familiarId)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .limit(1) // Solo necesitamos saber si hay al menos una
                .get()
                .await()
            
            val tienePendientes = !snapshot.isEmpty
            Result.Success(tienePendientes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las solicitudes de vinculación pendientes para un centro
     * 
     * @param centroId ID del centro educativo
     * @return Lista de solicitudes pendientes o error
     */
    suspend fun getSolicitudesPendientesByCentroId(centroId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene el historial completo de solicitudes de vinculación para un centro
     * 
     * @param centroId ID del centro educativo
     * @return Lista de solicitudes procesadas o error
     */
    suspend fun getHistorialSolicitudesByCentroId(centroId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }.sortedByDescending { it.fechaSolicitud }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de una solicitud de vinculación
     * 
     * @param solicitudId ID de la solicitud
     * @param nuevoEstado Nuevo estado de la solicitud
     * @return Resultado de la operación
     */
    suspend fun actualizarEstadoSolicitud(solicitudId: String, nuevoEstado: String): Result<Boolean> {
        return try {
            firestore.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .update("estado", nuevoEstado)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de una solicitud de vinculación incluyendo el administrador que la procesó
     * 
     * @param solicitudId ID de la solicitud
     * @param nuevoEstado Nuevo estado de la solicitud
     * @param adminId ID del administrador que procesa la solicitud
     * @param nombreAdmin Nombre del administrador que procesa la solicitud
     * @param observaciones Observaciones adicionales sobre la solicitud (opcional)
     * @return Resultado de la operación
     */
    suspend fun procesarSolicitud(
        solicitudId: String, 
        nuevoEstado: EstadoSolicitud,
        adminId: String,
        nombreAdmin: String,
        observaciones: String = ""
    ): Result<Boolean> {
        return try {
            val actualizaciones = mutableMapOf<String, Any>(
                "estado" to nuevoEstado.name,
                "adminId" to adminId,
                "nombreAdmin" to nombreAdmin,
                "fechaProcesamiento" to Timestamp.now()
            )
            
            if (observaciones.isNotEmpty()) {
                actualizaciones["observaciones"] = observaciones
            }
            
            firestore.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .update(actualizaciones)
                .await()
            
            // Obtener los detalles de la solicitud para enviar notificación al familiar
            val solicitudSnapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .get()
                .await()
                
            val solicitud = solicitudSnapshot.toObject(SolicitudVinculacion::class.java)
            if (solicitud != null) {
                // Enviar notificación push
                enviarNotificacionSolicitudProcesada(solicitud)
                
                // Si la solicitud fue aprobada, enviar email de aprobación
                if (nuevoEstado == EstadoSolicitud.APROBADA) {
                    try {
                        // Obtener datos del familiar para el email
                        val familiarResultado = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
                        if (familiarResultado is Result.Success && familiarResultado.data != null) {
                            val familiar = familiarResultado.data
                            
                            // Enviar email de aprobación
                            val emailEnviado = emailNotificationService.sendApprovalEmail(
                                email = familiar.email,
                                nombre = familiar.nombre
                            )
                            
                            if (emailEnviado) {
                                Timber.d("Email de aprobación enviado correctamente a ${familiar.email}")
                            } else {
                                Timber.w("No se pudo enviar el email de aprobación a ${familiar.email}")
                            }
                        } else {
                            Timber.w("No se encontró información del familiar con ID ${solicitud.familiarId}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al enviar email de aprobación")
                        // No interrumpimos el flujo principal si falla el envío de email
                    }
                }
            }
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Envía una notificación a los administradores de centro cuando hay una nueva solicitud pendiente
     */
    suspend fun enviarNotificacionSolicitudPendiente(solicitud: SolicitudVinculacion) {
        try {
            val centroId = solicitud.centroId
            val centro = centroRepository.obtenerCentroPorId(centroId)
            val familiar = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
            val alumno = solicitud.alumnoId?.let { alumnoRepository.obtenerAlumnoPorId(it) }
            
            val nombreFamiliar = familiar?.let { 
                if (it is Result.Success && it.data != null) {
                    "${it.data.nombre} ${it.data.apellidos}".trim()
                } else {
                    "Un familiar"
                }
            } ?: "Un familiar"
            
            val nombreAlumno = alumno?.let { 
                if (it is Result.Success && it.data != null) {
                    "${it.data.nombre} ${it.data.apellidos}".trim()
                } else {
                    "un alumno"
                }
            } ?: "un alumno"
            
            val nombreCentro = centro?.nombre ?: "tu centro"
            
            val titulo = "Nueva solicitud de vinculación"
            val mensaje = "El familiar $nombreFamiliar ha solicitado vincularse con $nombreAlumno en $nombreCentro"
            
            // Usar el servicio de notificaciones local en lugar de Cloud Functions
            notificationService.enviarNotificacionSolicitud(
                centroId = centroId,
                solicitudId = solicitud.id,
                titulo = titulo,
                mensaje = mensaje,
                onCompletion = { exito, mensajeResultado ->
                    Timber.d("Resultado envío notificación: $exito - $mensajeResultado")
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificación de solicitud pendiente")
        }
    }
    
    /**
     * Envía una notificación al familiar cuando su solicitud es procesada
     */
    suspend fun enviarNotificacionSolicitudProcesada(solicitud: SolicitudVinculacion) {
        try {
            val familiar = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
            if (familiar == null) {
                Timber.w("No se encontró el familiar con ID ${solicitud.familiarId}")
                return
            }
            
            val alumno = solicitud.alumnoId?.let { alumnoRepository.obtenerAlumnoPorId(it) }
            val nombreAlumno = alumno?.let { 
                if (it is Result.Success && it.data != null) {
                    "${it.data.nombre} ${it.data.apellidos}".trim()
                } else {
                    "el alumno"
                }
            } ?: "el alumno"
            
            val estado = solicitud.estado
            val titulo = when (estado) {
                EstadoSolicitud.APROBADA -> "Solicitud aprobada"
                EstadoSolicitud.RECHAZADA -> "Solicitud rechazada"
                else -> "Actualización de solicitud"
            }
            
            val mensaje = when (estado) {
                EstadoSolicitud.APROBADA -> "Tu solicitud para vincularte con $nombreAlumno ha sido aprobada"
                EstadoSolicitud.RECHAZADA -> "Tu solicitud para vincularte con $nombreAlumno ha sido rechazada"
                else -> "El estado de tu solicitud para vincularte con $nombreAlumno ha cambiado a $estado"
            }
            
            // Usar el servicio de notificaciones local en lugar de Cloud Functions
            notificationService.enviarNotificacionFamiliar(
                familiarId = solicitud.familiarId,
                solicitudId = solicitud.id,
                estado = estado.name,
                titulo = titulo,
                mensaje = mensaje,
                onCompletion = { exito, mensajeResultado ->
                    Timber.d("Resultado envío notificación: $exito - $mensajeResultado")
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificación de solicitud procesada")
        }
    }
} 