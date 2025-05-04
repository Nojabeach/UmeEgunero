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
    val isTipoUsuarioBloqueado: Boolean = false, // Nueva bandera para bloquear el tipo de usuario
    val firstInvalidField: AddUserFormField? = null, // Para scroll al error
    val validationAttemptFailed: Boolean = false, // Trigger para scroll al error
    val alergias: String = "",
    val medicacion: String = "",
    val necesidadesEspeciales: String = "",
    val observaciones: String = ""
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
    private val usuarioRepository: UsuarioRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los parámetros recibidos de la navegación.
     * Esta función debe llamarse una sola vez desde el Composable.
     */
    fun initialize(
        centroId: String?, // ID del centro pasado como argumento (puede ser null)
        bloqueado: Boolean, // Indicador de bloqueo pasado como argumento
        tipoUsuarioStr: String?, // Tipo de usuario pasado como argumento
        isAdminAppFlag: Boolean // Indica si el usuario actual es Admin App
    ) {
        // Solo inicializar una vez
        if (_uiState.value.initialCentroId != null && !_uiState.value.isLoading) return // Evitar reinicialización si ya está cargado

        Timber.d(">>> Iniciando AddUserViewModel.initialize <<<")
        Timber.d("Params: centroId=$centroId, bloqueado=$bloqueado, tipoUsuarioStr=$tipoUsuarioStr, isAdminAppFlag=$isAdminAppFlag")

        // 1. Determinar TipoUsuario
        val tipoUsuario = tipoUsuarioStr?.let { tipo ->
            when (tipo.uppercase()) {
                "ADMIN", "ADMIN_APP" -> TipoUsuario.ADMIN_APP
                "CENTRO", "ADMIN_CENTRO" -> TipoUsuario.ADMIN_CENTRO
                "PROFESOR" -> TipoUsuario.PROFESOR
                "FAMILIAR" -> TipoUsuario.FAMILIAR
                "ALUMNO" -> TipoUsuario.ALUMNO
                else -> TipoUsuario.FAMILIAR
            }
        } ?: TipoUsuario.FAMILIAR
        Timber.d("Tipo Usuario Determinado: $tipoUsuario")

        // 2. Determinar si el tipo de usuario debe estar bloqueado
        val bloquearTipoUsuario = tipoUsuario == TipoUsuario.ADMIN_CENTRO || // Siempre bloqueado al editar Admin Centro
                                  (tipoUsuario == TipoUsuario.PROFESOR && !isAdminAppFlag) || // Bloqueado si Admin Centro crea Profesor
                                  tipoUsuario == TipoUsuario.ALUMNO // Bloqueado si se preselecciona Alumno

        Timber.d("Bloquear Tipo Usuario Determinado: $bloquearTipoUsuario")

        // 3. Determinar si el centro debe estar bloqueado
        // Prioridad: Argumento 'bloqueado' > Lógica interna (AdminCentro, Profesor creado por AdminCentro)
        val isCentroBloqueado = bloqueado || // Si el argumento lo fuerza
                                (tipoUsuario == TipoUsuario.ADMIN_CENTRO && !isAdminAppFlag) || // Si es Admin Centro creado por otro Admin Centro
                                (tipoUsuario == TipoUsuario.PROFESOR && !isAdminAppFlag) // Si Admin Centro crea Profesor
        Timber.d("Centro Bloqueado Determinado: $isCentroBloqueado")

        // 4. Actualizar estado inicial con tipos y bloqueos
        _uiState.update {
            it.copy(
                tipoUsuario = tipoUsuario,
                isAdminApp = isAdminAppFlag,
                isTipoUsuarioBloqueado = bloquearTipoUsuario,
                isCentroBloqueado = isCentroBloqueado,
                isLoading = true // Indicar carga
            )
        }
        Timber.d("Estado UI actualizado (pre-carga centro): tipo=$tipoUsuario, centroBloq=$isCentroBloqueado, tipoBloq=$bloquearTipoUsuario")

        // 5. Decidir cómo cargar/seleccionar el centro
        viewModelScope.launch {
            try {
                if (isCentroBloqueado) {
                    // --- CASO CENTRO BLOQUEADO ---
                    Timber.d("Centro bloqueado. Priorizando centroId de argumento si existe: '$centroId'")
                    
                    if (!centroId.isNullOrBlank()) {
                        // Usar el centroId proporcionado en los argumentos
                        Timber.d("Usando centroId del argumento: $centroId")
                        _uiState.update {
                            it.copy(
                                initialCentroId = centroId,
                                centroId = centroId
                                // isLoading sigue true hasta que loadCentrosAndSelectInitial termine
                            )
                        }
                        loadCentrosAndSelectInitial() // Carga la lista y selecciona este centroId
                    } else {
                        // Si no vino centroId en los argumentos, intentar obtenerlo del admin actual como fallback
                        Timber.w("centroId del argumento es nulo/blanco. Intentando obtener centro del admin actual.")
                        val centroIdAdmin = obtenerCentroIdAdminActual() // Esta función ya actualiza el UI state
                        
                        if (!centroIdAdmin.isNullOrEmpty()) {
                            Timber.d("✅ Centro del admin obtenido vía fallback y UI State actualizado.")
                            // No hacer nada más, obtenerCentroIdAdminActual ya hizo el trabajo
                        } else {
                            Timber.e("❌ Fallback falló. No se pudo obtener centro ni por argumento ni por admin actual.")
                            _uiState.update { it.copy(isLoading = false, error = "No se pudo determinar el centro educativo.") }
                            // Opcionalmente, cargar lista vacía de centros
                            // loadCentros()
                        }
                    }
                } else {
                    // --- CASO CENTRO NO BLOQUEADO ---
                    Timber.d("Centro no bloqueado. Usando centroId de argumento si existe: $centroId")
                    // Actualizar initialCentroId y centroId con el valor del argumento (puede ser null)
                     _uiState.update {
                        it.copy(
                            initialCentroId = centroId,
                            centroId = centroId ?: ""
                            // isLoading sigue true hasta que se carguen los centros
                        )
                    }

                    if (centroId != null) {
                        // Si se pasó un centroId, cargar la lista e intentar seleccionarlo
                        Timber.d("Llamando a loadCentrosAndSelectInitial con centroId: $centroId")
                        loadCentrosAndSelectInitial()
                    } else {
                        // Si no se pasó centroId y no está bloqueado, solo cargar la lista
                        Timber.d("Llamando a loadCentros (sin preselección)")
                        loadCentros()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error general en initialize durante la carga del centro: ${e.message}")
                _uiState.update {
                    it.copy(
                        error = "Error al inicializar: ${e.message}",
                        isLoading = false
                    )
                }
            } finally {
                 // Asegurarse de que isLoading se desactive si no lo hizo antes
                 if (_uiState.value.isLoading) {
                     _uiState.update { it.copy(isLoading = false) }
                     Timber.d("Finalizando initialize (asegurando isLoading=false)")
                 }
            }
        }
    }
    
    /**
     * Obtiene el ID del centro del administrador actualmente autenticado
     * y también carga y selecciona el centro correspondiente
     */
    private suspend fun obtenerCentroIdAdminActual(): String? {
        try {
            val currentUser = usuarioRepository.obtenerUsuarioActual() ?: return null
            
            // Buscar el perfil de tipo ADMIN_CENTRO
            val perfilAdminCentro = currentUser.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
            val centroId = perfilAdminCentro?.centroId
            
            Timber.d("🔍 Buscando centro del administrador actual: ${currentUser.nombre} ${currentUser.apellidos}")
            
            if (!centroId.isNullOrEmpty()) {
                Timber.d("✅ Centro ID encontrado en perfil: $centroId")
                
                // Cargar directamente el centro por ID para asegurar que se preseleccione
                val centroResult = centroRepository.getCentroById(centroId)
                if (centroResult is Result.Success && centroResult.data != null) {
                    val centro = centroResult.data as Centro
                    Timber.d("✅ Centro encontrado por ID: ${centro.nombre} (${centro.id})")
                    
                    // Cargar todos los centros para completar la lista disponible
                    val centrosResult = centroRepository.getActiveCentros()
                    if (centrosResult is Result.Success) {
                        val centros = centrosResult.data
                        
                        Timber.d("📋 Total centros disponibles: ${centros.size}")
                        Timber.d("🎯 Preseleccionando centro: ${centro.nombre}")
                        
                        // Actualizar el estado UI para mostrar el centro seleccionado
                        _uiState.update {
                            it.copy(
                                centrosDisponibles = centros,
                                centroSeleccionado = centro,
                                centroId = centro.id,  // Asegurarse de que centroId esté actualizado también
                                isLoading = false
                            )
                        }
                        
                        // Verificar que el centro está correctamente seleccionado
                        if (_uiState.value.centroSeleccionado?.id == centro.id) {
                            Timber.d("✅ Centro seleccionado correctamente: ${centro.nombre}")
                        } else {
                            Timber.w("⚠️ Posible problema al seleccionar centro: estado actual=${_uiState.value.centroSeleccionado?.nombre ?: "ninguno"}")
                        }
                        
                        // Si estamos creando un alumno, cargar los cursos automáticamente
                        if (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO) {
                            loadCursos(centro.id)
                        }
                    } else {
                        Timber.e("❌ Error al cargar lista de centros")
                    }
                } else {
                    Timber.e("❌ No se pudo cargar el centro con ID: $centroId")
                }
            } else {
                Timber.w("⚠️ No se encontró centroId en el perfil del administrador actual")
            }
            
            return centroId
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al obtener centro del administrador actual")
            return null
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
            
            try {
                when (val result = centroRepository.getActiveCentros()) {
                    is Result.Success -> {
                        val centros = result.data
                        
                        // Buscar el centro por ID y asegurarse de encontrarlo
                        val centroInicial = centros.find { it.id == _uiState.value.initialCentroId }
                        
                        if (centroInicial != null) {
                            Timber.d("✅ Centro inicial encontrado: ${centroInicial.nombre} (${centroInicial.id})")
                            
                            // Actualizar estado con el centro seleccionado explícitamente
                            _uiState.update {
                                it.copy(
                                    centrosDisponibles = centros,
                                    centroSeleccionado = centroInicial,
                                    centroId = centroInicial.id, // Asegurarse de que centroId también se actualice
                                    isLoading = false
                                )
                            }
                            
                            // Verificar que el centro está correctamente seleccionado en el estado
                            if (_uiState.value.centroSeleccionado?.id == centroInicial.id) {
                                Timber.d("✅ Centro seleccionado correctamente en UI State: ${centroInicial.nombre}")
                            } else {
                                Timber.w("⚠️ Problema al seleccionar centro: estado actual=${_uiState.value.centroSeleccionado?.nombre ?: "ninguno"}")
                            }
                            
                            // Si se seleccionó un centro, cargar sus cursos si es necesario (ej. para Alumno)
                            if (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO) {
                                loadCursos(centroInicial.id)
                            }
                        } else {
                            // Si no encontramos el centro por ID pero tenemos la lista de centros,
                            // comprobar si hay un centroId en el estado actual que podamos usar
                            val currentCentroId = _uiState.value.centroId
                            if (currentCentroId.isNotEmpty()) {
                                val centroActual = centros.find { it.id == currentCentroId }
                                if (centroActual != null) {
                                    Timber.d("✅ Usando centroId actual: ${centroActual.nombre} (${centroActual.id})")
                                    _uiState.update {
                                        it.copy(
                                            centrosDisponibles = centros,
                                            centroSeleccionado = centroActual,
                                            isLoading = false
                                        )
                                    }
                                    
                                    // Si es un alumno, cargar los cursos para este centro
                                    if (_uiState.value.tipoUsuario == TipoUsuario.ALUMNO) {
                                        loadCursos(centroActual.id)
                                    }
                                    return@launch
                                }
                            }
                            
                            Timber.w("⚠️ No se encontró el centro con ID: ${_uiState.value.initialCentroId} entre los ${centros.size} centros disponibles")
                            _uiState.update {
                                it.copy(
                                    centrosDisponibles = centros,
                                    isLoading = false
                                )
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
            } catch (e: Exception) {
                Timber.e(e, "❌ Error inesperado en loadCentrosAndSelectInitial: ${e.message}")
                _uiState.update {
                    it.copy(
                        error = "Error al cargar centros: ${e.message}",
                        isLoading = false
                    )
                }
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
            !password.any { it.isLetter() } -> "La contraseña debe incluir al menos una letra"
            !password.any { it.isDigit() } -> "La contraseña debe incluir al menos un número"
            !password.any { !it.isLetterOrDigit() } -> "La contraseña debe incluir al menos un carácter especial"
            !validatePassword(password) -> "La contraseña no cumple los requisitos mínimos"
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
        // Determinar si el centro debe bloquearse para este tipo de usuario
        // Si ya estaba bloqueado, mantenerlo bloqueado
        val debeBloquearCentro = _uiState.value.isCentroBloqueado || 
                                tipoUsuario == TipoUsuario.ADMIN_CENTRO || 
                                (tipoUsuario == TipoUsuario.PROFESOR && !_uiState.value.isAdminApp)
        
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
                 // Resetear selección de centro si no es relevante (ej. admin app, familiar) y no está bloqueado
                centroSeleccionado = if ((tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) && !debeBloquearCentro) 
                                    null else it.centroSeleccionado,
                cursosDisponibles = if ((tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) && !debeBloquearCentro) 
                                   emptyList() else it.cursosDisponibles,
                clasesDisponibles = if ((tipoUsuario == TipoUsuario.ADMIN_APP || tipoUsuario == TipoUsuario.FAMILIAR) && !debeBloquearCentro) 
                                   emptyList() else it.clasesDisponibles,
                firstInvalidField = null, // Limpiar foco de error al cambiar tipo
                validationAttemptFailed = false,
                isCentroBloqueado = debeBloquearCentro
            )
        }
        
        Timber.d("Tipo de usuario actualizado a: $tipoUsuario, Centro bloqueado: ${_uiState.value.isCentroBloqueado}")
        
        // Si el nuevo tipo requiere centro y no está bloqueado, cargar centros si no están ya cargados
        if (!_uiState.value.isCentroBloqueado &&
            (tipoUsuario == TipoUsuario.ADMIN_CENTRO || tipoUsuario == TipoUsuario.PROFESOR || tipoUsuario == TipoUsuario.ALUMNO) &&
             _uiState.value.centrosDisponibles.isEmpty()) {
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
                    
                    val result = procesarGuardadoAlumno(currentState)
                    
                    when (result) {
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
                                    error = "Error al guardar alumno en Firestore: ${result.exception?.message}"
                                )
                            }
                            Timber.e(result.exception, "Error al guardar alumno en Firestore")
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
    
    /**
     * Procesa y guarda los datos de un alumno, realizando las validaciones
     * y la asignación a la clase correspondiente.
     *
     * @param currentState Estado actual de la UI con los datos del alumno
     * @return Result con el resultado de la operación
     */
    private suspend fun procesarGuardadoAlumno(currentState: AddUserUiState): Result<Any> {
        try {
            val centroId = currentState.centroSeleccionado?.id ?: ""
            val claseId = currentState.claseSeleccionada?.id ?: ""
            
            // Convertir listas de alergias y medicación desde strings separados por comas
            val alergiasList = currentState.alergias.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val medicacionList = currentState.medicacion.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Crear objeto Alumno
            val alumno = Alumno(
                dni = currentState.dni,
                nombre = currentState.nombre,
                apellidos = currentState.apellidos,
                fechaNacimiento = currentState.fechaNacimiento,
                telefono = currentState.telefono,
                email = currentState.email,
                centroId = centroId,
                curso = currentState.cursoSeleccionado?.nombre ?: "",
                clase = currentState.claseSeleccionada?.nombre ?: "",
                claseId = claseId,
                aulaId = currentState.claseSeleccionada?.aula ?: "",
                alergias = alergiasList,
                medicacion = medicacionList,
                necesidadesEspeciales = currentState.necesidadesEspeciales,
                observaciones = currentState.observaciones,
                activo = true
            )
            
            // Determinar si es edición o creación nueva
            val saveResult = if (currentState.isEditMode) {
                // Actualizar alumno existente
                Timber.d("Actualizando alumno existente con DNI: ${currentState.dni}")
                usuarioRepository.guardarAlumno(alumno)
            } else {
                // Crear nuevo alumno
                Timber.d("Registrando nuevo alumno con DNI: ${currentState.dni}")
                usuarioRepository.registrarAlumno(alumno)
            }
            
            // Actualizar la lista de alumnos de la clase si el guardado fue exitoso
            if (saveResult is Result.Success) {
                try {
                    Timber.d("Actualizando la lista de alumnos de la clase $claseId con el alumno ${alumno.dni}")
                    val asignacionResult = cursoRepository.asignarAlumnoAClase(alumno.dni, claseId)
                    
                    if (asignacionResult is Result.Error) {
                        Timber.e(asignacionResult.exception, "Error al asignar alumno a clase: ${asignacionResult.exception?.message}")
                        // Continuamos con el flujo normal, no bloqueamos por este error
                    } else {
                        Timber.d("Alumno asignado correctamente a la clase")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al intentar asignar el alumno a la clase: ${e.message}")
                    // Continuamos con el flujo normal, no bloqueamos por este error
                }
            }
            
            return saveResult
        } catch (e: Exception) {
            Timber.e(e, "Error en procesarGuardadoAlumno: ${e.message}")
            return Result.Error(e)
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
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toIntOrNull() ?: return false
        return dni.uppercase()[8] == letras[numero % 23]
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

    /**
     * Carga los datos de un usuario existente a partir de su DNI para edición
     * Esta función se llama cuando se accede al formulario en modo edición
     * 
     * @param dni DNI del usuario a editar
     */
    fun cargarUsuarioPorDni(dni: String) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }
            
            try {
                // Si estamos editando un alumno, primero buscamos en la colección alumnos
                val resultadoAlumno = usuarioRepository.getAlumnoPorDni(dni)
                
                if (resultadoAlumno is Result.Success<*>) {
                    val alumno = resultadoAlumno.data as Alumno
                    
                    // Buscar el curso y la clase para este alumno
                    val cursosResult = cursoRepository.obtenerCursosPorCentro(alumno.centroId)
                    if (cursosResult is Result.Success<*>) {
                        val cursos = cursosResult.data as List<Curso>
                        _uiState.update { state -> state.copy(cursosDisponibles = cursos) }
                        
                        // Seleccionar curso si está disponible
                        val cursoSeleccionado = cursos.find { curso -> curso.id == alumno.curso }
                        if (cursoSeleccionado != null) {
                            _uiState.update { state -> state.copy(cursoSeleccionado = cursoSeleccionado) }
                            
                            // Cargar clases del curso
                            val clasesResult = cursoRepository.obtenerClasesPorCurso(cursoSeleccionado.id)
                            if (clasesResult is Result.Success<*>) {
                                val clases = clasesResult.data as List<Clase>
                                _uiState.update { state -> state.copy(clasesDisponibles = clases) }
                                
                                // Seleccionar clase si está disponible
                                val claseSeleccionada = clases.find { clase -> clase.id == alumno.claseId }
                                if (claseSeleccionada != null) {
                                    _uiState.update { state -> state.copy(claseSeleccionada = claseSeleccionada) }
                                }
                            }
                        }
                    }
                    
                    // Buscar el centro para este alumno
                    val centroResult = centroRepository.getCentroById(alumno.centroId)
                    if (centroResult is Result.Success<*>) {
                        _uiState.update { state -> state.copy(centroSeleccionado = centroResult.data as Centro) }
                    }
                    
                    // Actualizar todos los campos del formulario con los datos del alumno y usuario
                    _uiState.update { state ->
                        state.copy(
                            dni = alumno.dni,
                            nombre = alumno.nombre,
                            apellidos = alumno.apellidos,
                            telefono = alumno.telefono,
                            email = alumno.email,
                            fechaNacimiento = alumno.fechaNacimiento,
                            tipoUsuario = TipoUsuario.ALUMNO,
                            isEditMode = true,
                            isLoading = false,
                            // Información médica
                            alergias = alumno.alergias.joinToString(", "),
                            medicacion = alumno.medicacion.joinToString(", "),
                            necesidadesEspeciales = alumno.necesidadesEspeciales,
                            observaciones = alumno.observaciones,
                            // Para administradores de centro y profesores, bloquear el cambio de tipo
                            isCentroBloqueado = false
                        )
                    }
                    
                    return@launch
                }
                
                // Si no es un alumno, intentamos cargar un usuario general
                val resultadoUsuario = usuarioRepository.getUsuarioPorDni(dni)
                
                if (resultadoUsuario is Result.Success<*>) {
                    val usuario = resultadoUsuario.data as Usuario
                    
                    // Determinar el tipo de usuario y el centro según los perfiles
                    var tipoUsuario = TipoUsuario.FAMILIAR // Valor por defecto
                    var centroId = ""
                    
                    if (usuario.perfiles.isNotEmpty()) {
                        // Buscar el perfil específico según prioridad
                        // Prioridad: ADMIN_CENTRO > PROFESOR > FAMILIAR > ADMIN_APP
                        val perfilAdminCentro = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                        val perfilProfesor = usuario.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                        val perfilAdminApp = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_APP }
                        val perfilFamiliar = usuario.perfiles.find { it.tipo == TipoUsuario.FAMILIAR }
                        
                        // Asignar tipo y centroId según la prioridad encontrada
                        when {
                            perfilAdminCentro != null -> {
                                tipoUsuario = TipoUsuario.ADMIN_CENTRO
                                centroId = perfilAdminCentro.centroId
                                Timber.d("Usuario identificado como ADMIN_CENTRO para centro: $centroId")
                            }
                            perfilProfesor != null -> {
                                tipoUsuario = TipoUsuario.PROFESOR
                                centroId = perfilProfesor.centroId
                                Timber.d("Usuario identificado como PROFESOR para centro: $centroId")
                            }
                            perfilAdminApp != null -> {
                                tipoUsuario = TipoUsuario.ADMIN_APP
                                Timber.d("Usuario identificado como ADMIN_APP")
                            }
                            perfilFamiliar != null -> {
                                tipoUsuario = TipoUsuario.FAMILIAR
                                Timber.d("Usuario identificado como FAMILIAR")
                            }
                        }
                        
                        // Cargar el centro si tiene uno asignado
                        if (centroId.isNotBlank()) {
                            val centroResult = centroRepository.getCentroById(centroId)
                            if (centroResult is Result.Success<*>) {
                                _uiState.update { state -> state.copy(centroSeleccionado = centroResult.data as Centro) }
                            }
                        }
                    }
                    
                    // Determinar si el centro debe estar bloqueado
                    // - Siempre bloqueado para Admin Centro
                    // - Bloqueado para Profesor si el usuario actual es Admin Centro (no es Admin App)
                    val bloquearCentro = tipoUsuario == TipoUsuario.ADMIN_CENTRO || 
                                        (tipoUsuario == TipoUsuario.PROFESOR && !_uiState.value.isAdminApp)
                    
                    // Actualizar el estado con los datos del usuario
                    _uiState.update { state ->
                        state.copy(
                            dni = usuario.dni,
                            email = usuario.email ?: "",
                            nombre = usuario.nombre,
                            apellidos = usuario.apellidos,
                            telefono = usuario.telefono ?: "",
                            tipoUsuario = tipoUsuario,
                            isEditMode = true,
                            // Para administrador de centro, bloquear el tipo de usuario
                            isTipoUsuarioBloqueado = tipoUsuario == TipoUsuario.ADMIN_CENTRO,
                            isCentroBloqueado = bloquearCentro,
                            isLoading = false
                        )
                    }
                    
                    Timber.d("Usuario cargado para edición: ${usuario.dni}, tipo: $tipoUsuario, centro bloqueado: $bloquearCentro")
                    
                } else {
                    // No se encontró el usuario
                    _uiState.update { state -> 
                        state.copy(
                            error = "No se encontró el usuario con DNI: $dni",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar usuario para edición")
                _uiState.update { state -> 
                    state.copy(
                        error = "Error al cargar datos: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    /**
     * Actualiza el texto de las alergias del alumno
     */
    fun updateAlergias(alergias: String) {
        _uiState.update { it.copy(alergias = alergias) }
    }
    
    /**
     * Actualiza el texto de la medicación del alumno
     */
    fun updateMedicacion(medicacion: String) {
        _uiState.update { it.copy(medicacion = medicacion) }
    }
    
    /**
     * Actualiza el texto de las necesidades especiales del alumno
     */
    fun updateNecesidadesEspeciales(necesidadesEspeciales: String) {
        _uiState.update { it.copy(necesidadesEspeciales = necesidadesEspeciales) }
    }
    
    /**
     * Actualiza el texto de las observaciones generales del alumno
     */
    fun updateObservaciones(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }

    /**
     * Función de validación de contraseña
     * Comprueba que la contraseña cumpla con los requisitos de seguridad:
     * - Al menos 6 caracteres
     * - Al menos una letra
     * - Al menos un dígito
     * - Al menos un carácter especial
     */
    private fun validatePassword(password: String): Boolean {
        return password.length >= 6 && 
               password.any { it.isLetter() } && 
               password.any { it.isDigit() } &&
               password.any { !it.isLetterOrDigit() } // Validar al menos un carácter especial
    }
}