package com.tfg.umeegunero.ui.components.firma

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.util.FirmaDigitalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Componente para mostrar una firma digital
 * 
 * @param firmaBase64 Firma en formato Base64
 * @param firmaUrl URL de la firma en Firebase Storage
 * @param firmaHash Hash de verificación de la firma
 * @param usuarioId ID del usuario que firmó
 * @param documentoId ID del documento firmado
 * @param timestamp Timestamp de la firma
 * @param mostrarVerificacion Si se debe mostrar el estado de verificación
 * @param modifier Modificador para personalizar el aspecto
 */
@Composable
fun FirmaDigitalView(
    firmaBase64: String?,
    firmaUrl: String?,
    firmaHash: String?,
    usuarioId: String,
    documentoId: String,
    timestamp: Long?,
    mostrarVerificacion: Boolean = true,
    modifier: Modifier = Modifier
) {
    var bitmapFirma by remember { mutableStateOf<Bitmap?>(null) }
    var esAutentica by remember { mutableStateOf(false) }
    var verificacionRealizada by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Cargar la firma desde Base64 o URL
    LaunchedEffect(firmaBase64, firmaUrl) {
        try {
            bitmapFirma = when {
                !firmaBase64.isNullOrEmpty() -> {
                    FirmaDigitalUtil.base64ABitmap(firmaBase64)
                }
                !firmaUrl.isNullOrEmpty() -> {
                    // Aquí se podría implementar la carga desde URL
                    // Por ahora usamos el Base64 como fallback
                    if (!firmaBase64.isNullOrEmpty()) {
                        FirmaDigitalUtil.base64ABitmap(firmaBase64)
                    } else {
                        null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            error = "Error al cargar la firma: ${e.message}"
        }
    }
    
    // Verificar la autenticidad de la firma
    LaunchedEffect(firmaHash, firmaBase64, timestamp) {
        if (mostrarVerificacion && !firmaHash.isNullOrEmpty() && 
            !firmaBase64.isNullOrEmpty() && timestamp != null) {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    FirmaDigitalUtil.verificarFirma(
                        firmaHash = firmaHash,
                        base64 = firmaBase64,
                        usuarioId = usuarioId,
                        documentoId = documentoId,
                        timestamp = timestamp
                    )
                }
                esAutentica = resultado
                verificacionRealizada = true
            } catch (e: Exception) {
                error = "Error al verificar la firma: ${e.message}"
                verificacionRealizada = true
                esAutentica = false
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Mostrar la firma
        if (bitmapFirma != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                bitmapFirma?.asImageBitmap()?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Firma digital",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "No hay firma disponible",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mostrar estado de verificación
        if (mostrarVerificacion && verificacionRealizada) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val verificacionColor = if (esAutentica) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
                
                val verificacionIcon = if (esAutentica) 
                    Icons.Default.CheckCircle 
                else 
                    Icons.Default.Error
                
                Icon(
                    imageVector = verificacionIcon,
                    contentDescription = if (esAutentica) "Firma auténtica" else "Firma no verificada",
                    tint = verificacionColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (esAutentica) "Firma verificada" else "Firma no verificada",
                    color = verificacionColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Componente para mostrar múltiples firmas de destinatarios
 * 
 * @param firmas Map con los IDs de usuario y URLs de sus firmas
 * @param modifier Modificador para personalizar el aspecto
 */
@Composable
fun FirmasDestinatariosView(
    firmas: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Firmas de destinatarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (firmas.isEmpty()) {
            Text(
                text = "No hay firmas de destinatarios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            firmas.forEach { (usuarioId, firmaUrl) ->
                // Aquí se podría mostrar información adicional del usuario
                // Por ahora solo mostramos el ID
                Text(
                    text = "Usuario: $usuarioId",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                // Mostrar la firma
                FirmaDigitalView(
                    firmaBase64 = null,
                    firmaUrl = firmaUrl,
                    firmaHash = null,
                    usuarioId = usuarioId,
                    documentoId = "",
                    timestamp = null,
                    mostrarVerificacion = false,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
        }
    }
} 