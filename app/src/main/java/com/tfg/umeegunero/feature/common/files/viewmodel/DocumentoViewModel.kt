package com.tfg.umeegunero.feature.common.files.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.InfoArchivo
import com.tfg.umeegunero.data.repository.StorageRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

/**
 * ViewModel para la gestión de documentos y archivos.
 * Proporciona funcionalidades para descargar, visualizar y gestionar archivos desde Storage.
 *
 * @property storageRepository Repositorio para gestionar operaciones con Firebase Storage
 */
@HiltViewModel
class DocumentoViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtener la ruta del documento de los argumentos
    private val rutaDocumento: String = checkNotNull(savedStateHandle["rutaDocumento"])
    private val nombreDocumento: String? = savedStateHandle["nombreDocumento"]

    // Estado observable para la información del archivo
    private val _infoArchivo = MutableStateFlow<Result<InfoArchivo>>(Result.Loading())
    val infoArchivo: StateFlow<Result<InfoArchivo>> = _infoArchivo.asStateFlow()

    // Estado observable para la URL de descarga
    private val _urlDescarga = MutableStateFlow<Result<String>>(Result.Loading())
    val urlDescarga: StateFlow<Result<String>> = _urlDescarga.asStateFlow()

    // Estado observable para el contenido descargado del archivo (para visualización directa)
    private val _contenidoArchivo = MutableStateFlow<Result<ByteArray>>(Result.Loading())
    val contenidoArchivo: StateFlow<Result<ByteArray>> = _contenidoArchivo.asStateFlow()

    // Estado observable para saber si se está descargando el archivo
    private val _descargando = MutableStateFlow(false)
    val descargando: StateFlow<Boolean> = _descargando.asStateFlow()

    // Estado para controlar errores generales
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado de la UI para la pantalla de documentos
    data class DocumentoUiState(
        val urlDescarga: String? = null,
        val infoArchivo: InfoArchivo? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val url: String = "",
        val nombre: String? = null,
        val tipoMime: String? = null,
        val isDescargando: Boolean = false,
        val archivoLocal: File? = null,
        val infoAdicional: Map<String, String> = emptyMap()
    )

    private val _uiState = MutableStateFlow(DocumentoUiState(isLoading = true))
    val uiState: StateFlow<DocumentoUiState> = _uiState.asStateFlow()

    // Inicializar
    fun inicializar(url: String, nombre: String?) {
        obtenerInformacionDocumento(url)
        obtenerUrlDescarga(url)
    }

    /**
     * Limpia el error actual
     */
    fun limpiarError() {
        _error.value = null
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Obtiene información metadata del documento desde Firebase Storage
     */
    private fun obtenerInformacionDocumento(url: String = rutaDocumento) {
        viewModelScope.launch {
            try {
                _infoArchivo.value = Result.Loading()
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                storageRepository.obtenerInfoArchivo(url).collect { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            val info = result.data as InfoArchivo
                            _infoArchivo.value = Result.Success(info)
                            _uiState.value = _uiState.value.copy(
                                infoArchivo = info,
                                isLoading = false
                            )
                        }
                        is Result.Error -> {
                            _infoArchivo.value = result
                            _error.value = result.exception?.message ?: "Error al obtener información del archivo"
                            _uiState.value = _uiState.value.copy(
                                error = result.exception?.message ?: "Error al obtener información del archivo",
                                isLoading = false
                            )
                        }
                        is Result.Loading<*> -> {
                            // Estado de carga ya actualizado
                        }
                    }
                }
            } catch (e: Exception) {
                _infoArchivo.value = Result.Error(e)
                _error.value = e.message ?: "Error al obtener información del archivo"
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al obtener información del archivo",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Obtiene la URL de descarga del documento desde Firebase Storage
     */
    private fun obtenerUrlDescarga(url: String = rutaDocumento) {
        viewModelScope.launch {
            try {
                _urlDescarga.value = Result.Loading()
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val urlDescarga = storageRepository.obtenerUrlDescarga(url)
                _urlDescarga.value = Result.Success(urlDescarga)
                _uiState.value = _uiState.value.copy(
                    urlDescarga = urlDescarga,
                    isLoading = false
                )
            } catch (e: Exception) {
                _urlDescarga.value = Result.Error(e)
                _error.value = e.message ?: "Error al obtener URL de descarga"
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al obtener URL de descarga",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Descarga el contenido del archivo para su visualización directa
     */
    fun descargarArchivo(nombreArchivo: String = nombreDocumento ?: "archivo") {
        viewModelScope.launch {
            try {
                _descargando.value = true
                _contenidoArchivo.value = Result.Loading()
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                storageRepository.descargarArchivo(rutaDocumento, nombreArchivo).collect { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            // Convertir el archivo a bytes
                            val file = result.data as File
                            val bytes = file.readBytes()
                            _contenidoArchivo.value = Result.Success(bytes)
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is Result.Error -> {
                            _contenidoArchivo.value = Result.Error(result.exception!!)
                            _error.value = result.exception?.message ?: "Error al descargar el archivo"
                            _uiState.value = _uiState.value.copy(
                                error = result.exception?.message ?: "Error al descargar el archivo",
                                isLoading = false
                            )
                        }
                        is Result.Loading<*> -> {
                            // Estado de carga ya actualizado
                        }
                    }
                }
            } catch (e: Exception) {
                _contenidoArchivo.value = Result.Error(e)
                _error.value = e.message ?: "Error al descargar el archivo"
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al descargar el archivo",
                    isLoading = false
                )
            } finally {
                _descargando.value = false
            }
        }
    }
} 