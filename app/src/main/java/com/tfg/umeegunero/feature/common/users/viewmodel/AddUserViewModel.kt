package com.tfg.umeegunero.feature.common.users.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.StorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.google.firebase.Timestamp
import java.util.Date
import com.tfg.umeegunero.util.DateUtils
import com.tfg.umeegunero.data.model.UsuarioEstado
import com.tfg.umeegunero.data.model.Preferencias
import com.tfg.umeegunero.data.model.Notificaciones
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.google.android.gms.tasks.Tasks
import android.content.Context

// Enum para identificar los campos del formulario
enum class AddUserFormField {
    DNI, EMAIL, PASSWORD, CONFIRM_PASSWORD, NOMBRE, APELLIDOS, TELEFONO,
    CENTRO, FECHA_NACIMIENTO, CURSO, CLASE,
    NUMERO_SS, CONDICIONES_MEDICAS
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
    val numeroSS: String = "",
    val condicionesMedicas: String = "",
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
    val centroError: String? = null,
    val fechaNacimientoError: String? = null,
    val numeroSSError: String? = null,
    val condicionesMedicasError: String? = null,
    val cursoError: String? = null,
    val claseError: String? = null,
    val firstInvalidField: AddUserFormField? = null,
    val validationAttemptFailed: Boolean = false,
    val isEditMode: Boolean = false,
    val userId: String = "",
    val fechaNacimiento: String = "",
    val centroSeleccionado: Centro? = null,
    val centrosDisponibles: List<Centro> = emptyList(),
    val cursoSeleccionado: Curso? = null,
    val cursosDisponibles: List<Curso> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val clasesDisponibles: List<Clase> = emptyList(),
    val showCentroWarning: Boolean = false,
    val showCursosWarning: Boolean = false,
    val isTipoUsuarioBloqueado: Boolean = false,
    val isCentroSeleccionadoBloqueado: Boolean = false,
    val isAdminApp: Boolean = false,
    // Propiedades adicionales que hacían falta
    val initialCentroId: String? = null,
    val isCentroBloqueado: Boolean = false,
    val alergias: String = "",
    val medicacion: String = "",
    val necesidadesEspeciales: String = "",
    val observaciones: String = "",
    val observacionesMedicas: String = ""
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
    private val claseRepository: ClaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()
    
    // Instancia de Firebase Storage para operaciones de almacenamiento
    private val storage = FirebaseStorage.getInstance()

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
                                (tipoUsuario == TipoUsuario.PROFESOR && !isAdminAppFlag) || // Si Admin Centro crea Profesor
                                (!isAdminAppFlag) // Si es un admin de centro creando cualquier tipo de usuario

        Timber.d("Centro Bloqueado Determinado: $isCentroBloqueado")

