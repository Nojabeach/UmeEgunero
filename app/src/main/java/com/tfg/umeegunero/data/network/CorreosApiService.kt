package com.tfg.umeegunero.data.network

import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.CorreosResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CorreosApiService {
    @GET("postalCodes")
    fun getCiudadesPorCodigoPostal(@Query("postalCode") codigoPostal: String): Call<CorreosResponse>
}