package com.tfg.umeegunero.ui.components.calendar

import androidx.compose.foundation.background
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
import com.tfg.umeegunero.data.model.TipoEvento
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Componente que muestra una vista diaria del calendario
 * 
 * @param fechaActual Fecha actual seleccionada
 * @param eventos Lista de eventos a mostrar
 * @param onCambioFecha Callback cuando se cambia de día
 * @param onClickEvento Callback cuando se hace clic en un evento
 */
@Composable
fun VistaCalendarioDiario(
    fechaActual: LocalDate,
    eventos: List<Evento>,
    onCambioFecha: (LocalDate) -> Unit,
    onClickEvento: (Evento) -> Unit
) {
    val hoy = LocalDate.now()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera con navegación de días
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCambioFecha(fechaActual.minusDays(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Día anterior"
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre del día de la semana
                Text(
                    text = fechaActual.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES")).capitalize(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Fecha completa
                Text(
                    text = fechaActual.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (fechaActual == hoy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { onCambioFecha(fechaActual.plusDays(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Día siguiente"
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Eventos del día seleccionado
        val eventosDia = eventos.filter { evento ->
            evento.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDate() == fechaActual
        }.sortedBy { 
            it.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalTime() 
        }
        
        if (eventosDia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay eventos programados para hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Agrupar eventos por hora
            val eventosPorHora = eventosDia.groupBy { evento ->
                evento.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalTime().hour
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Para cada hora del día (de 7 a 22 horas)
                for (hora in 7..22) {
                    val eventosEnEstaHora = eventosPorHora[hora] ?: emptyList()
                    
                    item {
                        FranjaHoraria(
                            hora = hora,
                            eventos = eventosEnEstaHora,
                            onClickEvento = onClickEvento
                        )
                        
                        if (hora < 22) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que representa una franja horaria en la vista diaria
 * 
 * @param hora Hora del día (0-23)
 * @param eventos Lista de eventos para esta hora
 * @param onClickEvento Callback cuando se hace clic en un evento
 */
@Composable
private fun FranjaHoraria(
    hora: Int,
    eventos: List<Evento>,
    onClickEvento: (Evento) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = if (eventos.isEmpty()) Alignment.CenterVertically else Alignment.Top
    ) {
        // Hora
        Box(
            modifier = Modifier
                .width(50.dp)
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = String.format("%02d:00", hora),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Línea vertical
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(if (eventos.isEmpty()) 24.dp else (eventos.size * 80).dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        // Eventos
        if (eventos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Sin eventos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                eventos.forEach { evento ->
                    TarjetaEvento(
                        evento = evento,
                        onClick = { onClickEvento(evento) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = tipoEvento.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Hora
            val hora = evento.fecha.toDate().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalTime()
            
            Text(
                text = hora.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Tipo y título
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = tipoEvento.color,
                            shape = MaterialTheme.shapes.small
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Descripción
            Text(
                text = evento.descripcion,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 