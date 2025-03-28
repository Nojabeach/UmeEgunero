package com.tfg.umeegunero.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sistema de caché para reducir las consultas a Firestore
 * Almacena temporalmente los datos para mejorar el rendimiento y reducir el consumo de datos
 */
@Singleton
class FirestoreCache @Inject constructor() {
    
    // Tiempo de expiración de caché por defecto en minutos
    private val defaultExpirationTimeMinutes = 5L
    
    // Estructura para almacenar los datos en caché
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val expirationTimeMillis: Long
    ) {
        fun isExpired(): Boolean {
            val currentTime = System.currentTimeMillis()
            return currentTime - timestamp > expirationTimeMillis
        }
    }
    
    // Mapa para almacenar los datos en caché
    private val cache = mutableMapOf<String, Any>()
    
    // Mutex para operaciones thread-safe
    private val mutex = Mutex()
    
    /**
     * Guarda un objeto en la caché con una clave específica
     * @param key Clave para identificar el objeto en la caché
     * @param data Objeto a almacenar
     * @param expirationMinutes Tiempo de expiración en minutos (opcional)
     */
    suspend fun <T> put(key: String, data: T, expirationMinutes: Long = defaultExpirationTimeMinutes) {
        mutex.withLock {
            val expirationTimeMillis = TimeUnit.MINUTES.toMillis(expirationMinutes)
            val entry = CacheEntry(data, System.currentTimeMillis(), expirationTimeMillis)
            cache[key] = entry as CacheEntry<Any>
            Timber.d("Objeto guardado en caché con clave: $key")
        }
    }
    
    /**
     * Obtiene un objeto de la caché usando su clave
     * @param key Clave del objeto a recuperar
     * @return El objeto almacenado o null si no existe o ha expirado
     */
    suspend fun <T> get(key: String): T? {
        return mutex.withLock {
            val entry = cache[key] as? CacheEntry<T>
            
            if (entry == null) {
                Timber.d("Caché miss: No se encontró objeto con clave $key")
                null
            } else if (entry.isExpired()) {
                Timber.d("Caché expirada para clave $key")
                cache.remove(key)
                null
            } else {
                Timber.d("Caché hit para clave $key")
                entry.data
            }
        }
    }
    
    /**
     * Invalida una entrada específica de la caché
     * @param key Clave de la entrada a invalidar
     */
    suspend fun invalidate(key: String) {
        mutex.withLock {
            cache.remove(key)
            Timber.d("Caché invalidada para clave $key")
        }
    }
    
    /**
     * Invalida todas las entradas de la caché que coincidan con un prefijo
     * @param keyPrefix Prefijo de las claves a invalidar
     */
    suspend fun invalidateByPrefix(keyPrefix: String) {
        mutex.withLock {
            val keysToRemove = cache.keys.filter { it.startsWith(keyPrefix) }
            keysToRemove.forEach { cache.remove(it) }
            Timber.d("Caché invalidada para ${keysToRemove.size} entradas con prefijo $keyPrefix")
        }
    }
    
    /**
     * Elimina todas las entradas de la caché
     */
    suspend fun clearAll() {
        mutex.withLock {
            val size = cache.size
            cache.clear()
            Timber.d("Caché completamente limpiada, $size entradas eliminadas")
        }
    }
} 