package com.tfg.umeegunero.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.di.ChildWorkerFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Inject

/**
 * Worker encargado de sincronizar datos en segundo plano
 * 
 * Realiza una sincronización periódica de los datos más importantes de la aplicación:
 * - Tareas asignadas al alumno o creadas por el profesor
 * - Eventos del calendario
 */
@HiltWorker
class SincronizacionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usuarioRepository: UsuarioRepository,
    private val tareaRepository: TareaRepository,
    private val eventoRepository: EventoRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando sincronización periódica")
        
        try {
            // Verificar que el usuario está autenticado
            val usuario = usuarioRepository.getUsuarioActualAuth()
            
            if (usuario == null) {
                Timber.w("No hay usuario autenticado para sincronizar")
                return Result.failure()
            }
            
            // Determinar si es profesor o alumno
            val esProfesor = usuarioRepository.esProfesor()
            
            // Sincronizar tareas
            sincronizarTareas(usuario.uid, esProfesor)
            
            // Sincronizar eventos
            sincronizarEventos(usuario.uid, esProfesor)
            
            Timber.d("Sincronización periódica completada")
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error en sincronización periódica")
            return Result.retry()
        }
    }
    
    /**
     * Sincroniza las tareas del usuario
     */
    private suspend fun sincronizarTareas(usuarioId: String, esProfesor: Boolean) {
        try {
            Timber.d("Sincronizando tareas")
            tareaRepository.actualizarTareasLocales(usuarioId, esProfesor)
            Timber.d("Sincronización de tareas completada")
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar tareas")
        }
    }
    
    /**
     * Sincroniza los eventos del calendario
     */
    private suspend fun sincronizarEventos(usuarioId: String, esProfesor: Boolean) {
        try {
            Timber.d("Sincronizando eventos")
            // Obtener el centro del usuario
            val centroId = usuarioRepository.getCentroIdUsuarioActual() ?: return
            eventoRepository.actualizarEventosLocales(centroId)
            Timber.d("Sincronización de eventos completada")
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar eventos")
        }
    }
    
    /**
     * Factory para crear instancias de SincronizacionWorker con Hilt
     * Ya no es necesaria con @HiltWorker, pero la mantenemos por compatibilidad
     */
    class Factory @Inject constructor(
        private val usuarioRepository: UsuarioRepository,
        private val tareaRepository: TareaRepository,
        private val eventoRepository: EventoRepository
    ) : ChildWorkerFactory {
        override fun create(
            appContext: Context,
            workerParams: WorkerParameters
        ): CoroutineWorker {
            return SincronizacionWorker(
                appContext,
                workerParams,
                usuarioRepository,
                tareaRepository,
                eventoRepository
            )
        }
    }
} 