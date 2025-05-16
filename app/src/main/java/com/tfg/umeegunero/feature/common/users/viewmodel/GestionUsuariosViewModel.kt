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
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GestionUsuariosUiState>(GestionUsuariosUiState.Loading)
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual

    private val _centroIdAdmin = MutableStateFlow<String>("")
    val centroIdAdmin: StateFlow<String> = _centroIdAdmin

    private val _editandoPerfil = MutableStateFlow(false)
    val editandoPerfil: StateFlow<Boolean> = _editandoPerfil
    
    // Nuevo estado para forzar el rol de administrador
    private val _forzarRolAdminApp = MutableStateFlow<Boolean?>(null)

    init {
        cargarUsuarioActual()
    }
    
    /**
     * Establece manualmente si el usuario es administrador de aplicación
     * Se usa para preservar el parámetro isAdminApp pasado desde la navegación
     */
    fun setIsAdminApp(isAdminApp: Boolean) {
        Timber.d("Forzando rol admin app: $isAdminApp")
        _forzarRolAdminApp.value = isAdminApp
    }

    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            usuarioRepository.getUsuarioActual().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val usuario = result.data
                        _usuarioActual.value = usuario
                        _uiState.value = GestionUsuariosUiState.Success
                        
                        // Solo asignar el centro si no estamos forzando el rol de admin app
                        if (_forzarRolAdminApp.value != true && usuario?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_CENTRO } == true) {
                            val centroId = usuario.perfiles.firstOrNull { it.tipo == TipoUsuario.ADMIN_CENTRO }?.centroId ?: ""
                            _centroIdAdmin.value = centroId
                            Timber.d("Usuario Admin Centro cargado. Centro ID: $centroId")
                        } else {
                            _centroIdAdmin.value = ""
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar usuario actual")
                        _uiState.value = GestionUsuariosUiState.Error(result.exception?.message ?: "Error desconocido")
                        _centroIdAdmin.value = ""
                    }
                    is Result.Loading -> {
                        _uiState.value = GestionUsuariosUiState.Loading
                    }
                }
            }
        }
    }

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _uiState.value = GestionUsuariosUiState.Loading
            when (val result = usuarioRepository.actualizarUsuario(usuario)) {
                is Result.Success -> {
                    _usuarioActual.value = usuario
                    _uiState.value = GestionUsuariosUiState.Success
                    _editandoPerfil.value = false
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al actualizar usuario")
                    _uiState.value = GestionUsuariosUiState.Error(result.exception?.message ?: "Error al actualizar usuario")
                }
                else -> {}
            }
        }
    }

    fun toggleEditarPerfil() {
        _editandoPerfil.value = !_editandoPerfil.value
    }

    fun esAdministrador(): Boolean {
        // Usar el valor forzado si está definido, de lo contrario usar los perfiles del usuario
        return _forzarRolAdminApp.value ?: (_usuarioActual.value?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: false)
    }

    fun obtenerNombreCompleto(): String {
        return with(_usuarioActual.value) {
            if (this != null) {
                "$nombre $apellidos"
            } else {
                ""
            }
        }
    }

    fun obtenerEmail(): String {
        return _usuarioActual.value?.email ?: ""
    }

    fun obtenerTelefono(): String {
        return _usuarioActual.value?.telefono ?: ""
    }

    fun obtenerDireccion(): String {
        return _usuarioActual.value?.direccion?.toString() ?: ""
    }
}

sealed class GestionUsuariosUiState {
    object Loading : GestionUsuariosUiState()
    object Success : GestionUsuariosUiState()
    data class Error(val message: String) : GestionUsuariosUiState()
} 