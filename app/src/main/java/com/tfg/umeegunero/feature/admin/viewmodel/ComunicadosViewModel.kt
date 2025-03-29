package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.ComunicadosUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel para la pantalla de comunicados del sistema
 */
@HiltViewModel
class ComunicadosViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicadosUiState())
    val uiState: StateFlow<ComunicadosUiState> = _uiState.asStateFlow()

    init {
        cargarComunicados()
    }

    /**
     * Carga los comunicados desde Firestore
     */
    fun cargarComunicados() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val comunicadosSnapshot = firestore.collection("comunicados")
                    .orderBy("fechaCreacion")
                    .get()
                    .await()

                val comunicados = comunicadosSnapshot.documents.mapNotNull { document ->
                    val comunicado = document.toObject(Comunicado::class.java)
                    comunicado?.copy(id = document.id)
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    comunicados = comunicados
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar comunicados"
                ) }
            }
        }
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

    /**
     * Muestra/oculta el formulario de nuevo comunicado
     */
    fun toggleNuevoComunicado() {
        _uiState.update { it.copy(
            mostrarFormulario = !it.mostrarFormulario,
            titulo = "",
            mensaje = "",
            tituloError = "",
            mensajeError = "",
            enviarATodos = false,
            enviarACentros = false,
            enviarAProfesores = false,
            enviarAFamiliares = false,
            enviado = false
        ) }
    }

    /**
     * Actualiza el título del comunicado
     */
    fun updateTitulo(titulo: String) {
        _uiState.update { it.copy(
            titulo = titulo,
            tituloError = if (titulo.isEmpty()) "El título es obligatorio" else ""
        ) }
    }

    /**
     * Actualiza el mensaje del comunicado
     */
    fun updateMensaje(mensaje: String) {
        _uiState.update { it.copy(
            mensaje = mensaje,
            mensajeError = if (mensaje.isEmpty()) "El mensaje es obligatorio" else ""
        ) }
    }

    /**
     * Activar/desactivar envío a todos los usuarios
     */
    fun toggleEnviarATodos(checked: Boolean) {
        _uiState.update { it.copy(
            enviarATodos = checked,
            enviarACentros = if (checked) false else it.enviarACentros,
            enviarAProfesores = if (checked) false else it.enviarAProfesores,
            enviarAFamiliares = if (checked) false else it.enviarAFamiliares
        ) }
    }

    /**
     * Activar/desactivar envío a centros
     */
    fun toggleEnviarACentros(checked: Boolean) {
        _uiState.update { it.copy(enviarACentros = checked) }
    }

    /**
     * Activar/desactivar envío a profesores
     */
    fun toggleEnviarAProfesores(checked: Boolean) {
        _uiState.update { it.copy(enviarAProfesores = checked) }
    }

    /**
     * Activar/desactivar envío a familiares
     */
    fun toggleEnviarAFamiliares(checked: Boolean) {
        _uiState.update { it.copy(enviarAFamiliares = checked) }
    }

    /**
     * Envía el comunicado a los destinatarios seleccionados
     */
    fun enviarComunicado() {
        viewModelScope.launch {
            // Validar datos
            val titulo = _uiState.value.titulo
            val mensaje = _uiState.value.mensaje
            
            var tituloError = ""
            var mensajeError = ""
            
            if (titulo.isEmpty()) {
                tituloError = "El título es obligatorio"
            }
            
            if (mensaje.isEmpty()) {
                mensajeError = "El mensaje es obligatorio"
            }
            
            val destinatariosSeleccionados = _uiState.value.enviarATodos || 
                    _uiState.value.enviarACentros || 
                    _uiState.value.enviarAProfesores ||
                    _uiState.value.enviarAFamiliares
            
            if (!destinatariosSeleccionados) {
                _uiState.update { it.copy(
                    error = "Debe seleccionar al menos un tipo de destinatario"
                ) }
                return@launch
            }
            
            if (tituloError.isNotEmpty() || mensajeError.isNotEmpty()) {
                _uiState.update { it.copy(
                    tituloError = tituloError,
                    mensajeError = mensajeError
                ) }
                return@launch
            }
            
            try {
                _uiState.update { it.copy(isEnviando = true) }
                
                // Determinar destinatarios
                val tiposDestinatarios = mutableListOf<String>()
                
                if (_uiState.value.enviarATodos) {
                    tiposDestinatarios.add("TODOS")
                } else {
                    if (_uiState.value.enviarACentros) tiposDestinatarios.add("ADMIN_CENTRO")
                    if (_uiState.value.enviarAProfesores) tiposDestinatarios.add("PROFESOR")
                    if (_uiState.value.enviarAFamiliares) tiposDestinatarios.add("FAMILIAR")
                }
                
                // Crear objeto comunicado
                val now = Date()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
                val fechaFormateada = dateFormat.format(now)
                
                val comunicado = Comunicado(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo,
                    mensaje = mensaje,
                    fechaCreacion = Timestamp(now),
                    fecha = fechaFormateada,
                    remitente = "Administrador del Sistema",
                    destinatarios = if (_uiState.value.enviarATodos) "Todos los usuarios" 
                                   else tiposDestinatarios.joinToString(", "),
                    tiposDestinatarios = tiposDestinatarios
                )
                
                // Guardar en Firestore
                firestore.collection("comunicados")
                    .document(comunicado.id)
                    .set(comunicado)
                    .await()
                
                // Simular tiempo de respuesta del servidor
                delay(1000)
                
                // Actualizar estado y mostrar mensaje de éxito
                _uiState.update { it.copy(
                    isEnviando = false,
                    enviado = true
                ) }
                
                // Recargar lista después de un breve retraso
                delay(2000)
                cargarComunicados()
                toggleNuevoComunicado()
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isEnviando = false,
                    error = e.message ?: "Error al enviar el comunicado"
                ) }
            }
        }
    }
} 