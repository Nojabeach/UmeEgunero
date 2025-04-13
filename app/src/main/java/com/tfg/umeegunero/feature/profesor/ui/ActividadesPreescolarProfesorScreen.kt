package com.tfg.umeegunero.feature.profesor.ui

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
import com.tfg.umeegunero.feature.familiar.screen.ActividadPreescolarItem

@Composable
fun ActividadesPreescolarProfesorListado(
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