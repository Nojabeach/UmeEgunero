package com.tfg.umeegunero.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Evento
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

/**
 * Componente que muestra una vista semanal del calendario
 * 
 * @param fechaActual Fecha actual seleccionada
 * @param eventos Lista de eventos a mostrar
 * @param onFechaSeleccionada Callback cuando se selecciona una fecha
 * @param onSemanaAnterior Callback cuando se navega a la semana anterior
 * @param onSemanaSiguiente Callback cuando se navega a la semana siguiente
 * @param onClickEvento Callback cuando se hace clic en un evento
 */
@Composable
fun VistaCalendarioSemanal(
    fechaActual: LocalDate,
    eventos: List<Evento>,
    onFechaSeleccionada: (LocalDate) -> Unit,
    onSemanaAnterior: () -> Unit,
    onSemanaSiguiente: () -> Unit,
    onClickEvento: (Evento) -> Unit
) {
    val hoy = LocalDate.now()
    val configuracionSemana = WeekFields.of(Locale.getDefault())
    val primerDiaSemana = fechaActual.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    
    // Lista de días de la semana actual
    val diasSemana = (0..6).map { primerDiaSemana.plusDays(it.toLong()) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera con navegación de semanas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSemanaAnterior) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Semana anterior"
                )
            }
            
            val formatoSemana = DateTimeFormatter.ofPattern("'Semana' w, MMMM yyyy", Locale("es", "ES"))
            Text(
                text = formatoSemana.format(fechaActual),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onSemanaSiguiente) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Semana siguiente"
                )
            }
        }
        
        // Cabecera con nombres de días
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            diasSemana.forEach { fecha ->
                val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                val esHoy = fecha.equals(hoy)
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = nombreDia,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (esHoy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Número del día
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (esHoy) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .border(
                                width = 1.dp,
                                color = if (fecha == fechaActual) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onFechaSeleccionada(fecha) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fecha.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (esHoy) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (fecha == fechaActual) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    // Indicador de eventos para este día
                    val numeroEventosDia = eventos.count { evento -> 
                        evento.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDate() == fecha 
                    }
                    
                    if (numeroEventosDia > 0) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    }
                }
            }
        }
        
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Eventos del día seleccionado
        val eventosFechaSeleccionada = eventos.filter { evento ->
            evento.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDate() == fechaActual
        }
        
        if (eventosFechaSeleccionada.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay eventos para ${fechaActual.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES")))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp)
            ) {
                items(eventosFechaSeleccionada) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        onClick = { onClickEvento(evento) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Tarjeta que muestra la información de un evento
 * 
 * @param evento Evento a mostrar
 * @param onClick Callback cuando se hace clic en el evento
 */
@Composable
private fun TarjetaEvento(
    evento: Evento,
    onClick: () -> Unit
) {
    // Obtener el tipo de evento
    val tipoEvento = evento.getTipoEvento()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = tipoEvento.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = tipoEvento.color,
                        shape = MaterialTheme.shapes.small
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 