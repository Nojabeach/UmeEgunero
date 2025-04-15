package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de comunicados del perfil familiar
 */
data class ComunicadosFamiliaUiState(
    val comunicados: List<Comunicado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mostrarSoloNoLeidos: Boolean = false,
    val mostrarSoloSinConfirmar: Boolean = false,
    val comunicadosNoLeidos: Int = 0,
    val comunicadosSinConfirmar: Int = 0,
    val estadoLecturaPorComunicado: Map<String, Boolean> = emptyMap(),
    val estadoConfirmacionPorComunicado: Map<String, Boolean> = emptyMap(),
    val mostrarDialogoConfirmacion: Boolean = false,
    val comunicadoSeleccionado: Comunicado? = null,
    val mostrarDetalle: Boolean = false,
    val comunicadoParaDetalle: Comunicado? = null
) {
    /**
     * Indica si hay algún filtro activo
     */
    val filtroActivo: Boolean
        get() = mostrarSoloNoLeidos || mostrarSoloSinConfirmar
        
    /**
     * Lista filtrada de comunicados según los filtros activos
     */
    val comunicadosFiltrados: List<Comunicado>
        get() = when {
            mostrarSoloNoLeidos -> comunicados.filter { 
                !(estadoLecturaPorComunicado[it.id] ?: false) 
            }
            mostrarSoloSinConfirmar -> comunicados.filter { 
                it.requiereConfirmacion && !(estadoConfirmacionPorComunicado[it.id] ?: false)
            }
            else -> comunicados
        }
}

/**
 * ViewModel para la pantalla de comunicados del perfil familiar
 * 
 * Gestiona la lógica de negocio relacionada con la visualización, carga y gestión de 
 * comunicados dirigidos a usuarios con perfil familiar.
 * 
 * @property firestore Instancia de Firebase Firestore para acceder a la base de datos
 * @property authRepository Repositorio que gestiona la autenticación de usuarios
 * @property comunicadoRepository Repositorio que gestiona los comunicados
 */
