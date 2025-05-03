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
import java.time.LocalDate

/**
 * Componente que permite seleccionar una fecha navegando por días
 * 
 * @param selectedDate La fecha actualmente seleccionada
 * @param onDateSelected Callback cuando se selecciona una nueva fecha
 * @param buttonColors Colores personalizados para el botón central
 */
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    buttonColors: ButtonColors = ButtonDefaults.outlinedButtonColors()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón para día anterior
        IconButton(
            onClick = { onDateSelected(selectedDate.minusDays(1)) }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Día anterior",
                tint = buttonColors.contentColor
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón central (seleccionar fecha)
        OutlinedButton(
            onClick = { 
                // Aquí se podría mostrar un DatePicker
                // Por ahora, simplemente volvemos a hoy
                onDateSelected(LocalDate.now())
            },
            colors = buttonColors
        ) {
            Text(text = "Hoy")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón para día siguiente
        IconButton(
            onClick = { onDateSelected(selectedDate.plusDays(1)) }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Día siguiente",
                tint = buttonColors.contentColor
            )
        }
    }
} 