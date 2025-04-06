package com.tfg.umeegunero.ui.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Tipos de vista disponibles para el calendario
 */
enum class TipoVistaCalendario {
    MENSUAL,
    SEMANAL,
    DIARIA
}

/**
 * Componente que permite seleccionar entre los diferentes tipos de vista para el calendario
 * @param vistaActual Tipo de vista actualmente seleccionada
 * @param onCambioVista Callback cuando se cambia el tipo de vista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorVistaCalendario(
    vistaActual: TipoVistaCalendario,
    onCambioVista: (TipoVistaCalendario) -> Unit
) {
    // Configuración de las opciones
    val opciones = listOf(
        OpcionVistaCalendario(
            tipo = TipoVistaCalendario.MENSUAL,
            texto = "Mensual",
            icono = Icons.Default.CalendarMonth
        ),
        OpcionVistaCalendario(
            tipo = TipoVistaCalendario.SEMANAL,
            texto = "Semanal",
            icono = Icons.Default.ViewWeek
        ),
        OpcionVistaCalendario(
            tipo = TipoVistaCalendario.DIARIA,
            texto = "Diaria",
            icono = Icons.Default.CalendarViewDay
        )
    )
    
    // Selector de tipo segmentado
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            opciones.forEach { opcion ->
                val seleccionada = vistaActual == opcion.tipo
                
                // Botón para cada opción
                FilledTonalButton(
                    onClick = { onCambioVista(opcion.tipo) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (seleccionada) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (seleccionada) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = opcion.icono,
                            contentDescription = opcion.texto,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = opcion.texto,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                if (opcion.tipo != TipoVistaCalendario.DIARIA) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

/**
 * Datos de una opción de vista del calendario
 */
private data class OpcionVistaCalendario(
    val tipo: TipoVistaCalendario,
    val texto: String,
    val icono: ImageVector
) 