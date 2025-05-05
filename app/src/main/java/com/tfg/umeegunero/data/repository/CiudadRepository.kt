package com.tfg.umeegunero.data.repository

import android.content.Context
import com.google.gson.Gson
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.CodigoPostalData
import com.tfg.umeegunero.data.model.toCiudad
import com.tfg.umeegunero.data.network.NominatimRetrofitClient
import com.tfg.umeegunero.data.network.NominatimPlace
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
 * Repositorio para gestionar información de ciudades en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para recuperar, almacenar y gestionar
 * información geográfica de ciudades, facilitando la gestión de direcciones,
 * ubicaciones de centros educativos y datos de contexto geográfico.
 *
 * Características principales:
 * - Recuperación de información de ciudades
 * - Búsqueda y filtrado de ciudades
 * - Integración con servicios de geolocalización
 * - Soporte para datos geográficos detallados
 * - Caching de información de ciudades
 *
 * El repositorio permite:
 * - Obtener información detallada de ciudades
 * - Buscar ciudades por diferentes criterios
 * - Autocompletar direcciones
 * - Validar información geográfica
 * - Enriquecer datos de ubicación
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property nominatimService Servicio de geolocalización para obtener datos detallados
 * @property networkManager Gestor de conectividad de red
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class CiudadRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Caché de provincias
    private var provinciasCache: List<String>? = null
    
    // Caché de búsquedas de códigos postales
    private val busquedasCodigosPostalesCache = mutableMapOf<String, List<Ciudad>>()
    
    // Indica si estamos usando datos de muestra o datos reales
    private var usandoDatosDeMuestra = false

    /**
     * Busca ciudades por código postal utilizando la API de Nominatim
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
                    // Buscar lugares por código postal en Nominatim
                    val ciudades = buscarCiudadesEnNominatim(codigoPostal)
                    
                    withContext(Dispatchers.Main) {
                        if (ciudades.isNotEmpty()) {
                            // Guardar en caché
                            busquedasCodigosPostalesCache[codigoPostal] = ciudades
                            callback(ciudades, null)
                        } else {
                            callback(null, "No se encontraron resultados. Introduce manualmente la ciudad y provincia.")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al buscar ciudades por código postal en Nominatim")
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
     * Busca ciudades en la API de Nominatim por código postal
     */
    private suspend fun buscarCiudadesEnNominatim(codigoPostal: String): List<Ciudad> {
        return withContext(Dispatchers.IO) {
            try {
                // Intentar obtener los lugares por código postal
                val response = NominatimRetrofitClient.nominatimApiService.searchByPostalCode(
                    postalCode = codigoPostal,
                    limit = 10
                )
                
                if (response.isSuccessful) {
                    val places = response.body() ?: emptyList()
                    
                    if (places.isNotEmpty()) {
                        Timber.d("Encontrados ${places.size} lugares para CP $codigoPostal en Nominatim")
                        
                        // Convertir a Ciudad
                        return@withContext places.mapNotNull { place -> 
                            place.address?.let { address ->
                                val cityName = address.getCityName() ?: return@let null
                                
                                // Nominatim usa "state" para la comunidad autónoma y "province" para la provincia
                                // En España, necesitamos mapear esto correctamente
                                val provincia = when {
                                    // Si tenemos province, usamos esa
                                    address.province != null && address.province.isNotBlank() -> address.province
                                    // Si no, intentamos con state (comunidad autónoma)
                                    address.state != null && address.state.isNotBlank() -> address.state
                                    // Si no tenemos ninguna, usamos un valor por defecto
                                    else -> "Desconocida"
                                }
                                
                                // Para el código de provincia, usamos county si está disponible
                                val codigoProvincia = address.county ?: ""
                                
                                Ciudad(
                                    nombre = cityName,
                                    codigoPostal = address.postcode ?: codigoPostal,
                                    provincia = provincia,
                                    codigoProvincia = codigoProvincia
                                )
                            }
                        }.distinctBy { it.nombre }
                    }
                } else {
                    // Registrar el error de la API
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Timber.e("Error en la respuesta de Nominatim: $errorBody")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener lugares de Nominatim para CP: $codigoPostal")
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
            
            // Nominatim no proporciona un endpoint directo para obtener todas las provincias
            // Por lo que usamos una lista predefinida
            return@withContext usarProvinciasEjemplo()
        }
    }
    
    /**
     * Obtiene la lista de provincias de España
     */
    private fun usarProvinciasEjemplo(): List<String> {
        val provinciasEspana = listOf(
            "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", 
            "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", 
            "Córdoba", "Cuenca", "Girona", "Granada", "Guadalajara", "Guipúzcoa", "Huelva", 
            "Huesca", "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas", "León", 
            "Lleida", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Orense", "Palencia", 
            "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", 
            "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
        )
        
        // Guardar en caché
        provinciasCache = provinciasEspana
        
        Timber.d("Usando lista predefinida de provincias de España")
        return provinciasEspana
    }
    
    /**
     * Busca códigos postales por provincia de forma asíncrona
     */
    suspend fun buscarCodigosPostalesPorProvincia(provincia: String): List<CodigoPostalData> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar lugares en la provincia especificada
                val response = NominatimRetrofitClient.nominatimApiService.search(
                    query = "$provincia, España",
                    limit = 20
                )
                
                if (response.isSuccessful) {
                    val places = response.body() ?: emptyList()
                    
                    // Filtrar lugares que tengan código postal y pertenezcan a la provincia
                    val codigosPostales = places
                        .filter { place -> 
                            val address = place.address
                            // Verificar que el lugar pertenece a la provincia buscada
                            ((address?.province != null && address.province == provincia) || 
                             (address?.state != null && address.state == provincia)) && 
                            // Y que tiene código postal
                            address?.postcode != null && address.postcode.isNotBlank()
                        }
                        .map { place ->
                            val address = place.address!!
                            CodigoPostalData(
                                codigoPostal = address.postcode ?: "",
                                municipio = address.getCityName() ?: place.displayName,
                                provincia = provincia,
                                codigoProvincia = address.county ?: ""
                            )
                        }
                        .distinctBy { it.codigoPostal }
                    
                    if (codigosPostales.isNotEmpty()) {
                        return@withContext codigosPostales
                    }
                }
                
                // Si no encontramos resultados, intentamos una búsqueda más general
                val fallbackResponse = NominatimRetrofitClient.nominatimApiService.search(
                    query = "postcode:$provincia",
                    limit = 10
                )
                
                if (fallbackResponse.isSuccessful) {
                    val places = fallbackResponse.body() ?: emptyList()
                    return@withContext places
                        .filter { it.address?.postcode != null }
                        .map { place ->
                            val address = place.address!!
                            CodigoPostalData(
                                codigoPostal = address.postcode ?: "",
                                municipio = address.getCityName() ?: place.displayName,
                                provincia = provincia,
                                codigoProvincia = address.county ?: ""
                            )
                        }
                        .distinctBy { it.codigoPostal }
                }
                
                // Si no encontramos nada, devolvemos una lista vacía
                emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar códigos postales por provincia: $provincia")
                emptyList()
            }
        }
    }
    
    /**
     * Limpia las cachés
     */
    fun limpiarCaches() {
        provinciasCache = null
        busquedasCodigosPostalesCache.clear()
    }
}