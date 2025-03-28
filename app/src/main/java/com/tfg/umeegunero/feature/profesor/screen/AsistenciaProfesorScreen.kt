package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.feature.profesor.viewmodel.AsistenciaViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de registro de asistencia para profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaProfesorScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pantalla de registro de asistencia en desarrollo",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun AsistenciaProfesorItem(
    alumno: Alumno,
    estadoAsistencia: Asistencia,
    onAsistenciaChange: (Asistencia) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Información del alumno
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${alumno.nombre} ${alumno.apellidos}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Selector de estado de asistencia
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Presente
            IconButton(
                onClick = { onAsistenciaChange(Asistencia.PRESENTE) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Presente",
                    tint = if (estadoAsistencia == Asistencia.PRESENTE) 
                        Color.Green else Color.Gray
                )
            }
            
            // Retrasado
            IconButton(
                onClick = { onAsistenciaChange(Asistencia.RETRASADO) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Retrasado",
                    tint = if (estadoAsistencia == Asistencia.RETRASADO) 
                        Color(0xFFFFA000) else Color.Gray
                )
            }
            
            // Ausente
            IconButton(
                onClick = { onAsistenciaChange(Asistencia.AUSENTE) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Ausente",
                    tint = if (estadoAsistencia == Asistencia.AUSENTE) 
                        Color.Red else Color.Gray
                )
            }
        }
    }
} 