package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.network.NominatimApiService
import com.tfg.umeegunero.util.DebugUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.regex.Pattern
import android.util.Log
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import retrofit2.HttpException
import java.io.IOException
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.data.model.EstadoSolicitud
import timber.log.Timber
import com.tfg.umeegunero.data.service.EmailNotificationService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import android.os.Build
import com.tfg.umeegunero.BuildConfig
import android.os.Handler
import android.os.Looper
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Estado UI para la pantalla de registro
 */
data class RegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val form: RegistroUsuarioForm = RegistroUsuarioForm(),
    val currentStep: Int = 1, // 1: Datos personales, 2: Dirección, 3: Datos de alumnos y centro
    val totalSteps: Int = 3,
    val centros: List<Centro> = emptyList(),
    val isLoadingCentros: Boolean = false,
    val formErrors: Map<String, String> = emptyMap(),
    // Errores de validación en tiempo real
    val dniError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val telefonoError: String? = null,
    val direccionError: String? = null,
    val centroIdError: String? = null,
    val alumnosDniError: String? = null,
    // Estado de búsqueda de dirección
    val isLoadingDireccion: Boolean = false,
    val coordenadasLatitud: Double? = null,
    val coordenadasLongitud: Double? = null,
    val mapaUrl: String? = null,
    val mensajeResultadoNominatim: String? = null
)

