package com.tfg.umeegunero.data.model

/**
 * Estado UI para la pantalla de añadir centro
 */
data class AddCentroUiState(
    val nombre: String = "",
    val tipoVia: String = "",
    val nombreVia: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val codigoPostal: String = "",
    val telefono: String = "",
    val email: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val isLoading: Boolean = false,
    val errorMessages: List<String> = emptyList(),
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val descripcion: String = "",
    val nombreDirector: String = "",
    val apellidosDirector: String = "",
    val dniDirector: String = "",
    val telefonoDirector: String = "",
    val emailDirector: String = "",
    val fechaNacimientoDirector: String = "",
    val errorMessageDirector: String? = null,
    val showDirectorDialog: Boolean = false,
) {
    val tieneUbicacionValida: Boolean
        get() = latitud.isNotBlank() && longitud.isNotBlank() && 
                latitud.toDoubleOrNull() != null && longitud.toDoubleOrNull() != null
} 