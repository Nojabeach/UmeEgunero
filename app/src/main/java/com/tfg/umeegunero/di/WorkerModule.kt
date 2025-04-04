package com.tfg.umeegunero.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.tfg.umeegunero.data.worker.EventoWorker
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Interfaz para factories de workers que utilizan inyección de dependencias
 */
interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): CoroutineWorker
}

/**
 * Anotación para el mapeo de workers a sus factories
 */
@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

/**
 * Factory personalizada para crear workers con inyección de dependencias
 */
@Singleton
class CustomWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards ChildWorkerFactory>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val workerClass = try {
            Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        } catch (e: ClassNotFoundException) {
            return null
        }

        val factory = workerFactories[workerClass] ?: return null
        return factory.create(appContext, workerParameters)
    }
}

/**
 * Módulo Hilt para proporcionar factories de workers
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(SincronizacionWorker::class)
    abstract fun bindSincronizacionWorkerFactory(factory: SincronizacionWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(EventoWorker::class)
    abstract fun bindEventoWorkerFactory(factory: EventoWorker.Factory): ChildWorkerFactory

    @Binds
    abstract fun bindWorkerFactory(factory: CustomWorkerFactory): WorkerFactory
    
    companion object {
        /**
         * Proporciona la configuración de WorkManager
         */
        @Provides
        @Singleton
        fun provideWorkManagerConfiguration(
            workerFactory: WorkerFactory
        ): Configuration {
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        }
        
        /**
         * Inicializa WorkManager con la configuración personalizada
         */
        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context,
            configuration: Configuration
        ): WorkManager {
            // Inicializar WorkManager con nuestra configuración
            WorkManager.initialize(context, configuration)
            return WorkManager.getInstance(context)
        }
    }
} 