package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AdminCentro
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class EditCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCentroUiState())
    val uiState: StateFlow<AddCentroUiState> = _uiState.asStateFlow()

    // ID del centro que se está editando
    private val centroId: String = savedStateHandle["centroId"] ?: ""

    init {
        if (centroId.isNotBlank()) {
            // Cargar los datos del centro
            loadCentroData(centroId)
        }
    }

    /**
     * Actualiza la latitud del centro
     */
    fun updateLatitud(latitud: Double?) {
        _uiState.update { 
            it.copy(
                latitud = latitud,
                tieneUbicacionValida = (latitud != null && it.longitud != null)
            )
        }
    }

    /**
     * Actualiza la longitud del centro
     */
    fun updateLongitud(longitud: Double?) {
        _uiState.update { 
            it.copy(
                longitud = longitud,
                tieneUbicacionValida = (it.latitud != null && longitud != null)
            )
        }
    }

    /**
     * Carga los datos del centro desde la API
     */
    private fun loadCentroData(centroId: String) {
        viewModelScope.launch {
            try {
                val resultCentro = centroRepository.getCentroById(centroId)
                
                if (resultCentro is com.tfg.umeegunero.data.repository.Result.Success) {
                    updateUiStateWithCentro(resultCentro.data)
                } else if (resultCentro is com.tfg.umeegunero.data.repository.Result.Error) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar el centro: ${resultCentro.exception.message}"
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )}
            }
        }
    }

    /**
     * Actualiza el nombre del centro
     */
    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(
            nombre = nombre,
            nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
        )}
    }

    /**
     * Actualiza la calle del centro
     */
    fun updateCalle(calle: String) {
        _uiState.update { it.copy(
            calle = calle,
            calleError = if (calle.isBlank()) "La calle es obligatoria" else null
        )}
    }

    /**
     * Actualiza el número del centro
     */
    fun updateNumero(numero: String) {
        _uiState.update { it.copy(
            numero = numero,
            numeroError = if (numero.isBlank()) "El número es obligatorio" else null
        )}
    }

    /**
     * Actualiza el código postal del centro
     */
    fun updateCodigoPostal(codigoPostal: String) {
        _uiState.update { it.copy(
            codigoPostal = codigoPostal,
            codigoPostalError = if (codigoPostal.isBlank()) "El código postal es obligatorio" else null
        )}
    }

    /**
     * Actualiza la ciudad del centro
     */
    fun updateCiudad(ciudad: String) {
        _uiState.update { it.copy(
            ciudad = ciudad,
            ciudadError = if (ciudad.isBlank()) "La ciudad es obligatoria" else null
        )}
    }

    /**
     * Actualiza la provincia del centro
     */
    fun updateProvincia(provincia: String) {
        _uiState.update { it.copy(
            provincia = provincia,
            provinciaError = if (provincia.isBlank()) "La provincia es obligatoria" else null
        )}
    }

    /**
     * Actualiza el teléfono del centro
     */
    fun updateTelefono(telefono: String) {
        _uiState.update { it.copy(
            telefono = telefono,
            telefonoError = validatePhoneNumber(telefono)
        )}
    }

    /**
     * Valida un número de teléfono
     */
    private fun validatePhoneNumber(telefono: String): String? {
        return when {
            telefono.isBlank() -> null // El teléfono es opcional
            !Pattern.matches("^[6-9][0-9]{8}$", telefono) -> "El formato del teléfono no es válido"
            else -> null
        }
    }

    /**
     * Valida un correo electrónico
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !Pattern.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+", email) -> "El formato del email no es válido"
            else -> null
        }
    }

    /**
     * Valida una contraseña
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> null // Permitimos contraseña vacía en modo edición
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !password.any { it.isDigit() } -> "La contraseña debe contener al menos un número"
            !password.any { it.isLetter() } -> "La contraseña debe contener al menos una letra"
            else -> null
        }
    }

    /**
     * Actualiza el email del administrador
     */
    fun updateAdminEmail(index: Int, email: String) {
        val adminList = _uiState.value.adminCentro.toMutableList()
        if (index < adminList.size) {
            val admin = adminList[index].copy(
                email = email,
                emailError = validateEmail(email)
            )
            adminList[index] = admin
            _uiState.update { it.copy(adminCentro = adminList) }
        }
    }

    /**
     * Actualiza la contraseña del administrador
     */
    fun updateAdminPassword(index: Int, password: String) {
        val adminList = _uiState.value.adminCentro.toMutableList()
        if (index < adminList.size) {
            val admin = adminList[index].copy(
                password = password,
                passwordError = if (password.isNotBlank()) validatePassword(password) else null
            )
            adminList[index] = admin
            _uiState.update { it.copy(adminCentro = adminList) }
        }
    }

    /**
     * Selecciona una ciudad de las sugeridas
     */
    fun seleccionarCiudad(ciudad: com.tfg.umeegunero.data.model.Ciudad) {
        _uiState.update { it.copy(
            ciudad = ciudad.nombre,
            provincia = ciudad.provincia,
            ciudadesSugeridas = emptyList()
        )}
    }

    /**
     * Guarda los cambios del centro
     */
    fun guardarCentro(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val centro = buildCentroFromState()
                val result = centroRepository.updateCentro(centroId, centro)
                
                when (result) {
                    is com.tfg.umeegunero.data.repository.Result.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }
                    is com.tfg.umeegunero.data.repository.Result.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        onError("Error al actualizar el centro: ${result.exception.message}")
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        onError("Estado inesperado al actualizar el centro")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError("Error inesperado: ${e.message}")
            }
        }
    }

    /**
     * Construye un objeto Centro a partir del estado
     */
    private fun buildCentroFromState(): Centro {
        with(_uiState.value) {
            return Centro(
                id = centroId,
                nombre = nombre,
                direccion = com.tfg.umeegunero.data.model.Direccion(
                    calle = calle,
                    numero = numero,
                    codigoPostal = codigoPostal,
                    ciudad = ciudad,
                    provincia = provincia
                ),
                latitud = latitud ?: 0.0,
                longitud = longitud ?: 0.0,
                contacto = com.tfg.umeegunero.data.model.Contacto(
                    telefono = telefono,
                    email = adminCentro.firstOrNull()?.email ?: ""
                )
            )
        }
    }

    /**
     * Actualiza el estado con los datos del centro cargado
     */
    private fun updateUiStateWithCentro(centro: Centro) {
        // Crear un objeto AdminCentro con la info del centro
        val admin = AdminCentro(
            email = centro.contacto.email,
            password = "",
            emailError = null,
            passwordError = null
        )
        
        _uiState.update { 
            it.copy(
                nombre = centro.nombre,
                calle = centro.direccion.calle,
                numero = centro.direccion.numero,
                codigoPostal = centro.direccion.codigoPostal,
                ciudad = centro.direccion.ciudad,
                provincia = centro.direccion.provincia,
                latitud = centro.latitud,
                longitud = centro.longitud,
                telefono = centro.contacto.telefono,
                adminCentro = listOf(admin),
                isLoading = false,
                tieneUbicacionValida = centro.latitud != 0.0 && centro.longitud != 0.0
            )
        }
    }
} 