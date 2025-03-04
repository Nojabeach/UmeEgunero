package com.tfg.umeegunero.data.repository

import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.CorreosResponse
import com.tfg.umeegunero.data.model.toCiudad
import com.tfg.umeegunero.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CiudadRepository @Inject constructor() {

    fun buscarCiudadesPorCodigoPostal(codigoPostal: String, callback: (List<Ciudad>?, String?) -> Unit) {
        // Validar que el código postal tenga 5 dígitos
        if (!codigoPostal.matches(Regex("^\\d{5}$"))) {
            callback(null, "El código postal debe tener 5 dígitos")
            return
        }

        RetrofitClient.instance.getCiudadesPorCodigoPostal(codigoPostal).enqueue(object : Callback<CorreosResponse> {
            override fun onResponse(call: Call<CorreosResponse>, response: Response<CorreosResponse>) {
                if (response.isSuccessful) {
                    val correosResponse = response.body()
                    if (correosResponse != null && correosResponse.status == "OK") {
                        // Convertir los datos de la API a nuestro modelo Ciudad
                        val ciudades = correosResponse.data.map { it.toCiudad() }
                        callback(ciudades, null)
                    } else {
                        callback(null, "No se encontraron resultados para el código postal")
                    }
                } else {
                    callback(null, "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CorreosResponse>, t: Throwable) {
                callback(null, "Error de conexión: ${t.message}")
            }
        })
    }
}