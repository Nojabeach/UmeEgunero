package com.tfg.umeegunero.feature.familiar.registros.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.feature.familiar.registros.viewmodel.ConsultaRegistroDiarioViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaRegistroDiarioScreen(
    viewModel: ConsultaRegistroDiarioViewModel,
    alumnoId: String,
    alumnoNombre: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(alumnoId) {
        viewModel.cargarRegistros(alumnoId)
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registros de $alumnoNombre") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.registros.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.registros.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}")
            }
        } else if (uiState.registros.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No hay registros disponibles")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.registros) { registro ->
                    RegistroDiarioCard(
                        registro = registro,
                        onClick = { viewModel.marcarComoVisto(registro.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!registro.visualizadoPorFamiliar) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabecera con fecha
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(registro.fecha.toDate()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (!registro.visualizadoPorFamiliar) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resumen de comidas
            InfoRow(
                icon = Icons.Default.LocalDining,
                title = "Comidas",
                content = obtenerResumenComidas(registro)
            )
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Siesta
            InfoRow(
                icon = Icons.Default.NightsStay,
                title = "Siesta",
                content = if (registro.haSiestaSiNo) {
                    val inicio = registro.horaInicioSiesta?.toDate()
                    val fin = registro.horaFinSiesta?.toDate()
                    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                    
                    if (inicio != null && fin != null) {
                        "De ${formatoHora.format(inicio)} a ${formatoHora.format(fin)}"
                    } else {
                        "Ha dormido siesta"
                    }
                } else {
                    "No ha dormido siesta"
                }
            )
            
            if (registro.observacionesSiesta.isNotEmpty()) {
                Text(
                    text = registro.observacionesSiesta,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Deposiciones
            InfoRow(
                icon = Icons.Default.Wc,
                title = "Deposiciones",
                content = if (registro.haHechoCaca) {
                    "Ha hecho caca ${registro.numeroCacas} ${if (registro.numeroCacas == 1) "vez" else "veces"}"
                } else {
                    "No ha hecho caca"
                }
            )
            
            if (registro.observacionesCaca.isNotEmpty()) {
                Text(
                    text = registro.observacionesCaca,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            
            // Material necesario
            if (registro.necesitaPanales || registro.necesitaToallitas || 
                registro.necesitaRopaCambio || registro.otroMaterialNecesario.isNotEmpty()) {
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    text = "Material necesario:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (registro.necesitaPanales) {
                    Text(
                        text = "• Pañales",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (registro.necesitaToallitas) {
                    Text(
                        text = "• Toallitas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (registro.necesitaRopaCambio) {
                    Text(
                        text = "• Ropa de cambio",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (registro.otroMaterialNecesario.isNotEmpty()) {
                    Text(
                        text = "• ${registro.otroMaterialNecesario}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Observaciones generales
            if (registro.observacionesGenerales.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoRow(
                    icon = Icons.Default.Subject,
                    title = "Observaciones generales",
                    content = ""
                )
                
                Text(
                    text = registro.observacionesGenerales,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(top = 3.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (content.isNotEmpty()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun obtenerResumenComidas(registro: RegistroActividad): String {
    val elementos = mutableListOf<String>()
    
    if (registro.primerPlato == EstadoComida.COMPLETO) elementos.add("Primer plato completo")
    else if (registro.primerPlato == EstadoComida.PARCIAL) elementos.add("Primer plato parcial")
    
    if (registro.segundoPlato == EstadoComida.COMPLETO) elementos.add("Segundo plato completo")
    else if (registro.segundoPlato == EstadoComida.PARCIAL) elementos.add("Segundo plato parcial")
    
    if (registro.postre == EstadoComida.COMPLETO) elementos.add("Postre completo")
    else if (registro.postre == EstadoComida.PARCIAL) elementos.add("Postre parcial")
    
    if (registro.merienda == EstadoComida.COMPLETO) elementos.add("Merienda completa")
    else if (registro.merienda == EstadoComida.PARCIAL) elementos.add("Merienda parcial")
    
    return if (elementos.isEmpty()) {
        "No hay registro de comidas"
    } else {
        elementos.joinToString(", ")
    }
}

@Composable
fun HiltConsultaRegistroDiarioScreen(
    viewModel: ConsultaRegistroDiarioViewModel = hiltViewModel(),
    alumnoId: String,
    alumnoNombre: String,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(alumnoId) {
        viewModel.cargarRegistros(alumnoId)
    }
    
    ConsultaRegistroDiarioScreen(
        viewModel = viewModel,
        alumnoId = alumnoId,
        alumnoNombre = alumnoNombre,
        onNavigateBack = onNavigateBack
    )
}

@Preview(showBackground = true)
@Composable
fun ConsultaRegistroDiarioScreenPreview() {
    UmeEguneroTheme {
        ConsultaRegistroDiarioScreen(
            viewModel = hiltViewModel(),
            alumnoId = "1",
            alumnoNombre = "Juan Pérez",
            onNavigateBack = {}
        )
    }
} 