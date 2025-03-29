package com.tfg.umeegunero

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.BuildConfig
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.util.DebugUtils
import com.tfg.umeegunero.util.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Clase principal de la aplicación que extiende de [Application].
 * 
 * Esta clase es responsable de inicializar los componentes globales de la aplicación:
 * - Configuración de Hilt para la inyección de dependencias
 * - Inicialización de Timber para el registro de logs
 * - Configuración de Firebase Remote Config
 * - Configuración de tareas periódicas en segundo plano con WorkManager
 * - Inicialización de recursos críticos de la aplicación
 *
 * Al utilizar la anotación [HiltAndroidApp], esta clase sirve como punto de entrada
 * para el contenedor de dependencias de Hilt a nivel de aplicación.
 *
 * @see SincronizacionWorker Para la implementación de tareas en segundo plano
 * @see RemoteConfigManager Para la gestión de configuraciones remotas
 * @see DebugUtils Utilidades para entornos de desarrollo
 */
@HiltAndroidApp
class UmeEguneroApp : Application() {

    /**
     * Utilidades de depuración inyectadas por Hilt.
     * Se utilizan para realizar operaciones específicas en entornos de desarrollo.
     */
    @Inject
    lateinit var debugUtils: DebugUtils

    /**
     * Método llamado cuando se crea la aplicación.
     *
     * Inicializa los componentes fundamentales de la aplicación:
     * 1. Timber para registro de logs en modo debug
     * 2. Firebase Remote Config para configuraciones remotas
     * 3. Comprueba y crea un administrador en caso de que no exista
     * 4. Configura trabajos periódicos para la sincronización de datos
     */
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber solo en debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Inicializar Remote Config
        RemoteConfigManager.getInstance().initialize(this)

        // Me aseguro de que haya un admin en el sistema
        debugUtils.ensureAdminExists()
        
        // Configurar el trabajo periódico de sincronización
        configurarSincronizacionPeriodica()
    }
    
    /**
     * Configura un trabajo periódico para sincronizar registros pendientes.
     * 
     * Este método utiliza WorkManager para crear una tarea programada que se ejecutará
     * cada 15 minutos cuando el dispositivo tenga conexión a Internet. La tarea
     * es responsable de sincronizar datos locales con el servidor para mantener
     * la coherencia entre el dispositivo y el backend.
     *
     * Restricciones:
     * - Requiere conexión a Internet para ejecutarse
     * - Se programa cada 15 minutos
     * - Reemplaza cualquier trabajo anterior con el mismo nombre
     *
     * @see SincronizacionWorker Implementación del trabajo de sincronización
     * @see WorkManager API de Android para la programación de tareas en segundo plano
     */
    private fun configurarSincronizacionPeriodica() {
        Timber.d("Configurando sincronización periódica")
        
        // Definimos las restricciones: necesitamos conexión a internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Creamos la solicitud de trabajo periódica (cada 15 minutos)
        val sincronizacionRequest = PeriodicWorkRequestBuilder<SincronizacionWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        // Registramos el trabajo, reemplazando cualquier trabajo previo con el mismo nombre
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            SincronizacionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            sincronizacionRequest
        )
    }
} 