        // 4. Actualizar estado inicial con tipos y bloqueos
        _uiState.update {
            it.copy(
                tipoUsuario = tipoUsuario,
                isAdminApp = isAdminAppFlag,
                isTipoUsuarioBloqueado = bloquearTipoUsuario,
                isCentroBloqueado = isCentroBloqueado,
                isCentroSeleccionadoBloqueado = isCentroBloqueado, // También bloquear la selección de centro
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
                        loadCentrosAndSelectInitial(centroId) // Carga la lista y selecciona este centroId
                    } else {
                        // Para administradores de centro, siempre obtener su centro asociado
                        Timber.d("Intentando obtener centro del admin actual.")
                        val centroIdAdmin = obtenerCentroIdAdminActual() // Esta función ya actualiza el UI state
                        
                        if (!centroIdAdmin.isNullOrEmpty()) {
                            Timber.d("✅ Centro del admin obtenido y UI State actualizado: $centroIdAdmin")
                            // No hacer nada más, obtenerCentroIdAdminActual ya hizo el trabajo
                        } else {
                            Timber.e("❌ No se pudo obtener centro del admin actual.")
                            _uiState.update { it.copy(isLoading = false, error = "No se pudo determinar el centro educativo.") }
                        }
                    }
                } else {
                    // --- CASO CENTRO NO BLOQUEADO (solo para admin app) ---
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
                        loadCentrosAndSelectInitial(centroId)
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

    /**
     * Carga los centros disponibles y selecciona el inicial
     *
     * @param centroIdInicial ID del centro a seleccionar, puede ser null
     */
    private fun loadCentrosAndSelectInitial(centroIdInicial: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                when (val result = centroRepository.getActiveCentros()) {
                    is Result.Success -> {
                        val centros = result.data
                        Timber.d("✅ Centros cargados: ${centros.size}")
                        
                        // Si hay un centro ID inicial, intentar seleccionarlo
                        if (!centroIdInicial.isNullOrEmpty()) {
                            Timber.d("🔍 Buscando centro con ID: $centroIdInicial")
                            
                            val centroSeleccionado = centros.find { it.id == centroIdInicial }
                            if (centroSeleccionado != null) {
                                Timber.d("✅ Centro encontrado y seleccionado: ${centroSeleccionado.nombre}")
                                _uiState.update {
                                    it.copy(
                                        centrosDisponibles = centros,
                                        centroSeleccionado = centroSeleccionado,
                                        isLoading = false
                                    )
                                }
                                return@launch
                            }
                        }
                        
                        Timber.w("⚠️ No se encontró el centro con ID: $centroIdInicial entre los ${centros.size} centros disponibles")
                        _uiState.update {
                            it.copy(
                                centrosDisponibles = centros,
                                isLoading = false
                            )
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
    
    /**
     * Guarda un nuevo usuario
     */
    fun saveUser() {
        Timber.d("⏳ Iniciando guardado/actualización de usuario...")
        
        // Validar todos los campos
        val validatedState = validateFields(_uiState.value)
        
        // Si hay errores, actualizar el estado y no seguir
        if (validatedState.firstInvalidField != null) {
            _uiState.update { 
                validatedState.copy(validationAttemptFailed = true) 
            }
            return
        }
        
        // Si pasa la validación, comenzar proceso de guardado
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val state = _uiState.value
                
                // Determinar si es una creación nueva o actualización
                if (state.isEditMode) {
                    // Actualizar usuario existente
                    updateExistingUser(state)
                } else {
                    // Crear nuevo usuario
                    createNewUser(state)
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error inesperado al guardar usuario: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al guardar: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }
    
    private suspend fun createNewUser(state: AddUserUiState) {
        try {
            Timber.d("⏳ Creando nuevo usuario con tipo: ${state.tipoUsuario}")
            
            when (state.tipoUsuario) {
                TipoUsuario.ALUMNO -> createNewAlumno(state)
                else -> createNewRegularUser(state)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al crear nuevo usuario: ${e.message}")
            _uiState.update { it.copy(
                error = "Error al crear usuario: ${e.message}",
                isLoading = false
            )}
        }
    }
    
    private suspend fun createNewAlumno(state: AddUserUiState) {
        try {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Timber.d("Creando nuevo alumno con DNI: ${state.dni}")
            
            // PASO 1: Obtener la URL del avatar antes de crear el alumno
            Timber.d("PASO 1: Subiendo avatar para el alumno")
            val avatarUrl = withContext(Dispatchers.IO) {
                try {
                    val url = subirAvatarDesdeAssets(TipoUsuario.ALUMNO)
                    Timber.d("Avatar de alumno subido correctamente: $url")
                    url
                } catch (e: Exception) {
                    Timber.e(e, "Error al subir avatar de alumno: ${e.message}, intentando método alternativo")
                    try {
                        val urlAlternativa = subirAvatarDesdeDrawable(TipoUsuario.ALUMNO)
                        Timber.d("Avatar de alumno subido con método alternativo: $urlAlternativa")
                        urlAlternativa
                    } catch (e2: Exception) {
                        Timber.e(e2, "Error en método alternativo para subir avatar: ${e2.message}")
                        ""
                    }
                }
            }
            
            // PASO 2: Crear objeto alumno CON la URL del avatar
            val alumno = Alumno(
                id = UUID.randomUUID().toString(),
                dni = state.dni,
                nombre = state.nombre,
                apellidos = state.apellidos,
                fechaNacimiento = DateUtils.parseDateString(state.fechaNacimiento),
                centroId = state.centroSeleccionado?.id ?: "",
                claseId = state.claseSeleccionada?.id ?: "",
                curso = state.cursoSeleccionado?.nombre ?: "",
                clase = state.claseSeleccionada?.nombre ?: "",
                numeroSS = state.numeroSS,
                condicionesMedicas = state.condicionesMedicas,
                alergias = if (state.alergias.isNotEmpty()) state.alergias.split(",").map { it.trim() } else emptyList(),
                medicacion = if (state.medicacion.isNotEmpty()) state.medicacion.split(",").map { it.trim() } else emptyList(),
                necesidadesEspeciales = state.necesidadesEspeciales,
                observaciones = state.observaciones,
                observacionesMedicas = state.observacionesMedicas,
                avatarUrl = avatarUrl // URL del avatar
            )
            
            Timber.d("Alumno creado: ${alumno.nombre} ${alumno.apellidos} con DNI: ${alumno.dni}, avatar: $avatarUrl")
            
            // PASO 3: Guardar el alumno en Firestore
            withContext(Dispatchers.IO) {
                try {
                    // Obtener el contexto para pasarlo al repositorio
                    val context = getApplication<Application>().applicationContext
                    val result = usuarioRepository.guardarAlumno(alumno, context)
                    
                    if (result is Result.Error) {
                        throw result.exception ?: Exception("Error desconocido al guardar alumno")
                    }
                    
                    Timber.d("Alumno guardado correctamente en Firestore con avatar")
                    
                    // Ya no necesitamos crear un usuario básico aquí, lo hace guardarAlumno
                } catch (e: Exception) {
                    Timber.e(e, "Error al guardar alumno en Firestore: ${e.message}")
                    throw e
                }
            }
            
            // Actualizar UI
            _uiState.update { it.copy(
                isLoading = false,
                success = true,
                error = null
            )}
        } catch (e: Exception) {
            Timber.e(e, "Error general al crear alumno: ${e.message}")
            _uiState.update { it.copy(
                isLoading = false,
                error = "Error al crear alumno: ${e.message}"
            )}
        }
    }
    
    /**
     * Crea un usuario regular (no alumno)
     */
    private suspend fun createNewRegularUser(state: AddUserUiState) {
        try {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("Creando nuevo usuario regular. Tipo seleccionado: ${state.tipoUsuario}")
            
            // Verificar que tenemos un tipo de usuario seleccionado
            if (state.tipoUsuario == null) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Debe seleccionar un tipo de usuario"
                )}
                return
            }
            
            // Verificar si es admin de centro y tiene centro seleccionado
            if (state.tipoUsuario == TipoUsuario.ADMIN_CENTRO && state.centroSeleccionado == null) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Debe seleccionar un centro para el administrador"
                )}
                return
            }

            // Si es profesor, debe tener centro seleccionado
            if (state.tipoUsuario == TipoUsuario.PROFESOR && state.centroSeleccionado == null) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Debe seleccionar un centro para el profesor"
                )}
                return
            }
            
            // PASO 1: Obtener la URL del avatar antes de crear el usuario
            Timber.d("PASO 1: Subiendo avatar para el tipo de usuario: ${state.tipoUsuario}")
            val avatarUrl = withContext(Dispatchers.IO) {
                try {
                    // Especial para admin de centro - subir como centro.png
                    if (state.tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
                        Timber.d("Subiendo avatar para ADMIN_CENTRO")
                    }
                    val url = subirAvatarDesdeAssets(state.tipoUsuario)
                    Timber.d("Avatar subido correctamente: $url")
                    url
                } catch (e: Exception) {
                    Timber.e(e, "Error al subir avatar: ${e.message}, intentando método alternativo")
                    // Intentar método alternativo si falla
                    try {
                        val urlAlternativa = subirAvatarDesdeDrawable(state.tipoUsuario)
                        Timber.d("Avatar subido correctamente con método alternativo: $urlAlternativa")
                        urlAlternativa
                    } catch (e2: Exception) {
                        Timber.e(e2, "Error en método alternativo para subir avatar: ${e2.message}")
                        // Devolver URL por defecto en caso de fallo total
                        ""
                    }
                }
            }
            
