package com.tfg.umeegunero.util

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Implementación de paginación eficiente para consultas en Firestore.
 * 
 * Esta clase facilita la realización de consultas paginadas a Firestore,
 * mejorando el rendimiento y reduciendo costos al cargar grandes
 * conjuntos de datos en partes más pequeñas.
 *
 * ## Características:
 * - Carga eficiente de datos en pequeños lotes
 * - Control de tamaño de página
 * - Soporte para ordenamiento y filtrado
 * - Cache de páginas para reducir consultas repetidas
 * - Gestión de límites de cuota de Firestore
 */
class FirestorePagination<T : Any>(
    private val db: FirebaseFirestore,
    private val collectionPath: String,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val orderBy: String? = null,
    private val descending: Boolean = false,
    private val parser: (Map<String, Any>) -> T
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
    
    // Cache de resultados por página
    private val pageCache = mutableMapOf<Int, List<T>>()
    
    // DocumentSnapshot del último elemento de cada página
    private val pageCursors = mutableMapOf<Int, DocumentSnapshot>()
    
    // Filtros actuales
    private var filters: MutableList<QueryFilter> = mutableListOf()
    
    // Total de elementos disponibles (si se conoce)
    var totalItems: Int? = null
        private set
    
    /**
     * Limpia la caché de páginas.
     * Útil cuando cambian los filtros o hay nuevos datos.
     */
    fun clearCache() {
        pageCache.clear()
        pageCursors.clear()
    }
    
    /**
     * Establece o actualiza filtros para la consulta.
     * Al cambiar los filtros, se limpia la caché automáticamente.
     * 
     * @param newFilters Lista de filtros a aplicar
     */
    fun setFilters(newFilters: List<QueryFilter>) {
        if (filters != newFilters) {
            clearCache()
            filters = newFilters.toMutableList()
        }
    }
    
    /**
     * Añade un filtro individual a la consulta.
     * 
     * @param filter Filtro a añadir
     */
    fun addFilter(filter: QueryFilter) {
        filters.add(filter)
        clearCache()
    }
    
    /**
     * Carga una página específica de resultados.
     * 
     * @param pageNumber Número de página a cargar (empezando en 0)
     * @return Lista de elementos de la página solicitada
     */
    suspend fun loadPage(pageNumber: Int): Result<List<T>> {
        // Validar número de página
        if (pageNumber < 0) {
            return Result.Error(IllegalArgumentException("El número de página no puede ser negativo"))
        }
        
        // Verificar si la página ya está en caché
        pageCache[pageNumber]?.let {
            Timber.d("Cargando página $pageNumber desde caché (${it.size} elementos)")
            return Result.Success(it)
        }
        
        // Determinar cómo cargar la página
        return when (pageNumber) {
            0 -> loadFirstPage()
            else -> {
                val previousPage = pageNumber - 1
                // Verificar que tengamos el cursor de la página anterior
                if (!pageCursors.containsKey(previousPage)) {
                    // Cargar página anterior si no tenemos su cursor
                    val previousResult = loadPage(previousPage)
                    if (previousResult is Result.Error) {
                        return previousResult
                    }
                }
                loadNextPage(pageNumber)
            }
        }
    }
    
    /**
     * Carga la primera página de resultados.
     */
    private suspend fun loadFirstPage(): Result<List<T>> {
        Timber.d("Cargando primera página de $collectionPath")
        return try {
            // Construir consulta
            val query = db.collection(collectionPath)
            
            // Aplicar filtros
            val queryWithFilters = applyFilters(query)
            
            // Aplicar ordenamiento si existe
            val queryWithOrdering = applyOrdering(queryWithFilters)
            
            // Limitar resultados al tamaño de página
            val finalQuery = queryWithOrdering.limit(pageSize.toLong())
            
            // Ejecutar consulta
            val querySnapshot = finalQuery.get().await()
            
            // Procesar resultados
            processQueryResults(querySnapshot, 0)
            
            Result.Success(pageCache[0] ?: emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Error cargando primera página de $collectionPath")
            Result.Error(e)
        }
    }
    
    /**
     * Carga la siguiente página de resultados utilizando el cursor de la página anterior.
     * 
     * @param pageNumber Número de página a cargar
     */
    private suspend fun loadNextPage(pageNumber: Int): Result<List<T>> {
        Timber.d("Cargando página $pageNumber de $collectionPath")
        
        val lastVisibleDocument = pageCursors[pageNumber - 1] ?: return Result.Error(
            IllegalStateException("No se encontró el cursor para la página ${pageNumber - 1}")
        )
        
        return try {
            // Construir consulta
            val query = db.collection(collectionPath)
            
            // Aplicar filtros
            val queryWithFilters = applyFilters(query)
            
            // Aplicar ordenamiento
            val queryWithOrdering = applyOrdering(queryWithFilters)
            
            // Empezar después del último documento de la página anterior
            val queryWithPaging = queryWithOrdering.startAfter(lastVisibleDocument)
            
            // Limitar resultados al tamaño de página
            val finalQuery = queryWithPaging.limit(pageSize.toLong())
            
            // Ejecutar consulta
            val querySnapshot = finalQuery.get().await()
            
            // Procesar resultados
            processQueryResults(querySnapshot, pageNumber)
            
            Result.Success(pageCache[pageNumber] ?: emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Error cargando página $pageNumber de $collectionPath")
            Result.Error(e)
        }
    }
    
    /**
     * Procesa los resultados de una consulta y los almacena en caché.
     * 
     * @param querySnapshot Resultados de la consulta
     * @param pageNumber Número de página
     */
    private fun processQueryResults(querySnapshot: QuerySnapshot, pageNumber: Int) {
        val items = mutableListOf<T>()
        
        querySnapshot.documents.forEach { doc ->
            val data = doc.data
            if (data != null) {
                try {
                    // Añadir ID al mapa de datos
                    val dataWithId = data.toMutableMap()
                    dataWithId["id"] = doc.id
                    
                    // Parsear documento a objeto de dominio
                    val item = parser(dataWithId)
                    items.add(item)
                } catch (e: Exception) {
                    Timber.e(e, "Error procesando documento ${doc.id}")
                }
            }
        }
        
        // Guardar resultados en caché
        pageCache[pageNumber] = items
        
        // Guardar cursor para la siguiente página
        if (items.isNotEmpty()) {
            pageCursors[pageNumber] = querySnapshot.documents.last()
        }
        
        Timber.d("Página $pageNumber cargada con ${items.size} elementos")
    }
    
    /**
     * Aplica filtros a una consulta.
     * 
     * @param query Consulta base
     * @return Consulta con filtros aplicados
     */
    private fun applyFilters(query: Query): Query {
        var filteredQuery = query
        
        for (filter in filters) {
            filteredQuery = when (filter.operator) {
                QueryOperator.EQUAL -> filteredQuery.whereEqualTo(filter.field, filter.value)
                QueryOperator.GREATER_THAN -> filteredQuery.whereGreaterThan(filter.field, filter.value)
                QueryOperator.LESS_THAN -> filteredQuery.whereLessThan(filter.field, filter.value)
                QueryOperator.GREATER_THAN_OR_EQUAL -> filteredQuery.whereGreaterThanOrEqualTo(filter.field, filter.value)
                QueryOperator.LESS_THAN_OR_EQUAL -> filteredQuery.whereLessThanOrEqualTo(filter.field, filter.value)
                QueryOperator.ARRAY_CONTAINS -> filteredQuery.whereArrayContains(filter.field, filter.value)
                QueryOperator.IN -> {
                    if (filter.value is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        filteredQuery.whereIn(filter.field, filter.value as List<Any>)
                    } else {
                        filteredQuery
                    }
                }
                QueryOperator.ARRAY_CONTAINS_ANY -> {
                    if (filter.value is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        filteredQuery.whereArrayContainsAny(filter.field, filter.value as List<Any>)
                    } else {
                        filteredQuery
                    }
                }
            }
        }
        
        return filteredQuery
    }
    
    /**
     * Aplica ordenamiento a una consulta si se ha especificado.
     * 
     * @param query Consulta base
     * @return Consulta con ordenamiento aplicado
     */
    private fun applyOrdering(query: Query): Query {
        return if (orderBy != null) {
            if (descending) {
                query.orderBy(orderBy, Query.Direction.DESCENDING)
            } else {
                query.orderBy(orderBy)
            }
        } else {
            query
        }
    }
    
    /**
     * Obtiene el número total de páginas disponibles.
     * Este valor puede no ser exacto si no se conoce el total de elementos.
     * 
     * @return Número estimado de páginas
     */
    fun getPageCount(): Int {
        return totalItems?.let { (it + pageSize - 1) / pageSize } ?: (pageCursors.size + 1)
    }
    
    /**
     * Verifica si hay más páginas disponibles después de la última cargada.
     * 
     * @return true si hay más páginas disponibles
     */
    fun hasMorePages(): Boolean {
        val lastPageNumber = pageCache.keys.maxOrNull() ?: return true
        val lastPage = pageCache[lastPageNumber] ?: return true
        return lastPage.size >= pageSize
    }
}

/**
 * Operadores de comparación disponibles para filtros en Firestore.
 */
enum class QueryOperator {
    EQUAL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL,
    ARRAY_CONTAINS,
    IN,
    ARRAY_CONTAINS_ANY
}

/**
 * Filtro para consultas de Firestore.
 * 
 * @property field Campo sobre el que se aplica el filtro
 * @property operator Operador de comparación
 * @property value Valor a comparar
 */
data class QueryFilter(
    val field: String,
    val operator: QueryOperator,
    val value: Any
) 