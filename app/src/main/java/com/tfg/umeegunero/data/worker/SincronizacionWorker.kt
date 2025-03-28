package com.tfg.umeegunero.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Worker para sincronizar registros pendientes con el servidor en segundo plano.
 * 
 * Este worker se ejecuta periódicamente para intentar sincronizar los registros
 * que fueron creados o modificados sin conexión.
 * 
 * @param context Contexto de la aplicación
 * @param workerParams Parámetros del worker
 * @param registroDiarioRepository Repositorio para sincronizar registros
 * 
 * @author Estudiante 2º DAM
 */
@HiltWorker
class SincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val registroDiarioRepository: RegistroDiarioRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando sincronización de registros pendientes")
        
        return try {
            registroDiarioRepository.sincronizarRegistrosPendientes()
            Timber.d("Sincronización completada con éxito")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error durante la sincronización de registros")
            // Si falla, indicamos que se debe reintentar
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "com.tfg.umeegunero.SINCRONIZACION_PERIODICA"
    }
} 