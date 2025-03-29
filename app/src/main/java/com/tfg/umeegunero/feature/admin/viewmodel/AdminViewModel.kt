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
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                // Crear nuevo documento con ID autogenerado
                val docRef = db.collection("centros").document()
                
                // Asignar ID generado al modelo antes de guardarlo
                val centroConId = centro.copy(id = docRef.id)
                
                // Guardar centro en Firestore
                docRef.set(centroConId).await()
                
                // Éxito, notificar al caller
                onComplete(true)
                
                // Actualizar la lista de centros si es necesario
                getCentros()
            } catch (e: Exception) {
                Timber.e(e, "Error al agregar centro")
                _error.value = "Error al agregar centro: ${e.message}"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getCentros() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val snapshot = db.collection("centros").get().await()
                val centrosList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Centro::class.java)
                }
                _centros.value = centrosList
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener centros")
                _error.value = "Error al obtener centros: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Estado de la UI para el administrador.
 */
data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) 