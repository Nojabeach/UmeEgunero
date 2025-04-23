package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de añadir/editar clase
 */
data class AddClaseUiState(
    val id: String = "",
    val cursoId: String = "",
    val centroId: String = "",
    val nombre: String = "",
    val aula: String = "",
    val horario: String = "",
    val capacidadMaxima: String = "",
    val profesorTitularId: String = "",
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val alumnosIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingProfesores: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nombreError: String? = null,
    val aulaError: String? = null,
    val horarioError: String? = null,
    val capacidadMaximaError: String? = null,
    val profesorTitularError: String? = null,
    val profesoresDisponibles: List<Usuario> = emptyList(),
    val isEditMode: Boolean = false
)

/**
 * ViewModel para la pantalla de añadir/editar clase
 * Maneja la lógica de validación y guardado de clases
 */
@HiltViewModel
class AddClaseViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddClaseUiState())
    val uiState: StateFlow<AddClaseUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el cursoId de los argumentos de navegación
        val cursoId = savedStateHandle.get<String>("cursoId")
        if (!cursoId.isNullOrEmpty()) {
            _uiState.update { it.copy(cursoId = cursoId) }
            obtenerCentroIdDelCurso(cursoId)
        }
        
        // Verificar si estamos en modo edición
        val claseId = savedStateHandle.get<String>("claseId")
        if (!claseId.isNullOrEmpty()) {
            cargarClase(claseId)
        } else {
            // Si no estamos en modo edición, establecemos un valor predeterminado para capacidadMaxima y cargamos los profesores
            _uiState.update { it.copy(capacidadMaxima = "25") }
            if (!cursoId.isNullOrEmpty()) {
                cargarProfesoresDisponibles()
            }
        }
    }
    
    /**
     * Obtiene el centroId del curso para la clase
     */
    private fun obtenerCentroIdDelCurso(cursoId: String) {
        Timber.d("Obteniendo centroId para el curso: $cursoId")
        viewModelScope.launch {
            when (val resultado = cursoRepository.getCursoById(cursoId)) {
                is Result.Success -> {
                    val centroId = resultado.data.centroId
                    Timber.d("Centro encontrado para el curso $cursoId: centroId=$centroId")
                    _uiState.update { it.copy(centroId = centroId) }
                    cargarProfesoresDisponibles()
                }
                is Result.Error -> {
                    Timber.e(resultado.exception, "Error al obtener el centro del curso $cursoId")
                    _uiState.update {
                        it.copy(error = "Error al obtener información del curso: ${resultado.exception?.message}")
                    }
                }
                is Result.Loading -> {
                    // No es necesario hacer nada aquí, ya se actualizó el estado a isLoading = true
                }
            }
        }
    }
    
    /**
     * Carga los profesores disponibles para seleccionar
     */
    private fun cargarProfesoresDisponibles() {
        val centroId = _uiState.value.centroId
        Timber.d("Iniciando carga de profesores para el centro: $centroId")
        _uiState.update { it.copy(isLoadingProfesores = true) }
        
        viewModelScope.launch {
            try {
                if (centroId.isEmpty()) {
                    Timber.e("Error: No se puede cargar profesores sin centroId")
                    _uiState.update {
                        it.copy(
                            isLoadingProfesores = false,
                            error = "No se pudo determinar el centro educativo"
                        )
                    }
                    return@launch
                }
                
                // Obtener los profesores del centro
                Timber.d("Consultando profesores del centro $centroId...")
                when (val resultado = usuarioRepository.getProfesoresByCentro(centroId)) {
                    is Result.Success -> {
                        val profesores = resultado.data
                        Timber.d("Profesores obtenidos del centro $centroId: ${profesores.size}")
                        
                        // Mostrar detalles de cada profesor encontrado
                        profesores.forEachIndexed { index, profesor ->
                            Timber.d("Profesor #${index+1}: ${profesor.nombre} ${profesor.apellidos} (DNI: ${profesor.dni})")
                            Timber.d("   - Email: ${profesor.email}")
                            Timber.d("   - Perfiles: ${profesor.perfiles.size}")
                            profesor.perfiles.forEach { perfil -> 
                                Timber.d("   - Perfil: tipo=${perfil.tipo}, centroId=${perfil.centroId}")
                            }
                        }
                        
                        _uiState.update {
                            it.copy(
                                profesoresDisponibles = profesores,
                                isLoadingProfesores = false
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al cargar profesores para el centro $centroId")
                        _uiState.update {
                            it.copy(
                                isLoadingProfesores = false,
                                error = "Error al cargar profesores: ${resultado.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Estado de carga ya manejado
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al cargar profesores para el centro $centroId")
                _uiState.update {
                    it.copy(
                        isLoadingProfesores = false,
                        error = "Error al cargar profesores: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga los datos de una clase existente para edición
     */
    private fun cargarClase(claseId: String) {
        _uiState.update { it.copy(isLoading = true, isEditMode = true) }
        
        viewModelScope.launch {
            when (val result = claseRepository.getClaseById(claseId)) {
                is Result.Success -> {
                    val clase = result.data
                    // Convertimos la capacidad máxima a string de forma segura
                    val capacidadMaximaStr = clase.capacidadMaxima.takeIf { it > 0 }?.toString() ?: ""
                    
                    _uiState.update {
                        it.copy(
                            id = clase.id,
                            cursoId = clase.cursoId,
                            centroId = clase.centroId,
                            nombre = clase.nombre,
                            aula = clase.aula,
                            horario = clase.horario,
                            capacidadMaxima = capacidadMaximaStr,
                            profesorTitularId = clase.profesorTitularId,
                            profesoresAuxiliaresIds = clase.profesoresAuxiliaresIds,
                            alumnosIds = clase.alumnosIds,
                            activo = clase.activo,
                            isLoading = false
                        )
                    }
                    cargarProfesoresDisponibles()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clase")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar datos de la clase: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya manejado
                }
            }
        }
    }
    
    /**
     * Actualiza el nombre de la clase
     */
    fun updateNombre(nombre: String) {
        _uiState.update { 
            it.copy(
                nombre = nombre,
                nombreError = null
            )
        }
    }
    
    /**
     * Actualiza el aula de la clase
     */
    fun updateAula(aula: String) {
        _uiState.update { 
            it.copy(
                aula = aula,
                aulaError = null
            )
        }
    }
    
    /**
     * Actualiza el horario de la clase
     */
    fun updateHorario(horario: String) {
        _uiState.update { 
            it.copy(
                horario = horario,
                horarioError = null
            )
        }
    }
    
    /**
     * Actualiza la capacidad máxima de la clase
     */
    fun updateCapacidadMaxima(capacidadMaxima: String) {
        // Eliminamos cualquier espacio en blanco y validamos el contenido
        val sanitizedValue = capacidadMaxima.trim()
        
        // Si está en blanco, solo actualizamos el valor sin error
        if (sanitizedValue.isEmpty()) {
            _uiState.update { 
                it.copy(
                    capacidadMaxima = sanitizedValue,
                    capacidadMaximaError = "La capacidad máxima es obligatoria"
                )
            }
            return
        }
        
        // Verificamos que no contenga caracteres no numéricos
        if (!sanitizedValue.all { it.isDigit() }) {
            _uiState.update { 
                it.copy(
                    capacidadMaxima = sanitizedValue,
                    capacidadMaximaError = "Introduce un número válido"
                )
            }
            return
        }
        
        // Verificamos que sea un número válido
        val capacidadInt = sanitizedValue.toIntOrNull()
        if (capacidadInt == null) {
            _uiState.update { 
                it.copy(
                    capacidadMaxima = sanitizedValue,
                    capacidadMaximaError = "Introduce un número válido"
                )
            }
            return
        }
        
        // Verificamos que sea mayor que cero
        if (capacidadInt <= 0) {
            _uiState.update { 
                it.copy(
                    capacidadMaxima = sanitizedValue,
                    capacidadMaximaError = "La capacidad debe ser mayor que 0"
                )
            }
            return
        }
        
        // Si todo está correcto, actualizamos el valor sin error
        _uiState.update { 
            it.copy(
                capacidadMaxima = sanitizedValue,
                capacidadMaximaError = null
            )
        }
    }
    
    /**
     * Actualiza el profesor titular de la clase (opcional)
     * El profesor se puede asignar posteriormente en la pantalla de vinculación de profesores
     */
    fun updateProfesorTitular(profesorId: String) {
        _uiState.update { 
            it.copy(
                profesorTitularId = profesorId,
                profesorTitularError = null
            )
        }
    }
    
    /**
     * Actualiza el estado activo de la clase
     */
    fun updateActivo(activo: Boolean) {
        _uiState.update { it.copy(activo = activo) }
    }
    
    /**
     * Guarda la clase en la base de datos
     */
    fun guardarClase() {
        if (!validarFormulario()) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val state = _uiState.value
                
                // Convertir la capacidad máxima a entero
                val capacidadMaxima = state.capacidadMaxima.toIntOrNull() ?: 25
                
                // Crear objeto clase
                val clase = Clase(
                    id = if (state.isEditMode) state.id else UUID.randomUUID().toString(),
                    cursoId = state.cursoId,
                    centroId = state.centroId,
                    nombre = state.nombre,
                    profesorTitularId = state.profesorTitularId,
                    profesoresAuxiliaresIds = state.profesoresAuxiliaresIds,
                    alumnosIds = state.alumnosIds,
                    capacidadMaxima = capacidadMaxima,
                    activo = state.activo,
                    horario = state.horario,
                    aula = state.aula
                )
                
                // Guardar la clase
                when (val resultado = if (state.isEditMode) {
                    claseRepository.guardarClase(clase)
                } else {
                    claseRepository.guardarClase(clase)
                }) {
                    is Result.Success -> {
                        Timber.d("Clase guardada con ID: ${resultado.data}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al guardar clase")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al guardar clase: ${resultado.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Estado de carga ya manejado
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al procesar datos de la clase")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al procesar datos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Valida todos los campos del formulario antes de guardar
     * @return true si todos los campos son válidos
     */
    private fun validarFormulario(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        // Validar nombre
        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        }
        
        // Validar aula
        if (state.aula.isBlank()) {
            _uiState.update { it.copy(aulaError = "El aula es obligatoria") }
            isValid = false
        }
        
        // Validar capacidad máxima
        val capacidadMaximaStr = state.capacidadMaxima.trim()
        if (capacidadMaximaStr.isEmpty()) {
            _uiState.update { it.copy(capacidadMaximaError = "La capacidad máxima es obligatoria") }
            isValid = false
        } else {
            val capacidadMaxima = capacidadMaximaStr.toIntOrNull()
            if (capacidadMaxima == null) {
                _uiState.update { it.copy(capacidadMaximaError = "Introduce un número válido") }
                isValid = false
            } else if (capacidadMaxima <= 0) {
                _uiState.update { it.copy(capacidadMaximaError = "La capacidad debe ser mayor que 0") }
                isValid = false
            }
        }
        
        // Ya no validamos el profesor titular, ahora es opcional
        
        return isValid
    }
    
    /**
     * Limpia el error general del estado
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 