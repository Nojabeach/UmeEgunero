package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber
import kotlinx.coroutines.flow.map

data class AdminDashboardUiState(
    val centros: List<Centro> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingUsuarios: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val navigateToWelcome: Boolean = false,
    val showListadoCentros: Boolean = false
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    // Propiedad para acceder directamente a los centros desde la UI 
    val centros = _uiState.asStateFlow().map { it.centros }

    init {
        loadCentros()
        loadUsuarios()
        loadCurrentUser()
    }

    fun loadCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = centroRepository.getAllCentros()

                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                centros = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los centros"
                            )
                        }
                        Timber.e(result.exception, "Error al cargar los centros")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los centros"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los centros")
            }
        }
    }
    
    fun loadUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsuarios = true, error = null) }
            
            try {
                val usuarios = mutableListOf<Usuario>()
                
                // Cargar todos los tipos de usuarios
                val tiposUsuario = listOf(
                    TipoUsuario.ADMIN_APP, 
                    TipoUsuario.ADMIN_CENTRO, 
                    TipoUsuario.PROFESOR, 
                    TipoUsuario.FAMILIAR
                )
                
                for (tipo in tiposUsuario) {
                    when (val result = usuarioRepository.getUsersByType(tipo)) {
                        is Result.Success -> {
                            usuarios.addAll(result.data)
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingUsuarios = false,
                                    error = result.exception.message ?: "Error al cargar los usuarios de tipo $tipo"
                                )
                            }
                            Timber.e(result.exception, "Error al cargar usuarios de tipo $tipo")
                            return@launch
                        }
                        else -> {}
                    }
                }
                
                _uiState.update {
                    it.copy(
                        usuarios = usuarios,
                        isLoadingUsuarios = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingUsuarios = false,
                        error = e.message ?: "Error inesperado al cargar los usuarios"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los usuarios")
            }
        }
    }

    fun deleteCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val result = centroRepository.deleteCentro(centroId)

                when (result) {
                    is Result.Success -> {
                        // Recargar la lista después de borrar
                        loadCentros()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(error = result.exception.message ?: "Error al eliminar el centro")
                        }
                        Timber.e(result.exception, "Error al eliminar el centro")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Error inesperado al eliminar el centro")
                }
                Timber.e(e, "Error inesperado al eliminar el centro")
            }
        }
    }
    
    fun deleteUsuario(usuarioDni: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val result = usuarioRepository.borrarUsuarioByDni(usuarioDni)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista después de borrar
                        loadUsuarios()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al eliminar el usuario"
                            )
                        }
                        Timber.e(result.exception, "Error al eliminar el usuario")
                    }
                    else -> { /* Ignorar estado loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el usuario"
                    )
                }
                Timber.e(e, "Error inesperado al eliminar el usuario")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // Intentar obtener el usuario directamente del repositorio de usuario
                // usando el ID del usuario autenticado actualmente
                val currentFirebaseUser = authRepository.getCurrentUser()
                
                if (currentFirebaseUser != null) {
                    // Aquí podríamos necesitar buscar el perfil completo del usuario
                    // usando algún campo identificador como el email o ID
                    when (val userResult = usuarioRepository.getUsuarioByEmail(currentFirebaseUser.email)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(currentUser = userResult.data) }
                        }
                        is Result.Error -> {
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario")
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el usuario actual")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update { it.copy(navigateToWelcome = true, currentUser = null) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar sesión")
                _uiState.update { 
                    it.copy(error = e.message ?: "Error al cerrar sesión")
                }
            }
        }
    }

    fun setShowListadoCentros(show: Boolean) {
        _uiState.value = _uiState.value.copy(showListadoCentros = show)
    }

    /**
     * Muestra el listado de centros
     */
    fun showListadoCentros() {
        _uiState.update { it.copy(showListadoCentros = true) }
        loadCentros() // Recargar centros para asegurar datos actualizados
    }
}