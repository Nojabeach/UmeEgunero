package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import com.tfg.umeegunero.data.model.TipoNotificacion

/**
 * Modelo de datos para las notificaciones
 */
data class Notificacion(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val fecha: Date,
    val leida: Boolean,
    val tipo: TipoNotificacion
)

/**
 * Pantalla de notificaciones para administrador
 * Muestra una lista de notificaciones del sistema
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificacionesScreen(
    navController: NavController
) {
    val notificaciones = remember {
        listOf(
            Notificacion(
                id = "1",
                titulo = "Actualización del sistema",
                mensaje = "Se ha completado la actualización programada del sistema",
                fecha = Date(),
                leida = true,
                tipo = TipoNotificacion.SISTEMA
            ),
            Notificacion(
                id = "2",
                titulo = "Nuevo centro registrado",
                mensaje = "El centro 'Colegio Ejemplo' se ha registrado correctamente",
                fecha = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
                leida = false,
                tipo = TipoNotificacion.EVENTO
            ),
            Notificacion(
                id = "3",
                titulo = "Solicitud de soporte",
                mensaje = "Hay una nueva solicitud de soporte técnico pendiente",
                fecha = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000),
                leida = false,
                tipo = TipoNotificacion.MENSAJE
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (notificaciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay notificaciones",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(notificaciones) { notificacion ->
                    NotificacionItem(notificacion = notificacion)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificacionItem(notificacion: Notificacion) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono según el tipo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notificacion.tipo) {
                        TipoNotificacion.SISTEMA -> Icons.Default.Notifications
                        TipoNotificacion.EVENTO -> Icons.Default.NotificationsActive
                        TipoNotificacion.MENSAJE -> Icons.Default.Notifications
                        TipoNotificacion.GENERAL -> Icons.Default.Notifications
                        TipoNotificacion.ANUNCIO -> Icons.Default.Campaign
                        TipoNotificacion.TAREA -> Icons.Default.Assignment
                        TipoNotificacion.URGENTE -> Icons.Default.PriorityHigh
                        TipoNotificacion.ACADEMICO -> Icons.Default.School
                        TipoNotificacion.ALERTA -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notificacion.leida) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = dateFormatter.format(notificacion.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // Indicador de no leída
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminNotificacionesScreenPreview() {
    UmeEguneroTheme {
        AdminNotificacionesScreen(navController = rememberNavController())
    }
} 