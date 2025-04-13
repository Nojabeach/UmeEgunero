package com.tfg.umeegunero.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.tfg.umeegunero.data.model.OperacionPendiente
import com.tfg.umeegunero.data.repository.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para gestionar la sincronización de datos cuando la aplicación está sin conexión
 */
@Singleton
class OfflineSyncUtil @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val syncRepository: SyncRepository
) {
    /**
     * Verifica si hay conexión a Internet
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Registra una operación pendiente de sincronización para un documento
     * 
     * @param tipoEntidad Tipo de entidad (COMUNICADO, FIRMA_DIGITAL, etc.)
     * @param entidadId ID de la entidad
     * @param tipoOperacion Tipo de operación (CREAR, ACTUALIZAR, ELIMINAR)
     * @param datos Datos adicionales para la operación
     */
    suspend fun registrarOperacionPendiente(
        tipoEntidad: OperacionPendiente.TipoEntidad,
        entidadId: String,
        tipoOperacion: OperacionPendiente.Tipo,
        datos: Map<String, Any?> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val operacion = OperacionPendiente(
            tipo = tipoOperacion,
            tipoEntidad = tipoEntidad,
            entidadId = entidadId,
            datos = datos
        )
        
        syncRepository.addOperacionPendiente(operacion)
        Timber.d("Operación pendiente registrada: $tipoOperacion para $tipoEntidad $entidadId")
    }
    
    /**
     * Sincroniza las operaciones pendientes con Firestore
     * 
     * @param scope CoroutineScope para ejecutar la sincronización
     */
    fun sincronizarOperacionesPendientes(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            if (!isNetworkAvailable()) {
                Timber.d("No hay conexión a Internet, no se pueden sincronizar operaciones pendientes")
                return@launch
            }
            
            try {
                syncRepository.procesarOperacionesPendientes()
            } catch (e: Exception) {
                Timber.e(e, "Error al sincronizar operaciones pendientes")
            }
        }
    }
    
    /**
     * Sincroniza un documento específico con Firestore
     */
    suspend fun sincronizarDocumento(
        coleccion: String,
        documentoId: String,
        datos: Map<String, Any>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isNetworkAvailable()) {
                // Sincronizar directamente con Firestore
                firestore.collection(coleccion)
                    .document(documentoId)
                    .set(datos)
                    .await()
                
                Timber.d("Documento sincronizado directamente: $coleccion/$documentoId")
                return@withContext true
            } else {
                // Registrar operación pendiente
                val operacion = OperacionPendiente(
                    tipo = OperacionPendiente.Tipo.CREAR,
                    tipoEntidad = when (coleccion) {
                        "comunicados" -> OperacionPendiente.TipoEntidad.COMUNICADO
                        "usuarios" -> OperacionPendiente.TipoEntidad.USUARIO
                        else -> OperacionPendiente.TipoEntidad.ARCHIVO
                    },
                    entidadId = documentoId,
                    datos = mapOf(
                        "coleccion" to coleccion,
                        "documentoId" to documentoId,
                        "datos" to datos
                    )
                )
                
                syncRepository.addOperacionPendiente(operacion)
                Timber.d("Documento guardado para sincronización posterior: $coleccion/$documentoId")
                return@withContext false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar documento: $coleccion/$documentoId")
            return@withContext false
        }
    }
    
    /**
     * Tipos de operaciones de sincronización
     */
    enum class TipoOperacion {
        CREAR, ACTUALIZAR, ELIMINAR
    }
} 