package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Centro
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la administración de funcionalidades del administrador.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _centros = MutableStateFlow<List<Centro>>(emptyList())
    val centros: StateFlow<List<Centro>> = _centros.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Agrega un nuevo centro a la base de datos.
     * 
     * @param centro Centro a agregar
     * @param onComplete Callback para cuando la operación es exitosa o falla
     */
    fun agregarCentro(centro: Centro, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("centros")
                    .document(centro.id.ifEmpty { db.collection("centros").document().id })
                    .set(centro)
                    .await()
                _isLoading.value = false
                onComplete(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al agregar centro: ${e.message}"
                Timber.e(e, "Error al agregar centro")
                onComplete(false)
            }
        }
    }

    /**
     * Obtiene un centro por su ID.
     * 
     * @param centroId ID del centro a obtener
     * @param onComplete Callback con el centro obtenido o null si no existe
     */
    fun getCentro(centroId: String, onComplete: (Centro?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("centros").document(centroId).get().await()
                if (document.exists()) {
                    val centro = document.toObject(Centro::class.java)
                    _isLoading.value = false
                    onComplete(centro)
                } else {
                    _isLoading.value = false
                    onComplete(null)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al obtener centro: ${e.message}"
                Timber.e(e, "Error al obtener centro")
                onComplete(null)
            }
        }
    }

    /**
     * Actualiza un centro existente en la base de datos.
     * 
     * @param centro Centro a actualizar (debe tener ID válido)
     * @param onComplete Callback para cuando la operación es exitosa o falla
     */
    fun actualizarCentro(centro: Centro, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            if (centro.id.isEmpty()) {
                _error.value = "Error: ID de centro inválido"
                _isLoading.value = false
                onComplete(false)
                return@launch
            }
            
            try {
                db.collection("centros")
                    .document(centro.id)
                    .set(centro)
                    .await()
                _isLoading.value = false
                onComplete(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al actualizar centro: ${e.message}"
                Timber.e(e, "Error al actualizar centro")
                onComplete(false)
            }
        }
    }

    /**
     * Elimina un centro de la base de datos.
     * 
     * @param centroId ID del centro a eliminar
     * @param onComplete Callback para cuando la operación es exitosa o falla
     */
    fun eliminarCentro(centroId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("centros")
                    .document(centroId)
                    .delete()
                    .await()
                _isLoading.value = false
                onComplete(true)
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al eliminar centro: ${e.message}"
                Timber.e(e, "Error al eliminar centro")
                onComplete(false)
            }
        }
    }

    /**
     * Obtiene todos los centros.
     */
    fun getCentros() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("centros").get().await()
                val listaCentros = snapshot.toObjects(Centro::class.java)
                _centros.value = listaCentros
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al obtener centros: ${e.message}"
                Timber.e(e, "Error al obtener centros")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

/**
 * Estado de la UI para el administrador.
 */
data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) 