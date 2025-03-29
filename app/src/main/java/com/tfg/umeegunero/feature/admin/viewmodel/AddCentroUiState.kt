package com.tfg.umeegunero.feature.admin.viewmodel

import com.tfg.umeegunero.data.model.Ciudad

data class AddCentroUiState(
    val id: String = "",
    val centroId: String = "",
    // Información del centro
    val nombre: String = "",
    val nombreError: String? = null,

    // Dirección
    val calle: String = "",
    val calleError: String? = null,
    val numero: String = "",
    val numeroError: String? = null,
    val codigoPostal: String = "",
    val codigoPostalError: String? = null,
    val ciudad: String = "",
    val ciudadError: String? = null,
    val provincia: String = "",
    val provinciaError: String? = null,

    // Teléfono general del centro (opcional)
    val telefono: String = "",
    val telefonoError: String? = null,
    
    // Email del centro
    val email: String = "",
    
    // Administradores del centro
    val adminCentro: List<AdminCentro> = listOf(AdminCentro()),
    val adminCentroError: String? = null,

    // Estado de la UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val centroLoaded: Boolean = false,
    
    // Ciudades sugeridas por código postal
    val ciudadesSugeridas: List<Ciudad> = emptyList(),
    val isBuscandoCiudades: Boolean = false,
    val errorBusquedaCiudades: String? = null,
    
    // Coordenadas para el mapa
    val latitud: Double? = null,
    val longitud: Double? = null,
    val mostrarMapa: Boolean = false,
    val direccionCompleta: String = "",
    
    // Propiedades calculadas
    val tieneUbicacionValida: Boolean = latitud != null && longitud != null
) {
    val isFormValid: Boolean
        get() {
            // Validaciones básicas del centro
            val centroValido = nombre.isNotBlank() && nombreError == null &&
                calle.isNotBlank() && calleError == null &&
                numero.isNotBlank() && numeroError == null &&
                codigoPostal.isNotBlank() && codigoPostalError == null &&
                ciudad.isNotBlank() && ciudadError == null &&
                provincia.isNotBlank() && provinciaError == null &&
                telefonoError == null  // Teléfono es opcional, solo verificamos que no tenga error
            
            // Validación de administradores
            val adminValidos = adminCentro.isNotEmpty() && adminCentro.all { it.isValid }
            
            return centroValido && adminValidos
        }
}

// Modelo para representar un usuario administrador de centro
data class AdminCentroUsuario(
    val dni: String = "",
    val dniError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val telefono: String = "",    // Opcional
    val telefonoError: String? = null,
    val password: String = "",
    val passwordError: String? = null
) {
    val isValid: Boolean
        get() = dni.isNotBlank() && dniError == null &&
                nombre.isNotBlank() && nombreError == null &&
                apellidos.isNotBlank() && apellidosError == null &&
                email.isNotBlank() && emailError == null &&
                telefonoError == null && // Telefono es opcional pero no debe tener error
                password.isNotBlank() && passwordError == null
}

// Modelo para representar un usuario administrador de centro simplificado
data class AdminCentro(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null
) {
    val isValid: Boolean
        get() = email.isNotBlank() && emailError == null &&
                (password.isBlank() || passwordError == null) // Permitimos contraseña vacía en edición
} 