package com.tfg.umeegunero.feature.common.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Estado UI para la pantalla de visualización de documentos
 */
data class DocumentoUiState(
    val url: String = "",
    val nombre: String? = null,
    val tipoMime: String? = null,
    val archivoLocal: File? = null,
    val isDescargando: Boolean = false,
    val error: String? = null,
    val infoAdicional: Map<String, String> = emptyMap()
)

/**
 * ViewModel para la pantalla de visualización de documentos
 */
@HiltViewModel
class DocumentoViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DocumentoUiState())
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()
    
    /**
     * Inicializa el ViewModel con la URL y nombre del documento
     */
    fun inicializar(url: String, nombre: String?) {
        _uiState.update { 
            it.copy(
                url = url,
                nombre = nombre ?: obtenerNombreDesdeUrl(url),
                isDescargando = false,
                error = null
            ) 
        }
        
        // Intenta obtener información adicional del archivo
        obtenerInformacionArchivo()
    }
    
    /**
     * Obtiene información adicional del archivo
     */
    private fun obtenerInformacionArchivo() {
        viewModelScope.launch {
            try {
                val url = _uiState.value.url
                if (url.isEmpty()) return@launch
                
                storageRepository.obtenerInfoArchivo(url).collectLatest { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            // No actualizamos nada durante la carga
                        }
                        is Resultado.Exito -> {
                            val infoArchivo = resultado.datos
                            _uiState.update { 
                                it.copy(
                                    tipoMime = infoArchivo.tipo,
                                    infoAdicional = infoArchivo.metadatos + mapOf(
                                        "Tamaño" to formatearTamaño(infoArchivo.tamaño),
                                        "Fecha de creación" to formatearFecha(infoArchivo.fechaCreacion)
                                    )
                                ) 
                            }
                        }
                        is Resultado.Error -> {
                            Timber.e("Error al obtener información del archivo: ${resultado.mensaje}")
                            // No mostramos error al usuario, simplemente continuamos sin la información adicional
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener información del archivo")
            }
        }
    }
    
    /**
     * Descarga el archivo para visualización local
     */
    fun descargarArchivo() {
        viewModelScope.launch {
            try {
                val url = _uiState.value.url
                val nombre = _uiState.value.nombre ?: obtenerNombreDesdeUrl(url)
                
                if (url.isEmpty()) {
                    _uiState.update { it.copy(error = "URL no válida") }
                    return@launch
                }
                
                _uiState.update { it.copy(isDescargando = true) }
                
                storageRepository.descargarArchivo(url, nombre).collectLatest { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            // Ya actualizamos el estado de carga antes
                        }
                        is Resultado.Exito -> {
                            val archivo = resultado.datos
                            _uiState.update { 
                                it.copy(
                                    archivoLocal = archivo,
                                    isDescargando = false
                                ) 
                            }
                        }
                        is Resultado.Error -> {
                            _uiState.update { 
                                it.copy(
                                    error = "Error al descargar archivo: ${resultado.mensaje}",
                                    isDescargando = false
                                ) 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al descargar archivo")
                _uiState.update { 
                    it.copy(
                        error = "Error al descargar archivo: ${e.message}",
                        isDescargando = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Obtiene el nombre del archivo a partir de su URL
     */
    private fun obtenerNombreDesdeUrl(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }
    
    /**
     * Formatea el tamaño del archivo para mostrar
     */
    private fun formatearTamaño(tamañoBytes: Long): String {
        return when {
            tamañoBytes < 1024 -> "$tamañoBytes B"
            tamañoBytes < 1024 * 1024 -> "${tamañoBytes / 1024} KB"
            tamañoBytes < 1024 * 1024 * 1024 -> "${tamañoBytes / (1024 * 1024)} MB"
            else -> "${tamañoBytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * Formatea la fecha para mostrar
     */
    private fun formatearFecha(timestamp: Long): String {
        val fecha = java.util.Date(timestamp)
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return formato.format(fecha)
    }
} 