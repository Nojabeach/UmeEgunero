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
import com.tfg.umeegunero.data.service.TipoPlantilla
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import java.util.UUID

/**
 * Repositorio para gestionar solicitudes en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y gestionar
 * diferentes tipos de solicitudes en el contexto educativo, como vinculaciones
 * de usuarios, solicitudes de registro y otras interacciones que requieren
 * aprobación o seguimiento.
 *
 * Características principales:
 * - Creación de solicitudes de vinculación
 * - Gestión del ciclo de vida de las solicitudes
 * - Control de estados de solicitud
 * - Soporte para diferentes tipos de solicitudes
 * - Registro de interacciones y cambios de estado
 *
 * El repositorio permite:
 * - Crear solicitudes de vinculación familiar-alumno
 * - Gestionar solicitudes de registro de usuarios
 * - Rastrear el estado de las solicitudes
 * - Notificar sobre cambios en solicitudes
 * - Implementar flujos de aprobación
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property notificacionRepository Repositorio para enviar notificaciones relacionadas
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class SolicitudRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationManager: AppNotificationManager,
    private val notificationService: NotificationService,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val emailNotificationService: EmailNotificationService,
    private val unifiedMessageRepository: UnifiedMessageRepository
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
            
            // Crear mensaje en el sistema unificado para los administradores
            crearMensajeSolicitudPendiente(solicitudConId)
            
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
                
                // Crear mensaje en el sistema unificado
                crearMensajeSolicitudProcesada(solicitud, nombreAdmin)
                
                // Si la solicitud fue aprobada, enviar email de aprobación
                if (nuevoEstado == EstadoSolicitud.APROBADA) {
                    try {
                        // Obtener datos del familiar para el email
                        val familiarResultado = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
                        if (familiarResultado is Result.Success) {
                            val familiar = familiarResultado.data
                            
                            // Obtener información del alumno para personalizar el email
                            val alumnoInfo = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
                            val nombreAlumno = if (alumnoInfo is Result.Success && alumnoInfo.data != null) {
                                "${alumnoInfo.data.nombre} ${alumnoInfo.data.apellidos}".trim()
                            } else {
                                solicitud.alumnoNombre
                            }
                            
                            // Contenido HTML personalizado para el email de aprobación
                            val contenidoHtml = """
                                <!DOCTYPE html>
                                <html lang="es">
                                <head>
                                    <meta charset="UTF-8">
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            font-family: 'Segoe UI', Arial, sans-serif;
                                            line-height: 1.6;
                                            color: #333333;
                                            margin: 0;
                                            padding: 0;
                                            background-color: #f5f5f5;
                                        }
                                        .container {
                                            max-width: 600px;
                                            margin: 0 auto;
                                            padding: 20px;
                                            background-color: #ffffff;
                                            border-radius: 8px;
                                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                        }
                                        .header {
                                            background-color: #0D47A1;
                                            color: white;
                                            padding: 20px;
                                            text-align: center;
                                            border-radius: 8px 8px 0 0;
                                        }
                                        .content {
                                            padding: 20px;
                                        }
                                        .footer {
                                            text-align: center;
                                            padding: 20px;
                                            color: #666666;
                                            font-size: 12px;
                                        }
                                        .button {
                                            display: inline-block;
                                            padding: 12px 24px;
                                            background-color: #4CAF50;
                                            color: white;
                                            text-decoration: none;
                                            border-radius: 4px;
                                            margin-top: 20px;
                                        }
                                        .status {
                                            background-color: #4CAF50;
                                            color: white;
                                            padding: 8px 16px;
                                            border-radius: 20px;
                                            display: inline-block;
                                            margin: 10px 0;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="header">
                                            <h1>UmeEgunero</h1>
                                        </div>
                                        <div class="content">
                                            <h2>¡Solicitud Aprobada!</h2>
                                            <div class="status">APROBADA</div>
                                            <p>Estimado/a ${familiar.nombre},</p>
                                            <p>Nos complace informarle que su solicitud para vincularse con <strong>${nombreAlumno}</strong> en UmeEgunero ha sido <strong>aprobada</strong> por el centro.</p>
                                            <p>Ya puede acceder a la plataforma para ver la información académica.</p>
                                            <a href="https://umeegunero.com/login" class="button">Acceder a la plataforma</a>
                                        </div>
                                        <div class="footer">
                                            <p>Este es un mensaje automático, por favor no responda a este email.</p>
                                            <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
                                        </div>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            // Enviar email personalizado de aprobación usando el nuevo método
                            val emailEnviado = emailNotificationService.enviarEmailSolicitudProcesada(
                                destinatario = familiar.email,
                                nombre = familiar.nombre,
                                esAprobacion = true,
                                nombreAlumno = nombreAlumno,
                                contenidoHtml = contenidoHtml
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
                } else if (nuevoEstado == EstadoSolicitud.RECHAZADA) {
                    try {
                        // Obtener datos del familiar para el email
                        val familiarResultado = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
                        if (familiarResultado is Result.Success) {
                            val familiar = familiarResultado.data
                            
                            // Obtener información del alumno para personalizar el email
                            val alumnoInfo = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
                            val nombreAlumno = if (alumnoInfo is Result.Success && alumnoInfo.data != null) {
                                "${alumnoInfo.data.nombre} ${alumnoInfo.data.apellidos}".trim()
                            } else {
                                solicitud.alumnoNombre
                            }
                            
                            // Contenido HTML personalizado para el email de rechazo
                            val contenidoHtml = """
                                <!DOCTYPE html>
                                <html lang="es">
                                <head>
                                    <meta charset="UTF-8">
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            font-family: 'Segoe UI', Arial, sans-serif;
                                            line-height: 1.6;
                                            color: #333333;
                                            margin: 0;
                                            padding: 0;
                                            background-color: #f5f5f5;
                                        }
                                        .container {
                                            max-width: 600px;
                                            margin: 0 auto;
                                            padding: 20px;
                                            background-color: #ffffff;
                                            border-radius: 8px;
                                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                        }
                                        .header {
                                            background-color: #0D47A1;
                                            color: white;
                                            padding: 20px;
                                            text-align: center;
                                            border-radius: 8px 8px 0 0;
                                        }
                                        .content {
                                            padding: 20px;
                                        }
                                        .footer {
                                            text-align: center;
                                            padding: 20px;
                                            color: #666666;
                                            font-size: 12px;
                                        }
                                        .status {
                                            background-color: #F44336;
                                            color: white;
                                            padding: 8px 16px;
                                            border-radius: 20px;
                                            display: inline-block;
                                            margin: 10px 0;
                                        }
                                        .contact-info {
                                            background-color: #f8f9fa;
                                            padding: 15px;
                                            border-radius: 4px;
                                            margin: 20px 0;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="header">
                                            <h1>UmeEgunero</h1>
                                        </div>
                                        <div class="content">
                                            <h2>Estado de su Solicitud</h2>
                                            <div class="status">RECHAZADA</div>
                                            <p>Estimado/a ${familiar.nombre},</p>
                                            <p>Lamentamos informarle que su solicitud para vincularse con <strong>${nombreAlumno}</strong> en UmeEgunero ha sido <strong>rechazada</strong> por el centro.</p>
                                            ${if (observaciones.isNotEmpty()) "<p><strong>Motivo:</strong> $observaciones</p>" else ""}
                                            <div class="contact-info">
                                                <h3>¿Necesita más información?</h3>
                                                <p>Por favor, póngase en contacto con la secretaría del centro para obtener más detalles sobre esta decisión.</p>
                                            </div>
                                        </div>
                                        <div class="footer">
                                            <p>Este es un mensaje automático, por favor no responda a este email.</p>
                                            <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
                                        </div>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            // Enviar email personalizado de rechazo usando el nuevo método
                            val emailEnviado = emailNotificationService.enviarEmailSolicitudProcesada(
                                destinatario = familiar.email,
                                nombre = familiar.nombre,
                                esAprobacion = false,
                                nombreAlumno = nombreAlumno,
                                observaciones = observaciones,
                                contenidoHtml = contenidoHtml
                            )
                            
                            if (emailEnviado) {
                                Timber.d("Email de rechazo enviado correctamente a ${familiar.email}")
                            } else {
                                Timber.w("No se pudo enviar el email de rechazo a ${familiar.email}")
                            }
                        } else {
                            Timber.w("No se encontró información del familiar con ID ${solicitud.familiarId}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al enviar email de rechazo")
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
            Timber.d("🚀 Iniciando envío de notificación para solicitud ${solicitud.id} en centro $centroId")
            
            val centro = centroRepository.obtenerCentroPorId(centroId)
            val familiar = usuarioRepository.obtenerUsuarioPorId(solicitud.familiarId)
            val alumno = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
            
            val nombreFamiliar = familiar?.let { 
                if (it is Result.Success) {
                    "${it.data.nombre} ${it.data.apellidos}".trim()
                } else {
                    "Un familiar"
                }
            } ?: "Un familiar"
            
            val nombreAlumno = if (alumno is Result.Success && alumno.data != null) {
                "${alumno.data?.nombre ?: ""} ${alumno.data?.apellidos ?: ""}".trim()
            } else {
                "un alumno"
            }
            
            val nombreCentro = centro?.nombre ?: "tu centro"
            
            val titulo = "Nueva solicitud de vinculación"
            val mensaje = "El familiar $nombreFamiliar ha solicitado vincularse con $nombreAlumno en $nombreCentro"
            
            Timber.d("📝 Mensaje preparado: '$titulo' - '$mensaje'")
            
            // Usar el servicio de notificaciones local en lugar de Cloud Functions
            notificationService.enviarNotificacionSolicitud(
                centroId = centroId,
                solicitudId = solicitud.id,
                titulo = titulo,
                mensaje = mensaje,
                onCompletion = { exito, mensajeResultado ->
                    Timber.d("📊 Resultado envío notificación: $exito - $mensajeResultado")
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "💥 Error al enviar notificación de solicitud pendiente")
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
            
            val alumno = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
            val alumnoNombre = if (alumno is Result.Success && alumno.data != null) {
                "${alumno.data?.nombre ?: ""} ${alumno.data?.apellidos ?: ""}".trim()
            } else {
                "el alumno"
            }
            
            val estado = solicitud.estado
            val titulo = when (estado) {
                EstadoSolicitud.APROBADA -> "Solicitud aprobada"
                EstadoSolicitud.RECHAZADA -> "Solicitud rechazada"
                else -> "Actualización de solicitud"
            }
            
            val mensaje = when (estado) {
                EstadoSolicitud.APROBADA -> "Tu solicitud para vincularte con $alumnoNombre ha sido aprobada"
                EstadoSolicitud.RECHAZADA -> "Tu solicitud para vincularte con $alumnoNombre ha sido rechazada"
                else -> "El estado de tu solicitud para vincularte con $alumnoNombre ha cambiado a $estado"
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
    
    /**
     * Crea un mensaje en el sistema unificado para notificar sobre una solicitud pendiente
     * 
     * @param solicitud Datos de la solicitud
     */
    private suspend fun crearMensajeSolicitudPendiente(solicitud: SolicitudVinculacion) {
        try {
            // Obtener información del centro para determinar los administradores
            val adminsResult = usuarioRepository.getAdminsByCentroId(solicitud.centroId)
            if (adminsResult is Result.Success && adminsResult.data.isNotEmpty()) {
                // Obtener información del alumno y centro para el mensaje
                val alumnoInfo = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
                val centroInfo = centroRepository.getCentroById(solicitud.centroId)
                
                val alumnoNombre = if (alumnoInfo is Result.Success && alumnoInfo.data != null) {
                    "${alumnoInfo.data?.nombre} ${alumnoInfo.data?.apellidos}".trim()
                } else {
                    "Alumno"
                }
                val centroNombre = if (centroInfo is Result.Success) centroInfo.data?.nombre ?: "Centro" else "Centro"
                
                // Crear un mensaje para cada administrador
                adminsResult.data.forEach { admin ->
                    val message = UnifiedMessage(
                        id = UUID.randomUUID().toString(),
                        title = "Nueva solicitud de vinculación pendiente",
                        content = "El familiar ${solicitud.nombreFamiliar} ha solicitado vincularse con $alumnoNombre en $centroNombre.",
                        type = MessageType.NOTIFICATION,
                        priority = MessagePriority.HIGH,
                        senderId = "system",
                        senderName = "Sistema UmeEgunero",
                        receiverId = admin.dni,
                        receiversIds = emptyList(),
                        timestamp = Timestamp.now(),
                        status = MessageStatus.UNREAD,
                        metadata = mapOf(
                            "solicitudId" to solicitud.id,
                            "alumnoDni" to solicitud.alumnoDni,
                            "tipoNotificacion" to "SOLICITUD_VINCULACION"
                        ),
                        relatedEntityId = solicitud.id,
                        relatedEntityType = "SOLICITUD_VINCULACION"
                    )
                    
                    unifiedMessageRepository.sendMessage(message)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al crear mensaje para solicitud pendiente")
        }
    }
    
    /**
     * Crea un mensaje en el sistema unificado para notificar sobre una solicitud procesada
     * 
     * @param solicitud Datos de la solicitud
     * @param nombreAdmin Nombre del administrador que procesó la solicitud
     */
    private suspend fun crearMensajeSolicitudProcesada(solicitud: SolicitudVinculacion, nombreAdmin: String) {
        try {
            // Obtener información del familiar para el mensaje
            val familiarResult = usuarioRepository.getUsuarioById(solicitud.familiarId)
                
                // Obtener información del alumno y centro para personalizar el mensaje
                val alumnoInfo = alumnoRepository.getAlumnoByDni(solicitud.alumnoDni)
                val centroInfo = centroRepository.getCentroById(solicitud.centroId)
                
            val alumnoNombre = if (alumnoInfo is Result.Success && alumnoInfo.data != null) {
                "${alumnoInfo.data?.nombre ?: ""} ${alumnoInfo.data?.apellidos ?: ""}".trim()
            } else {
                "el alumno"
            }
                val centroNombre = if (centroInfo is Result.Success) centroInfo.data?.nombre ?: "el centro" else "el centro"
                
                // Determinar título y contenido según el estado
                val title = when(solicitud.estado) {
                    EstadoSolicitud.APROBADA -> "Solicitud de vinculación aprobada"
                    EstadoSolicitud.RECHAZADA -> "Solicitud de vinculación rechazada"
                    else -> "Actualización de solicitud de vinculación"
                }
                
                val content = when(solicitud.estado) {
                    EstadoSolicitud.APROBADA -> 
                        "Su solicitud para vincularse con $alumnoNombre en $centroNombre ha sido aprobada por $nombreAdmin."
                    EstadoSolicitud.RECHAZADA -> 
                        "Su solicitud para vincularse con $alumnoNombre en $centroNombre ha sido rechazada por $nombreAdmin."
                    else -> 
                        "Su solicitud para vincularse con $alumnoNombre en $centroNombre ha sido actualizada."
                }
                
                // Crear mensaje para el familiar
                val message = UnifiedMessage(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    type = MessageType.NOTIFICATION,
                    priority = if (solicitud.estado == EstadoSolicitud.APROBADA) MessagePriority.HIGH else MessagePriority.NORMAL,
                    senderId = "system",
                    senderName = "Sistema UmeEgunero",
                    receiverId = solicitud.familiarId,
                    receiversIds = emptyList(),
                    timestamp = Timestamp.now(),
                    status = MessageStatus.UNREAD,
                    metadata = mapOf(
                        "solicitudId" to solicitud.id,
                        "alumnoDni" to solicitud.alumnoDni,
                        "estado" to solicitud.estado.name,
                        "tipoNotificacion" to "SOLICITUD_VINCULACION"
                    ),
                    relatedEntityId = solicitud.id,
                    relatedEntityType = "SOLICITUD_VINCULACION"
                )
                
                unifiedMessageRepository.sendMessage(message)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear mensaje para solicitud procesada")
        }
    }
} 