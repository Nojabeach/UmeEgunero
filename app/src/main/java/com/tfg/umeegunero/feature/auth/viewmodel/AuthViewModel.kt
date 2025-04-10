package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UserRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la autenticación
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val error: String? = null,
    val success: String? = null,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = ""
)

/**
 * ViewModel para la gestión de la autenticación
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    /**
     * Verifica el estado de autenticación del usuario
     */
    private fun checkAuthState() {
        val currentUser = userRepository.getCurrentUser()
        _uiState.update { 
            it.copy(
                isLoggedIn = currentUser != null,
                currentUser = currentUser
            )
        }
    }
    
    /**
     * Inicia sesión con email y contraseña
     */
    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val email = _uiState.value.email
            val password = _uiState.value.password
            
            if (email.isBlank() || password.isBlank()) {
                _uiState.update { 
                    it.copy(
                        error = "Por favor, completa todos los campos",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            when (val result = userRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoggedIn = true,
                            currentUser = result.data,
                            success = "Inicio de sesión exitoso",
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception?.message ?: "Error al iniciar sesión",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    /**
     * Registra un nuevo usuario
     */
    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val email = _uiState.value.email
            val password = _uiState.value.password
            val confirmPassword = _uiState.value.confirmPassword
            val nombre = _uiState.value.nombre
            val apellidos = _uiState.value.apellidos
            val dni = _uiState.value.dni
            
            // Validaciones básicas
            if (email.isBlank() || password.isBlank() || nombre.isBlank() || apellidos.isBlank() || dni.isBlank()) {
                _uiState.update { 
                    it.copy(
                        error = "Por favor, completa todos los campos",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            if (password != confirmPassword) {
                _uiState.update { 
                    it.copy(
                        error = "Las contraseñas no coinciden",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            if (password.length < 6) {
                _uiState.update { 
                    it.copy(
                        error = "La contraseña debe tener al menos 6 caracteres",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            val nombreCompleto = "$nombre $apellidos"
            
            when (val result = userRepository.register(email, password, nombreCompleto)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoggedIn = true,
                            currentUser = result.data,
                            success = "Registro exitoso",
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception?.message ?: "Error al registrar usuario",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        when (val result = userRepository.logout()) {
            is Result.Success -> {
                _uiState.update { 
                    AuthUiState(success = "Sesión cerrada correctamente")
                }
            }
            is Result.Error -> {
                _uiState.update { 
                    it.copy(error = result.exception?.message ?: "Error al cerrar sesión")
                }
            }
            is Result.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }
    
    /**
     * Envía un correo para restablecer la contraseña
     */
    fun resetPassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val email = _uiState.value.email
            
            if (email.isBlank()) {
                _uiState.update { 
                    it.copy(
                        error = "Por favor, ingresa tu correo electrónico",
                        isLoading = false
                    )
                }
                return@launch
            }
            
            when (val result = userRepository.resetPassword(email)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            success = "Se ha enviado un correo para restablecer tu contraseña",
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception?.message ?: "Error al enviar correo de recuperación",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
    
    /**
     * Actualiza el email en el formulario
     */
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    
    /**
     * Actualiza la contraseña en el formulario
     */
    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }
    
    /**
     * Actualiza la confirmación de contraseña en el formulario
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }
    
    /**
     * Actualiza el nombre en el formulario
     */
    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }
    
    /**
     * Actualiza los apellidos en el formulario
     */
    fun updateApellidos(apellidos: String) {
        _uiState.update { it.copy(apellidos = apellidos) }
    }
    
    /**
     * Actualiza el DNI en el formulario
     */
    fun updateDni(dni: String) {
        _uiState.update { it.copy(dni = dni) }
    }
    
    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun clearSuccess() {
        _uiState.update { it.copy(success = null) }
    }
} 