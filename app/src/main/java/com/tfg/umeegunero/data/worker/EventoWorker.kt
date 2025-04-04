package com.tfg.umeegunero.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.di.ChildWorkerFactory
import com.tfg.umeegunero.notification.AppNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Worker encargado de revisar eventos próximos y programar notificaciones
 */
@HiltWorker
class EventoWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventoRepository: EventoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val notificationManager: AppNotificationManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando revisión de eventos programados")
        
        try {
            // Verificar que el usuario está autenticado
            val usuarioId = usuarioRepository.getUsuarioActualAuth()?.uid
            
            if (usuarioId == null) {
                Timber.w("No hay usuario autenticado para revisar eventos")
                return Result.failure()
            }
            
            // Verificar preferencias de notificaciones
            val notificacionesActivadas = preferenciasRepository.notificacionesGeneralFlow.first()
            
            if (!notificacionesActivadas) {
                Timber.d("Notificaciones generales desactivadas por el usuario")
                return Result.success()
            }
            
            // Obtener eventos próximos
            revisarEventosProximos(usuarioId)
            
            Timber.d("Revisión de eventos completada")
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error al revisar eventos")
            return Result.retry()
        }
    }
    
    /**
     * Revisa eventos próximos y programa notificaciones para ellos
     */
    private suspend fun revisarEventosProximos(usuarioId: String) {
        val ahora = Calendar.getInstance().time
        val eventosProximos = eventoRepository.obtenerEventosProximos(usuarioId)
        
        Timber.d("Encontrados ${eventosProximos.size} eventos próximos")
        
        for (evento in eventosProximos) {
            programarNotificacionParaEvento(evento, ahora)
        }
    }
    
    /**
     * Programa notificaciones para un evento según su proximidad
     */
    private fun programarNotificacionParaEvento(evento: Evento, ahora: Date) {
        try {
            val fechaEvento = evento.fecha.toDate()
            val tiempoRestante = fechaEvento.time - ahora.time
            
            // Solo notificar eventos futuros
            if (tiempoRestante <= 0) return
            
            // Si el evento es hoy (menos de 24 horas)
            if (tiempoRestante < TimeUnit.HOURS.toMillis(24)) {
                enviarNotificacionEvento(evento, "Evento hoy", 
                    "El evento '${evento.titulo}' está programado para hoy")
                return
            }
            
            // Si el evento es mañana (entre 24 y 48 horas)
            if (tiempoRestante < TimeUnit.HOURS.toMillis(48)) {
                enviarNotificacionEvento(evento, "Evento mañana", 
                    "El evento '${evento.titulo}' está programado para mañana")
                return
            }
            
            // Para eventos en 3 días, enviar notificación ahora
            if (tiempoRestante < TimeUnit.DAYS.toMillis(3)) {
                enviarNotificacionEvento(
                    evento, 
                    "Evento próximo", 
                    "El evento '${evento.titulo}' tendrá lugar en pocos días"
                )
                Timber.d("Notificación enviada para evento próximo: ${evento.titulo}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificación para evento ${evento.id}")
        }
    }
    
    /**
     * Envía una notificación inmediata sobre un evento
     */
    private fun enviarNotificacionEvento(evento: Evento, titulo: String, mensaje: String) {
        notificationManager.showNotification(
            "Próximo evento: ${evento.titulo}",
            mensaje,
            AppNotificationManager.CHANNEL_ID_GENERAL,
            evento.id.hashCode()
        )
        
        Timber.d("Notificación inmediata enviada para evento: ${evento.titulo}")
    }
    
    /**
     * Factory para crear instancias de EventoWorker con Hilt
     * Ya no es necesaria con @HiltWorker, pero la mantenemos por compatibilidad
     */
    class Factory @Inject constructor(
        private val eventoRepository: EventoRepository,
        private val usuarioRepository: UsuarioRepository,
        private val preferenciasRepository: PreferenciasRepository,
        private val notificationManager: AppNotificationManager
    ) : ChildWorkerFactory {
        override fun create(
            appContext: Context,
            workerParams: WorkerParameters
        ): CoroutineWorker {
            return EventoWorker(
                appContext,
                workerParams,
                eventoRepository,
                usuarioRepository,
                preferenciasRepository,
                notificationManager
            )
        }
    }
} 