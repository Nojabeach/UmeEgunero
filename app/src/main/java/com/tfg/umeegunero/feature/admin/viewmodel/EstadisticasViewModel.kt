package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.EstadisticasUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel para la pantalla de estadísticas del administrador
 */
@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    /**
     * Carga las estadísticas desde Firestore
     */
    private fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Obtener estadísticas de Firestore
                val centros = firestore.collection("centros").get().await()
                val usuarios = firestore.collection("usuarios").get().await()
                
                // Contar por roles
                val profesores = usuarios.documents.filter { it.getString("rol") == "PROFESOR" }
                val alumnos = usuarios.documents.filter { it.getString("rol") == "ALUMNO" }
                val familiares = usuarios.documents.filter { it.getString("rol") == "FAMILIAR" }
                
                // Obtener nuevos registros en los últimos 7 días
                val fechaLimite = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                
                val nuevosCentros = centros.documents.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosProfesores = profesores.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosAlumnos = alumnos.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosFamiliares = familiares.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                // Actualizar estado UI
                _uiState.update { it.copy(
                    isLoading = false,
                    totalCentros = centros.size(),
                    totalUsuarios = usuarios.size(),
                    totalProfesores = profesores.size,
                    totalAlumnos = alumnos.size,
                    totalFamiliares = familiares.size,
                    nuevosCentros = nuevosCentros,
                    nuevosProfesores = nuevosProfesores,
                    nuevosAlumnos = nuevosAlumnos,
                    nuevosFamiliares = nuevosFamiliares
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar estadísticas"
                ) }
            }
        }
    }

    /**
     * Genera un informe detallado
     */
    fun generarInforme() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulamos la generación del informe
            delay(2000)
            
            _uiState.update { it.copy(
                isLoading = false,
                informeGenerado = true
            ) }
        }
    }

    /**
     * Descarga el informe generado
     */
    fun descargarInforme() {
        viewModelScope.launch {
            // Aquí se implementaría la descarga real del informe
            // Por ahora solo simulamos un tiempo de espera
            _uiState.update { it.copy(isLoading = true) }
            
            delay(1500)
            
            _uiState.update { it.copy(
                isLoading = false,
                informeGenerado = false
            ) }
        }
    }

    /**
     * Exporta datos del sistema
     */
    fun exportarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulamos la exportación
            delay(2000)
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Recarga las estadísticas
     */
    fun recargarEstadisticas() {
        cargarEstadisticas()
    }
} 