            // PASO 2: Verificar primero si el email ya existe en Firebase Auth
            Timber.d("PASO 2: Verificando si el email ya existe en Firebase Authentication")
            var emailExiste = false
            var firebaseUid = ""
            
            try {
            val authResult = withContext(Dispatchers.IO) {
                    val signInMethods = Tasks.await(firebaseAuth.fetchSignInMethodsForEmail(state.email))
                    if ((signInMethods.signInMethods?.size ?: 0) > 0) {
                        emailExiste = true
                        Timber.w("El email ${state.email} ya está registrado en Firebase Auth")
                        null
                    } else {
                        emailExiste = false
                        Timber.d("El email ${state.email} no existe en Firebase Auth, continuando con la creación")
                        try {
                            Tasks.await(firebaseAuth.createUserWithEmailAndPassword(state.email, state.password))
                } catch (e: Exception) {
                            // Doble verificación por si hay discrepancia entre fetchSignInMethods y createUser
                            if (e.message?.contains("email address is already in use") == true) {
                                emailExiste = true
                                Timber.w("Conflicto detectado: El email existe pero fetchSignInMethods no lo detectó")
                                null
                            } else {
                    throw e
                }
            }
                    }
                }
                
                if (emailExiste) {
                    // Si el email ya existe, mostrar mensaje claro al usuario y detener el proceso
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "El correo electrónico ${state.email} ya está registrado. Por favor, utiliza otro email para registrarte o inicia sesión con este."
                    )}
                    return
                }
                
                // Si continuamos, es que el usuario se creó con éxito
                firebaseUid = authResult?.user?.uid ?: throw Exception("Error al obtener UID de Firebase Auth")
            Timber.d("Cuenta creada en Firebase Authentication con UID: $firebaseUid")
            } catch (e: Exception) {
                if (e.message?.contains("email address is already in use") == true) {
                    Timber.e(e, "El email ya está en uso: ${state.email}")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "El correo electrónico ya está registrado. Por favor, utiliza otro email o inicia sesión con este."
                    )}
                    return
                } else {
                    Timber.e(e, "Error al verificar/crear usuario en Firebase Auth: ${e.message}")
                    throw e
                }
            }
            
            // PASO 3: Crear el perfil principal
            Timber.d("PASO 3: Creando perfil principal para tipo ${state.tipoUsuario}")
            val perfilPrincipal = Perfil(
                tipo = state.tipoUsuario,
                centroId = state.centroSeleccionado?.id ?: "",
                verificado = true
            )
            
            Timber.d("Perfil principal creado: ${perfilPrincipal.tipo} para centro: ${perfilPrincipal.centroId}")

            // PASO 4: Crear objeto usuario CON la URL del avatar ya incluida
            Timber.d("PASO 4: Creando usuario en Firestore con avatarUrl: $avatarUrl")
            val usuario = Usuario(
                dni = state.dni,
                nombre = state.nombre,
                apellidos = state.apellidos,
                telefono = state.telefono,
                email = state.email,
                perfiles = listOf(perfilPrincipal),
                avatarUrl = avatarUrl, // Ya incluimos la URL del avatar
                firebaseUid = firebaseUid, // Incluimos el UID de Firebase
                fechaRegistro = Timestamp.now(),
                activo = true,
                preferencias = Preferencias(
                    notificaciones = Notificaciones(
                        push = true,
                        email = true,
                        deviceId = "device_${System.currentTimeMillis()}",
                        fcmTokens = mapOf()
                    )
                )
            )
            
            Timber.d("Usuario creado: ${usuario.nombre} ${usuario.apellidos} con DNI: ${usuario.dni}, tipo: ${perfilPrincipal.tipo}, avatar: $avatarUrl")

            // PASO 5: Guardar en Firestore
            withContext(Dispatchers.IO) {
                try {
                    val task = firebaseFirestore.collection("usuarios")
                        .document(usuario.dni)
                        .set(usuario)
                    Tasks.await(task)
                    
                    Timber.d("Usuario guardado correctamente en Firestore con avatar")
                    
                    // Si es ADMIN_CENTRO, actualizar también el centro con el mismo avatar
                    if (state.tipoUsuario == TipoUsuario.ADMIN_CENTRO && state.centroSeleccionado != null) {
                        val centroId = state.centroSeleccionado.id
                        Timber.d("Actualizando centro con ID: $centroId, logo: $avatarUrl")
                        
                        try {
                            val updateTask = firebaseFirestore.collection("centros").document(centroId)
                                .update("logo", avatarUrl)
                            Tasks.await(updateTask)
                            
                            Timber.d("Centro actualizado correctamente con el logo: $avatarUrl")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al actualizar centro con logo: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al guardar usuario en Firestore: ${e.message}")
                    throw e
                }
            }
            
            // Actualizar UI
            _uiState.update { it.copy(
                isLoading = false,
                success = true,
                error = null
            )}
        } catch (e: Exception) {
            Timber.e(e, "Error general al crear usuario: ${e.message}")
            _uiState.update { it.copy(
                isLoading = false,
                error = "Error al crear usuario: ${e.message}"
            )}
        }
    }
    
    /**
     * Sube un avatar desde los assets para un tipo de usuario específico
     */
    private suspend fun subirAvatarDesdeAssets(tipoUsuario: TipoUsuario): String {
        val resourceName = when (tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "AdminAvatar.png"
            TipoUsuario.ADMIN_CENTRO -> "centro.png"
            TipoUsuario.PROFESOR -> "profesor.png"
            TipoUsuario.FAMILIAR -> "familiar.png"
            TipoUsuario.ALUMNO -> "alumno.png"
            else -> "default.png"
        }
        
        Timber.d("Buscando imagen en assets/images: $resourceName")
        val context = getApplication<Application>().applicationContext
        
        // También intentar buscar en resources/images - ruta alternativa
        val resourcesDir = "/Users/maitane/AndroidStudioProjects/UmeEgunero/app/src/main/resources/images"
        val resourceFile = File("$resourcesDir/$resourceName")
        
        // Comprobar si el avatar ya existe en Firebase Storage
        try {
            val storageFilePath = "avatares/@${resourceName.lowercase().replace("@", "")}"
            val storageRef = FirebaseStorage.getInstance().reference.child(storageFilePath)
            
            try {
                val existingUrl = Tasks.await(storageRef.downloadUrl).toString()
                Timber.d("Avatar ya existe en Storage: $existingUrl")
                return existingUrl
            } catch (e: Exception) {
                Timber.d("Avatar no encontrado en Storage, se procederá a subirlo: ${e.message}")
                // Continuamos con la subida
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar existencia del avatar: ${e.message}")
            // Continuamos con la subida
        }
        
        // Crear archivo temporal desde los assets o resources
        val archivoTemporal = File(context.cacheDir, resourceName)
        
        try {
            // Intentar primero con recursos externos
            if (resourceFile.exists()) {
                Timber.d("Usando imagen de resources/images: ${resourceFile.absolutePath}")
                resourceFile.copyTo(archivoTemporal, overwrite = true)
            } else {
                // Intentar abrir el stream del asset desde assets/images
            try {
                context.assets.open("images/$resourceName").use { inputStream ->
                    FileOutputStream(archivoTemporal).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                    Timber.d("Imagen cargada desde assets/images: $resourceName")
            } catch (e: IOException) {
                // Si no encuentra la imagen específica, usar AdminAvatar.png como fallback
                Timber.d("⚠️ No se encontró images/$resourceName, usando AdminAvatar.png como fallback")
                context.assets.open("images/AdminAvatar.png").use { inputStream ->
                    FileOutputStream(archivoTemporal).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
            
            // Convertir a Uri y subir
            val uri = Uri.fromFile(archivoTemporal)
            
            // Construir la ruta en Firebase Storage
            val storagePath = "avatares"
            
            // Asegurarnos de usar el nombre correcto aunque hayamos usado un fallback
            // IMPORTANTE: Añadir el prefijo @ al nombre del archivo para mantener la convención
            val storageFileName = "@" + when (tipoUsuario) {
                TipoUsuario.ADMIN_APP -> "adminavatar.png"
                TipoUsuario.ADMIN_CENTRO -> "centro.png"
                TipoUsuario.PROFESOR -> "profesor.png"
                TipoUsuario.FAMILIAR -> "familiar.png"
                TipoUsuario.ALUMNO -> "alumno.png"
                else -> "default.png"
            }
            
            // Subir a Firebase Storage
            val avatarRef = storage.reference.child("$storagePath/$storageFileName")
            
            val uploadTask = avatarRef.putFile(uri)
            val taskSnapshot = Tasks.await(uploadTask)
            val downloadUrl = Tasks.await(taskSnapshot.storage.downloadUrl)
            
            Timber.d("✅ Imagen subida con éxito desde assets. URL: $downloadUrl")
            
            // Limpiar archivo temporal
            try {
                if (archivoTemporal.exists()) {
                    archivoTemporal.delete()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar archivo temporal")
            }
            
            return downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error al subir avatar desde assets: ${e.message}")
            throw e
        }
    }
    
    /**
     * Sube un avatar desde los recursos drawable para un tipo de usuario específico
     */
    private suspend fun subirAvatarDesdeDrawable(tipoUsuario: TipoUsuario): String {
        val resourceName = when (tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "adminavatar"
            TipoUsuario.ADMIN_CENTRO -> "centro"
            TipoUsuario.PROFESOR -> "profesor"
            TipoUsuario.FAMILIAR -> "familiar"
            TipoUsuario.ALUMNO -> "alumno"
            else -> "default"
        }
        
        val context = getApplication<Application>().applicationContext
        var resourceId = context.resources.getIdentifier(
            resourceName.lowercase(), 
            "drawable", 
            context.packageName
        )
        
        if (resourceId == 0) {
            // Intentar buscar en drawable-nodpi si no se encuentra en drawable
            resourceId = context.resources.getIdentifier(
                resourceName.lowercase(),
                "drawable-nodpi",
                context.packageName
            )
        }
        
        if (resourceId == 0) {
            throw Exception("No se encontró recurso para $resourceName")
        }
        
        // Tenemos un recurso válido, lo subimos
        val fileName = when (tipoUsuario) {
            TipoUsuario.ADMIN_CENTRO -> "centro.png"
            TipoUsuario.PROFESOR -> "profesor.png"
            TipoUsuario.FAMILIAR -> "familiar.png"
            TipoUsuario.ALUMNO -> "alumno.png"
            TipoUsuario.ADMIN_APP -> "adminavatar.png"
            else -> "default.png"
        }
        
        val downloadUrl = StorageUtil.uploadImageFromResource(
            context = context,
            resourceId = resourceId,
            storagePath = "avatares",
            fileName = fileName
        )
        
        if (downloadUrl != null) {
            Timber.d("Imagen subida con éxito desde drawable. URL: $downloadUrl")
            return downloadUrl
        } else {
            throw Exception("Error al subir imagen desde drawable")
        }
    }

    private suspend fun updateExistingUser(state: AddUserUiState) {
        // Implementación de actualización de usuario existente
        // Código similar pero sin crear cuenta en Auth
        // ...
    }

    /**
     * Crea un objeto Usuario a partir del estado actual del UI
     */
    private fun createUsuarioObject(nuevoId: String): Usuario {
        val state = _uiState.value
        
        // Obtener la URL del avatar según el tipo de usuario
        val avatarUrl = when(state.tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40AdminAvatar.png?alt=media&token=1b86693e-cecc-45be-9669-69b21a6c909a"
            TipoUsuario.ADMIN_CENTRO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40centro.png?alt=media&token=69a60931-e98c-45f6-b783-aca87946ecdc"
            TipoUsuario.PROFESOR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40profesor.png?alt=media"
            TipoUsuario.FAMILIAR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40familiar.png?alt=media"
            TipoUsuario.ALUMNO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40alumno.png?alt=media"
            else -> ""
        }
        
        return Usuario(
            dni = state.dni,
            nombre = state.nombre,
            apellidos = state.apellidos,
            email = state.email,
            telefono = state.telefono,
            perfiles = createPerfiles(),
            activo = true,
            firebaseUid = nuevoId,  // Guardar el ID generado como firebaseUid
            avatarUrl = avatarUrl,  // Añadir la URL del avatar
            preferencias = Preferencias(
                notificaciones = Notificaciones(
                    push = true,
                    email = true,
                    deviceId = "device_${System.currentTimeMillis()}",
                    fcmTokens = mapOf()
                )
            )
        )
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
            TipoUsuario.OTRO -> {
                // No crear ningún perfil para tipo otro
                Timber.w("Tipo de usuario OTRO, no se crea perfil")
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
                            observacionesMedicas = alumno.observacionesMedicas,
                            numeroSS = alumno.numeroSS,
                            condicionesMedicas = alumno.condicionesMedicas,
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
     * Actualiza el número de seguridad social del alumno
     */
    fun updateNumeroSS(numeroSS: String) {
        // Limpiamos el input de cualquier caracter no numérico
        val numeroLimpio = numeroSS.filter { it.isDigit() }.take(12)
        
        _uiState.update { it.copy(
            numeroSS = numeroLimpio,
            numeroSSError = null,
            firstInvalidField = if (it.firstInvalidField == AddUserFormField.NUMERO_SS) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
    }

    /**
     * Valida el formato del número de la Seguridad Social
     * Acepta varios formatos:
     * - 12 dígitos seguidos
     * - XX/XXXXXXXX (formato simple con provincia)
     * - XX/XXXXXXXX/XX (formato completo)
     */
    private fun isValidNumeroSS(numeroSS: String): Boolean {
        // Formatos aceptados:
        // 1. 12 dígitos seguidos: 280123456789
        // 2. Formato con separadores: 28/01234567/89 o 28/01234567
        val regexDigitos = """^\d{12}$""".toRegex()
        val regexFormateado1 = """^\d{2}/\d{8}$""".toRegex()
        val regexFormateado2 = """^\d{2}/\d{8}/\d{2}$""".toRegex()
        
        return regexDigitos.matches(numeroSS) || 
               regexFormateado1.matches(numeroSS) || 
               regexFormateado2.matches(numeroSS) ||
               numeroSS.isEmpty() // Permitir que esté vacío
    }

    /**
     * Actualiza las condiciones médicas del alumno
     */
    fun updateCondicionesMedicas(condicionesMedicas: String) {
        _uiState.update { it.copy(
            condicionesMedicas = condicionesMedicas,
            firstInvalidField = if (it.firstInvalidField == AddUserFormField.CONDICIONES_MEDICAS) null else it.firstInvalidField,
            validationAttemptFailed = false
        )}
    }

    /**
     * Actualiza la lista de alergias del alumno
     */
    fun updateAlergias(alergias: String) {
        _uiState.update { it.copy(
            alergias = alergias,
            validationAttemptFailed = false
        )}
    }

    /**
     * Actualiza la lista de medicación del alumno
     */
    fun updateMedicacion(medicacion: String) {
        _uiState.update { it.copy(
            medicacion = medicacion,
            validationAttemptFailed = false
        )}
    }

    /**
     * Actualiza las necesidades especiales del alumno
     */
    fun updateNecesidadesEspeciales(necesidadesEspeciales: String) {
        _uiState.update { it.copy(
            necesidadesEspeciales = necesidadesEspeciales,
            validationAttemptFailed = false
        )}
    }

    /**
     * Actualiza las observaciones generales del alumno
     */
    fun updateObservaciones(observaciones: String) {
        _uiState.update { it.copy(
            observaciones = observaciones,
            validationAttemptFailed = false
        )}
    }

    /**
     * Actualiza las observaciones médicas del alumno
     */
    fun updateObservacionesMedicas(observacionesMedicas: String) {
        _uiState.update { it.copy(
            observacionesMedicas = observacionesMedicas,
            validationAttemptFailed = false
        )}
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

    /**
     * Valida todos los campos según el tipo de usuario seleccionado
     */
    private fun validateFields(uiState: AddUserUiState): AddUserUiState {
        var currentState = uiState.copy(
            dniError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            nombreError = null,
            apellidosError = null,
            telefonoError = null,
            centroError = null,
            fechaNacimientoError = null,
            numeroSSError = null,
            condicionesMedicasError = null,
            firstInvalidField = null
        )

        // Validar DNI
        if (currentState.dni.isEmpty()) {
            currentState = currentState.copy(
                dniError = "El DNI/NIE es obligatorio",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.DNI else currentState.firstInvalidField
            )
        } else if (!isValidDNI(currentState.dni)) {
            currentState = currentState.copy(
                dniError = "El formato del DNI/NIE no es válido",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.DNI else currentState.firstInvalidField
            )
        }

        // Validar nombre
        if (currentState.nombre.isEmpty()) {
            currentState = currentState.copy(
                nombreError = "El nombre es obligatorio",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.NOMBRE else currentState.firstInvalidField
            )
        }

        // Validar apellidos
        if (currentState.apellidos.isEmpty()) {
            currentState = currentState.copy(
                apellidosError = "Los apellidos son obligatorios",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.APELLIDOS else currentState.firstInvalidField
            )
        }

        // Validar teléfono
        if (currentState.telefono.isEmpty()) {
            currentState = currentState.copy(
                telefonoError = "El teléfono es obligatorio",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.TELEFONO else currentState.firstInvalidField
            )
        } else if (!isValidPhone(currentState.telefono)) {
            currentState = currentState.copy(
                telefonoError = "El formato del teléfono no es válido",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.TELEFONO else currentState.firstInvalidField
            )
        }

        // Validar correo (solo para usuarios excepto alumnos)
        if (currentState.tipoUsuario != TipoUsuario.ALUMNO) {
            if (currentState.email.isEmpty()) {
                currentState = currentState.copy(
                    emailError = "El email es obligatorio",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.EMAIL else currentState.firstInvalidField
                )
            } else if (!isValidEmail(currentState.email)) {
                currentState = currentState.copy(
                    emailError = "El formato del email no es válido",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.EMAIL else currentState.firstInvalidField
                )
            }

            // Validar contraseña
            if (currentState.password.isEmpty()) {
                currentState = currentState.copy(
                    passwordError = "La contraseña es obligatoria",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.PASSWORD else currentState.firstInvalidField
                )
            } else if (!isValidPassword(currentState.password)) {
                currentState = currentState.copy(
                    passwordError = "La contraseña debe tener al menos 6 caracteres",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.PASSWORD else currentState.firstInvalidField
                )
            }

            // Validar confirmación de contraseña
            if (currentState.confirmPassword.isEmpty()) {
                currentState = currentState.copy(
                    confirmPasswordError = "Debe confirmar la contraseña",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.CONFIRM_PASSWORD else currentState.firstInvalidField
                )
            } else if (currentState.password != currentState.confirmPassword) {
                currentState = currentState.copy(
                    confirmPasswordError = "Las contraseñas no coinciden",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.CONFIRM_PASSWORD else currentState.firstInvalidField
                )
            }
        }

        // Validar centro seleccionado
        if (currentState.centroId.isEmpty()) {
            currentState = currentState.copy(
                centroError = "Debe seleccionar un centro",
                firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.CENTRO else currentState.firstInvalidField
            )
        }

        // Validaciones específicas para alumnos
        if (currentState.tipoUsuario == TipoUsuario.ALUMNO) {
            // Validar fecha de nacimiento
            if (currentState.fechaNacimiento.isEmpty()) {
                currentState = currentState.copy(
                    fechaNacimientoError = "La fecha de nacimiento es obligatoria",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.FECHA_NACIMIENTO else currentState.firstInvalidField
                )
            } else if (!isValidDateFormat(currentState.fechaNacimiento)) {
                currentState = currentState.copy(
                    fechaNacimientoError = "El formato de fecha debe ser DD/MM/AAAA",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.FECHA_NACIMIENTO else currentState.firstInvalidField
                )
            }

            // Validar Número de Seguridad Social (opcional pero con formato válido si se proporciona)
            if (currentState.numeroSS.isNotEmpty() && !isValidNumeroSS(currentState.numeroSS)) {
                currentState = currentState.copy(
                    numeroSSError = "El formato del número de la Seguridad Social no es válido",
                    firstInvalidField = if (currentState.firstInvalidField == null) AddUserFormField.NUMERO_SS else currentState.firstInvalidField
                )
            }

            // La validación de condiciones médicas no es estricta, ya que es información opcional y de formato libre
        }

        return currentState
    }

    /**
     * Valida el formato del DNI/NIE
     */
    private fun isValidDNI(dni: String): Boolean {
        // Regex para DNI: 8 números + letra
        val dniRegex = """^\d{8}[A-Za-z]$""".toRegex()
        // Regex para NIE: X/Y/Z + 7 números + letra
        val nieRegex = """^[XYZxyz]\d{7}[A-Za-z]$""".toRegex()
        return dniRegex.matches(dni) || nieRegex.matches(dni)
    }

    /**
     * Valida el formato del teléfono
     */
    private fun isValidPhone(telefono: String): Boolean {
        val regex = """^[6789]\d{8}$""".toRegex()
        return regex.matches(telefono)
    }

    /**
     * Valida el formato del email
     */
    private fun isValidEmail(email: String): Boolean {
        val regex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
        return regex.matches(email)
    }

    /**
     * Valida el formato de la contraseña
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valida el formato de la fecha
     */
    private fun isValidDateFormat(fecha: String): Boolean {
        val regex = """^\d{2}/\d{2}/\d{4}$""".toRegex()
        return regex.matches(fecha)
    }

    /**
     * Procesa el resultado de la operación de guardado
     */
    private fun handleSaveResult(result: Result<Unit>) {
        when (result) {
            is Result.Success -> {
                _uiState.update { it.copy(
                    isLoading = false,
                    showSuccessDialog = true
                ) }
            }
            is Result.Error -> {
                _uiState.update { it.copy(
                    error = result.exception?.message ?: "Error desconocido al guardar",
                    isLoading = false
                ) }
            }
            is Result.Loading -> {
                // No hacer nada, ya estamos mostrando el estado de carga
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    /**
     * Obtiene los tipos de usuario que se pueden crear según el rol del administrador actual
     */
    fun getUserTypes(): List<TipoUsuario> {
        return if (_uiState.value.isAdminApp) {
            // Administrador de aplicación puede crear todos los tipos
            listOf(
                TipoUsuario.ADMIN_APP,
                TipoUsuario.ADMIN_CENTRO,
                TipoUsuario.PROFESOR,
                TipoUsuario.FAMILIAR,
                TipoUsuario.ALUMNO
            )
        } else {
            // Administrador de centro solo puede crear estos roles
            listOf(
                TipoUsuario.PROFESOR,
                TipoUsuario.FAMILIAR,
                TipoUsuario.ALUMNO
            )
        }
    }

    /**
     * Obtiene la URL del avatar predefinido según el tipo de usuario
     */
    fun getAvatarUrlForUserType(tipoUsuario: TipoUsuario): String {
        val avatarPath = when (tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "avatares/adminavatar.png"
            TipoUsuario.ADMIN_CENTRO -> "avatares/centro.png" // Cambiado para usar el avatar específico de centro
            TipoUsuario.PROFESOR -> "avatares/profesor.png"
            TipoUsuario.FAMILIAR -> "avatares/familiar.png"
            TipoUsuario.ALUMNO -> "avatares/alumno.png"
            else -> "avatares/default.png" // Avatar por defecto para otros tipos desconocidos
        }
        Timber.d("Avatar seleccionado para tipo $tipoUsuario: $avatarPath")
        return avatarPath
    }
    
    /**
     * Asigna un avatar predefinido al usuario según su tipo
     * 
     * @param usuarioId DNI del usuario para actualizar con la URL del avatar
     * @param tipoUsuario Tipo de usuario para determinar qué avatar asignar
     * @return URL de descarga del avatar
     */
    suspend fun asignarAvatarPredefinido(usuarioId: String, tipoUsuario: TipoUsuario): String {
        try {
            // Obtener la ruta del avatar predefinido
            val avatarPath = getAvatarUrlForUserType(tipoUsuario)
            Timber.d("Asignando avatar predefinido: $avatarPath para usuario tipo $tipoUsuario")
            
            // Verificar si el avatar ya existe en el storage
            if (StorageUtil.fileExists(avatarPath)) {
                Timber.d("Avatar encontrado en Storage: $avatarPath")
                // El archivo existe, obtenemos su URL
                val downloadUrl = StorageUtil.getDownloadUrl(avatarPath)
                
                if (downloadUrl != null) {
                    Timber.d("URL de avatar obtenida: $downloadUrl")
                    // Actualizar el usuario con la URL
                    try {
                        Timber.d("Actualizando usuario con URL de avatar: $downloadUrl")
                        
                        firebaseFirestore.collection("usuarios").document(usuarioId)
                            .update("avatarUrl", downloadUrl)
                            .addOnSuccessListener {
                                Timber.d("Usuario actualizado correctamente con el avatar: $downloadUrl")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                    }
                    
                    return downloadUrl
                }
            }
            
            // Si el avatar no existe en Storage, intentamos subirlo desde los assets
            val resourceName = when (tipoUsuario) {
                TipoUsuario.ADMIN_APP -> "AdminAvatar.png"
                TipoUsuario.ADMIN_CENTRO -> "centro.png"
                TipoUsuario.PROFESOR -> "profesor.png"
                TipoUsuario.FAMILIAR -> "familiar.png"
                TipoUsuario.ALUMNO -> "alumno.png"
                else -> "default.png"
            }
            
            Timber.d("Buscando imagen en assets/images: $resourceName")
            val context = getApplication<Application>().applicationContext
            
            try {
                // Crear archivo temporal desde los assets
                val archivoTemporal = File(context.cacheDir, resourceName)
                
                try {
                    try {
                        // Intentar abrir el stream del asset
                        context.assets.open("images/$resourceName").use { inputStream ->
                            // Escribir al archivo temporal
                            FileOutputStream(archivoTemporal).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        Timber.d("✅ Archivo de assets copiado a temporal: ${archivoTemporal.absolutePath}")
                    } catch (e: IOException) {
                        // Si no encuentra centro.png, usar AdminAvatar.png como fallback para ADMIN_CENTRO
                        if (tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
                            Timber.d("⚠️ No se encontró images/centro.png, usando AdminAvatar.png como fallback")
                            context.assets.open("images/AdminAvatar.png").use { inputStream ->
                                // Escribir al archivo temporal
                                FileOutputStream(archivoTemporal).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        } else {
                            throw e // Reenviar la excepción si no es un ADMIN_CENTRO
                        }
                    }
                    
                    // Convertir a Uri y subir
                    val uri = Uri.fromFile(archivoTemporal)
                    
                    // Construir la ruta en Firebase Storage
                    val storagePath = "avatares"
                    
                    // Asegurarnos de usar el nombre correcto aunque hayamos usado un fallback
                    val storageFileName = when (tipoUsuario) {
                        TipoUsuario.ADMIN_CENTRO -> "centro.png"
                        else -> resourceName
                    }
                    
                    // Subir a Firebase Storage
                    val avatarRef = storage.reference.child("$storagePath/$storageFileName")
                    
                    val uploadTask = avatarRef.putFile(uri)
                    val taskSnapshot = Tasks.await(uploadTask)
                    val downloadUrl = Tasks.await(taskSnapshot.storage.downloadUrl)
                    
                    Timber.d("✅ Imagen subida con éxito desde assets. URL: $downloadUrl")
                    
                    // Limpiar archivo temporal
                    try {
                        if (archivoTemporal.exists()) {
                            archivoTemporal.delete()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al eliminar archivo temporal")
                    }
                    
                    // Actualizar el usuario con la URL
                    try {
                        Timber.d("Actualizando usuario con URL de avatar: $downloadUrl")
                        
                        firebaseFirestore.collection("usuarios").document(usuarioId)
                            .update("avatarUrl", downloadUrl)
                            .addOnSuccessListener {
                                Timber.d("Usuario actualizado correctamente con el avatar: $downloadUrl")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                    }
                    
                    return downloadUrl.toString()
                } catch (e: IOException) {
                    Timber.e(e, "❌ Error al acceder al asset: images/$resourceName - ${e.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error general al cargar avatar desde assets: ${e.message}")
            }
            
            // Si llegamos aquí, intentamos el método anterior con resources
            Timber.d("Intentando método alternativo con recursos de drawable...")
            var resourceId = context.resources.getIdentifier(
                resourceName.substringBefore(".").lowercase(), 
                "drawable", 
                context.packageName
            )
            
            if (resourceId == 0) {
                // Intentar buscar en drawable-nodpi si no se encuentra en drawable
                Timber.d("Recurso no encontrado en drawable, buscando en drawable-nodpi: $resourceName")
                val resourceIdNodpi = context.resources.getIdentifier(
                    resourceName.lowercase(),
                    "drawable-nodpi",
                    context.packageName
                )
                
                if (resourceIdNodpi != 0) {
                    Timber.d("Recurso encontrado en drawable-nodpi con ID: $resourceIdNodpi")
                    resourceId = resourceIdNodpi
                }
            }
            
            if (resourceId != 0) {
                Timber.d("Recurso encontrado con ID: $resourceId, subiendo a Storage...")
                // Tenemos un recurso válido, lo subimos
                val fileName = avatarPath.substringAfterLast("/")
                val downloadUrl = StorageUtil.uploadImageFromResource(
                    context = context,
                    resourceId = resourceId,
                    storagePath = "avatares",
                    fileName = fileName
                )
                
                if (downloadUrl != null) {
                    Timber.d("Imagen subida con éxito. URL: $downloadUrl")
                    // Actualizar el usuario con la URL
                    try {
                        Timber.d("Actualizando usuario con URL de avatar: $downloadUrl")
                        
                        firebaseFirestore.collection("usuarios").document(usuarioId)
                            .update("avatarUrl", downloadUrl)
                            .addOnSuccessListener {
                                Timber.d("Usuario actualizado correctamente con el avatar: $downloadUrl")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al actualizar usuario con URL de avatar: ${e.message}")
                    }
                    
                    return downloadUrl
                }
            } else {
                Timber.e("No se encontró el recurso de imagen: $resourceName (ni en drawable ni en drawable-nodpi)")
            }
            
            // Si llegamos aquí, ha fallado la subida, usamos una URL por defecto
            val defaultUrl = "https://firebasestorage.googleapis.com/v0/b/umeegunero.appspot.com/o/avatares%2Fdefault.png?alt=media"
            Timber.d("Usando URL de avatar por defecto: $defaultUrl")
            
            // Actualizar el usuario con la URL por defecto
            try {
                Timber.d("Actualizando usuario con URL de avatar por defecto: $defaultUrl")
                
                firebaseFirestore.collection("usuarios").document(usuarioId)
                    .update("avatarUrl", defaultUrl)
                    .addOnSuccessListener {
                        Timber.d("Usuario actualizado correctamente con el avatar por defecto: $defaultUrl")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error al actualizar usuario con URL de avatar por defecto: ${e.message}")
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar usuario con URL de avatar por defecto: ${e.message}")
            }
            
            return defaultUrl
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar avatar predefinido: ${e.message}")
            return ""
        }
    }

    /**
     * Carga un usuario por su DNI para edición
     */
    fun loadUser(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = usuarioRepository.getUsuarioById(dni)
                when (result) {
                    is Result.Success -> {
                        // Usuario encontrado, actualizar estado
                        val usuario = result.data
                        
                        // Determinar tipo de usuario primario (el primero en la lista de perfiles)
                        val tipoUsuario = if (usuario.perfiles.isNotEmpty()) {
                            usuario.perfiles[0].tipo
                        } else {
                            TipoUsuario.FAMILIAR // Valor por defecto
                        }
                        
                        // Obtener centroId si es profesor o admin de centro
                        var centroId = ""
                        if (tipoUsuario == TipoUsuario.PROFESOR || tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
                            centroId = usuario.perfiles.firstOrNull { 
                                it.tipo == tipoUsuario 
                            }?.centroId ?: ""
                            
                            // Cargar datos del centro si tenemos un ID
                            if (centroId.isNotEmpty()) {
                                loadCentrosAndSelectInitial(centroId)
                            }
                        }
                        
                        // Actualizar estado
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isEditMode = true,
                                userId = dni,
                                dni = usuario.dni,
                                nombre = usuario.nombre,
                                apellidos = usuario.apellidos,
                                email = usuario.email ?: "",
                                telefono = usuario.telefono ?: "",
                                tipoUsuario = tipoUsuario,
                                isTipoUsuarioBloqueado = true // Bloquear cambio de tipo en edición
                            )
                        }
                    }
                    is Result.Error -> {
                        // Error al cargar usuario
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar usuario: ${result.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos mostrando estado de carga
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    private fun isValidFecha(fecha: String): Boolean {
        val regex = Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(19|20)\\d\\d$")
        return regex.matches(fecha)
    }
    
    /**
     * Crea un objeto Usuario o Alumno a partir del estado actual del UI
     */
    private fun getUserData(): Any {
        val state = _uiState.value
        
        // Obtener la URL del avatar según el tipo de usuario
        val avatarUrl = when(state.tipoUsuario) {
            TipoUsuario.ADMIN_APP -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40AdminAvatar.png?alt=media&token=1b86693e-cecc-45be-9669-69b21a6c909a"
            TipoUsuario.ADMIN_CENTRO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40centro.png?alt=media&token=69a60931-e98c-45f6-b783-aca87946ecdc"
            TipoUsuario.PROFESOR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40profesor.png?alt=media"
            TipoUsuario.FAMILIAR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40familiar.png?alt=media"
            TipoUsuario.ALUMNO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40alumno.png?alt=media"
            else -> ""
        }

        return when (state.tipoUsuario) {
            TipoUsuario.ADMIN_APP, TipoUsuario.ADMIN_CENTRO, TipoUsuario.PROFESOR, TipoUsuario.FAMILIAR -> {
                Usuario(
                    dni = state.dni,
                    nombre = state.nombre,
                    apellidos = state.apellidos,
                    email = state.email,
                    telefono = state.telefono,
                    perfiles = createPerfiles(),
                    activo = true,
                    avatarUrl = avatarUrl // URL predeterminada según tipo de usuario
                )
            }
            TipoUsuario.ALUMNO -> {
                Alumno(
                    id = state.userId.ifEmpty { UUID.randomUUID().toString() },
                    dni = state.dni,
                    nombre = state.nombre,
                    apellidos = state.apellidos,
                    fechaNacimiento = DateUtils.parseDateString(state.fechaNacimiento),
                    centroId = state.centroSeleccionado?.id ?: "",
                    claseId = state.claseSeleccionada?.id ?: "",
                    curso = state.cursoSeleccionado?.nombre ?: "",
                    clase = state.claseSeleccionada?.nombre ?: "",
                    numeroSS = state.numeroSS,
                    condicionesMedicas = state.condicionesMedicas,
                    alergias = if (state.alergias.isNotEmpty()) state.alergias.split(",").map { it.trim() } else emptyList(),
                    medicacion = if (state.medicacion.isNotEmpty()) state.medicacion.split(",").map { it.trim() } else emptyList(),
                    necesidadesEspeciales = state.necesidadesEspeciales,
                    observaciones = state.observaciones,
                    observacionesMedicas = state.observacionesMedicas,
                    avatarUrl = avatarUrl // URL predeterminada para alumno
                )
            }
            else -> throw IllegalArgumentException("Tipo de usuario no soportado: ${state.tipoUsuario}")
        }
    }
}