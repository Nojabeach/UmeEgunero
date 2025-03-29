package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.model.Centro
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la administración de funcionalidades del administrador.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    /**
     * Agrega un nuevo centro a la base de datos.
     * 
     * @param centro Centro a agregar
     * @param onSuccess Callback para cuando la operación es exitosa
     * @param onError Callback para cuando ocurre un error, con el mensaje del error
     */
    fun agregarCentro(
        centro: Centro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Inicio de la operación
                _uiState.update { it.copy(isLoading = true) }
                
                // Guardar el centro en Firestore
                val centroRef = firestore.collection("centros").document()
                val centroConId = centro.copy(id = centroRef.id)
                
                centroRef.set(centroConId)
                    .addOnSuccessListener {
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = e.message ?: "Error al guardar el centro"
                            ) 
                        }
                        onError(e.message ?: "Error al guardar el centro")
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error inesperado"
                    ) 
                }
                onError(e.message ?: "Error inesperado")
            }
        }
    }
    
    // Otras funciones del ViewModel...
}

/**
 * Estado de la UI para el administrador.
 */
data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) 