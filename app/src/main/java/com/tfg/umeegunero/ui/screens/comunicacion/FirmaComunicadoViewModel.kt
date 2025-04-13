package com.tfg.umeegunero.ui.screens.comunicacion

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.util.FirmaDigitalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la pantalla de firma de comunicados
 */
@HiltViewModel
class FirmaComunicadoViewModel @Inject constructor(
    private val comunicadoRepository: ComunicadoRepository
) : ViewModel() {

    // Estado de la firma
    private val _estadoFirma = MutableStateFlow(EstadoFirma.Inicial)
    val estadoFirma: StateFlow<EstadoFirma> = _estadoFirma.asStateFlow()
    
    // Comunicado a firmar
    private val _comunicado = MutableStateFlow<Comunicado?>(null)
    val comunicado: StateFlow<Comunicado?> = _comunicado.asStateFlow()
    
    // Mensaje de error
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    /**
     * Carga un comunicado por su ID
     */
    fun cargarComunicado(comunicadoId: String) {
        viewModelScope.launch {
            try {
                _estadoFirma.value = EstadoFirma.Inicial
                _mensajeError.value = null
                
                val resultado = comunicadoRepository.getComunicadoById(comunicadoId)
                when (resultado) {
                    is Resultado.Exito -> {
                        _comunicado.value = resultado.datos
                    }
                    is Resultado.Error -> {
                        _mensajeError.value = "Error al cargar el comunicado: ${resultado.mensaje}"
                        _estadoFirma.value = EstadoFirma.Error
                    }
                    is Resultado.Cargando -> {
                        // Estado de carga, podría manejarse si es necesario
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el comunicado")
                _mensajeError.value = "Error inesperado al cargar el comunicado: ${e.message}"
                _estadoFirma.value = EstadoFirma.Error
            }
        }
    }
    
    /**
     * Firma un comunicado con una firma digital
     */
    fun firmarComunicado(comunicadoId: String, firmaBitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _estadoFirma.value = EstadoFirma.Firmando
                
                // Obtener datos del usuario actual (simulado en este caso)
                val usuarioId = "usuario_actual" // En una implementación real, obtener del sistema de autenticación
                val timestamp = System.currentTimeMillis()

                // Convertir firma a Base64
                val firmaBase64 = FirmaDigitalUtil.bitmapABase64(firmaBitmap)

                // Guardar firma en Storage
                val firmaUrl = FirmaDigitalUtil.guardarFirmaEnStorage(
                    bitmap = firmaBitmap,
                    usuarioId = usuarioId,
                    documentoId = comunicadoId
                ) ?: throw Exception("No se pudo guardar la firma en Storage")

                // Generar hash de la firma
                val firmaHash = FirmaDigitalUtil.generarHashFirma(
                    base64 = firmaBase64,
                    usuarioId = usuarioId,
                    documentoId = comunicadoId,
                    timestamp = timestamp
                )

                // Usar el método simplificado para añadir firma digital
                val resultado = comunicadoRepository.añadirFirmaDigital(
                    comunicadoId = comunicadoId,
                    firmaBase64 = firmaBase64
                )

                when (resultado) {
                    is Resultado.Exito -> {
                        _estadoFirma.value = EstadoFirma.Exito
                    }
                    is Resultado.Error -> {
                        _mensajeError.value = "Error al guardar la firma: ${resultado.mensaje}"
                        _estadoFirma.value = EstadoFirma.Error
                    }
                    is Resultado.Cargando -> {
                        // Estado de carga, podría manejarse si es necesario
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al firmar el comunicado")
                _mensajeError.value = "Error inesperado al firmar el comunicado: ${e.message}"
                _estadoFirma.value = EstadoFirma.Error
            }
        }
    }

    /**
     * Reinicia el estado de la firma
     */
    fun resetEstado() {
        _estadoFirma.value = EstadoFirma.Inicial
        _mensajeError.value = null
    }
    
    /**
     * Estados posibles durante el proceso de firma
     */
    enum class EstadoFirma {
        Inicial,
        Firmando,
        Exito,
        Error
    }
} 