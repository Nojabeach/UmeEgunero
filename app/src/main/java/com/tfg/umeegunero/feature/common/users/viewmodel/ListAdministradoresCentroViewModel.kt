package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
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
 * Estado UI para la pantalla de listado de administradores de centros
 */
data class ListAdministradoresCentroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val adminsCentro: List<Usuario> = emptyList(),
    val adminsCentroFiltrados: List<Usuario> = emptyList(),
    val filtroNombre: String = "",
    val filtroCentro: String = "",
    val filtrosAplicados: Boolean = false
)

/**
 * ViewModel para la gestión de administradores de centros educativos
 */
@HiltViewModel
class ListAdministradoresCentroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAdministradoresCentroUiState())
    val uiState: StateFlow<ListAdministradoresCentroUiState> = _uiState.asStateFlow()

    /**
     * Carga los administradores de centros desde el repositorio
     */
    fun cargarAdministradoresCentro() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = usuarioRepository.getUsersByType(TipoUsuario.ADMIN_CENTRO)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            adminsCentro = result.data,
                            adminsCentroFiltrados = result.data,
                            isLoading = false
                        )
                    }
                    Timber.d("Administradores de centro cargados: ${result.data.size}")
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar administradores: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    Timber.e(result.exception, "Error al cargar administradores de centro")
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /**
     * Elimina un administrador de centro por su DNI
     */
    fun eliminarAdminCentro(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Aquí implementaríamos la lógica real para eliminar en el backend
            // Por ahora simulamos una eliminación local exitosa
            
            val nuevaLista = _uiState.value.adminsCentro.filter { it.dni != dni }
            
            _uiState.update { 
                it.copy(
                    adminsCentro = nuevaLista,
                    adminsCentroFiltrados = if (it.filtrosAplicados) {
                        aplicarFiltrosInterno(nuevaLista, it.filtroNombre, it.filtroCentro)
                    } else {
                        nuevaLista
                    },
                    isLoading = false
                )
            }
        }
    }

    /**
     * Actualiza el filtro por nombre
     */
    fun updateFiltroNombre(nombre: String) {
        _uiState.update { it.copy(filtroNombre = nombre) }
    }

    /**
     * Actualiza el filtro por centro
     */
    fun updateFiltroCentro(centro: String) {
        _uiState.update { it.copy(filtroCentro = centro) }
    }

    /**
     * Aplica los filtros establecidos
     */
    fun aplicarFiltros() {
        val resultado = aplicarFiltrosInterno(
            _uiState.value.adminsCentro,
            _uiState.value.filtroNombre,
            _uiState.value.filtroCentro
        )
        
        _uiState.update { 
            it.copy(
                adminsCentroFiltrados = resultado,
                filtrosAplicados = true
            )
        }
    }

    /**
     * Limpia todos los filtros
     */
    fun limpiarFiltros() {
        _uiState.update { 
            it.copy(
                filtroNombre = "",
                filtroCentro = "",
                adminsCentroFiltrados = it.adminsCentro,
                filtrosAplicados = false
            )
        }
    }

    /**
     * Función auxiliar para aplicar los filtros a una lista
     */
    private fun aplicarFiltrosInterno(
        lista: List<Usuario>,
        nombre: String,
        centro: String
    ): List<Usuario> {
        return lista.filter { admin ->
            val matchNombre = if (nombre.isBlank()) {
                true
            } else {
                "${admin.nombre} ${admin.apellidos}".contains(nombre, ignoreCase = true)
            }
            
            val matchCentro = if (centro.isBlank()) {
                true
            } else {
                // Para simplificar, ignoramos el filtro por centro
                // hasta que implementemos el modelo completo
                true
            }
            
            matchNombre && matchCentro
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 