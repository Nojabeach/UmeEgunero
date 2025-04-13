package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tfg.umeegunero.data.local.dao.OperacionesPendientesDao
import com.tfg.umeegunero.data.model.OperacionPendiente
import com.tfg.umeegunero.util.NetworkConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar operaciones pendientes de sincronización
 */
@Singleton
class OperacionesPendientesRepository @Inject constructor(
    private val operacionesDao: OperacionesPendientesDao,
    private val firestore: FirebaseFirestore,
    private val networkManager: NetworkConnectivityManager
) {
    companion object {
        const val MAX_INTENTOS = 5
    }

    private val _estadoSincronizacion = MutableStateFlow(EstadoSincronizacion.INACTIVO)
    val estadoSincronizacion: StateFlow<EstadoSincronizacion> = _estadoSincronizacion.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Obtiene todas las operaciones pendientes
     */
    fun obtenerOperacionesPendientes(): Flow<List<OperacionPendiente>> {
        return operacionesDao.obtenerTodasLasOperaciones()
    }
    
    /**
     * Guarda una operación pendiente para sincronizar cuando haya conexión
     */
    suspend fun guardarOperacionPendiente(operacion: OperacionPendiente) {
        withContext(Dispatchers.IO) {
            Timber.d("Guardando operación pendiente: ${operacion.tipo} - ${operacion.tipoEntidad}")
            operacionesDao.insertar(operacion)
            
            // Si hay conexión a internet, intentar sincronizar inmediatamente
            if (networkManager.isCurrentlyConnected()) {
                coroutineScope.launch {
                    sincronizarOperacionesPendientes()
                }
            }
        }
    }
    
    /**
     * Procesa una operación pendiente específica
     */
    suspend fun procesarOperacion(operacion: OperacionPendiente): Boolean {
        return try {
            Timber.d("Procesando operación ${operacion.id}: ${operacion.tipo} - ${operacion.tipoEntidad}")
            
            val rutaColeccion = obtenerRutaColeccion(operacion.tipoEntidad)
            val coleccion = firestore.collection(rutaColeccion)
            
            when (operacion.tipo) {
                OperacionPendiente.Tipo.CREAR -> {
                    val datos = operacion.datos ?: return false
                    coleccion.document(operacion.entidadId).set(datos).await()
                }
                OperacionPendiente.Tipo.ACTUALIZAR -> {
                    val datos = operacion.datos ?: return false
                    coleccion.document(operacion.entidadId).set(datos, SetOptions.merge()).await()
                }
                OperacionPendiente.Tipo.ELIMINAR -> {
                    coleccion.document(operacion.entidadId).delete().await()
                }
                OperacionPendiente.Tipo.FIRMA_DIGITAL,
                OperacionPendiente.Tipo.COMUNICADO,
                OperacionPendiente.Tipo.CONFIRMACION_LECTURA,
                OperacionPendiente.Tipo.ARCHIVO -> {
                    Timber.d("Tipo de operación ${operacion.tipo} gestionado por otro procesador")
                    return true // Estos tipos se gestionan en el SyncRepository
                }
            }
            
            // Si la operación se ejecutó correctamente, eliminarla de la base de datos local
            operacionesDao.eliminar(operacion.id)
            Timber.i("Operación ${operacion.id} procesada con éxito")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar operación ${operacion.id}")
            
            // Incrementar contador de intentos
            val operacionActualizada = operacion.copy(intentos = operacion.intentos + 1)
            operacionesDao.actualizar(operacionActualizada)
            
            false
        }
    }
    
    /**
     * Sincroniza todas las operaciones pendientes cuando hay conexión a internet
     */
    suspend fun sincronizarOperacionesPendientes() {
        if (_estadoSincronizacion.value == EstadoSincronizacion.EN_PROGRESO) {
            Timber.d("Sincronización ya en progreso, saltando...")
            return
        }
        
        if (!networkManager.isCurrentlyConnected()) {
            Timber.d("Sin conexión a internet, sincronización pospuesta")
            return
        }
        
        try {
            _estadoSincronizacion.value = EstadoSincronizacion.EN_PROGRESO
            
            val operaciones = operacionesDao.obtenerOperacionesList()
            Timber.i("Iniciando sincronización de ${operaciones.size} operaciones pendientes")
            
            var operacionesExitosas = 0
            
            for (operacion in operaciones) {
                if (procesarOperacion(operacion)) {
                    operacionesExitosas++
                }
            }
            
            _estadoSincronizacion.value = EstadoSincronizacion.COMPLETADO
            Timber.i("Sincronización completada: $operacionesExitosas de ${operaciones.size} operaciones procesadas con éxito")
        } catch (e: Exception) {
            Timber.e(e, "Error durante la sincronización")
            _estadoSincronizacion.value = EstadoSincronizacion.ERROR
        } finally {
            // Volver al estado inactivo después de un tiempo
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _estadoSincronizacion.value = EstadoSincronizacion.INACTIVO
            }
        }
    }
    
    /**
     * Obtiene la ruta de la colección de Firestore para el tipo de entidad
     */
    private fun obtenerRutaColeccion(tipoEntidad: OperacionPendiente.TipoEntidad): String {
        return when (tipoEntidad) {
            OperacionPendiente.TipoEntidad.COMUNICADO -> "comunicados"
            OperacionPendiente.TipoEntidad.FIRMA_DIGITAL -> "firmas_digitales"
            OperacionPendiente.TipoEntidad.CONFIRMACION_LECTURA -> "confirmaciones_lectura"
            OperacionPendiente.TipoEntidad.USUARIO -> "usuarios"
            OperacionPendiente.TipoEntidad.ARCHIVO -> "archivos"
        }
    }
    
    /**
     * Limpia las operaciones fallidas que han excedido el número máximo de intentos
     */
    suspend fun limpiarOperacionesFallidas() {
        withContext(Dispatchers.IO) {
            val operacionesFallidas = operacionesDao.obtenerOperacionesFallidas(MAX_INTENTOS)
            if (operacionesFallidas.isNotEmpty()) {
                Timber.w("Eliminando ${operacionesFallidas.size} operaciones fallidas")
                operacionesDao.eliminarOperaciones(operacionesFallidas)
            }
        }
    }
    
    /**
     * Obtiene el número total de operaciones pendientes
     */
    suspend fun contarOperacionesPendientes(): Int {
        return withContext(Dispatchers.IO) {
            operacionesDao.contarOperaciones()
        }
    }
}

/**
 * Estados posibles de la sincronización
 */
enum class EstadoSincronizacion {
    INACTIVO,
    EN_PROGRESO,
    COMPLETADO,
    ERROR
} 