package com.tfg.umeegunero.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio para acceder a la API de GeoAPI España
 * Documentación: https://geoapi.es/pruebalo
 */
interface GeoApiService {
    /**
     * Obtiene la lista de comunidades autónomas
     */
    @GET("comunidades")
    suspend fun getComunidades(
        @Query("type") type: String = "JSON",
        @Query("key") key: String = "",
        @Query("sandbox") sandbox: Int = 1
    ): Response<GeoApiResponse<ComunidadData>>
    
    /**
     * Obtiene la lista de provincias
     * @param CCOM Código de comunidad autónoma (opcional)
     */
    @GET("provincias")
    suspend fun getProvincias(
        @Query("CCOM") codigoComunidad: String? = null,
        @Query("type") type: String = "JSON",
        @Query("key") key: String = "",
        @Query("sandbox") sandbox: Int = 1
    ): Response<GeoApiResponse<ProvinciaData>>
    
    /**
     * Obtiene la lista de municipios
     * @param CPRO Código de provincia (opcional)
     */
    @GET("municipios")
    suspend fun getMunicipios(
        @Query("CPRO") codigoProvincia: String? = null,
        @Query("type") type: String = "JSON",
        @Query("key") key: String = "",
        @Query("sandbox") sandbox: Int = 1
    ): Response<GeoApiResponse<MunicipioData>>
    
    /**
     * Obtiene la lista de códigos postales
     * @param CPRO Código de provincia (opcional)
     * @param CMUM Código de municipio (opcional)
     */
    @GET("cps")
    suspend fun getCodigosPostales(
        @Query("CPRO") codigoProvincia: String? = null,
        @Query("CMUM") codigoMunicipio: String? = null,
        @Query("type") type: String = "JSON",
        @Query("key") key: String = "",
        @Query("sandbox") sandbox: Int = 1
    ): Response<GeoApiResponse<CodigoPostalGeoData>>
}

/**
 * Cliente Retrofit para GeoAPI España
 */
object GeoApiRetrofitClient {
    private const val BASE_URL = "https://apiv1.geoapi.es/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val geoApiService: GeoApiService = retrofit.create(GeoApiService::class.java)
}

/**
 * Respuesta genérica de GeoAPI
 */
data class GeoApiResponse<T>(
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("error") val error: List<String> = emptyList()
)

/**
 * Datos de Comunidad Autónoma
 */
data class ComunidadData(
    @SerializedName("CCOM") val codigo: String,
    @SerializedName("COM") val nombre: String
)

/**
 * Datos de Provincia
 */
data class ProvinciaData(
    @SerializedName("CCOM") val codigoComunidad: String,
    @SerializedName("CPRO") val codigo: String,
    @SerializedName("PRO") val nombre: String
)

/**
 * Datos de Municipio
 */
data class MunicipioData(
    @SerializedName("CPRO") val codigoProvincia: String,
    @SerializedName("CMUM") val codigo: String,
    @SerializedName("DMUN50") val nombre: String
)

/**
 * Datos de Código Postal
 */
data class CodigoPostalGeoData(
    @SerializedName("CPRO") val codigoProvincia: String,
    @SerializedName("PRO") val provincia: String,
    @SerializedName("CMUM") val codigoMunicipio: String,
    @SerializedName("DMUN50") val municipio: String,
    @SerializedName("CPOS") val codigoPostal: String
) 