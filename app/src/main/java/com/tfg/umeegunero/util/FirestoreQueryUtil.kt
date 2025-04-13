package com.tfg.umeegunero.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para optimizar consultas a Firestore
 * Implementa técnicas de caché y consultas eficientes
 */
@Singleton
class FirestoreQueryUtil @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        // Tiempo de expiración de caché en milisegundos (5 minutos)
        private const val CACHE_EXPIRATION = 5 * 60 * 1000L
    }
    
    // Mapa para almacenar resultados en caché
    private val queryCache = mutableMapOf<String, CacheEntry>()
    
    /**
     * Entrada de caché con timestamp de expiración
     */
    private data class CacheEntry(
        val data: QuerySnapshot,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRATION
        }
    }
    
    /**
     * Obtiene datos de Firestore con soporte para caché
     * 
     * @param query Query de Firestore a ejecutar
     * @param forceRefresh Si es true, ignora la caché y fuerza una consulta al servidor
     * @return QuerySnapshot con los resultados
     */
    suspend fun getQueryWithCache(
        query: Query,
        forceRefresh: Boolean = false
    ): QuerySnapshot {
        val cacheKey = generateCacheKey(query)
        
        // Verificar si hay datos en caché válidos
        if (!forceRefresh) {
            val cachedEntry = queryCache[cacheKey]
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                Timber.d("Usando datos en caché para query: $cacheKey")
                return cachedEntry.data
            }
        }
        
        // Si no hay caché o está expirada, consultar al servidor
        return try {
            val snapshot = query.get(Source.SERVER).await()
            
            // Almacenar en caché
            queryCache[cacheKey] = CacheEntry(snapshot)
            
            snapshot
        } catch (e: Exception) {
            Timber.e(e, "Error al ejecutar query: $cacheKey")
            
            // Si hay error y tenemos caché (aunque esté expirada), usarla como fallback
            val cachedEntry = queryCache[cacheKey]
            if (cachedEntry != null) {
                Timber.d("Usando caché expirada como fallback para query: $cacheKey")
                return cachedEntry.data
            }
            
            throw e
        }
    }
    
    /**
     * Limpia la caché para una query específica
     */
    fun invalidateCache(query: Query) {
        val cacheKey = generateCacheKey(query)
        queryCache.remove(cacheKey)
        Timber.d("Caché invalidada para query: $cacheKey")
    }
    
    /**
     * Limpia toda la caché
     */
    fun clearCache() {
        queryCache.clear()
        Timber.d("Caché completa limpiada")
    }
    
    /**
     * Genera una clave única para la query
     */
    private fun generateCacheKey(query: Query): String {
        // Extraer información relevante de la query para generar una clave única
        // Utilizamos el hashCode como identificador único y reproducible
        return query.toString().hashCode().toString()
    }
    
    /**
     * Optimiza una consulta para obtener comunicados con paginación
     * 
     * @param limit Número máximo de documentos a obtener
     * @param lastDocument Último documento de la página anterior (para paginación)
     * @return Query optimizada
     */
    fun getOptimizedComunicadosQuery(
        limit: Long = 20,
        lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    ): Query {
        var query = firestore.collection("comunicados")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(limit)
        
        // Aplicar paginación si hay un último documento
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }
        
        return query
    }
    
    /**
     * Optimiza una consulta para obtener comunicados por tipo de usuario
     * 
     * @param tipoUsuario Tipo de usuario para filtrar
     * @param limit Número máximo de documentos a obtener
     * @return Query optimizada
     */
    fun getComunicadosByTipoUsuarioQuery(
        tipoUsuario: String,
        limit: Long = 50
    ): Query {
        return firestore.collection("comunicados")
            .whereEqualTo("tipoUsuario", tipoUsuario)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(limit)
    }
    
    /**
     * Optimiza una consulta para obtener comunicados que requieren firma
     * 
     * @param usuarioId ID del usuario
     * @param limit Número máximo de documentos a obtener
     * @return Query optimizada
     */
    fun getComunicadosRequierenFirmaQuery(
        usuarioId: String,
        limit: Long = 20
    ): Query {
        return firestore.collection("comunicados")
            .whereEqualTo("requiereFirmaDestinatario", true)
            .whereNotIn("firmasDestinatarios.$usuarioId", listOf(true))
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(limit)
    }
} 