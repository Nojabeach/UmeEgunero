package com.tfg.umeegunero.feature.admin.viewmodel.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Patterns
import com.tfg.umeegunero.data.model.PlantillaEmail
import com.tfg.umeegunero.data.service.EmailNotificationService
import timber.log.Timber

/**
 * ViewModel para la gestión de la pantalla de pruebas de email.
 *
 * Este ViewModel maneja la lógica de negocio para:
 * - Validación de emails en tiempo real
 * - Gestión de plantillas de email
 * - Previsualización de contenido HTML
 * - Control de estados de la UI
 *
 * @property emailState Estado actual de la UI
 * @see PruebaEmailUiState
 * @see PlantillaEmail
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@HiltViewModel
class PruebaEmailViewModel @Inject constructor(
    private val emailService: EmailNotificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PruebaEmailUiState())
    val uiState: StateFlow<PruebaEmailUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email del destinatario y valida su formato.
     *
     * @param email Dirección de email a validar
     */
    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                destinatario = email,
                emailValido = validarEmail(email),
                mostrarError = email.isNotBlank() && !validarEmail(email)
            )
        }
    }

    /**
     * Selecciona una plantilla de email y actualiza la previsualización.
     *
     * @param tipo Tipo de plantilla a seleccionar
     */
    fun seleccionarPlantilla(tipo: TipoPlantilla) {
        _uiState.update { currentState ->
            currentState.copy(
                plantillaSeleccionada = tipo,
                previsualizacionHtml = obtenerContenidoHtml(tipo, currentState.nombrePrueba)
            )
        }
    }

    /**
     * Actualiza el nombre de prueba para las plantillas.
     *
     * @param nombre Nombre a utilizar en las plantillas
     */
    fun updateNombrePrueba(nombre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nombrePrueba = nombre,
                previsualizacionHtml = obtenerContenidoHtml(currentState.plantillaSeleccionada, nombre)
            )
        }
    }

    /**
     * Valida el formato de una dirección de email.
     *
     * @param email Email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    private fun validarEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Obtiene el contenido HTML para una plantilla específica.
     *
     * @param tipo Tipo de plantilla
     * @param nombre Nombre a incluir en la plantilla
     * @return Contenido HTML de la plantilla
     */
    private fun obtenerContenidoHtml(tipo: TipoPlantilla, nombre: String): String {
        return when (tipo) {
            TipoPlantilla.APROBACION -> PlantillaEmail.obtenerPlantillaAprobacion(nombre)
            TipoPlantilla.RECHAZO -> PlantillaEmail.obtenerPlantillaRechazo(nombre)
            TipoPlantilla.BIENVENIDA -> PlantillaEmail.obtenerPlantillaBienvenida(nombre)
            TipoPlantilla.RECORDATORIO -> PlantillaEmail.obtenerPlantillaRecordatorio(nombre)
            TipoPlantilla.NINGUNA -> ""
        }
    }

    /**
     * Envía un email utilizando el servicio.
     *
     * @param destinatario El email del destinatario
     * @param nombre El nombre del destinatario
     * @param tipoPlantilla El tipo de plantilla a utilizar
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun enviarEmail(destinatario: String, nombre: String, tipoPlantilla: TipoPlantilla): Boolean {
        val plantillaServicio = when (tipoPlantilla) {
            TipoPlantilla.APROBACION -> com.tfg.umeegunero.data.service.TipoPlantilla.APROBACION
            TipoPlantilla.RECHAZO -> com.tfg.umeegunero.data.service.TipoPlantilla.RECHAZO
            TipoPlantilla.BIENVENIDA -> com.tfg.umeegunero.data.service.TipoPlantilla.BIENVENIDA
            TipoPlantilla.RECORDATORIO -> com.tfg.umeegunero.data.service.TipoPlantilla.RECORDATORIO
            TipoPlantilla.NINGUNA -> com.tfg.umeegunero.data.service.TipoPlantilla.NINGUNA
        }
        
        return emailService.sendEmail(destinatario, nombre, plantillaServicio)
    }
}

/**
 * Estado de la UI para la pantalla de pruebas de email.
 *
 * @property destinatario Email del destinatario
 * @property nombrePrueba Nombre utilizado en las plantillas
 * @property emailValido Indica si el formato del email es válido
 * @property mostrarError Indica si se debe mostrar el error de validación
 * @property plantillaSeleccionada Tipo de plantilla seleccionada actualmente
 * @property previsualizacionHtml Contenido HTML de la plantilla actual
 */
data class PruebaEmailUiState(
    val destinatario: String = "",
    val nombrePrueba: String = "NombrePrueba",
    val emailValido: Boolean = false,
    val mostrarError: Boolean = false,
    val plantillaSeleccionada: TipoPlantilla = TipoPlantilla.NINGUNA,
    val previsualizacionHtml: String = ""
)

/**
 * Tipos de plantillas de email disponibles.
 */
enum class TipoPlantilla {
    NINGUNA,
    APROBACION,
    RECHAZO,
    BIENVENIDA,
    RECORDATORIO
} 