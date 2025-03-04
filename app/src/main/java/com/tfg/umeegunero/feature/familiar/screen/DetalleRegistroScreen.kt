package com.tfg.umeegunero.feature.familiar.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleRegistroViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRegistroScreen(
    viewModel: DetalleRegistroViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Extraer el registro para poder usarlo de forma segura
    val registro = uiState.registro

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de actividad",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Botón para chatear con el profesor
                    registro?.profesorId?.let { profesorId ->
                        IconButton(
                            onClick = { onNavigateToChat(profesorId, registro.alumnoId) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Contactar al profesor",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FamiliarColor)
                }
            } else if (registro == null) {
                // Mostrar mensaje de error si no hay registro
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No se encontró el registro de actividad",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Volver al listado")
                        }
                    }
                }
            } else {
                // Mostrar detalles del registro
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Información básica
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = formatDateExtended(registro.fecha),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "Profesor: ${uiState.profesorNombre ?: "Desconocido"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Sección de alimentación
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Alimentación",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Primer plato
                            registro.comidas.primerPlato.takeIf { it.descripcion.isNotBlank() }?.let { plato ->
                                Text(
                                    text = "Primer plato",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = plato.descripcion,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Consumido: ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = nivelConsumoText(plato.consumo),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = nivelConsumoColor(plato.consumo)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Segundo plato
                            registro.comidas.segundoPlato.takeIf { it.descripcion.isNotBlank() }?.let { plato ->
                                Text(
                                    text = "Segundo plato",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = plato.descripcion,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Consumido: ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = nivelConsumoText(plato.consumo),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = nivelConsumoColor(plato.consumo)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Postre
                            registro.comidas.postre.takeIf { it.descripcion.isNotBlank() }?.let { plato ->
                                Text(
                                    text = "Postre",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = plato.descripcion,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Consumido: ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = nivelConsumoText(plato.consumo),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = nivelConsumoColor(plato.consumo)
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Siesta
                    registro.siesta?.let { siesta ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Descanso",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Hora inicio
                                siesta.inicio?.let { inicio ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Inicio: ${formatTime(inicio)}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Hora fin
                                siesta.fin?.let { fin ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Fin: ${formatTime(fin)}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Duración calculada
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Duración: ${formatDuracion(siesta.duracion)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Necesidades Fisiológicas
                    if (registro.necesidadesFisiologicas.pipi ||
                        registro.necesidadesFisiologicas.caca ||
                        registro.necesidadesFisiologicas.observaciones.isNotBlank()) {

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Higiene",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (registro.necesidadesFisiologicas.pipi) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Pipi",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.necesidadesFisiologicas.caca) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Caca",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (registro.necesidadesFisiologicas.observaciones.isNotBlank()) {
                                    Text(
                                        text = "Observaciones:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = registro.necesidadesFisiologicas.observaciones,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Observaciones Generales
                    if (registro.observaciones.isNotEmpty()) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Observaciones Generales",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = registro.observaciones.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Funciones utilitarias
fun formatDateExtended(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return dateFormat.format(timestamp.toDate()).replaceFirstChar { it.uppercase() }
}

fun formatTime(timestamp: Timestamp): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(timestamp.toDate())
}

fun formatDuracion(minutos: Int): String {
    val horas = minutos / 60
    val minutosRestantes = minutos % 60

    return if (horas > 0) {
        "$horas h $minutosRestantes min"
    } else {
        "$minutosRestantes min"
    }
}

fun nivelConsumoText(nivel: NivelConsumo): String {
    return when (nivel) {
        NivelConsumo.NADA -> "Nada"
        NivelConsumo.POCO -> "Poco"
        NivelConsumo.BIEN -> "Bien"
    }
}

fun nivelConsumoColor(nivel: NivelConsumo): Color {
    return when (nivel) {
        NivelConsumo.NADA -> Color.Red
        NivelConsumo.POCO -> Color(0xFFFF9800) // Orange
        NivelConsumo.BIEN -> Color(0xFF4CAF50) // Green
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleRegistroScreenPreview() {
    UmeEguneroTheme {
        // Proporcionar un viewModel de simulación para la vista previa
        DetalleRegistroScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetalleRegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        // Proporcionar un viewModel de simulación para la vista previa
        DetalleRegistroScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
}