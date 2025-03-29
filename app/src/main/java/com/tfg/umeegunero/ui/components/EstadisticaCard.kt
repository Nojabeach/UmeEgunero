package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.TrendingUp

/**
 * Componente que muestra una tarjeta de estadística con un valor destacado y su descripción.
 *
 * Este componente se utiliza para presentar métricas importantes en los informes de rendimiento
 * y uso de la aplicación. Cada tarjeta contiene un icono relacionado con la estadística, un valor
 * numérico destacado y una descripción que contextualiza el dato mostrado.
 *
 * @param titulo Título breve que describe la estadística
 * @param valor Valor principal a mostrar (puede ser un número o porcentaje)
 * @param descripcion Texto explicativo sobre el significado de esta estadística
 * @param icono Icono que representa visualmente la categoría de esta estadística
 * @param iconoTint Color personalizado para el icono (por defecto: color primario del tema)
 * @param modifier Modificador opcional para personalizar el diseño del componente
 * @param valorColor Color personalizado para el valor destacado (por defecto: color primario)
 * @param valorSufijo Sufijo opcional a mostrar junto al valor (ej. "%", "pts")
 * @param onClick Función opcional que se ejecuta al hacer clic en la tarjeta
 */
@Composable
fun EstadisticaCard(
    titulo: String,
    valor: String,
    descripcion: String,
    icono: ImageVector,
    iconoTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    valorColor: Color = MaterialTheme.colorScheme.primary,
    valorSufijo: String = "",
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    ElevatedCard(
        modifier = cardModifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de la estadística
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = iconoTint,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Título de la estadística
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Valor destacado
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = valorColor
                )
                
                if (valorSufijo.isNotEmpty()) {
                    Text(
                        text = valorSufijo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = valorColor,
                        modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción de la estadística
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Vista previa del componente EstadisticaCard.
 *
 * Muestra tres ejemplos de tarjetas de estadísticas con diferentes configuraciones visuales.
 */
@Preview(showBackground = true)
@Composable
fun EstadisticaCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ejemplo 1: Estadística de nota media
            EstadisticaCard(
                titulo = "Nota Media",
                valor = "7.8",
                descripcion = "Promedio de calificaciones del último trimestre",
                icono = Icons.Default.School,
                valorSufijo = "pts"
            )
            
            // Ejemplo 2: Estadística de asistencia
            EstadisticaCard(
                titulo = "Tasa de Asistencia",
                valor = "92",
                descripcion = "Porcentaje de asistencia a clase este mes",
                icono = Icons.AutoMirrored.Filled.TrendingUp,
                valorColor = MaterialTheme.colorScheme.tertiary,
                valorSufijo = "%"
            )
            
            // Ejemplo 3: Estadística de uso
            EstadisticaCard(
                titulo = "Usuarios Activos",
                valor = "486",
                descripcion = "Usuarios que han iniciado sesión esta semana",
                icono = Icons.Default.Insights,
                iconoTint = MaterialTheme.colorScheme.secondary,
                valorColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
} 