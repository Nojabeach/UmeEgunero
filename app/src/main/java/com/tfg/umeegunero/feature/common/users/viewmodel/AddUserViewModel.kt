package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Enum para identificar los campos del formulario
enum class AddUserFormField {
    DNI, EMAIL, PASSWORD, CONFIRM_PASSWORD, NOMBRE, APELLIDOS, TELEFONO,
    CENTRO, FECHA_NACIMIENTO, CURSO, CLASE
}

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
    val success: Boolean = false, // Se reemplazará por showSuccessDialog
    val showSuccessDialog: Boolean = false, // Nuevo para feedback de éxito
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
    val isEditMode: Boolean = false,
    val initialCentroId: String? = null,
    val isCentroBloqueado: Boolean = false,
    val firstInvalidField: AddUserFormField? = null, // Para scroll al error
    val validationAttemptFailed: Boolean = false // Trigger para scroll al error
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
                                         tipoUsuario == TipoUsuario.ADMIN_CENTRO ||
                                         tipoUsuario == TipoUsuario.ALUMNO) { // Alumno también necesita centro
                centroSeleccionado != null
            } else true
            
            // Validación específica para alumnos
            val alumnoValidation = if (tipoUsuario == TipoUsuario.ALUMNO) {
                fechaNacimiento.isNotBlank() && 
                fechaNacimientoError == null &&
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
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los parámetros recibidos de la navegación.
     * Esta función debe llamarse una sola vez desde el Composable.
     */
    fun initialize(
        centroId: String?,
        bloqueado: Boolean,
        tipoUsuarioStr: String?,
        isAdminAppFlag: Boolean
    ) {
        // Solo inicializar una vez (evitar llamadas múltiples desde LaunchedEffect)
        if (_uiState.value.initialCentroId == null && centroId != null) {
            val tipoUsuario = tipoUsuarioStr?.let { tipo ->
                when (tipo.lowercase()) {
                    "admin" -> TipoUsuario.ADMIN_APP
                    "centro" -> TipoUsuario.ADMIN_CENTRO
                    "profesor" -> TipoUsuario.PROFESOR
                    "familiar" -> TipoUsuario.FAMILIAR
                    "alumno" -> TipoUsuario.ALUMNO
                    else -> TipoUsuario.FAMILIAR // Default o manejar error
                }
            } ?: TipoUsuario.FAMILIAR // Default si no viene

            _uiState.update {
                it.copy(
                    initialCentroId = centroId,
                    isCentroBloqueado = bloqueado,
                    tipoUsuario = tipoUsuario,
                    isAdminApp = isAdminAppFlag
                )
            }
            // Cargar centros y luego intentar preseleccionar
            loadCentrosAndSelectInitial()
        } else if (_uiState.value.initialCentroId == null) {
             // Si no viene centroId, inicializar igualmente tipo y admin flag
             val tipoUsuario = tipoUsuarioStr?.let { tipo ->
                when (tipo.lowercase()) {
                    "admin" -> TipoUsuario.ADMIN_APP
                    "centro" -> TipoUsuario.ADMIN_CENTRO
                    "profesor" -> TipoUsuario.PROFESOR
                    "familiar" -> TipoUsuario.FAMILIAR
                    "alumno" -> TipoUsuario.ALUMNO
                    else -> TipoUsuario.FAMILIAR // Default o manejar error
                }
            } ?: TipoUsuario.FAMILIAR // Default si no viene
             _uiState.update {
                it.copy(
                    tipoUsuario = tipoUsuario,
                    isAdminApp = isAdminAppFlag
                )
            }
             loadCentros() // Cargar centros igualmente
        }
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

    // Nueva función para cargar centros y luego intentar seleccionar el inicial
    private fun loadCentrosAndSelectInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = centroRepository.getActiveCentros()) {
                is Result.Success -> {
                    val centros = result.data
                    val centroInicial = centros.find { it.id == _uiState.value.initialCentroId }
                    _uiState.update {
                        it.copy(
                            centrosDisponibles = centros,
                            isLoading = false,
                            centroSeleccionado = centroInicial // Intentar preseleccionar
                        )
                    }
                    Timber.d("Centros cargados: ${centros.size}. Centro inicial encontrado: ${centroInicial != null}")
                    // Si se seleccionó un centro, cargar sus cursos si es necesario (ej. para Alumno)
                    centroInicial?.let { centro ->
                         if (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO) {
                             loadCursos(centro.id)
                         }
                    }
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
                is Result.Loading -> {} // Ya en isLoading = true
            }
        }
    }

    // Carga los cursos del centro seleccionado
    fun loadCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, cursosDisponibles = emptyList(), cursoSeleccionado = null, clasesDisponibles = emptyList(), claseSeleccionada = null) } // Resetear al cargar
            Timber.d("⏳ Iniciando carga de cursos para centroId: $centroId")
            
            try {
                 // Usar la función suspendida que devuelve directamente la lista
                 val cursosList = cursoRepository.obtenerCursosPorCentro(centroId)
                 Timber.d("Cursos cargados: ${cursosList.size}")
                 cursosList.forEach { curso -> 
                     Timber.d("Curso: ${curso.nombre}, id: ${curso.id}")
                 }
                 _uiState.update { it.copy(
                    cursosDisponibles = cursosList, 
                    isLoading = false,
                    // No seleccionamos automáticamente ningún curso
                    cursoSeleccionado = null
                 )}
                 
                 // Ya no hacemos selección automática del primer curso
                 // para que el usuario vea "Elija el curso" y seleccione uno explícitamente
            } catch (e: Exception) {
                Timber.e(e, "❌ Error al cargar cursos para centroId: $centroId")
                _uiState.update { it.copy(
                    error = "Error al cargar los cursos: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    // Carga las clases del curso seleccionado
    fun loadClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, clasesDisponibles = emptyList(), claseSeleccionada = null) } // Resetear clases
            Timber.d("⏳ Iniciando carga de clases para cursoId: $cursoId")
            
            try {
                when (val result = cursoRepository.obtenerClasesPorCurso(cursoId)) {
                    is Result.Success -> {
                        val clasesList = result.data
                        Timber.d("✅ Clases cargadas: ${clasesList.size} para el curso $cursoId")
                        
                        _uiState.update { 
                            it.copy(
                                clasesDisponibles = clasesList,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌ Error al cargar clases para cursoId: $cursoId")
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar las clases: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos mostrando el estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error inesperado al cargar clases para cursoId: $cursoId")
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar las clases: ${e.message}",
                        isLoading = false
                    )
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
            dniError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.DNI) null else it.firstInvalidField,
            validationAttemptFailed = false
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
            emailError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.EMAIL) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
    }

    // Actualiza la contraseña
    fun updatePassword(password: String) {
        val error = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        
        val confirmError = if (_uiState.value.confirmPassword.isNotBlank() && password != _uiState.value.confirmPassword) {
            "Las contraseñas no coinciden"
        } else _uiState.value.confirmPasswordError // Mantener el error de confirmación si ya existía y era otro

        _uiState.update { it.copy(
            password = password,
            passwordError = error,
            confirmPasswordError = confirmError, // Actualizar error de confirmación también
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.PASSWORD) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
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
            confirmPasswordError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.CONFIRM_PASSWORD) null else it.firstInvalidField,
            validationAttemptFailed = false
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
            nombreError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.NOMBRE) null else it.firstInvalidField,
            validationAttemptFailed = false
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
            apellidosError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.APELLIDOS) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
    }

    // Actualiza el teléfono
    fun updateTelefono(telefono: String) {
        val error = when {
            telefono.isBlank() -> "El teléfono es obligatorio"
            !isTelefonoValid(telefono) -> "Teléfono no válido (9 dígitos)"
            else -> null
        }
        
        _uiState.update { it.copy(
            telefono = telefono,
            telefonoError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.TELEFONO) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
    }

    // Actualiza el tipo de usuario
    fun updateTipoUsuario(tipoUsuario: TipoUsuario) {
        // Al cambiar tipo, resetear campos específicos y validaciones potencialmente irrelevantes
        _uiState.update {
            it.copy(
                tipoUsuario = tipoUsuario,
                // Resetear campos de alumno si no es alumno
                fechaNacimiento = if (tipoUsuario != TipoUsuario.ALUMNO) "" else it.fechaNacimiento,
                fechaNacimientoError = if (tipoUsuario != TipoUsuario.ALUMNO) null else it.fechaNacimientoError,
                cursoSeleccionado = if (tipoUsuario != TipoUsuario.ALUMNO) null else it.cursoSeleccionado,
                claseSeleccionada = if (tipoUsuario != TipoUsuario.ALUMNO) null else it.claseSeleccionada,
                 // Resetear credenciales si es alumno
                email = if (tipoUsuario == TipoUsuario.ALUMNO) "" else it.email,
                emailError = if (tipoUsuario == TipoUsuario.ALUMNO) null else it.emailError,
                password = if (tipoUsuario == TipoUsuario.ALUMNO) "" else it.password,
                passwordError = if (tipoUsuario == TipoUsuario.ALUMNO) null else it.passwordError,
                confirmPassword = if (tipoUsuario == TipoUsuario.ALUMNO) "" else it.confirmPassword,
                confirmPasswordError = if (tipoUsuario == TipoUsuario.ALUMNO) null else it.confirmPasswordError,
                 // Resetear selección de centro si no es relevante (ej. admin app, familiar)
                centroSeleccionado = if (tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) null else it.centroSeleccionado,
                cursosDisponibles = if (tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) emptyList() else it.cursosDisponibles,
                clasesDisponibles = if (tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) emptyList() else it.clasesDisponibles,
                firstInvalidField = null, // Limpiar foco de error al cambiar tipo
                validationAttemptFailed = false
            )
        }
        // Si el nuevo tipo requiere centro y no está bloqueado, cargar centros si no están ya cargados
        if (!uiState.value.isCentroBloqueado &&
            (tipoUsuario == TipoUsuario.ADMIN_CENTRO || tipoUsuario == TipoUsuario.PROFESOR || tipoUsuario == TipoUsuario.ALUMNO) &&
             uiState.value.centrosDisponibles.isEmpty()) {
            loadCentros()
        }
    }

    // Actualiza el centro seleccionado
    fun updateCentroSeleccionado(centroId: String) {
        // Buscar el centro por ID
        val centro = _uiState.value.centrosDisponibles.find { it.id == centroId }
        
        _uiState.update { it.copy(
            centroId = centroId,
            centroSeleccionado = centro,
            // Resetear curso y clase al cambiar centro
            cursoSeleccionado = null,
            cursosDisponibles = emptyList(),
            claseSeleccionada = null,
            clasesDisponibles = emptyList(),
             firstInvalidField = if (it.firstInvalidField == AddUserFormField.CENTRO) null else it.firstInvalidField,
             validationAttemptFailed = false
        ) }
        
        // Cargar cursos si es Alumno, Profesor o AdminCentro (aunque profesor/admin no usan curso/clase aquí, podrían en futuro)
        if (centro != null && (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO || _uiState.value.tipoUsuario == TipoUsuario.PROFESOR || _uiState.value.tipoUsuario == TipoUsuario.ADMIN_CENTRO) ) {
            loadCursos(centro.id)
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
            claseSeleccionada = clase,
            firstInvalidField = if (it.firstInvalidField == AddUserFormField.CLASE) null else it.firstInvalidField,
            validationAttemptFailed = false
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
            fechaNacimientoError = error,
            firstInvalidField = if (error == null && it.firstInvalidField == AddUserFormField.FECHA_NACIMIENTO) null else it.firstInvalidField,
            validationAttemptFailed = false
        ) }
    }
    
    // Guarda un usuario en Firebase Authentication y Firestore
    fun saveUser() {
        if (!_uiState.value.isFormValid) {
             Timber.w("saveUser llamada con formulario inválido!")
             attemptSaveAndFocusError() // Trigger focus en el primer error
             return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, validationAttemptFailed = false, firstInvalidField = null) }
            
            try {
                val currentState = _uiState.value // Cachear estado actual

                // --- CASO ALUMNO --- 
                if (currentState.tipoUsuario == TipoUsuario.ALUMNO) {
                    Timber.d("Iniciando guardado de ALUMNO")
                    // Validar IDs necesarios para alumno
                    val centroId = currentState.centroSeleccionado?.id
                    val cursoId = currentState.cursoSeleccionado?.id
                    val claseId = currentState.claseSeleccionada?.id
                    
                    if (centroId.isNullOrBlank() || cursoId.isNullOrBlank() || claseId.isNullOrBlank()) {
                         Timber.e("Error: Faltan IDs necesarios para guardar alumno. Centro: $centroId, Curso: $cursoId, Clase: $claseId")
                         _uiState.update { it.copy(isLoading = false, error = "Faltan datos académicos (centro, curso o clase) para guardar el alumno.") }
                         return@launch
                    }

                    // Crear objeto Alumno (asumiendo que existe una clase Alumno)
                    // Mapear los IDs y nombres de curso/clase desde los objetos seleccionados
                    val alumno = Alumno(
                        dni = currentState.dni,
                        nombre = currentState.nombre,
                        apellidos = currentState.apellidos,
                        fechaNacimiento = currentState.fechaNacimiento, // Asegurar formato correcto si es necesario
                        telefono = currentState.telefono, // Opcional para Alumno?
                        centroId = centroId,
                        curso = currentState.cursoSeleccionado.nombre, // cursoSeleccionado no es nulo aquí
                        clase = currentState.claseSeleccionada.nombre, // claseSeleccionada no es nulo aquí
                        aulaId = currentState.claseSeleccionada.id, // claseSeleccionada e id no son nulos aquí
                        activo = true
                        // otros campos relevantes inicializados a vacío o por defecto
                    )
                    
                    // Llamar al método que guarda en ambas colecciones: alumnos y usuarios
                    // val saveResult = usuarioRepository.guardarAlumno(alumno) // Llamada anterior
                    val saveResult = usuarioRepository.registrarAlumno(alumno)

                    when (saveResult) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    showSuccessDialog = true,
                                    error = null
                                )
                            }
                            Timber.d("Alumno guardado correctamente con DNI: ${currentState.dni}")
                        }
                        is Result.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "Error al guardar alumno en Firestore: ${saveResult.exception?.message}"
                                )
                            }
                            Timber.e(saveResult.exception, "Error al guardar alumno en Firestore")
                        }
                        else -> { /* Ignorar estado Loading */ }
                    }
                    
                    return@launch // Salir de la coroutine para no ejecutar el código de Auth
                }

                // --- OTROS TIPOS DE USUARIO (REQUIEREN AUTH) --- 
                Timber.d("Iniciando guardado de usuario tipo: ${currentState.tipoUsuario}")
                val email = currentState.email
                val password = currentState.password
                
                // Doble validación por seguridad
                if (email.isBlank() || password.isBlank()) {
                    Timber.e("Error: Email o Password vacíos al intentar crear cuenta Auth para tipo ${currentState.tipoUsuario}")
                    _uiState.update { it.copy(isLoading = false, error = "El email y la contraseña no pueden estar vacíos.") }
                    return@launch
                }
                
                // 1. Crear usuario en Firebase Authentication
                val authResult = if (currentState.tipoUsuario == TipoUsuario.ADMIN_CENTRO || 
                                    currentState.tipoUsuario == TipoUsuario.PROFESOR) {
                    centroRepository.createUserWithEmailAndPassword(email, password)
                } else {
                    usuarioRepository.crearUsuarioConEmailYPassword(email, password)
                }
                
                when (authResult) {
                    is Result.Success -> {
                         // Obtener el UID directamente, ya que ambos métodos devuelven Result<String>
                         var uid: String = authResult.data as String
                         
                         if (currentState.tipoUsuario == TipoUsuario.ADMIN_CENTRO || currentState.tipoUsuario == TipoUsuario.PROFESOR) {
                             Timber.d("Usuario Auth (Centro/Prof) creado con UID: $uid")
                         } else {
                             Timber.d("Usuario Auth (AdminApp/Familiar) creado con UID: $uid")
                         }
                         
                         // Validar que se obtuvo un UID
                         if (uid.isBlank()) {
                             Timber.e("Error: UID nulo después de crear usuario en Auth para tipo ${currentState.tipoUsuario}.")
                             _uiState.update { it.copy(isLoading = false, error = "Error interno al obtener ID de usuario.") }
                             return@launch
                         }

                        // 2. Crear objeto Usuario
                        val perfiles = createPerfiles() // Crear perfiles ANTES de crear el objeto Usuario
                        if (perfiles.isEmpty() && currentState.tipoUsuario != TipoUsuario.FAMILIAR && currentState.tipoUsuario != TipoUsuario.ADMIN_APP) {
                            // Si se requiere un perfil con centroId (ADMIN_CENTRO, PROFESOR) y falló,
                            // no continuar con el guardado en Firestore.
                              Timber.e("Error: No se pudieron crear perfiles válidos para el usuario tipo ${currentState.tipoUsuario}.")
                              _uiState.update { it.copy(isLoading = false, error = "Error al crear el perfil de usuario. Verifique la selección del centro.") }
                              // Considerar eliminar el usuario de Auth si falla la creación de perfil?
                              return@launch
                         }

                         // Crear objeto Usuario y guardarlo en una variable para usarlo después
                         val usuario = Usuario(
                              dni = currentState.dni,
                              email = currentState.email,
                              nombre = currentState.nombre,
                              apellidos = currentState.apellidos,
                              telefono = currentState.telefono,
                              fechaRegistro = com.google.firebase.Timestamp.now(),
                              perfiles = perfiles,
                              activo = true // Por defecto activo al crear
                          )
                         
                         // 3. Guardar usuario en Firestore
                         Timber.d("Guardando usuario en Firestore con ID (DNI): ${usuario.dni}")
                         val saveResult = usuarioRepository.guardarUsuario(usuario)
                         
                         when (saveResult) {
                             is Result.Success -> {
                                 _uiState.update { 
                                     it.copy(
                                         isLoading = false,
                                         showSuccessDialog = true,
                                         error = null
                                     )
                                 }
                                 Timber.d("Usuario (${currentState.tipoUsuario}) guardado correctamente en Firestore con ID: $uid")
                             }
                             is Result.Error -> {
                                 _uiState.update { 
                                     it.copy(
                                         isLoading = false,
                                         error = "Error al guardar usuario en Firestore: ${saveResult.exception?.message}"
                                     )
                                 }
                                 Timber.e(saveResult.exception, "Error al guardar usuario (${currentState.tipoUsuario}) en Firestore con ID: $uid")
                                 // Considerar eliminar el usuario de Auth si falla el guardado en Firestore?
                             }
                             else -> { /* Ignorar estado Loading */ }
                         }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al crear cuenta en Firebase: ${authResult.exception?.message}"
                            )
                        }
                        Timber.e(authResult.exception, "Error al crear cuenta en Firebase para tipo ${currentState.tipoUsuario}")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}",
                        showSuccessDialog = false
                    )
                }
                Timber.e(e, "Error inesperado al guardar usuario")
            } finally {
                _uiState.update { it.copy(validationAttemptFailed = false, firstInvalidField = null) }
            }
        }
    }
    
    // Crea la lista de perfiles según el tipo de usuario
    private fun createPerfiles(): List<Perfil> {
        val perfiles = mutableListOf<Perfil>()
        
        // Crear perfil según el tipo de usuario
        when (_uiState.value.tipoUsuario) {
            TipoUsuario.ADMIN_APP -> {
                perfiles.add(Perfil(
                    tipo = TipoUsuario.ADMIN_APP,
                    verificado = true
                ))
                Timber.d("Creando perfil de ADMIN_APP")
            }
            TipoUsuario.ADMIN_CENTRO -> {
                val centroId = _uiState.value.centroSeleccionado?.id
                if (!centroId.isNullOrBlank()) {
                    perfiles.add(Perfil(
                        tipo = TipoUsuario.ADMIN_CENTRO,
                        centroId = centroId,
                        verificado = true
                    ))
                    Timber.d("Creando perfil de ADMIN_CENTRO para centro: $centroId")
                } else {
                    Timber.e("Error: centroId nulo o vacío al crear perfil de ADMIN_CENTRO")
                    // Considerar lanzar excepción o devolver lista vacía para indicar fallo
                }
            }
            TipoUsuario.PROFESOR -> {
                val centroId = _uiState.value.centroSeleccionado?.id
                if (!centroId.isNullOrBlank()) {
                    perfiles.add(Perfil(
                        tipo = TipoUsuario.PROFESOR,
                        centroId = centroId,
                        verificado = true
                    ))
                    Timber.d("Creando perfil de PROFESOR para centro: $centroId")
                } else {
                    Timber.e("Error: centroId nulo o vacío al crear perfil de PROFESOR")
                    // Considerar lanzar excepción o devolver lista vacía para indicar fallo
                }
            }
            TipoUsuario.FAMILIAR -> {
                perfiles.add(Perfil(
                    tipo = TipoUsuario.FAMILIAR,
                    verificado = true
                ))
                Timber.d("Creando perfil de FAMILIAR")
            }
            TipoUsuario.ALUMNO -> {
                // Para alumnos se creará una entrada en la colección de alumnos
                // en otro método si es necesario
                Timber.d("No se crea perfil para ALUMNO (se gestiona aparte)")
            }
            TipoUsuario.DESCONOCIDO -> {
                // No crear ningún perfil para tipo desconocido
                Timber.w("Tipo de usuario DESCONOCIDO, no se crea perfil")
            }
        }
        
        // Mostrar perfiles creados para depuración
        perfiles.forEachIndexed { index, perfil ->
            Timber.d("Perfil #${index+1}: tipo=${perfil.tipo}, centroId=${perfil.centroId}, verificado=${perfil.verificado}")
        }
        
        return perfiles
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
            // Usar el formato correcto dd/MM/yyyy
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            formatter.parse(fecha)
            true
        } catch (e: DateTimeParseException) {
            Timber.w("Error al parsear fecha: $fecha - ${e.message}")
            false
        }
    }

    // --- INICIO NUEVA FUNCIÓN ---
    /**
     * Maneja la selección de un curso por parte del usuario.
     * Actualiza el estado con el curso seleccionado y dispara la carga de las clases
     * correspondientes a ese curso.
     */
    fun onCursoSelected(cursoId: String) {
        val cursoSeleccionado = _uiState.value.cursosDisponibles.find { it.id == cursoId }
        if (cursoSeleccionado != null) {
            _uiState.update {
                it.copy(
                    cursoSeleccionado = cursoSeleccionado,
                    // Limpiar selección y lista de clases anteriores
                    claseSeleccionada = null,
                    clasesDisponibles = emptyList() 
                )
            }
            // Cargar las clases para el curso recién seleccionado
            loadClases(cursoId)
        } else {
            Timber.w("Curso seleccionado con ID $cursoId no encontrado en la lista.")
            _uiState.update { 
                it.copy(
                    error = "El curso seleccionado no es válido.",
                    cursoSeleccionado = null, 
                    claseSeleccionada = null,
                    clasesDisponibles = emptyList()
                ) 
            }
        }
    }
    // --- FIN NUEVA FUNCIÓN ---

    // --- Lógica de Guardado y Foco ---
    private fun findFirstInvalidField(): AddUserFormField? {
        val state = _uiState.value
        // Revisar errores en orden de aparición en la pantalla
        return when {
            // Sección Tipo Usuario / Centro
            (state.tipoUsuario == TipoUsuario.PROFESOR || state.tipoUsuario == TipoUsuario.ADMIN_CENTRO || state.tipoUsuario == TipoUsuario.ALUMNO) && state.centroSeleccionado == null -> AddUserFormField.CENTRO

            // Sección Info Personal
            state.dniError != null -> AddUserFormField.DNI
            state.nombreError != null -> AddUserFormField.NOMBRE
            state.apellidosError != null -> AddUserFormField.APELLIDOS
            state.telefonoError != null -> AddUserFormField.TELEFONO

             // Sección Alumno
            state.tipoUsuario == TipoUsuario.ALUMNO && state.fechaNacimientoError != null -> AddUserFormField.FECHA_NACIMIENTO
            state.tipoUsuario == TipoUsuario.ALUMNO && state.cursoSeleccionado == null -> AddUserFormField.CURSO // Asumiendo que curso es obligatorio si se es alumno
            state.tipoUsuario == TipoUsuario.ALUMNO && state.claseSeleccionada == null -> AddUserFormField.CLASE // Asumiendo que clase es obligatoria si se es alumno

            // Sección Credenciales
            state.tipoUsuario != TipoUsuario.ALUMNO && state.emailError != null -> AddUserFormField.EMAIL
            state.tipoUsuario != TipoUsuario.ALUMNO && state.passwordError != null -> AddUserFormField.PASSWORD
            state.tipoUsuario != TipoUsuario.ALUMNO && state.confirmPasswordError != null -> AddUserFormField.CONFIRM_PASSWORD

            else -> null // No hay errores O el error no está mapeado aquí
        }
    }

    fun attemptSaveAndFocusError() {
        val firstError = findFirstInvalidField()
        // Asegurarse de que el formulario realmente NO es válido según la lógica completa
        if (!_uiState.value.isFormValid) {
             _uiState.update {
                it.copy(
                    // Establecer el primer error encontrado (puede ser null si isFormValid falla por otra razón)
                    firstInvalidField = firstError,
                    validationAttemptFailed = true // Activar el trigger
                )
            }
        } else {
             // Si isFormValid es true pero esta función fue llamada, es un estado inconsistente.
             // Resetear por si acaso.
              _uiState.update { it.copy(validationAttemptFailed = false, firstInvalidField = null) }
              Timber.w("attemptSaveAndFocusError llamada cuando isFormValid es true.")
        }
    }

     fun clearValidationAttemptTrigger() {
        // Llamado desde el LaunchedEffect después de intentar enfocar
        if (_uiState.value.validationAttemptFailed) {
             _uiState.update { it.copy(validationAttemptFailed = false) }
        }
    }

    // --- Feedback de Éxito ---
    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }
}