/**
 * ViewModel para el proceso de registro de usuarios en la aplicación UmeEgunero.
 * 
 * Este ViewModel implementa el patrón MVVM (Model-View-ViewModel) y utiliza las mejores prácticas
 * de desarrollo Android moderno, incluyendo:
 * - Jetpack Compose para la UI
 * - Coroutines para operaciones asíncronas
 * - StateFlow para la gestión del estado
 * - Hilt para la inyección de dependencias
 * 
 * @property authRepository Repositorio para operaciones de autenticación
 * @property uiState Estado actual de la UI
 * @property currentStep Paso actual del formulario
 * @property totalSteps Total de pasos del formulario
 * 
 * @see RegistroUiState
 * @see RegistroUsuarioForm
 */
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val solicitudRepository: SolicitudRepository,
    private val nominatimApiService: NominatimApiService,
    private val emailNotificationService: EmailNotificationService,
    private val debugUtils: DebugUtils
) : ViewModel() {

    /**
     * Estado actual de la UI del registro.
     * 
     * Este estado es inmutable y contiene toda la información necesaria para renderizar la UI.
     * Se actualiza mediante el patrón de diseño StateFlow para garantizar la reactividad.
     * 
     * @property form Datos actuales del formulario
     * @property isLoading Indica si hay operaciones en curso
     * @property error Mensaje de error, si existe
     * @property success Indica si el registro fue exitoso
     * @property emailError Error de validación del email
     * @property dniError Error de validación del DNI
     * @property passwordError Error de validación de la contraseña
     * @property nombreError Error de validación del nombre
     * @property apellidosError Error de validación de los apellidos
     * @property telefonoError Error de validación del teléfono
     */
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Variable para indicar si se debe mostrar el diálogo de permiso de ubicación
    private val _showLocationPermissionRequest = MutableStateFlow(false)
    val showLocationPermissionRequest = _showLocationPermissionRequest.asStateFlow()

    init {
        cargarCentros()
    }

    fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCentros = true) }
            
            Timber.d("🏫 Iniciando carga de centros educativos en RegistroViewModel...")
            
            // Reintentar hasta 3 veces con retraso entre intentos
            var intentos = 0
            var resultado: Result<List<Centro>>? = null
            
            while (intentos < 3 && (resultado == null || resultado is Result.Error)) {
                intentos++
                Timber.d("🔄 Intento $intentos de cargar centros educativos")
                
                try {
                    resultado = usuarioRepository.getCentrosEducativos()
                    
                    when (resultado) {
                        is Result.Success -> {
                            val centros = resultado.data
                            Timber.d("✅ Centros cargados exitosamente: ${centros.size}")
                            
                            if (centros.isEmpty()) {
                                Timber.w("⚠️ La lista de centros está vacía")
                                // Esperar y reintentar
                                if (intentos < 3) {
                                    delay(1000L * intentos) // Retraso progresivo
                                    continue
                                }
                            }
                            
                            _uiState.update {
                                it.copy(centros = centros, isLoadingCentros = false)
                            }
                            
                            // Verificar si la actualización de estado funcionó
                            val tamañoActualizado = _uiState.value.centros.size
                            Timber.d("📊 Centros en UI State después de actualización: $tamañoActualizado")
                            
                            break // Salir del bucle si fue exitoso
                        }
                        is Result.Error -> {
                            Timber.e(resultado.exception, "❌ Error en intento $intentos: ${resultado.exception?.message}")
                            
                            // Esperar y reintentar
                            if (intentos < 3) {
                                delay(1000L * intentos) // Retraso progresivo
                            }
                        }
                        else -> { /* Ignorar Loading */ }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Excepción no manejada en intento $intentos: ${e.message}")
                    resultado = Result.Error(e)
                    
                    // Esperar y reintentar
                    if (intentos < 3) {
                        delay(1000L * intentos) // Retraso progresivo
                    }
                }
            }
            
            // Si todos los intentos fallaron, actualizar estado con error
            if (resultado is Result.Error || _uiState.value.centros.isEmpty()) {
                val errorMsg = if (resultado is Result.Error) {
                    "Error al cargar centros: ${resultado.exception?.message}"
                } else {
                    "No se pudieron cargar los centros educativos"
                }
                
                Timber.e("❌ Todos los intentos fallaron: $errorMsg")
                
                _uiState.update {
                    it.copy(
                        error = errorMsg,
                        isLoadingCentros = false
                    )
                }
                
                // En modo debug, crear centros de prueba si no se pudieron cargar
                if (BuildConfig.DEBUG) {
                    Timber.d("🔧 Modo DEBUG: Generando centros de prueba tras fallo...")
                    val centrosPrueba = listOf(
                        Centro(
                            id = "centro_prueba_1",
                            nombre = "Centro Educativo de Prueba 1",
                            direccion = "Calle Principal 123",
                            telefono = "945123456",
                            email = "centro1@prueba.com",
                            activo = true
                        ),
                        Centro(
                            id = "centro_prueba_2",
                            nombre = "Centro Educativo de Prueba 2",
                            direccion = "Avenida Secundaria 456",
                            telefono = "945654321",
                            email = "centro2@prueba.com",
                            activo = true
                        )
                    )
                    
                    _uiState.update {
                        it.copy(
                            centros = centrosPrueba,
                            isLoadingCentros = false,
                            error = null // Limpiar el error ya que tenemos datos de prueba
                        )
                    }
                }
            }
        }
    }

    /**
     * Actualiza un campo del formulario y dispara validaciones en tiempo real.
     *
     * @param field Nombre del campo (ej. "email", "calle", "centroId").
     * @param value Nuevo valor del campo.
     */
    fun updateFormField(field: String, value: String) {
        _uiState.update { currentState ->
            val newForm = when (field) {
                "dni" -> currentState.form.copy(dni = value)
                "email" -> currentState.form.copy(email = value)
                "password" -> currentState.form.copy(password = value)
                "confirmPassword" -> currentState.form.copy(confirmPassword = value)
                "nombre" -> currentState.form.copy(nombre = value)
                "apellidos" -> currentState.form.copy(apellidos = value)
                "telefono" -> currentState.form.copy(telefono = value)
                // Campos de Dirección
                "calle" -> currentState.form.copy(direccion = currentState.form.direccion.copy(calle = value))
                "numero" -> currentState.form.copy(direccion = currentState.form.direccion.copy(numero = value))
                "piso" -> currentState.form.copy(direccion = currentState.form.direccion.copy(piso = value))
                "codigoPostal" -> {
                    val formUpdated = currentState.form.copy(direccion = currentState.form.direccion.copy(codigoPostal = value))
                    // Si el código postal tiene 5 dígitos, buscar automáticamente
                    if (value.length == 5 && value.all { it.isDigit() }) {
                        // Solicitar permiso de ubicación si es necesario
                        _showLocationPermissionRequest.value = true
                        // Lanzar búsqueda en segundo plano
                        buscarDireccionPorCP(value)
                    }
                    formUpdated
                }
                "ciudad" -> currentState.form.copy(direccion = currentState.form.direccion.copy(ciudad = value))
                "provincia" -> currentState.form.copy(direccion = currentState.form.direccion.copy(provincia = value))
                // Campo de Centro
                "centroId" -> currentState.form.copy(centroId = value)
                else -> currentState.form // No hacer nada si el campo no se reconoce
            }
            // Llamar a validación después de actualizar el formulario
            validateField(field, newForm, currentState.copy(form = newForm))
        }
    }

    /**
     * Busca la dirección completa basada en un código postal usando la API de Nominatim.
     * Actualiza automáticamente los campos de ciudad y provincia.
     * 
     * @param codigoPostal El código postal a buscar (5 dígitos para España)
     */
    private fun buscarDireccionPorCP(codigoPostal: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingDireccion = true) }
                
                // Registrar actividad en Crashlytics para errores reales
                FirebaseCrashlytics.getInstance().log("Iniciando búsqueda de CP: $codigoPostal")
                Timber.d("Iniciando búsqueda de dirección para CP: $codigoPostal")
                
                val response = nominatimApiService.searchByPostalCode(
                    postalCode = codigoPostal,
                    limit = 1
                )
                
                Timber.d("Respuesta recibida: ${response.isSuccessful}, código: ${response.code()}")
                FirebaseCrashlytics.getInstance().setCustomKey("nominatim_response_code", response.code())
                
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val place = response.body()!!.first()
                    
                    // Registrar datos recibidos para depuración
                    Timber.d("Datos recibidos: ${place.displayName}")
                    
                    // Extraer ciudad y provincia
                    val ciudad = place.address?.getCityName() ?: ""
                    val provincia = place.address?.province ?: place.address?.state ?: ""
                    
                    Timber.d("Ciudad extraída: $ciudad, Provincia: $provincia")
                    
                    // Extraer coordenadas
                    val latitud = place.lat.toDoubleOrNull()
                    val longitud = place.lon.toDoubleOrNull()
                    
                    // Generar URL de mapa estático (usar OpenStreetMap en lugar de Google Maps)
                    val mapaUrl = if (latitud != null && longitud != null) {
                        // URL para OpenStreetMap
                        "https://staticmap.openstreetmap.de/staticmap.php?center=$latitud,$longitud&zoom=14&size=400x200&markers=$latitud,$longitud"
                    } else null
                    
                    // Actualizar el formulario con los datos obtenidos
                    _uiState.update { state ->
                        state.copy(
                            form = state.form.copy(
                                direccion = state.form.direccion.copy(
                                    ciudad = ciudad,
                                    provincia = provincia
                                )
                            ),
                            coordenadasLatitud = latitud,
                            coordenadasLongitud = longitud,
                            mapaUrl = mapaUrl,
                            isLoadingDireccion = false
                        )
                    }
                    
                    Timber.d("Formulario actualizado con datos de CP: $codigoPostal")
                } else {
                    // Registrar errores específicos
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Timber.e("Error en respuesta Nominatim: código ${response.code()}, error: $errorBody")
                    FirebaseCrashlytics.getInstance().log("Error Nominatim: $errorBody")
                    FirebaseCrashlytics.getInstance().setCustomKey("nominatim_error_body", errorBody)
                    
                    if (!response.isSuccessful) {
                        FirebaseCrashlytics.getInstance().recordException(Exception("Error en Nominatim: ${response.code()} - $errorBody"))
                    } else if (response.body()?.isEmpty() == true) {
                        Timber.e("Nominatim devolvió una lista vacía para CP: $codigoPostal")
                        FirebaseCrashlytics.getInstance().log("Nominatim devolvió lista vacía para CP: $codigoPostal")
                    }
                    
                    _uiState.update { it.copy(isLoadingDireccion = false) }
                }
            } catch (e: IOException) {
                Timber.e(e, "Error de red al buscar dirección para CP $codigoPostal: ${e.message}")
                FirebaseCrashlytics.getInstance().log("Error red Nominatim: ${e.message}")
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.update { it.copy(isLoadingDireccion = false) }
            } catch (e: HttpException) {
                Timber.e(e, "Error HTTP al buscar dirección para CP $codigoPostal: ${e.message}, código: ${e.code()}")
                FirebaseCrashlytics.getInstance().log("Error HTTP Nominatim: ${e.message}, código: ${e.code()}")
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.update { it.copy(isLoadingDireccion = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error general al buscar dirección para CP $codigoPostal: ${e.message}")
                FirebaseCrashlytics.getInstance().log("Error general Nominatim: ${e.message}")
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.update { it.copy(isLoadingDireccion = false) }
            }
        }
    }

    /**
     * Valida un campo específico y actualiza el estado de error correspondiente.
     *
     * @param field Nombre del campo validado.
     * @param updatedForm El formulario con el valor ya actualizado.
     * @param currentState El estado actual (antes de aplicar errores de validación).
     * @return El nuevo estado con los errores de validación actualizados.
     */
    private fun validateField(field: String, updatedForm: RegistroUsuarioForm, currentState: RegistroUiState): RegistroUiState {
        var newState = currentState.copy(form = updatedForm) // Empezar con el formulario actualizado

        when (field) {
            "email" -> {
                val emailError = if (updatedForm.email.isNotBlank() && !isEmailValid(updatedForm.email)) {
                    "Formato de email inválido."
                } else null
                newState = newState.copy(emailError = emailError)
            }
            "dni" -> {
                val dniError = if (updatedForm.dni.isNotBlank() && !validateDni(updatedForm.dni)) {
                    "Formato de DNI/NIE inválido."
                } else null
                newState = newState.copy(dniError = dniError)
            }
            "password" -> {
                var passwordError: String? = null
                if (updatedForm.password.isNotBlank() && !validatePassword(updatedForm.password)) {
                    passwordError = "La contraseña no cumple los requisitos."
                }
                newState = newState.copy(passwordError = passwordError)
                // Revalidar confirmación si la contraseña cambia
                val confirmError = if (updatedForm.confirmPassword.isNotBlank() && updatedForm.password != updatedForm.confirmPassword) {
                    "Las contraseñas no coinciden."
                } else null
                newState = newState.copy(confirmPasswordError = confirmError)
            }
            "confirmPassword" -> {
                val confirmError = if (updatedForm.confirmPassword.isNotBlank() && updatedForm.password != updatedForm.confirmPassword) {
                    "Las contraseñas no coinciden."
                } else null
                newState = newState.copy(confirmPasswordError = confirmError)
            }
             "telefono" -> {
                 val telefonoError = if (updatedForm.telefono.isNotBlank() && !isPhoneValid(updatedForm.telefono)) {
                     "Formato de teléfono inválido."
                 } else null
                 newState = newState.copy(telefonoError = telefonoError)
             }
             "centroId" -> {
                 val centroError = if (updatedForm.centroId.isBlank()) {
                     "Debes seleccionar un centro."
                 } else null
                 newState = newState.copy(centroIdError = centroError)
             }
             "codigoPostal" -> {
                 val cpError = if (updatedForm.direccion.codigoPostal.isNotBlank() && 
                                   (updatedForm.direccion.codigoPostal.length != 5 || 
                                    !updatedForm.direccion.codigoPostal.all { it.isDigit() })) {
                     "Código postal debe tener 5 dígitos"
                 } else null
                 newState = newState.copy(direccionError = cpError)
             }
            // Añadir validaciones para otros campos si es necesario (nombre, apellidos, dirección)
        }
        return newState
    }

    /**
     * Actualiza el tipo de familiar seleccionado.
     */
    fun updateSubtipoFamiliar(subtipo: SubtipoFamiliar) {
        _uiState.update { it.copy(form = it.form.copy(subtipo = subtipo)) }
    }

    /**
     * Añade un nuevo campo vacío para DNI de alumno.
     */
    fun addAlumnoDni() {
        _uiState.update { currentState ->
            val newList = currentState.form.alumnosDni.toMutableList().apply { add("") }
            currentState.copy(form = currentState.form.copy(alumnosDni = newList))
        }
    }

    /**
     * Actualiza el DNI de un alumno específico y valida la lista.
     */
    fun updateAlumnoDni(index: Int, value: String) {
        _uiState.update { currentState ->
            val newList = currentState.form.alumnosDni.toMutableList()
            if (index >= 0 && index < newList.size) {
                newList[index] = value
                // Validar DNI específico aquí si se desea feedback inmediato
                 val dniError = if (value.isNotBlank() && !validateDni(value)) "Formato DNI inválido" else null
                 // Podrías guardar errores por índice si necesitas mostrarlos individualmente
                 currentState.copy(form = currentState.form.copy(alumnosDni = newList))
             } else {
                 currentState // No hacer nada si el índice es inválido
             }
        }
    }

    /**
     * Elimina el DNI de un alumno específico y valida la lista.
     */
    fun removeAlumnoDni(index: Int) {
        _uiState.update { currentState ->
            val newList = currentState.form.alumnosDni.toMutableList()
            if (index >= 0 && index < newList.size) {
                newList.removeAt(index)
            }
            currentState.copy(form = currentState.form.copy(alumnosDni = newList))
        }
    }

    /**
      * Valida la lista de DNIs de alumnos (ej. si se requiere al menos uno).
      */
    private fun validateAlumnosDniList(form: RegistroUsuarioForm, currentState: RegistroUiState): RegistroUiState {
        // Ejemplo: Validar que al menos un DNI no esté vacío si estamos en el paso 3 o más allá
        val error = if (currentState.currentStep >= 3 && form.alumnosDni.all { it.isBlank() }) {
            "Debes añadir al menos un DNI de alumno."
        } else {
            null
        }
        return currentState.copy(form = form, alumnosDniError = error)
    }

    /**
     * Avanza al siguiente paso del formulario.
     * La validación ahora se hace en la UI antes de llamar a esta función.
     */
    fun nextStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep < currentState.totalSteps) {
                currentState.copy(currentStep = currentState.currentStep + 1, error = null) // Limpiar error al avanzar
            } else {
                currentState // No hacer nada si ya está en el último paso
            }
        }
    }

    /**
     * Retrocede al paso anterior del formulario.
     */
    fun previousStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep > 1) {
                currentState.copy(currentStep = currentState.currentStep - 1, error = null) // Limpiar error al retroceder
            } else {
                currentState // No hacer nada si ya está en el primer paso
            }
        }
    }

    /**
     * Registra al usuario en Firebase Auth y guarda sus datos en Firestore.
     * Además, crea las solicitudes de vinculación para cada alumno introducido.
     */
    fun registrarUsuario() {
        if (!isFormValid()) {
            _uiState.update { it.copy(error = "Por favor, completa todos los campos requeridos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val form = _uiState.value.form
            val resultRegistro = usuarioRepository.registrarUsuarioCompleto(
                email = form.email,
                password = form.password,
                dni = form.dni,
                nombre = form.nombre,
                apellidos = form.apellidos,
                telefono = form.telefono,
                tipoUsuario = com.tfg.umeegunero.data.model.TipoUsuario.FAMILIAR, // Tipo fijo para registro familiar
                subtipo = form.subtipo.name, // Usar el subtipo seleccionado
                direccion = form.direccion,
                centroId = form.centroId, // Pasar centroId para perfil
                perfilesAdicionales = emptyList() // Sin perfiles adicionales en registro familiar inicial
            )

            when (resultRegistro) {
                is Result.Success<Usuario> -> {
                    // Usuario creado con éxito, ahora crear solicitudes de vinculación
                    val familiarId = form.dni // Usar DNI del familiar como ID
                    val nombreFamiliarCompleto = "${form.nombre} ${form.apellidos}"
                    val tipoRelacionSeleccionado = form.subtipo.name
                    val centroIdSeleccionado = form.centroId
                    var todasSolicitudesCreadas = true
                    
                    // Filtrar DNIs de alumnos válidos y no vacíos
                    val alumnosDniValidos = form.alumnosDni.filter { it.isNotBlank() && validateDni(it) }
                    
                    if (alumnosDniValidos.isEmpty()) {
                         Timber.w("Registro de usuario exitoso, pero no se encontraron DNIs de alumnos válidos para crear solicitudes.")
                         // Considerar si esto es un error o un caso válido
                         // >>> ENVÍO DE EMAIL DE BIENVENIDA <<<
                         viewModelScope.launch {
                             val emailEnviado = emailNotificationService.sendWelcomeEmail(form.email, form.nombre)
                             if (emailEnviado) {
                                 Timber.i("Email de bienvenida enviado a ${form.email}")
                             } else {
                                 Timber.e("Error al enviar email de bienvenida a ${form.email}")
                             }
                         }
                         _uiState.update { it.copy(isLoading = false, success = true) } // Marcar éxito aunque no haya solicitudes
                         return@launch
                    }
                    
                    alumnosDniValidos.forEach { alumnoDni ->
                        val solicitud = SolicitudVinculacion(
                            // id se genera automáticamente en el repositorio
                            familiarId = familiarId,
                            alumnoId = alumnoDni, // Usar DNI del alumno como ID por ahora
                            alumnoDni = alumnoDni,
                            alumnoNombre = "", // TODO: Intentar obtener nombre del alumno si es posible?
                            nombreFamiliar = nombreFamiliarCompleto,
                            tipoRelacion = tipoRelacionSeleccionado,
                            centroId = centroIdSeleccionado,
                            fechaSolicitud = Timestamp.now(),
                            estado = EstadoSolicitud.PENDIENTE,
                            // Campos de admin y procesamiento vacíos inicialmente
                            adminId = "",
                            nombreAdmin = "",
                            fechaProcesamiento = null,
                            observaciones = ""
                        )
                        
                        when (val resultSolicitud = solicitudRepository.crearSolicitudVinculacion(solicitud)) {
                            is Result.Success -> {
                                Timber.d("Solicitud de vinculación creada para alumno $alumnoDni")
                            }
                            is Result.Error -> {
                                Timber.e(resultSolicitud.exception, "Error al crear solicitud para alumno $alumnoDni")
                                todasSolicitudesCreadas = false
                                // No actualizamos el error principal aquí para no sobrescribir éxito de registro
                            }
                            else -> {} // Ignorar Loading
                        }
                    }
                    
                    // >>> ENVÍO DE EMAIL DE BIENVENIDA <<<
                    viewModelScope.launch {
                        val emailEnviado = emailNotificationService.sendWelcomeEmail(form.email, form.nombre)
                        if (emailEnviado) {
                            Timber.i("Email de bienvenida enviado a ${form.email}")
                        } else {
                            Timber.e("Error al enviar email de bienvenida a ${form.email}")
                            // Considerar añadir un mensaje no bloqueante en la UI si falla?
                        }
                    }
                    
                    if (!todasSolicitudesCreadas) {
                         // Podríamos añadir un mensaje no bloqueante indicando que alguna solicitud falló
                         Timber.w("Registro de usuario exitoso, pero falló la creación de una o más solicitudes de vinculación.")
                    }
                    
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Error en el registro: ${resultRegistro.exception?.message ?: "Desconocido"}"
                        )
                    }
                }
                else -> { /* Ignorar Loading */ }
            }
        }
    }

    // --- Funciones de Validación Auxiliares (Mantenidas o movidas a Utils) ---
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateDni(dni: String): Boolean {
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toIntOrNull() ?: return false
        return dni.uppercase()[8] == letras[numero % 23]
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 6 && 
               password.any { it.isLetter() } && 
               password.any { it.isDigit() } &&
               password.any { !it.isLetterOrDigit() } // Validar al menos un carácter especial
    }

     private fun isPhoneValid(phone: String): Boolean {
         // Implementar validación de formato de teléfono si es necesario
         return phone.all { it.isDigit() } && phone.length >= 9 // Ejemplo básico
     }

    // --- Función de validación general del formulario (ajustada) ---
    private fun isFormValid(): Boolean {
        val s = _uiState.value
        return s.form.email.isNotBlank() && s.emailError == null &&
               s.form.dni.isNotBlank() && s.dniError == null &&
               s.form.nombre.isNotBlank() && s.nombreError == null &&
               s.form.apellidos.isNotBlank() && s.apellidosError == null &&
               s.form.telefono.isNotBlank() && s.telefonoError == null &&
               s.form.password.isNotBlank() && s.passwordError == null && validatePassword(s.form.password) &&
               s.form.confirmPassword.isNotBlank() && s.confirmPasswordError == null && s.form.password == s.form.confirmPassword &&
               s.form.direccion.calle.isNotBlank() &&
               s.form.direccion.numero.isNotBlank() &&
               s.form.direccion.codigoPostal.isNotBlank() &&
               s.form.direccion.ciudad.isNotBlank() &&
               s.form.direccion.provincia.isNotBlank() &&
               s.form.centroId.isNotBlank() &&
               s.form.alumnosDni.any { it.isNotBlank() } && // Al menos un DNI
               s.form.alumnosDni.filter { it.isNotBlank() }.all { validateDni(it) } // Todos los no vacíos son válidos
    }
    
    // Métodos para manejar la respuesta del permiso de ubicación
    
    /**
     * Reinicia el estado de solicitud de permiso de ubicación
     */
    fun resetLocationPermissionRequest() {
        _showLocationPermissionRequest.value = false
    }
    
    /**
     * Maneja la concesión del permiso de ubicación
     */
    fun onLocationPermissionGranted() {
        resetLocationPermissionRequest()
        // Intentar buscar la localización de nuevo ya que ahora tenemos permiso
        val codigoPostal = _uiState.value.form.direccion.codigoPostal
        if (codigoPostal.length == 5) {
            buscarDireccionPorCP(codigoPostal)
        }
    }
    
    /**
     * Maneja el rechazo del permiso de ubicación
     */
    fun onLocationPermissionDenied() {
        resetLocationPermissionRequest()
        // Informar al usuario que algunas funcionalidades pueden no estar disponibles
        _uiState.update { 
            it.copy(direccionError = "La geolocalización no estará disponible sin el permiso de ubicación")
        }
    }
}