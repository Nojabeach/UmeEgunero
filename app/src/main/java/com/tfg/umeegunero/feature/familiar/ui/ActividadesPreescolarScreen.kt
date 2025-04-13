package com.tfg.umeegunero.feature.familiar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.feature.familiar.viewmodel.FiltroActividad
import com.tfg.umeegunero.feature.familiar.screen.ActividadPreescolarItem

/**
 * Parte del listado de actividades con componentes comunes
 */
@Composable
fun ActividadesPreescolarListado(
    actividades: List<ActividadPreescolar>,
    onActividadClick: (ActividadPreescolar) -> Unit
) {
    if (actividades.isEmpty()) {
        // Mensaje cuando no hay actividades
        Text("No hay actividades para mostrar")
    } else {
        LazyColumn {
            items(actividades) { actividad ->
                ActividadPreescolarItem(
                    actividad = actividad,
                    onClick = { onActividadClick(actividad) }
                )
                Divider()
            }
        }
    }
}

/**
 * Componente para un chip con estado seleccionado
 */
@Composable
fun ChipData(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }
    )
    Spacer(modifier = Modifier.width(8.dp))
}

/**
 * Componente para filtrar actividades
 */
@Composable
fun ChipRow(
    filtroSeleccionado: FiltroActividad,
    onFiltroChanged: (FiltroActividad) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ChipData(
            text = "Pendientes",
            icon = Icons.Default.HourglassFull,
            isSelected = filtroSeleccionado == FiltroActividad.PENDIENTES,
            onClick = { onFiltroChanged(FiltroActividad.PENDIENTES) }
        )
        ChipData(
            text = "Completadas",
            icon = Icons.Default.Done,
            isSelected = filtroSeleccionado == FiltroActividad.COMPLETADAS,
            onClick = { onFiltroChanged(FiltroActividad.COMPLETADAS) }
        )
        ChipData(
            text = "Recientes",
            icon = Icons.Default.Pending,
            isSelected = filtroSeleccionado == FiltroActividad.RECIENTES,
            onClick = { onFiltroChanged(FiltroActividad.RECIENTES) }
        )
        ChipData(
            text = "Todas",
            icon = Icons.Default.List,
            isSelected = filtroSeleccionado == FiltroActividad.TODAS,
            onClick = { onFiltroChanged(FiltroActividad.TODAS) }
        )
    }
}