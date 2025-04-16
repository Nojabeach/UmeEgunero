package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de creación y edición de usuarios
 */
data class AddUserUiState(
    val dni: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val tipoUsuario: TipoUsuario = TipoUsuario.FAMILIAR,
    val centroId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val dniError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val telefonoError: String? = null,
    val isAdminApp: Boolean = false,
    val centrosDisponibles: List<Centro> = emptyList(),
    val cursosDisponibles: List<Curso> = emptyList(),
    val clasesDisponibles: List<Clase> = emptyList(),
    val centroSeleccionado: Centro? = null,
    val cursoSeleccionado: Curso? = null,
    val claseSeleccionada: Clase? = null,
    val fechaNacimiento: String = "",
    val fechaNacimientoError: String? = null,
    val isEditMode: Boolean = false
) {
    // Determina si el formulario es válido según el tipo de usuario
    val isFormValid: Boolean
        get() {
            // Validación básica para todos los usuarios
            val basicValidation = dni.isNotBlank() &&
                    nombre.isNotBlank() &&
                    apellidos.isNotBlank() &&
                    telefono.isNotBlank() &&
                    dniError == null &&
                    nombreError == null &&
                    apellidosError == null &&
                    telefonoError == null
            
            // Validación de credenciales solo necesaria para los usuarios con acceso al sistema
            val credentialsValidation = if (tipoUsuario != TipoUsuario.ALUMNO) {
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null
            } else true
            
            // Validación específica para profesor o admin de centro
            val centroValidation = if (tipoUsuario == TipoUsuario.PROFESOR || 
                                         tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
                centroSeleccionado != null
            } else true
            
            // Validación específica para alumnos
            val alumnoValidation = if (tipoUsuario == TipoUsuario.ALUMNO) {
                fechaNacimiento.isNotBlank() && 
                fechaNacimientoError == null &&
                centroSeleccionado != null &&
                cursoSeleccionado != null &&
                claseSeleccionada != null
            } else true
            
            return basicValidation && credentialsValidation && centroValidation && alumnoValidation
        }
}

/**
 * ViewModel para la gestión de usuarios en el sistema.
 * NOTA: Esta es una versión simplificada para propósitos de desarrollo.
 */
