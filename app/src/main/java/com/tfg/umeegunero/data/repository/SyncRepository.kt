package com.tfg.umeegunero.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tfg.umeegunero.data.dao.OperacionPendienteDao
import com.tfg.umeegunero.data.model.OperacionPendiente
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.util.NetworkConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de gestionar la sincronización de operaciones pendientes
 * cuando hay conectividad a internet.
 */
@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val operacionPendienteDao: OperacionPendienteDao,
    private val comunicadoRepository: ComunicadoRepository,
    private val firestore: FirebaseFirestore
) {
    
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _pendingOperationsCount = MutableStateFlow(0)
    val pendingOperationsCount: StateFlow<Int> = _pendingOperationsCount.asStateFlow()
    
    private val networkConnectivityManager = NetworkConnectivityManager(context)
    
    val pendingOperations: Flow<List<OperacionPendiente>> = operacionPendienteDao.obtenerTodasLasOperacionesPendientes()
    
    init {
        // Iniciar monitoreo de red
        networkConnectivityManager.startNetworkMonitoring()
        
        // Subscribirse a cambios de conectividad
        CoroutineScope(Dispatchers.IO).launch {
            networkConnectivityManager.isNetworkAvailable.collect { isAvailable ->
                _isOnline.value = isAvailable
                if (isAvailable) {
                    procesarOperacionesPendientes()
                }
            }
        }
        
        // Iniciar la cuenta de operaciones pendientes
        CoroutineScope(Dispatchers.IO).launch {
            actualizarContadorOperacionesPendientes()
        }
    }
    
    /**
     * Añade una operación pendiente a la base de datos local
     */
    suspend fun addOperacionPendiente(operacion: OperacionPendiente) {
        withContext(Dispatchers.IO) {
            operacionPendienteDao.insertar(operacion)
            actualizarContadorOperacionesPendientes()
        }
    }
    
    /**
     * Elimina una operación pendiente de la base de datos local
     */
    suspend fun removeOperacionPendiente(operacionId: String) {
        withContext(Dispatchers.IO) {
            operacionPendienteDao.eliminarPorId(operacionId)
            actualizarContadorOperacionesPendientes()
        }
    }
    
    /**
     * Procesa las operaciones pendientes cuando hay conexión a internet
     */
    suspend fun procesarOperacionesPendientes() {
        if (!_isOnline.value) {
            Timber.d("No hay conexión a internet, no se pueden procesar operaciones pendientes")
            return
        }
        
        _syncState.value = SyncState.SYNCING
        
        try {
            val operacionesPendientes = pendingOperations.first()
            Timber.d("Procesando ${operacionesPendientes.size} operaciones pendientes")
            
            operacionesPendientes.forEach { operacion ->
                try {
                    // Crear una copia modificable
                    val operacionActualizada = operacion.copy(
                        estado = OperacionPendiente.Estado.EN_PROCESO
                    )
                    operacionPendienteDao.actualizar(operacionActualizada)
                    
                    procesarOperacion(operacion)
                    
                    // Marcar como completada
                    val operacionCompletada = operacion.copy(
                        estado = OperacionPendiente.Estado.COMPLETADA
                    )
                    operacionPendienteDao.actualizar(operacionCompletada)
                    removeOperacionPendiente(operacion.id)
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error al procesar la operación pendiente ${operacion.id}")
                    // Marcar como error
                    val operacionError = operacion.copy(
                        estado = OperacionPendiente.Estado.ERROR,
                        mensajeError = e.message ?: "Error desconocido",
                        intentos = operacion.intentos + 1
                    )
                    operacionPendienteDao.actualizar(operacionError)
                }
            }
            
            _syncState.value = SyncState.SUCCESS
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar operaciones pendientes")
            _syncState.value = SyncState.ERROR
        }
    }
    
    /**
     * Procesa una operación individual según su tipo
     */
    private suspend fun procesarOperacion(operacion: OperacionPendiente) {
        when (operacion.tipo) {
            OperacionPendiente.Tipo.FIRMA_DIGITAL -> procesarOperacionFirmaDigital(operacion)
            OperacionPendiente.Tipo.COMUNICADO -> procesarOperacionComunicado(operacion)
            OperacionPendiente.Tipo.CONFIRMACION_LECTURA -> procesarOperacionConfirmacionLectura(operacion)
            OperacionPendiente.Tipo.ARCHIVO -> procesarOperacionArchivo(operacion)
            else -> {
                Timber.w("Tipo de operación no implementado: ${operacion.tipo}")
            }
        }
    }
    
    /**
     * Procesa una operación de firma digital
     */
    private suspend fun procesarOperacionFirmaDigital(operacion: OperacionPendiente) {
        val datos = operacion.datos
        
        val comunicadoId = datos["comunicadoId"] as? String ?: return
        val firmaBase64 = datos["firmaBase64"] as? String ?: return
        val usuarioId = datos["usuarioId"] as? String ?: return
        val timestamp = (datos["timestamp"] as? Long) ?: System.currentTimeMillis()
        
        // Guardar la firma en Firebase Storage y obtener la URL
        try {
            val resultado = comunicadoRepository.añadirFirmaDigital(
                comunicadoId = comunicadoId,
                firmaBase64 = firmaBase64,
                usuarioId = usuarioId,
                timestamp = timestamp
            )
            
            if (resultado is Resultado.Error) {
                throw Exception("Error al guardar la firma digital: ${resultado.mensaje ?: "Error desconocido"}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar operación de firma digital")
            throw e
        }
    }
    
    /**
     * Procesa una operación de comunicado
     */
    private suspend fun procesarOperacionComunicado(operacion: OperacionPendiente) {
        val datos = operacion.datos
        
        when (datos["accion"] as? String) {
            "crear" -> {
                // Implementar la creación de un comunicado
            }
            "actualizar" -> {
                // Implementar la actualización de un comunicado
            }
            "eliminar" -> {
                val comunicadoId = datos["comunicadoId"] as? String ?: return
                comunicadoRepository.eliminarComunicado(comunicadoId)
            }
            else -> {
                Timber.w("Acción de comunicado no implementada: ${datos["accion"]}")
            }
        }
    }
    
    /**
     * Procesa una operación de confirmación de lectura
     */
    private suspend fun procesarOperacionConfirmacionLectura(operacion: OperacionPendiente) {
        val datos = operacion.datos
        
        val comunicadoId = datos["comunicadoId"] as? String ?: return
        val usuarioId = datos["usuarioId"] as? String ?: return
        
        comunicadoRepository.confirmarLectura(comunicadoId, usuarioId)
    }
    
    /**
     * Procesa una operación de archivo
     */
    private suspend fun procesarOperacionArchivo(operacion: OperacionPendiente) {
        // Implementar procesamiento de operaciones de archivo
        Timber.d("Procesando operación de archivo: ${operacion.datos}")
    }
    
    /**
     * Actualiza el contador de operaciones pendientes
     */
    private suspend fun actualizarContadorOperacionesPendientes() {
        val count = operacionPendienteDao.contarOperacionesPendientes()
        _pendingOperationsCount.value = count
    }
    
    /**
     * Obtiene todas las operaciones pendientes
     */
    fun obtenerOperacionesPendientes(): Flow<List<OperacionPendiente>> {
        return pendingOperations
    }
    
    /**
     * Estados de sincronización
     */
    enum class SyncState {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR
    }

    /**
     * Constantes para la configuración
     */
    companion object {
        const val MAX_RETRY_COUNT = 5
    }

    /**
     * Elimina las operaciones que han fallado demasiadas veces
     */
    suspend fun limpiarOperacionesFallidas() = withContext(Dispatchers.IO) {
        val operacionesFallidas = operacionPendienteDao.obtenerOperacionesFallidas(MAX_RETRY_COUNT)
        for (operacion in operacionesFallidas) {
            Timber.w("Eliminando operación fallida: ${operacion.id} - Tipo: ${operacion.tipo}")
            operacionPendienteDao.eliminarPorId(operacion.id)
        }
    }
    
    /**
     * Obtiene el número total de operaciones pendientes
     */
    suspend fun obtenerNumeroOperacionesPendientes(): Int = withContext(Dispatchers.IO) {
        return@withContext operacionPendienteDao.contarOperacionesPendientes()
    }
} 