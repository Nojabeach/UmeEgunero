package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Perfil
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
import java.time.LocalDate
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    val isEditMode: Boolean = false,
    val initialCentroId: String? = null,
    val isCentroBloqueado: Boolean = false
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
            _uiState.update { it.copy(isLoading = true) }
            Timber.d("⏳ Iniciando carga de cursos para centroId: $centroId")
            
            try {
                 // Usar la función suspendida que devuelve directamente la lista
                 val cursosList = cursoRepository.obtenerCursosPorCentro(centroId)
                 Timber.d("Cursos cargados: ${cursosList.size}")
                 cursosList.forEach { curso -> 
                     Timber.d("Curso: ${curso.nombre}, id: ${curso.id}")
                 }
                 _uiState.update { it.copy(cursosDisponibles = cursosList, isLoading = false) }
                    
                 if (cursosList.isNotEmpty()) {
                     updateCursoSeleccionado(cursosList.first().id)
                     Timber.d("🔄 Seleccionado automáticamente el primer curso: ${cursosList.first().nombre}")
                 }

                /* // Código antiguo con Result
                when (val result = cursoRepository.obtenerCursosPorCentroResult(centroId)) {
                    is Result.Success<*> -> { // Usar <*> para tipo genérico
                        val cursos = result.data as List<Curso> // Cast necesario
                        cursos.forEach { curso: Curso -> // Especificar tipo en forEach
                            Timber.d("Curso: ${curso.nombre}, id: ${curso.id}")
                        }
                        _uiState.update { it.copy(cursosDisponibles = cursos) }
                        
                        if (cursos.isNotEmpty()) {
                            updateCursoSeleccionado(cursos.first().id)
                            Timber.d("🔄 Seleccionado automáticamente el primer curso: ${cursos.first().nombre}")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌ Error al cargar cursos: ${result.exception?.message}")
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar los cursos: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading<*> -> { // Usar <*> para tipo genérico
                        // Ya estamos mostrando el estado de carga
                    }
                }
                */
            } catch (e: Exception) {
                 Timber.e(e, "❌ Error inesperado al cargar cursos: ${e.message}")
                 _uiState.update { 
                     it.copy(
                         error = "Error inesperado al cargar los cursos: ${e.message}",
                         isLoading = false
                     )
                 }
            } finally {
                _uiState.update { it.copy(isLoading = false) } // Asegurar que isLoading se ponga a false
            }
        }
    }

    // Carga las clases del curso seleccionado
    @Suppress("UNCHECKED_CAST")
    fun loadClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = cursoRepository.obtenerClasesPorCurso(cursoId)) {
                is Result.Success<*> -> {
                    val clasesList = result.data as List<Clase>
                    _uiState.update { 
                        it.copy(
                            clasesDisponibles = clasesList,
                            isLoading = false
                        )
                    }
                    Timber.d("Clases cargadas: ${clasesList.size}")
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
    
    // Guarda un usuario en Firebase Authentication y Firestore
    fun saveUser() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. Crear usuario en Firebase Authentication
                val email = _uiState.value.email
                val password = _uiState.value.password
                
                // Usar el repositorio adecuado según el tipo de usuario
                val authResult = if (_uiState.value.tipoUsuario == TipoUsuario.ADMIN_CENTRO || 
                                    _uiState.value.tipoUsuario == TipoUsuario.PROFESOR) {
                    // Para profesores y admin de centro, usamos CentroRepository
                    centroRepository.createUserWithEmailAndPassword(email, password)
                } else {
                    // Para otros usuarios (admins, familiares), usamos UsuarioRepository
                    usuarioRepository.crearUsuarioConEmailYPassword(email, password)
                }
                
                when (authResult) {
                    is Result.Success -> {
                        // 2. Crear objeto Usuario
                        val usuario = Usuario(
                            dni = _uiState.value.dni,
                            email = _uiState.value.email,
                            nombre = _uiState.value.nombre,
                            apellidos = _uiState.value.apellidos,
                            telefono = _uiState.value.telefono,
                            fechaRegistro = com.google.firebase.Timestamp.now(),
                            perfiles = createPerfiles()
                        )
                        
                        // 3. Guardar usuario en Firestore
                        val saveResult = usuarioRepository.guardarUsuario(usuario)
                        
                        when (saveResult) {
                            is Result.Success -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        success = true,
                                        error = null
                                    )
                                }
                                Timber.d("Usuario guardado correctamente con DNI: ${usuario.dni}")
                            }
                            is Result.Error -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "Error al guardar usuario en Firestore: ${saveResult.exception?.message}"
                                    )
                                }
                                Timber.e(saveResult.exception, "Error al guardar usuario en Firestore")
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
                        Timber.e(authResult.exception, "Error al crear cuenta en Firebase")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
                Timber.e(e, "Error inesperado al guardar usuario")
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
                val centroId = _uiState.value.centroSeleccionado?.id ?: ""
                if (centroId.isNotBlank()) {
                    perfiles.add(Perfil(
                        tipo = TipoUsuario.ADMIN_CENTRO,
                        centroId = centroId,
                        verificado = true
                    ))
                    Timber.d("Creando perfil de ADMIN_CENTRO para centro: $centroId")
                } else {
                    Timber.e("Error: No hay centroId para ADMIN_CENTRO")
                }
            }
            TipoUsuario.PROFESOR -> {
                val centroId = _uiState.value.centroSeleccionado?.id ?: ""
                if (centroId.isNotBlank()) {
                    perfiles.add(Perfil(
                        tipo = TipoUsuario.PROFESOR,
                        centroId = centroId,
                        verificado = true
                    ))
                    Timber.d("Creando perfil de PROFESOR para centro: $centroId")
                    
                    // Mostrar información adicional para depuración
                    val centro = _uiState.value.centroSeleccionado
                    Timber.d("Información del centro seleccionado: ID=${centro?.id}, Nombre=${centro?.nombre}")
                } else {
                    Timber.e("Error: No hay centroId para PROFESOR")
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
            LocalDate.parse(fecha)
            true
        } catch (e: Exception) {
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
}