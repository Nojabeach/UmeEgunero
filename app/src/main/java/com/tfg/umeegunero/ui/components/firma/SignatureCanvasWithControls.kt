package com.tfg.umeegunero.ui.components.firma

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.util.FirmaDigitalUtil

/**
 * Componente que encapsula un lienzo de firma con controles para manipularlo
 *
 * Proporciona un canvas para capturar la firma del usuario junto con botones
 * para limpiar y confirmar la firma capturada.
 *
 * @param modifier Modificador para personalizar el aspecto del componente
 * @param strokeWidth Grosor del trazo de la firma
 * @param strokeColor Color del trazo de la firma
 * @param backgroundColor Color de fondo del canvas
 * @param showConfirmButton Indica si se debe mostrar el botón de confirmar firma
 * @param onFirmaCapturada Callback que se ejecuta cuando cambia la firma
 * @param onFirmaConfirmada Callback que se ejecuta cuando se confirma la firma
 */
@Composable
fun SignatureCanvasWithControls(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    strokeColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    showConfirmButton: Boolean = false,
    onFirmaCapturada: (List<FirmaDigitalUtil.PuntoFirma>) -> Unit = {},
    onFirmaConfirmada: (List<FirmaDigitalUtil.PuntoFirma>) -> Unit = {}
) {
    var puntosFirma by remember { mutableStateOf(listOf<FirmaDigitalUtil.PuntoFirma>()) }
    var canvasKey by remember { mutableStateOf(0) }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Canvas de firma con key para forzar redibujado cuando se limpia
        SignatureCanvas(
            modifier = Modifier.weight(1f),
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            backgroundColor = backgroundColor,
            onFirmaChange = {
                puntosFirma = it
                onFirmaCapturada(it)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones de control
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    puntosFirma = emptyList()
                    canvasKey++  // Incrementar key para forzar redibujado
                    onFirmaCapturada(emptyList())
                },
                modifier = Modifier.weight(1f).padding(end = if (showConfirmButton) 8.dp else 0.dp)
            ) {
                Text("Limpiar")
            }
            
            if (showConfirmButton) {
                Button(
                    onClick = { 
                        if (puntosFirma.isNotEmpty()) {
                            onFirmaConfirmada(puntosFirma)
                        }
                    },
                    enabled = puntosFirma.isNotEmpty(),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
} 