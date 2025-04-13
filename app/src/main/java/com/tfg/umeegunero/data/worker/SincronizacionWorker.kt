package com.tfg.umeegunero.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tfg.umeegunero.data.repository.SyncRepository
import com.tfg.umeegunero.util.SyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Worker que se encarga de la sincronización periódica de datos.
 */
@HiltWorker
class SincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando tarea de sincronización periódica")
        
        try {
            val operacionesPendientes = syncRepository.obtenerNumeroOperacionesPendientes()
            
            if (operacionesPendientes > 0) {
                // Si hay operaciones pendientes, iniciar el servicio de sincronización
                Timber.d("Hay $operacionesPendientes operaciones pendientes, iniciando servicio de sincronización")
                syncManager.iniciarServicioSincronizacion()
                return Result.success()
            }
            
            Timber.d("No hay operaciones pendientes que sincronizar")
            return Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error en el worker de sincronización")
            return Result.retry()
        }
    }
} 