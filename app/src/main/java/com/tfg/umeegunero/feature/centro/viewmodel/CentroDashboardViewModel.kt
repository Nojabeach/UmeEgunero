package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CentroDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val cursos: List<Curso> = emptyList(),
    val navigateToWelcome: Boolean = false
)

@HiltViewModel
class CentroDashboardViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CentroDashboardUiState())
    val uiState: StateFlow<CentroDashboardUiState> = _uiState.asStateFlow()
    
    // Propiedad para acceder directamente a los cursos desde la UI
    val cursos = _uiState.asStateFlow().map { it.cursos }
    
    init {
        loadCurrentUser()
        loadCursos()
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
    
    private fun loadCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En producción, se debería filtrar por centro del usuario
                when (val result = cursoRepository.getAllCursos()) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                cursos = result.data,
                                isLoading = false
                            ) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los cursos"
                            )
                        }
                        Timber.e(result.exception, "Error al cargar los cursos")
                    }
                    else -> { /* Ignorar estado loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los cursos")
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
} 