package com.tfg.umeegunero.ui.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.domain.model.Clase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de clases
 * Maneja la lógica de negocio relacionada con las clases
 */
class GestionClasesViewModel : ViewModel() {
    private val _clases = MutableStateFlow<List<Clase>>(emptyList())
    val clases: StateFlow<List<Clase>> = _clases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Carga las clases del curso especificado
     * @param cursoId ID del curso del que se quieren cargar las clases
     */
    fun cargarClases(cursoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para cargar las clases
                // _clases.value = claseRepository.getClasesByCursoId(cursoId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Añade una nueva clase
     * @param clase Clase a añadir
     */
    fun añadirClase(clase: Clase) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para añadir la clase
                // claseRepository.addClase(clase)
                // Recargar la lista de clases
                cargarClases(clase.cursoId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza una clase existente
     * @param clase Clase a actualizar
     */
    fun actualizarClase(clase: Clase) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para actualizar la clase
                // claseRepository.updateClase(clase)
                // Recargar la lista de clases
                cargarClases(clase.cursoId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una clase
     * @param claseId ID de la clase a eliminar
     * @param cursoId ID del curso al que pertenece la clase
     */
    fun eliminarClase(claseId: String, cursoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para eliminar la clase
                // claseRepository.deleteClase(claseId)
                // Recargar la lista de clases
                cargarClases(cursoId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Asigna un profesor a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a asignar
     * @param cursoId ID del curso al que pertenece la clase
     */
    fun asignarProfesor(claseId: String, profesorId: String, cursoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para asignar el profesor
                // claseRepository.asignarProfesor(claseId, profesorId)
                // Recargar la lista de clases
                cargarClases(cursoId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 