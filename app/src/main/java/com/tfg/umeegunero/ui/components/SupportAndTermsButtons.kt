package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Componente que muestra los botones de soporte y términos y condiciones.
 * 
 * Este componente sigue las guías de diseño de Material 3 y está optimizado para aplicaciones educativas.
 * Incluye botones para acceder al soporte técnico, FAQ y términos y condiciones, con un diseño moderno
 * y accesible.
 *
 * @param onNavigateToTechnicalSupport Función lambda que se ejecuta al pulsar el botón de soporte técnico
 * @param onNavigateToFAQ Función lambda que se ejecuta al pulsar el botón de FAQ
 * @param onNavigateToTerminosCondiciones Función lambda que se ejecuta al pulsar el botón de términos y condiciones
 * @param modifier Modificador opcional para personalizar el diseño del componente
 */
@Composable
fun SupportAndTermsButtons(
    onNavigateToTechnicalSupport: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToTerminosCondiciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToTechnicalSupport,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .defaultMinSize(minWidth = 0.dp)
                        .padding(horizontal = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Soporte técnico",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onNavigateToFAQ,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FAQ")
                }
            }
            
            Button(
                onClick = onNavigateToTerminosCondiciones,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Términos y condiciones")
            }
        }
    }
}

/**
 * Preview del componente SupportAndTermsButtons en tema claro.
 */
@Preview(showBackground = true)
@Composable
private fun SupportAndTermsButtonsLightPreview() {
    UmeEguneroTheme(darkTheme = false) {
        Surface {
            SupportAndTermsButtons(
                onNavigateToTechnicalSupport = {},
                onNavigateToFAQ = {},
                onNavigateToTerminosCondiciones = {}
            )
        }
    }
}

/**
 * Preview del componente SupportAndTermsButtons en tema oscuro.
 */
@Preview(showBackground = true)
@Composable
private fun SupportAndTermsButtonsDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            SupportAndTermsButtons(
                onNavigateToTechnicalSupport = {},
                onNavigateToFAQ = {},
                onNavigateToTerminosCondiciones = {}
            )
        }
    }
} 