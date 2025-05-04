package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.FirebaseFunctionsException
import timber.log.Timber

data class RegistroDiarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val registro: RegistroActividad = RegistroActividad(),
    
    // UI state para las comidas
    val primerPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val segundoPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val postre: EstadoComida = EstadoComida.NO_SERVIDO,
    val merienda: EstadoComida = EstadoComida.NO_SERVIDO,
    val observacionesComida: String = "",
    
    // UI state para la siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: String = "",
    val horaFinSiesta: String = "",
    val observacionesSiesta: String = "",
    
    // UI state para los materiales
    val necesitaPanales: Boolean = false,
    val necesitaToallitas: Boolean = false,
    val necesitaRopaCambio: Boolean = false,
    val otroMaterialNecesario: String = "",
    
    // UI state para las observaciones
    val observacionesGenerales: String = "",
    val haHechoCaca: Boolean = false,
    val numeroCacas: Int = 0,
    val observacionesCaca: String = "",
    
    // UI de la pantalla
    val alumnoId: String = "",
    val claseId: String = "",
    val alumnoNombre: String? = null,
    val claseNombre: String? = null,
    val fechaSeleccionada: Date = Date(),
    val showSuccessDialog: Boolean = false
)

@HiltViewModel
class RegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository,
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegistroDiarioUiState())
    val uiState: StateFlow<RegistroDiarioUiState> = _uiState.asStateFlow()
    
    /**
     * Carga un registro diario existente o crea uno nuevo si no existe
     */
    fun cargarRegistroDiario(alumnoId: String, claseId: String, profesorId: String, fecha: Date = Date()) {
        _uiState.update { it.copy(isLoading = true, error = null, alumnoId = alumnoId, claseId = claseId) }
        
        viewModelScope.launch {
            try {
                // Cargar información del alumno
                val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                if (alumnoResult is Result.Success) {
                    val alumno = alumnoResult.data
                    _uiState.update { it.copy(alumnoNombre = "${alumno.nombre} ${alumno.apellidos}") }
                }
                
                // Cargar información de la clase
                val claseIdAUsar = if (claseId.isNotEmpty()) claseId else {
                    // Si no se proporciona claseId, intentamos obtenerla del alumno
                    val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                    if (alumnoResult is Result.Success) {
                        alumnoResult.data.claseId
                    } else {
                        ""
                    }
                }
                
                if (claseIdAUsar.isNotEmpty()) {
                    val claseResult = claseRepository.getClaseById(claseIdAUsar)
                    if (claseResult is Result.Success) {
                        val clase = claseResult.data
                        _uiState.update { it.copy(claseNombre = clase.nombre, claseId = claseIdAUsar) }
                    }
                }
                
                // Obtener o crear el registro diario
                val result = registroDiarioRepository.obtenerOCrearRegistroDiario(
                    alumnoId = alumnoId,
                    claseId = _uiState.value.claseId,
                    profesorId = profesorId,
                    fecha = fecha
                )
                
                when(result) {
                    is Result.Success -> {
                        val registro = result.data
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                registro = registro,
                                primerPlato = registro.primerPlato,
                                segundoPlato = registro.segundoPlato,
                                postre = registro.postre,
                                merienda = registro.merienda,
                                observacionesComida = registro.observacionesComida,
                                haSiestaSiNo = registro.haSiestaSiNo,
                                horaInicioSiesta = registro.horaInicioSiesta?.toDate()?.let { 
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) 
                                } ?: "",
                                horaFinSiesta = registro.horaFinSiesta?.toDate()?.let { 
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) 
                                } ?: "",
                                observacionesSiesta = registro.observacionesSiesta,
                                haHechoCaca = registro.haHechoCaca,
                                numeroCacas = registro.numeroCacas,
                                observacionesCaca = registro.observacionesCaca,
                                necesitaPanales = registro.necesitaPanales,
                                necesitaToallitas = registro.necesitaToallitas,
                                necesitaRopaCambio = registro.necesitaRopaCambio,
                                otroMaterialNecesario = registro.otroMaterialNecesario,
                                observacionesGenerales = registro.observacionesGenerales
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.exception?.message) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar registro: ${e.message}") }
            }
        }
    }
    
    /**
     * Actualiza el estado de la comida
     */
    fun actualizarEstadoComida(tipo: String, estado: EstadoComida) {
        _uiState.update { state ->
            when (tipo) {
                "primerPlato" -> state.copy(primerPlato = estado)
                "segundoPlato" -> state.copy(segundoPlato = estado)
                "postre" -> state.copy(postre = estado)
                "merienda" -> state.copy(merienda = estado)
                else -> state
            }
        }
    }
    
    /**
     * Actualiza el campo de observaciones de comida
     */
    fun actualizarObservacionesComida(texto: String) {
        _uiState.update { it.copy(observacionesComida = texto) }
    }
    
    /**
     * Actualiza el estado de la siesta
     */
    fun toggleSiesta(haceSiesta: Boolean) {
        _uiState.update { it.copy(haSiestaSiNo = haceSiesta) }
    }
    
    /**
     * Establece la hora de inicio de la siesta
     */
    fun establecerHoraInicioSiesta(hora: String) {
        _uiState.update { it.copy(horaInicioSiesta = hora) }
    }
    
    /**
     * Establece la hora de fin de la siesta
     */
    fun establecerHoraFinSiesta(hora: String) {
        _uiState.update { it.copy(horaFinSiesta = hora) }
    }
    
    /**
     * Actualiza las observaciones de la siesta
     */
    fun actualizarObservacionesSiesta(texto: String) {
        _uiState.update { it.copy(observacionesSiesta = texto) }
    }
    
    /**
     * Actualiza el estado de las deposiciones
     */
    fun toggleCaca(haHechoCaca: Boolean) {
        _uiState.update { it.copy(haHechoCaca = haHechoCaca) }
    }
    
    /**
     * Incrementa el contador de cacas
     */
    fun incrementarCacas() {
        _uiState.update { it.copy(numeroCacas = it.numeroCacas + 1) }
    }
    
    /**
     * Decrementa el contador de cacas
     */
    fun decrementarCacas() {
        _uiState.update { it.copy(numeroCacas = maxOf(0, it.numeroCacas - 1)) }
    }
    
    /**
     * Actualiza las observaciones de las deposiciones
     */
    fun actualizarObservacionesCaca(texto: String) {
        _uiState.update { it.copy(observacionesCaca = texto) }
    }
    
    /**
     * Actualiza el estado de los materiales necesarios
     */
    fun toggleMaterial(tipo: String, necesita: Boolean) {
        _uiState.update { state ->
            when (tipo) {
                "panales" -> state.copy(necesitaPanales = necesita)
                "toallitas" -> state.copy(necesitaToallitas = necesita)
                "ropa" -> state.copy(necesitaRopaCambio = necesita)
                else -> state
            }
        }
    }
    
    /**
     * Actualiza el campo de otros materiales necesarios
     */
    fun actualizarOtroMaterial(texto: String) {
        _uiState.update { it.copy(otroMaterialNecesario = texto) }
    }
    
    /**
     * Actualiza las observaciones generales
     */
    fun actualizarObservacionesGenerales(texto: String) {
        _uiState.update { it.copy(observacionesGenerales = texto) }
    }
    
    /**
     * Guarda el registro con los datos actuales
     */
    fun guardarRegistro(profesorId: String) {
        val state = _uiState.value
        
        // Prepara las fechas para la siesta
        val horaInicioSiesta = if (state.haSiestaSiNo && state.horaInicioSiesta.isNotEmpty()) {
            try {
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                Timestamp(formatoHora.parse(state.horaInicioSiesta) ?: Date())
            } catch (e: Exception) {
                null
            }
        } else null
        
        val horaFinSiesta = if (state.haSiestaSiNo && state.horaFinSiesta.isNotEmpty()) {
            try {
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                Timestamp(formatoHora.parse(state.horaFinSiesta) ?: Date())
            } catch (e: Exception) {
                null
            }
        } else null
        
        val registro = state.registro.copy(
            alumnoId = state.alumnoId,
            claseId = state.claseId,
            profesorId = profesorId,
            fecha = Timestamp(state.fechaSeleccionada),
            primerPlato = state.primerPlato,
            segundoPlato = state.segundoPlato,
            postre = state.postre,
            merienda = state.merienda,
            observacionesComida = state.observacionesComida,
            haSiestaSiNo = state.haSiestaSiNo,
            horaInicioSiesta = horaInicioSiesta,
            horaFinSiesta = horaFinSiesta,
            observacionesSiesta = state.observacionesSiesta,
            haHechoCaca = state.haHechoCaca,
            numeroCacas = state.numeroCacas,
            observacionesCaca = state.observacionesCaca,
            necesitaPanales = state.necesitaPanales,
            necesitaToallitas = state.necesitaToallitas,
            necesitaRopaCambio = state.necesitaRopaCambio,
            otroMaterialNecesario = state.otroMaterialNecesario,
            observacionesGenerales = state.observacionesGenerales
        )
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val result = registroDiarioRepository.actualizarRegistroDiario(registro)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, success = true, showSuccessDialog = true) }
                        
                        // Enviar notificación a familiares solo si hay cambios importantes
                        val cambiosImportantes = hayCambiosImportantes(registro)
                        if (cambiosImportantes) {
                            enviarNotificacionActualizacion(registro)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false, 
                            success = false, 
                            error = result.exception?.message ?: "Error desconocido"
                        ) }
                    }
                    is Result.Loading -> {
                        // Ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false, 
                    success = false, 
                    error = "Error al guardar: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Determina si hay cambios importantes que merezcan una notificación
     */
    private fun hayCambiosImportantes(registro: RegistroActividad): Boolean {
        // Consideramos cambios importantes:
        // - Cambios en comidas (especialmente si no comió)
        // - Cambios en el estado de ánimo
        // - Cambios en actividades completadas
        // - Adición de observaciones
        
        return (registro.primerPlato != EstadoComida.SIN_DATOS) ||
               (registro.segundoPlato != EstadoComida.SIN_DATOS) ||
               (registro.postre != EstadoComida.SIN_DATOS) ||
               (registro.merienda != EstadoComida.SIN_DATOS) ||
               registro.observacionesComida.isNotBlank() ||
               registro.haSiestaSiNo ||
               registro.haHechoCaca ||
               registro.numeroCacas > 0 ||
               registro.observacionesSiesta.isNotBlank() ||
               registro.observacionesCaca.isNotBlank() ||
               registro.observacionesGenerales.isNotBlank()
    }
    
    /**
     * Envía notificación a los familiares sobre la actualización del registro
     */
    private fun enviarNotificacionActualizacion(registro: RegistroActividad) {
        viewModelScope.launch {
            try {
                val alumnoId = _uiState.value.alumnoId
                val profesorId = registro.profesorId ?: ""
                
                // Usar el servicio de notificaciones en vez de Cloud Functions
                notificationService.procesarActualizacionRegistroDiario(
                    registroId = registro.id,
                    alumnoId = alumnoId,
                    profesorId = profesorId,
                    cambiosImportantes = true
                )
                
                Timber.d("Notificación de actualización de registro enviada para alumno $alumnoId")
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar notificación de actualización de registro: ${e.message}")
            }
        }
    }
    
    /**
     * Cierra el diálogo de éxito
     */
    fun ocultarDialogoExito() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }
    
    /**
     * Limpia el error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 