@HiltViewModel
class AddUserViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()

    init {
        // Cargar los centros al inicializar
        loadCentros()
    }

    // Establece si el usuario es admin de la app
    fun setIsAdminApp(isAdminApp: Boolean) {
        _uiState.update { it.copy(isAdminApp = isAdminApp) }
    }

    // Carga los centros disponibles desde Firestore
    fun loadCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = centroRepository.getActiveCentros()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            centrosDisponibles = result.data,
                            isLoading = false
                        )
                    }
                    Timber.d("Centros cargados: ${result.data.size}")
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar los centros: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    Timber.e(result.exception, "Error al cargar centros")
                }
                is Result.Loading -> {
                    // No hacemos nada, ya estamos mostrando el estado de carga
                }
            }
        }
    }

    // Carga los cursos del centro seleccionado
    fun loadCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = cursoRepository.getCursosByCentro(centroId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            cursosDisponibles = result.data,
                            isLoading = false
                        )
                    }
                    Timber.d("Cursos cargados: ${result.data.size}")
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar los cursos: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    Timber.e(result.exception, "Error al cargar cursos")
                }
                is Result.Loading -> {
                    // Ya estamos mostrando el estado de carga
                }
            }
        }
    }

    // Carga las clases del curso seleccionado
    fun loadClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = cursoRepository.obtenerClasesPorCurso(cursoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            clasesDisponibles = result.data,
                            isLoading = false
                        )
                    }
                    Timber.d("Clases cargadas: ${result.data.size}")
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar las clases: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    Timber.e(result.exception, "Error al cargar clases")
                }
                is Result.Loading -> {
                    // Ya estamos mostrando el estado de carga
                }
            }
        }
    }

    // Actualiza el DNI
    fun updateDni(dni: String) {
        val error = when {
            dni.isBlank() -> "El DNI es obligatorio"
            !isDniValid(dni) -> "DNI no válido"
            else -> null
        }
        
        _uiState.update { it.copy(
            dni = dni,
            dniError = error
        )}
    }

    // Actualiza el email
    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "El email es obligatorio"
            !isEmailValid(email) -> "Email no válido"
            else -> null
        }
        
        _uiState.update { it.copy(
            email = email,
            emailError = error
        )}
    }

    // Actualiza la contraseña
    fun updatePassword(password: String) {
        val error = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        
        _uiState.update { it.copy(
            password = password,
            passwordError = error
        )}
        
        // Si ya había una confirmación, verificar que coincide
        val currentState = _uiState.value
        if (currentState.confirmPassword.isNotBlank()) {
            updateConfirmPassword(currentState.confirmPassword)
        }
    }

    // Actualiza la confirmación de contraseña
    fun updateConfirmPassword(confirmPassword: String) {
        val error = when {
            confirmPassword.isBlank() -> "Debe confirmar la contraseña"
            confirmPassword != _uiState.value.password -> "Las contraseñas no coinciden"
            else -> null
        }
        
        _uiState.update { it.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = error
        )}
    }

    // Actualiza el nombre
    fun updateNombre(nombre: String) {
        val error = when {
            nombre.isBlank() -> "El nombre es obligatorio"
            else -> null
        }
        
        _uiState.update { it.copy(
            nombre = nombre,
            nombreError = error
        )}
    }

    // Actualiza los apellidos
    fun updateApellidos(apellidos: String) {
        val error = when {
            apellidos.isBlank() -> "Los apellidos son obligatorios"
            else -> null
        }
        
        _uiState.update { it.copy(
            apellidos = apellidos,
            apellidosError = error
        )}
    }

    // Actualiza el teléfono
    fun updateTelefono(telefono: String) {
        val error = when {
            telefono.isBlank() -> "El teléfono es obligatorio"
            !isTelefonoValid(telefono) -> "Teléfono no válido"
            else -> null
        }
        
        _uiState.update { it.copy(
            telefono = telefono,
            telefonoError = error
        )}
    }

    // Actualiza el tipo de usuario
    fun updateTipoUsuario(tipoUsuario: TipoUsuario) {
        _uiState.update { it.copy(
            tipoUsuario = tipoUsuario
        )}
    }

    // Actualiza el centro seleccionado
    fun updateCentroSeleccionado(centroId: String) {
        // Buscar el centro por ID
        val centro = _uiState.value.centrosDisponibles.find { it.id == centroId }
        
        _uiState.update { it.copy(
            centroId = centroId,
            centroSeleccionado = centro
        ) }
        
        // Si es un alumno, cargamos los cursos disponibles para este centro
        if (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO) {
            loadCursos(centroId)
        }
    }

    // Actualiza el curso seleccionado
    fun updateCursoSeleccionado(cursoId: String) {
        // Buscar el curso por ID
        val curso = _uiState.value.cursosDisponibles.find { it.id == cursoId }
        
        _uiState.update { it.copy(
            cursoSeleccionado = curso,
            // Al cambiar de curso, reseteamos la clase seleccionada
            claseSeleccionada = null
        ) }
        
        // Cargar las clases disponibles para este curso
        if (cursoId.isNotBlank()) {
            loadClases(cursoId)
        }
    }

    // Actualiza la clase seleccionada
    fun updateClaseSeleccionada(claseId: String) {
        // Buscar la clase por ID
        val clase = _uiState.value.clasesDisponibles.find { it.id == claseId }
        
        _uiState.update { it.copy(
            claseSeleccionada = clase
        ) }
    }

    // Actualiza la fecha de nacimiento
    fun updateFechaNacimiento(fechaNacimiento: String) {
        val error = when {
            fechaNacimiento.isBlank() -> "La fecha de nacimiento es obligatoria"
            !isFechaNacimientoValid(fechaNacimiento) -> "La fecha no es válida"
            else -> null
        }
        
        _uiState.update { it.copy(
            fechaNacimiento = fechaNacimiento,
            fechaNacimientoError = error
        ) }
    }
    
    // Simula guardar un usuario
    fun saveUser() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch {
            // Simulamos un proceso de guardado
            _uiState.update { it.copy(isLoading = true) }
            
            // Delay para simular proceso
            kotlinx.coroutines.delay(1000)
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    success = true
                )
            }
        }
    }

    // Limpia los mensajes de error
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Validaciones auxiliares
    private fun isDniValid(dni: String): Boolean {
        return dni.length == 9 && dni.substring(0, 8).all { it.isDigit() }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isTelefonoValid(telefono: String): Boolean {
        return telefono.length == 9 && telefono.all { it.isDigit() }
    }
    
    private fun isFechaNacimientoValid(fecha: String): Boolean {
        return try {
            LocalDate.parse(fecha)
            true
        } catch (e: Exception) {
            false
        }
    }
}