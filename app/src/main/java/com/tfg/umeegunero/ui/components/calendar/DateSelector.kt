package com.tfg.umeegunero.ui.components.calendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date
import java.util.Calendar

/**
 * Componente que permite seleccionar una fecha navegando por días
 * 
 * @param selectedDate La fecha actualmente seleccionada
 * @param onDateSelected Callback cuando se selecciona una nueva fecha
 * @param onDismissRequest Callback para cerrar el selector (opcional)
 * @param buttonColors Colores personalizados para el botón central
 */
@Composable
fun DateSelector(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismissRequest: () -> Unit = {},
    buttonColors: ButtonColors = ButtonDefaults.outlinedButtonColors()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón para día anterior
        IconButton(
            onClick = { 
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                onDateSelected(calendar.time)
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Día anterior",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón central (seleccionar fecha)
        OutlinedButton(
            onClick = { 
                // Por ahora, simplemente volvemos a hoy
                onDateSelected(Date())
            },
            colors = buttonColors
        ) {
            Text(text = "Hoy")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón para día siguiente
        IconButton(
            onClick = { 
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                onDateSelected(calendar.time)
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Día siguiente",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 