package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListAdministradoresUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val administradores: List<Usuario> = emptyList(),
    val filteredAdministradores: List<Usuario> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class ListAdministradoresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAdministradoresUiState())
    val uiState: StateFlow<ListAdministradoresUiState> = _uiState.asStateFlow()

    init {
        loadAdministradores()
    }

    fun loadAdministradores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = usuarioRepository.getAdministradores()) {
                is Result.Success -> {
                    val administradores = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            administradores = administradores,
                            filteredAdministradores = administradores,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar administradores: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya manejado al inicio
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            val filteredList = if (query.isBlank()) {
                _uiState.value.administradores
            } else {
                _uiState.value.administradores.filter { admin ->
                    admin.nombre.contains(query, ignoreCase = true) ||
                            admin.apellidos.contains(query, ignoreCase = true) ||
                            admin.email.contains(query, ignoreCase = true) ||
                            admin.dni.contains(query, ignoreCase = true)
                }
            }
            
            _uiState.update {
                it.copy(
                    searchQuery = query,
                    filteredAdministradores = filteredList
                )
            }
        }
    }
} 