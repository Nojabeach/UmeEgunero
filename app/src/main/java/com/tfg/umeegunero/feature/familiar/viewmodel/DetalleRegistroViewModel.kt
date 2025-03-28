package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Actividad
import com.tfg.umeegunero.data.model.CacaControl
import com.tfg.umeegunero.data.model.Comida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Siesta
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.familiar.screen.RegistroModel
import com.tfg.umeegunero.feature.familiar.screen.DetalleRegistroUiState
import com.tfg.umeegunero.feature.familiar.screen.ComidaModel
import com.tfg.umeegunero.feature.familiar.screen.SiestaModel
import com.tfg.umeegunero.feature.familiar.screen.CacaControlModel
import com.tfg.umeegunero.feature.familiar.screen.ActividadesModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle de registro de actividad de un alumno
 */
@HiltViewModel
class DetalleRegistroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleRegistroUiState())
    val uiState: StateFlow<DetalleRegistroUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del registro de la navegación
        val registroId = savedStateHandle.get<String>("registroId")

        if (registroId != null) {
            cargarRegistro(registroId)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener el ID del registro")
            }
        }
    }

    /**
     * Carga los datos del registro de actividad
     */
    private fun cargarRegistro(registroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val registroResult = usuarioRepository.getRegistroById(registroId)

                when (registroResult) {
                    is Result.Success -> {
                        val registroOriginal = registroResult.data
                        
                        // Convertir a nuestro modelo RegistroModel
                        val registro = RegistroModel(
                            id = registroOriginal.id,
                            alumnoId = registroOriginal.alumnoId,
                            alumnoNombre = registroOriginal.alumnoNombre,
                            fecha = registroOriginal.fecha,
                            profesorId = registroOriginal.profesorId,
                            profesorNombre = registroOriginal.profesorNombre,
                            comida = registroOriginal.comida?.let { comida ->
                                ComidaModel(
                                    consumoPrimero = comida.consumoPrimero,
                                    descripcionPrimero = comida.descripcionPrimero,
                                    consumoSegundo = comida.consumoSegundo,
                                    descripcionSegundo = comida.descripcionSegundo,
                                    consumoPostre = comida.consumoPostre,
                                    descripcionPostre = comida.descripcionPostre,
                                    observaciones = comida.observaciones
                                )
                            },
                            siesta = registroOriginal.siesta?.let { siesta ->
                                SiestaModel(
                                    duracion = siesta.duracion,
                                    observaciones = siesta.observaciones,
                                    inicio = siesta.inicio,
                                    fin = siesta.fin
                                )
                            },
                            cacaControl = registroOriginal.cacaControl?.let { necesidades ->
                                CacaControlModel(
                                    tipo1 = necesidades.tipo1,
                                    tipo2 = necesidades.tipo2,
                                    tipo3 = necesidades.tipo3,
                                    hora = necesidades.hora,
                                    cantidad = necesidades.cantidad,
                                    tipo = necesidades.tipo,
                                    descripcion = necesidades.descripcion
                                )
                            },
                            actividades = registroOriginal.actividades?.let { actividad ->
                                ActividadesModel(
                                    titulo = actividad.titulo,
                                    descripcion = actividad.descripcion,
                                    participacion = actividad.participacion,
                                    observaciones = actividad.observaciones
                                )
                            },
                            observaciones = registroOriginal.observaciones?.toString()
                        )

                        _uiState.update {
                            it.copy(
                                registro = registro,
                                isLoading = false
                            )
                        }

                        // Cargamos el nombre del profesor si no viene incluido
                        if (registro.profesorId != null && registro.profesorNombre == null) {
                            cargarProfesor(registro.profesorId)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar el registro: ${registroResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(registroResult.exception, "Error al cargar registro")
                    }
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar registro")
            }
        }
    }

    /**
     * Carga los datos del profesor que creó el registro
     */
    private fun cargarProfesor(profesorId: String?) {
        if (profesorId == null) return

        viewModelScope.launch {
            try {
                val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                when (profesorResult) {
                    is Result.Success -> {
                        val profesor = profesorResult.data
                        _uiState.update {
                            it.copy(profesorNombre = "${profesor.nombre} ${profesor.apellidos}")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(profesorResult.exception, "Error al cargar profesor")
                    }
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar profesor")
            }
        }
    }

    /**
     * Limpia los errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}