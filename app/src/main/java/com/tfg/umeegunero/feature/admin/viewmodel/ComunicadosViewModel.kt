package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.ComunicadosUiState
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.repository.ComunicadoRepository
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

/**
 * ViewModel para la pantalla de comunicados del sistema
 */
@HiltViewModel
class ComunicadosViewModel @Inject constructor(
    private val comunicadoRepository: ComunicadoRepository
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val resultado = comunicadoRepository.getComunicados()
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            comunicados = resultado.datos,
                            isLoading = false
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error desconocido",
                            isLoading = false
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error desconocido",
                    isLoading = false
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
    fun toggleFormulario() {
        _uiState.update { it.copy(
            mostrarFormulario = !it.mostrarFormulario,
            titulo = "",
            mensaje = "",
            enviarATodos = false,
            enviarACentros = false,
            enviarAProfesores = false,
            enviarAFamiliares = false
        ) }
    }

    /**
     * Actualiza el título del comunicado
     */
    fun updateTitulo(titulo: String) {
        _uiState.update { it.copy(titulo = titulo) }
    }

    /**
     * Actualiza el mensaje del comunicado
     */
    fun updateMensaje(mensaje: String) {
        _uiState.update { it.copy(mensaje = mensaje) }
    }

    /**
     * Activar/desactivar envío a todos los usuarios
     */
    fun toggleEnviarATodos(checked: Boolean) {
        _uiState.update { it.copy(enviarATodos = checked) }
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
        val currentState = _uiState.value
        if (currentState.titulo.isBlank() || currentState.mensaje.isBlank()) {
            _uiState.update { it.copy(error = "El título y el mensaje son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tiposDestinatarios = mutableListOf<String>()
                if (currentState.enviarATodos) tiposDestinatarios.add("TODOS")
                if (currentState.enviarACentros) tiposDestinatarios.add("CENTRO")
                if (currentState.enviarAProfesores) tiposDestinatarios.add("PROFESOR")
                if (currentState.enviarAFamiliares) tiposDestinatarios.add("FAMILIAR")

                val comunicado = Comunicado(
                    id = UUID.randomUUID().toString(),
                    titulo = currentState.titulo,
                    mensaje = currentState.mensaje,
                    fechaCreacion = Timestamp.now(),
                    tiposDestinatarios = tiposDestinatarios
                )
                
                val resultado = comunicadoRepository.crearComunicado(comunicado)
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            success = "Comunicado enviado correctamente",
                            mostrarFormulario = false,
                            titulo = "",
                            mensaje = "",
                            enviarATodos = false,
                            enviarACentros = false,
                            enviarAProfesores = false,
                            enviarAFamiliares = false
                        ) }
                        cargarComunicados()
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error desconocido",
                            isLoading = false
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error desconocido",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Registra la lectura de un comunicado
     */
    fun registrarLectura(comunicadoId: String, usuarioId: String) {
        viewModelScope.launch {
            try {
                val resultado = comunicadoRepository.registrarLectura(comunicadoId, usuarioId)
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            success = "Lectura registrada correctamente"
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error al registrar lectura"
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al registrar lectura"
                ) }
            }
        }
    }

    /**
     * Confirma la lectura de un comunicado
     */
    fun confirmarLectura(comunicadoId: String, usuarioId: String) {
        viewModelScope.launch {
            try {
                val resultado = comunicadoRepository.confirmarLectura(comunicadoId, usuarioId)
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            success = "Lectura confirmada correctamente"
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error al confirmar lectura"
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al confirmar lectura"
                ) }
            }
        }
    }

    /**
     * Añade una firma digital a un comunicado
     */
    fun añadirFirmaDigital(comunicadoId: String, firmaDigital: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val resultado = comunicadoRepository.añadirFirmaDigital(comunicadoId, firmaDigital)
                
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            success = "Firma digital añadida correctamente",
                            isLoading = false
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error al añadir firma digital",
                            isLoading = false
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al añadir firma digital",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Muestra/oculta el panel de estadísticas
     */
    fun toggleEstadisticas(comunicadoId: String) {
        viewModelScope.launch {
            try {
                val resultado = comunicadoRepository.getEstadisticasLectura(comunicadoId)
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            mostrarEstadisticas = !it.mostrarEstadisticas,
                            estadisticasLectura = resultado.datos
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error al obtener estadísticas"
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al obtener estadísticas"
                ) }
            }
        }
    }

    /**
     * Muestra las estadísticas de un comunicado
     */
    fun verEstadisticas(comunicado: Comunicado) {
        _uiState.update { it.copy(
            isLoading = true,
            error = null
        ) }
        cargarEstadisticasLectura(comunicado.id ?: "")
    }
    
    /**
     * Carga las estadísticas de lectura de un comunicado
     */
    private fun cargarEstadisticasLectura(comunicadoId: String) {
        if (comunicadoId.isEmpty()) {
            _uiState.update { it.copy(
                error = "ID de comunicado no válido",
                isLoading = false
            ) }
            return
        }
        
        viewModelScope.launch {
            try {
                val resultado = comunicadoRepository.getEstadisticasLectura(comunicadoId)
                when (resultado) {
                    is Resultado.Exito -> {
                        _uiState.update { it.copy(
                            showEstadisticas = true,
                            estadisticasLectura = resultado.datos,
                            isLoading = false
                        ) }
                    }
                    is Resultado.Error -> {
                        _uiState.update { it.copy(
                            error = resultado.mensaje ?: "Error al obtener estadísticas",
                            isLoading = false
                        ) }
                    }
                    is Resultado.Cargando -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al obtener estadísticas",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Muestra la firma digital de un comunicado
     */
    fun verFirmaDigital(comunicado: Comunicado) {
        _uiState.update { it.copy(
            firmaDigital = comunicado.firmaDigital,
            showFirmaDigital = true
        ) }
    }

    fun cerrarEstadisticas() {
        _uiState.update { it.copy(showEstadisticas = false) }
    }

    fun cerrarFirmaDigital() {
        _uiState.update { it.copy(showFirmaDigital = false) }
    }
} 