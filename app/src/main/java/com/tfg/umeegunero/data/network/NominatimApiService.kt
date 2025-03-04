package com.tfg.umeegunero.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio para acceder a la API de Nominatim
 * Documentación: https://nominatim.org/release-docs/develop/api/
 */
interface NominatimApiService {
    /**
     * Busca lugares por texto libre (dirección, ciudad, código postal, etc.)
     * @param q Texto de búsqueda (dirección, ciudad, código postal, etc.)
     * @param format Formato de respuesta (json, jsonv2, xml, etc.)
     * @param addressdetails Si se incluyen detalles de la dirección (0 o 1)
     * @param limit Número máximo de resultados
     * @param countrycodes Códigos de país para filtrar (es, fr, etc.)
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("countrycodes") countryCodes: String = "es",
        @Query("accept-language") acceptLanguage: String = "es"
    ): Response<List<NominatimPlace>>
    
    /**
     * Busca lugares por código postal
     * @param postalcode Código postal
     * @param format Formato de respuesta (json, jsonv2, xml, etc.)
     * @param addressdetails Si se incluyen detalles de la dirección (0 o 1)
     * @param limit Número máximo de resultados
     * @param countrycodes Códigos de país para filtrar (es, fr, etc.)
     */
    @GET("search")
    suspend fun searchByPostalCode(
        @Query("postalcode") postalCode: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("countrycodes") countryCodes: String = "es",
        @Query("accept-language") acceptLanguage: String = "es"
    ): Response<List<NominatimPlace>>
    
    /**
     * Búsqueda inversa (coordenadas a dirección)
     * @param lat Latitud
     * @param lon Longitud
     * @param format Formato de respuesta (json, jsonv2, xml, etc.)
     * @param addressdetails Si se incluyen detalles de la dirección (0 o 1)
     */
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") acceptLanguage: String = "es"
    ): Response<NominatimPlace>
    
    /**
     * Busca detalles de objetos OSM (nodos, vías, relaciones)
     * @param osmIds Lista de IDs de OSM con prefijo (N para nodos, W para vías, R para relaciones)
     * @param format Formato de respuesta (json, jsonv2, xml, etc.)
     * @param addressdetails Si se incluyen detalles de la dirección (0 o 1)
     */
    @GET("lookup")
    suspend fun lookup(
        @Query("osm_ids") osmIds: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") acceptLanguage: String = "es"
    ): Response<List<NominatimPlace>>
}

/**
 * Modelo de datos para la respuesta de Nominatim
 */
data class NominatimPlace(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("licence") val licence: String,
    @SerializedName("osm_type") val osmType: String?,
    @SerializedName("osm_id") val osmId: Long,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("class") val placeClass: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("importance") val importance: Double?,
    @SerializedName("address") val address: NominatimAddress?,
    @SerializedName("boundingbox") val boundingBox: List<String>?
)

/**
 * Modelo de datos para la dirección en la respuesta de Nominatim
 */
data class NominatimAddress(
    @SerializedName("house_number") val houseNumber: String?,
    @SerializedName("road") val road: String?,
    @SerializedName("neighbourhood") val neighbourhood: String?,
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("town") val town: String?,
    @SerializedName("village") val village: String?,
    @SerializedName("municipality") val municipality: String?,
    @SerializedName("county") val county: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("province") val province: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("country_code") val countryCode: String?
) {
    /**
     * Obtiene el nombre de la ciudad, independientemente de cómo esté etiquetada
     * (city, town, village, etc.)
     */
    fun getCityName(): String? {
        return city ?: town ?: village ?: municipality
    }
} 