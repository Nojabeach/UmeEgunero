package com.tfg.umeegunero.data.repository

import android.content.Context
import com.google.gson.Gson
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.CodigoPostalData
import com.tfg.umeegunero.data.model.toCiudad
import com.tfg.umeegunero.data.network.GeoApiRetrofitClient
import com.tfg.umeegunero.data.network.CodigoPostalGeoData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Repositorio para gestionar datos de ciudades y códigos postales.
 * 
 * Fuente de datos: GeoAPI España (https://geoapi.es/)
 * Se utiliza el modo sandbox para pruebas.
 */
@Singleton
class CiudadRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Caché de provincias
    private var provinciasCache: List<String>? = null
    
    // Caché de códigos postales por provincia
    private val codigosPostalesPorProvinciaCache = mutableMapOf<String, List<CodigoPostalGeoData>>()
    
    // Caché de búsquedas de códigos postales
    private val busquedasCodigosPostalesCache = mutableMapOf<String, List<Ciudad>>()
    
    // Indica si estamos usando datos de muestra o datos reales
    private var usandoDatosDeMuestra = false

    /**
     * Busca ciudades por código postal utilizando la API de GeoAPI España
     */
    fun buscarCiudadesPorCodigoPostal(codigoPostal: String, callback: (List<Ciudad>?, String?) -> Unit) {
        // Validar que el código postal tenga 5 dígitos
        if (!codigoPostal.matches(Regex("^\\d{5}$"))) {
            callback(null, "El código postal debe tener 5 dígitos")
            return
        }

        try {
            // Comprobar si ya tenemos esta búsqueda en caché
            if (busquedasCodigosPostalesCache.containsKey(codigoPostal)) {
                val ciudadesEnCache = busquedasCodigosPostalesCache[codigoPostal]
                if (ciudadesEnCache != null && ciudadesEnCache.isNotEmpty()) {
                    callback(ciudadesEnCache, null)
                    return
                }
            }
            
            // Realizar la búsqueda en una coroutine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Obtener el código de provincia (primeros dos dígitos del CP)
                    val codigoProvincia = codigoPostal.substring(0, 2)
                    
                    // Intentar obtener los códigos postales de la API
                    val ciudades = buscarCiudadesEnGeoApi(codigoPostal, codigoProvincia)
                    
                    withContext(Dispatchers.Main) {
                        if (ciudades.isNotEmpty()) {
                            // Guardar en caché
                            busquedasCodigosPostalesCache[codigoPostal] = ciudades
                            callback(ciudades, null)
                        } else {
                            // Si no hay resultados, intentar buscar por provincia
                            CoroutineScope(Dispatchers.IO).launch {
                                val ciudadesPorProvincia = buscarCiudadesPorProvincia(codigoProvincia)
                                
                                withContext(Dispatchers.Main) {
                                    if (ciudadesPorProvincia.isNotEmpty()) {
                                        callback(ciudadesPorProvincia, "No se encontró el código postal exacto. Mostrando resultados de la provincia.")
                                    } else {
                                        callback(null, "No se encontraron resultados. Introduce manualmente la ciudad y provincia.")
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al buscar ciudades por código postal en GeoAPI")
                    withContext(Dispatchers.Main) {
                        callback(null, "Error al buscar ciudades. Introduce manualmente la ciudad y provincia.")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al iniciar búsqueda de ciudades por código postal")
            callback(null, "Error al buscar ciudades. Introduce manualmente la ciudad y provincia.")
        }
    }
    
    /**
     * Busca ciudades en la API de GeoAPI España por código postal
     */
    private suspend fun buscarCiudadesEnGeoApi(codigoPostal: String, codigoProvincia: String): List<Ciudad> {
        return withContext(Dispatchers.IO) {
            try {
                // Intentar obtener los códigos postales de la API
                val response = GeoApiRetrofitClient.geoApiService.getCodigosPostales(
                    codigoProvincia = codigoProvincia
                )
                
                if (response.isSuccessful) {
                    val codigosPostalesData = response.body()?.data ?: emptyList()
                    
                    // Filtrar por el código postal exacto
                    val codigosPostalesFiltrados = codigosPostalesData.filter { 
                        it.codigoPostal == codigoPostal 
                    }
                    
                    // Convertir a Ciudad
                    return@withContext codigosPostalesFiltrados.map { 
                        Ciudad(
                            nombre = it.municipio,
                            codigoPostal = it.codigoPostal,
                            provincia = it.provincia,
                            codigoProvincia = it.codigoProvincia
                        )
                    }.distinctBy { it.nombre }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener códigos postales de GeoAPI")
            }
            
            return@withContext emptyList()
        }
    }
    
    /**
     * Busca ciudades por provincia
     */
    private suspend fun buscarCiudadesPorProvincia(codigoProvincia: String): List<Ciudad> {
        return withContext(Dispatchers.IO) {
            try {
                // Comprobar si ya tenemos los códigos postales de esta provincia en caché
                if (codigosPostalesPorProvinciaCache.containsKey(codigoProvincia)) {
                    return@withContext codigosPostalesPorProvinciaCache[codigoProvincia]?.map { 
                        Ciudad(
                            nombre = it.municipio,
                            codigoPostal = it.codigoPostal,
                            provincia = it.provincia,
                            codigoProvincia = it.codigoProvincia
                        )
                    }?.distinctBy { it.nombre }?.take(5) ?: emptyList()
                }
                
                // Obtener los códigos postales de la provincia
                val response = GeoApiRetrofitClient.geoApiService.getCodigosPostales(
                    codigoProvincia = codigoProvincia
                )
                
                if (response.isSuccessful) {
                    val codigosPostalesData = response.body()?.data ?: emptyList()
                    
                    // Guardar en caché
                    codigosPostalesPorProvinciaCache[codigoProvincia] = codigosPostalesData
                    
                    // Convertir a Ciudad y devolver los primeros 5 resultados
                    return@withContext codigosPostalesData
                        .map { 
                            Ciudad(
                                nombre = it.municipio,
                                codigoPostal = it.codigoPostal,
                                provincia = it.provincia,
                                codigoProvincia = it.codigoProvincia
                            ) 
                        }
                        .distinctBy { it.nombre }
                        .take(5)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener ciudades por provincia de GeoAPI")
            }
            
            return@withContext emptyList()
        }
    }
    
    /**
     * Obtiene la lista de provincias disponibles
     */
    suspend fun obtenerProvincias(): List<String> {
        return withContext(Dispatchers.IO) {
            // Si ya tenemos las provincias en caché, devolverlas
            if (provinciasCache != null) {
                return@withContext provinciasCache!!
            }
            
            try {
                // Obtener las provincias de la API
                val response = GeoApiRetrofitClient.geoApiService.getProvincias()
                
                if (response.isSuccessful) {
                    val provinciasData = response.body()?.data ?: emptyList()
                    val provincias = provinciasData.map { it.nombre }.sorted()
                    
                    // Guardar en caché
                    provinciasCache = provincias
                    
                    return@withContext provincias
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener provincias de GeoAPI")
            }
            
            // Si hay un error, devolver una lista de provincias de ejemplo
            return@withContext usarProvinciasEjemplo()
        }
    }
    
    /**
     * Obtiene la lista de provincias de ejemplo como fallback
     */
    private fun usarProvinciasEjemplo(): List<String> {
        usandoDatosDeMuestra = true
        
        val provinciasEjemplo = listOf(
            "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", 
            "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", 
            "Córdoba", "Cuenca", "Girona", "Granada", "Guadalajara", "Guipúzcoa", "Huelva", 
            "Huesca", "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas", "León", 
            "Lleida", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Orense", "Palencia", 
            "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", 
            "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
        )
        
        // Guardar en caché
        provinciasCache = provinciasEjemplo
        
        Timber.d("Usando provincias de ejemplo (fallback)")
        return provinciasEjemplo
    }
    
    /**
     * Busca códigos postales por provincia de forma asíncrona
     */
    suspend fun buscarCodigosPostalesPorProvincia(provincia: String): List<CodigoPostalData> {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener todas las provincias para encontrar el código
                val provinciasResponse = GeoApiRetrofitClient.geoApiService.getProvincias()
                
                if (provinciasResponse.isSuccessful) {
                    val provinciasData = provinciasResponse.body()?.data ?: emptyList()
                    val provinciaData = provinciasData.find { it.nombre.equals(provincia, ignoreCase = true) }
                    
                    if (provinciaData != null) {
                        // Obtener los códigos postales de la provincia
                        val codigosPostalesResponse = GeoApiRetrofitClient.geoApiService.getCodigosPostales(
                            codigoProvincia = provinciaData.codigo
                        )
                        
                        if (codigosPostalesResponse.isSuccessful) {
                            val codigosPostalesData = codigosPostalesResponse.body()?.data ?: emptyList()
                            
                            // Convertir a CodigoPostalData
                            return@withContext codigosPostalesData.map { 
                                CodigoPostalData(
                                    codigoPostal = it.codigoPostal,
                                    municipio = it.municipio,
                                    provincia = it.provincia,
                                    codigoProvincia = it.codigoProvincia
                                )
                            }
                        }
                    }
                }
                
                emptyList<CodigoPostalData>()
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar códigos postales por provincia")
                emptyList<CodigoPostalData>()
            }
        }
    }
    
    /**
     * Limpia las cachés
     */
    fun limpiarCaches() {
        provinciasCache = null
        codigosPostalesPorProvinciaCache.clear()
        busquedasCodigosPostalesCache.clear()
    }
}