@HiltViewModel
class ComunicadosFamiliaViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val comunicadoRepository: ComunicadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicadosFamiliaUiState())
    val uiState: StateFlow<ComunicadosFamiliaUiState> = _uiState.asStateFlow()

    init {
        cargarComunicados()
    }

    /**
     * Carga los comunicados desde Firestore y actualiza el estado
     */
    fun cargarComunicados() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Obtenemos el usuario actual
                val usuarioActual = authRepository.getCurrentUser()
                
                if (usuarioActual == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "No se pudo obtener el usuario actual"
                        ) 
                    }
                    return@launch
                }
                
                // Obtenemos los comunicados para el tipo de usuario FAMILIAR
                val resultComunicados = comunicadoRepository.getComunicadosByTipoUsuario(TipoUsuario.FAMILIAR)
                
                if (resultComunicados is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Error al cargar comunicados: ${resultComunicados.exception?.message ?: "Error desconocido"}"
                        ) 
                    }
                    return@launch
                }
                
                // Si la carga fue exitosa, obtenemos la lista de comunicados
                val comunicados = (resultComunicados as Result.Success).data
                
                // Ahora verificamos el estado de lectura y confirmación de cada comunicado
                val estadoLectura = mutableMapOf<String, Boolean>()
                val estadoConfirmacion = mutableMapOf<String, Boolean>()
                
                // Para cada comunicado, verificamos si ha sido leído/confirmado
                comunicados.forEach { comunicado ->
                    // Verificar estado de lectura
                    when (val resultLectura = authRepository.haLeidoComunicado(comunicado.id)) {
                        is Result.Success -> estadoLectura[comunicado.id] = resultLectura.data
                        is Result.Error -> {
                            Timber.e(resultLectura.exception, "Error al verificar estado de lectura")
                            estadoLectura[comunicado.id] = false
                        }
                        else -> {
                            estadoLectura[comunicado.id] = false
                        }
                    }
                    
                    // Verificar estado de confirmación
                    when (val resultConfirmacion = authRepository.haConfirmadoLecturaComunicado(comunicado.id)) {
                        is Result.Success -> estadoConfirmacion[comunicado.id] = resultConfirmacion.data
                        is Result.Error -> {
                            Timber.e(resultConfirmacion.exception, "Error al verificar estado de confirmación")
                            estadoConfirmacion[comunicado.id] = false
                        }
                        else -> {
                            estadoConfirmacion[comunicado.id] = false
                        }
                    }
                }
                
                // Contadores para el UI
                val noLeidos = comunicados.count { !estadoLectura.getOrDefault(it.id, false) }
                val sinConfirmar = comunicados.count { 
                    it.requiereConfirmacion && !estadoConfirmacion.getOrDefault(it.id, false) 
                }
                
                // Actualizar el estado de la UI
                _uiState.update { state ->
                    state.copy(
                        comunicados = comunicados.sortedByDescending { it.fechaCreacion },
                        isLoading = false,
                        error = null,
                        comunicadosNoLeidos = noLeidos,
                        comunicadosSinConfirmar = sinConfirmar,
                        estadoLecturaPorComunicado = estadoLectura,
                        estadoConfirmacionPorComunicado = estadoConfirmacion
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar comunicados")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar comunicados: ${e.message ?: "Error desconocido"}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Verifica si un comunicado ha sido leído
     * 
     * @param comunicadoId ID del comunicado a verificar
     * @return true si el comunicado ha sido leído, false en caso contrario
     */
    fun esComunicadoLeido(comunicadoId: String): Boolean {
        return _uiState.value.estadoLecturaPorComunicado[comunicadoId] ?: false
    }
    
    /**
     * Verifica si un comunicado ha sido confirmado
     * 
     * @param comunicadoId ID del comunicado a verificar
     * @return true si el comunicado ha sido confirmado, false en caso contrario
     */
    fun esComunicadoConfirmado(comunicadoId: String): Boolean {
        return _uiState.value.estadoConfirmacionPorComunicado[comunicadoId] ?: false
    }

    /**
     * Marca un comunicado como leído y actualiza el estado local
     * 
     * @param comunicadoId ID del comunicado a marcar como leído
     */
    fun marcarComoLeido(comunicadoId: String) {
        viewModelScope.launch {
            try {
                // Ya está marcado como leído, no hacemos nada
                if (esComunicadoLeido(comunicadoId)) {
                    return@launch
                }
                
                // Marcamos como leído usando el repositorio
                when (val resultado = authRepository.marcarComunicadoComoLeido(comunicadoId)) {
                    is Result.Success -> {
                        // Actualizar estado local
                        _uiState.update { state ->
                            val nuevoEstadoLectura = state.estadoLecturaPorComunicado.toMutableMap().apply {
                                put(comunicadoId, true)
                            }
                            
                            state.copy(
                                estadoLecturaPorComunicado = nuevoEstadoLectura,
                                comunicadosNoLeidos = state.comunicadosNoLeidos - 1
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al marcar comunicado como leído")
                        _uiState.update { 
                            it.copy(error = "Error al marcar como leído: ${resultado.exception?.message ?: "Error desconocido"}") 
                        }
                    }
                    else -> {} // No hacemos nada para otros casos
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar comunicado como leído")
                _uiState.update { it.copy(error = "Error al marcar como leído: ${e.message ?: "Error desconocido"}") }
            }
        }
    }
    
    /**
     * Confirma la lectura de un comunicado importante
     * 
     * @param comunicadoId ID del comunicado a confirmar
     */
    fun confirmarLectura(comunicadoId: String) {
        viewModelScope.launch {
            try {
                // Ya está confirmado, no hacemos nada
                if (esComunicadoConfirmado(comunicadoId)) {
                    cerrarDialogoConfirmacion()
                    return@launch
                }
                
                // Confirmamos la lectura usando el repositorio
                when (val resultado = authRepository.confirmarLecturaComunicado(comunicadoId)) {
                    is Result.Success -> {
                        // Actualizar estado local
                        _uiState.update { state ->
                            val nuevoEstadoConfirmacion = state.estadoConfirmacionPorComunicado.toMutableMap().apply {
                                put(comunicadoId, true)
                            }
                            val nuevoEstadoLectura = state.estadoLecturaPorComunicado.toMutableMap().apply {
                                put(comunicadoId, true)
                            }
                            
                            state.copy(
                                estadoConfirmacionPorComunicado = nuevoEstadoConfirmacion,
                                estadoLecturaPorComunicado = nuevoEstadoLectura,
                                comunicadosSinConfirmar = state.comunicadosSinConfirmar - 1,
                                comunicadosNoLeidos = if (!esComunicadoLeido(comunicadoId)) 
                                    state.comunicadosNoLeidos - 1 else state.comunicadosNoLeidos,
                                mostrarDialogoConfirmacion = false,
                                comunicadoSeleccionado = null
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al confirmar lectura")
                        _uiState.update { 
                            it.copy(
                                error = "Error al confirmar la lectura: ${resultado.exception?.message ?: "Error desconocido"}",
                                mostrarDialogoConfirmacion = false
                            ) 
                        }
                    }
                    else -> {} // No hacemos nada para otros casos
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al confirmar lectura")
                _uiState.update { 
                    it.copy(
                        error = "Error al confirmar la lectura: ${e.message ?: "Error desconocido"}",
                        mostrarDialogoConfirmacion = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Muestra el diálogo de confirmación para un comunicado
     * 
     * @param comunicado Comunicado a confirmar
     */
    fun mostrarConfirmacion(comunicado: Comunicado) {
        _uiState.update { currentState ->
            currentState.copy(
                mostrarDialogoConfirmacion = true,
                comunicadoSeleccionado = comunicado
            )
        }
        
        // Si no está marcado como leído, lo marcamos
        if (!esComunicadoLeido(comunicado.id)) {
            marcarComoLeido(comunicado.id)
        }
    }
    
    /**
     * Cierra el diálogo de confirmación
     */
    fun cerrarDialogoConfirmacion() {
        _uiState.update { currentState ->
            currentState.copy(
                mostrarDialogoConfirmacion = false,
                comunicadoSeleccionado = null
            )
        }
    }
    
    /**
     * Muestra el diálogo de detalle para un comunicado
     * 
     * @param comunicado Comunicado a mostrar en detalle
     */
    fun mostrarDetalle(comunicado: Comunicado) {
        _uiState.update { currentState ->
            currentState.copy(
                mostrarDetalle = true,
                comunicadoParaDetalle = comunicado
            )
        }
        
        // Si no está marcado como leído, lo marcamos
        if (!esComunicadoLeido(comunicado.id)) {
            marcarComoLeido(comunicado.id)
        }
    }
    
    /**
     * Cierra el diálogo de detalle
     */
    fun cerrarDetalle() {
        _uiState.update { currentState ->
            currentState.copy(
                mostrarDetalle = false,
                comunicadoParaDetalle = null
            )
        }
    }
    
    /**
     * Alterna el filtro para mostrar solo comunicados no leídos
     */
    fun toggleFiltroNoLeidos() {
        _uiState.update { 
            it.copy(
                mostrarSoloNoLeidos = !it.mostrarSoloNoLeidos,
                mostrarSoloSinConfirmar = false
            ) 
        }
    }
    
    /**
     * Alterna el filtro para mostrar solo comunicados sin confirmar
     */
    fun toggleFiltroSinConfirmar() {
        _uiState.update { 
            it.copy(
                mostrarSoloSinConfirmar = !it.mostrarSoloSinConfirmar,
                mostrarSoloNoLeidos = false
            ) 
        }
    }
    
    /**
     * Restablece todos los filtros activos
     */
    fun resetFiltros() {
        _uiState.update { 
            it.copy(
                mostrarSoloNoLeidos = false,
                mostrarSoloSinConfirmar = false
            ) 
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 