package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.centro.util.CentroAdminUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de gestión de profesores
 */
data class GestionProfesoresUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profesores: List<Usuario> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val clasesAsignadas: List<Clase> = emptyList(),
    val selectedProfesor: Usuario? = null,
    val showAddProfesorDialog: Boolean = false,
    val showEditProfesorDialog: Boolean = false,
    val showAsignarClasesDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showSuccessMessage: String? = null
)

/**
 * ViewModel para la gestión de profesores del centro
 */
@HiltViewModel
class GestionProfesoresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionProfesoresUiState())
    val uiState: StateFlow<GestionProfesoresUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga los datos iniciales (profesores y clases)
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Obtener el ID del centro actual
            val centroId = CentroAdminUtils.getCentroIdActual()
            
            // Cargar profesores
            val resultProfesores = usuarioRepository.getUsersByType(TipoUsuario.PROFESOR)

            when (resultProfesores) {
                is Result.Success -> {
                    _uiState.update { it.copy(profesores = resultProfesores.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "Error al cargar profesores: ${resultProfesores.exception.message}") }
                    Timber.e(resultProfesores.exception, "Error al cargar profesores")
                }
                is Result.Loading -> {
                    // No hacemos nada, ya estamos en estado de carga
                }
            }
            
            // Cargar clases
            val resultClases = claseRepository.getClasesByCentro(centroId)

            when (resultClases) {
                is Result.Success -> {
                    _uiState.update { it.copy(clases = resultClases.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "Error al cargar clases: ${resultClases.exception.message}") }
                    Timber.e(resultClases.exception, "Error al cargar clases")
                }
                is Result.Loading -> {
                    // No hacemos nada, ya estamos en estado de carga
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Selecciona un profesor para ver sus detalles o editarlo
     */
    fun seleccionarProfesor(profesor: Usuario) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedProfesor = profesor, isLoading = true) }
            
            // Cargar las clases asignadas a este profesor
            val resultClases = claseRepository.getClasesByProfesor(profesor.dni)
            
            when (resultClases) {
                is Result.Success -> {
                    _uiState.update { it.copy(clasesAsignadas = resultClases.data, isLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases asignadas: ${resultClases.exception.message}",
                            isLoading = false
                        )
                    }
                    Timber.e(resultClases.exception, "Error al cargar clases asignadas")
                }
                is Result.Loading -> {
                    // No hacemos nada, ya estamos en estado de carga
                }
            }
        }
    }

    /**
     * Asigna un conjunto de clases al profesor seleccionado
     */
    fun asignarClases(profesorId: String, clasesIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // En una implementación real, esto llamaría a un repositorio
                // Por ahora, simulamos la asignación
                Timber.d("Asignando profesor $profesorId a clases: $clasesIds")
                
                // Simular éxito después de 1 segundo
                delay(1000)
                
                _uiState.update { 
                    it.copy(
                        showSuccessMessage = "Clases asignadas correctamente",
                        isLoading = false,
                        showAsignarClasesDialog = false
                    )
                }
                
                // Recargar datos
                cargarDatos()
                
                // Recargar clases del profesor
                val profesor = _uiState.value.selectedProfesor
                if (profesor != null) {
                    seleccionarProfesor(profesor)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al asignar clases: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error al asignar clases")
            }
        }
    }

    /**
     * Crea un nuevo profesor
     */
    fun crearProfesor(dni: String, nombre: String, apellidos: String, email: String, telefono: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // En una implementación real, esto llamaría a métodos del repositorio
                // Por ahora, simulamos la creación
                Timber.d("Creando profesor: $nombre $apellidos, DNI: $dni")
                
                // Simular éxito después de 1 segundo
                delay(1000)
                
                // Crear un profesor ficticio para añadir a la lista
                val nuevoProfesor = Usuario(
                    dni = dni,
                    nombre = nombre,
                    apellidos = apellidos,
                    email = email,
                    telefono = telefono
                )
                
                // Actualizar la lista de profesores
                val nuevaLista = _uiState.value.profesores.toMutableList()
                nuevaLista.add(nuevoProfesor)
                
                _uiState.update { 
                    it.copy(
                        profesores = nuevaLista,
                        showSuccessMessage = "Profesor creado correctamente",
                        isLoading = false,
                        showAddProfesorDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al crear profesor: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error al crear profesor")
            }
        }
    }

    /**
     * Actualiza un profesor existente
     */
    fun actualizarProfesor(dni: String, nombre: String, apellidos: String, email: String, telefono: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // En una implementación real, esto llamaría a métodos del repositorio
                // Por ahora, simulamos la actualización
                Timber.d("Actualizando profesor con DNI: $dni")
                
                // Simular éxito después de 1 segundo
                delay(1000)
                
                // Encontrar y actualizar el profesor en la lista
                val profesorActualizado = _uiState.value.profesores.find { it.dni == dni }?.copy(
                    nombre = nombre,
                    apellidos = apellidos,
                    email = email,
                    telefono = telefono
                )
                
                if (profesorActualizado != null) {
                    val nuevaLista = _uiState.value.profesores.toMutableList()
                    val index = nuevaLista.indexOfFirst { it.dni == dni }
                    if (index >= 0) {
                        nuevaLista[index] = profesorActualizado
                    }
                    
                    _uiState.update { 
                        it.copy(
                            profesores = nuevaLista,
                            selectedProfesor = profesorActualizado,
                            showSuccessMessage = "Profesor actualizado correctamente",
                            isLoading = false,
                            showEditProfesorDialog = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "No se encontró el profesor a actualizar",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al actualizar profesor: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error al actualizar profesor")
            }
        }
    }

    /**
     * Elimina un profesor
     */
    fun eliminarProfesor(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // En una implementación real, esto llamaría a métodos del repositorio
                // Por ahora, simulamos la eliminación
                Timber.d("Eliminando profesor con DNI: $dni")
                
                // Simular éxito después de 1 segundo
                delay(1000)
                
                // Eliminar el profesor de la lista
                val nuevaLista = _uiState.value.profesores.toMutableList()
                nuevaLista.removeIf { it.dni == dni }
                
                _uiState.update { 
                    it.copy(
                        profesores = nuevaLista,
                        selectedProfesor = null,
                        clasesAsignadas = emptyList(),
                        showSuccessMessage = "Profesor eliminado correctamente",
                        isLoading = false,
                        showDeleteConfirmDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar profesor: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error al eliminar profesor")
            }
        }
    }

    // Funciones para gestionar los diálogos
    fun mostrarDialogoAddProfesor() {
        _uiState.update { it.copy(showAddProfesorDialog = true) }
    }

    fun ocultarDialogoAddProfesor() {
        _uiState.update { it.copy(showAddProfesorDialog = false) }
    }

    fun mostrarDialogoEditProfesor() {
        _uiState.update { it.copy(showEditProfesorDialog = true) }
    }

    fun ocultarDialogoEditProfesor() {
        _uiState.update { it.copy(showEditProfesorDialog = false) }
    }

    fun mostrarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = true) }
    }

    fun ocultarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = false) }
    }

    fun mostrarDialogoDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun ocultarDialogoDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun limpiarMensajeExito() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 