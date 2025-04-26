package com.tfg.umeegunero.feature.common.perfil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            usuarioRepository.getUsuarioActual().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _usuario.value = result.data
                        _uiState.value = EditProfileUiState.Success
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar usuario")
                        _uiState.value = EditProfileUiState.Error(result.exception?.message ?: "Error desconocido")
                    }
                    is Result.Loading -> {
                        _uiState.value = EditProfileUiState.Loading
                    }
                }
            }
        }
    }

    fun updateNombre(nombre: String) {
        _usuario.value = _usuario.value?.copy(nombre = nombre)
    }

    fun updateApellidos(apellidos: String) {
        _usuario.value = _usuario.value?.copy(apellidos = apellidos)
    }

    fun updateEmail(email: String) {
        _usuario.value = _usuario.value?.copy(email = email)
    }

    fun updateTelefono(telefono: String) {
        _usuario.value = _usuario.value?.copy(telefono = telefono)
    }

    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            _usuario.value?.let { usuario ->
                when (val result = usuarioRepository.actualizarUsuario(usuario)) {
                    is Result.Success -> {
                        _uiState.value = EditProfileUiState.Success
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al guardar cambios")
                        _uiState.value = EditProfileUiState.Error(result.exception?.message ?: "Error al guardar cambios")
                    }
                    else -> {}
                }
            }
        }
    }
}

sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
} 