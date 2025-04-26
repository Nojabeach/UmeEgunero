package com.tfg.umeegunero.feature.profesor.registros.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.RegistroDiarioViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDiarioScreen(
    viewModel: RegistroDiarioViewModel,
    onNavigateBack: () -> Unit,
    alumnoNombre: String,
    claseNombre: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isLoading) {
        LoadingScreen()
        return
    }
    
    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error ?: "Error desconocido", 
            onDismiss = { viewModel.limpiarError() }
        )
    }
    
    if (uiState.showSuccessDialog) {
        SuccessDialog(
            onDismiss = { 
                viewModel.ocultarDialogoExito()
                onNavigateBack()
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(text = "Registro diario")
                        Text(
                            text = alumnoNombre,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            GuardarRegistroButton(
                isLoading = uiState.isLoading,
                onClick = { viewModel.guardarRegistro(uiState.registro.profesorId ?: "") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RegistroDiarioHeader(
                alumnoNombre = alumnoNombre,
                claseNombre = claseNombre,
                fecha = uiState.fechaSeleccionada
            )
            ElevatedSectionCard(
                title = "Comidas",
                icon = Icons.Default.Fastfood
            ) {
                ComidaRow(
                    titulo = "Primer plato",
                    estado = uiState.primerPlato,
                    onEstadoChange = { viewModel.actualizarEstadoComida("primerPlato", it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ComidaRow(
                    titulo = "Segundo plato",
                    estado = uiState.segundoPlato,
                    onEstadoChange = { viewModel.actualizarEstadoComida("segundoPlato", it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ComidaRow(
                    titulo = "Postre",
                    estado = uiState.postre,
                    onEstadoChange = { viewModel.actualizarEstadoComida("postre", it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ComidaRow(
                    titulo = "Merienda",
                    estado = uiState.merienda,
                    onEstadoChange = { viewModel.actualizarEstadoComida("merienda", it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.observacionesComida,
                    onValueChange = { viewModel.actualizarObservacionesComida(it) },
                    label = { Text("Observaciones de comida") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            ElevatedSectionCard(
                title = "Siesta",
                icon = Icons.Default.Alarm
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "¿Ha dormido siesta?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Switch(
                        checked = uiState.haSiestaSiNo,
                        onCheckedChange = { viewModel.toggleSiesta(it) }
                    )
                }
                
                if (uiState.haSiestaSiNo) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Aquí iría un selector de tiempo para inicio y fin de siesta
                    // Por ahora usamos campos de texto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = uiState.horaInicioSiesta,
                            onValueChange = { viewModel.establecerHoraInicioSiesta(it) },
                            label = { Text("Hora inicio") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedTextField(
                            value = uiState.horaFinSiesta,
                            onValueChange = { viewModel.establecerHoraFinSiesta(it) },
                            label = { Text("Hora fin") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.observacionesSiesta,
                        onValueChange = { viewModel.actualizarObservacionesSiesta(it) },
                        label = { Text("Observaciones de siesta") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            ElevatedSectionCard(
                title = "Deposiciones",
                icon = Icons.Default.Close
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "¿Ha hecho caca?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Switch(
                        checked = uiState.haHechoCaca,
                        onCheckedChange = { viewModel.toggleCaca(it) }
                    )
                }
                
                if (uiState.haHechoCaca) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Número de veces:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.decrementarCacas() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrementar"
                                )
                            }
                            
                            Text(
                                text = uiState.numeroCacas.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            IconButton(
                                onClick = { viewModel.incrementarCacas() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Incrementar"
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.observacionesCaca,
                        onValueChange = { viewModel.actualizarObservacionesCaca(it) },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            ElevatedSectionCard(
                title = "Material necesario",
                icon = Icons.Default.ChildCare
            ) {
                MaterialCheckRow(
                    titulo = "Necesita pañales",
                    checked = uiState.necesitaPanales,
                    onCheckedChange = { viewModel.toggleMaterial("panales", it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                MaterialCheckRow(
                    titulo = "Necesita toallitas",
                    checked = uiState.necesitaToallitas,
                    onCheckedChange = { viewModel.toggleMaterial("toallitas", it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                MaterialCheckRow(
                    titulo = "Necesita ropa de cambio",
                    checked = uiState.necesitaRopaCambio,
                    onCheckedChange = { viewModel.toggleMaterial("ropa", it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = uiState.otroMaterialNecesario,
                    onValueChange = { viewModel.actualizarOtroMaterial(it) },
                    label = { Text("Otros materiales necesarios") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            ElevatedSectionCard(
                title = "Observaciones generales",
                icon = Icons.Default.Description
            ) {
                OutlinedTextField(
                    value = uiState.observacionesGenerales,
                    onValueChange = { viewModel.actualizarObservacionesGenerales(it) },
                    label = { Text("Observaciones del día") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MaterialCheckRow(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ComidaRow(
    titulo: String,
    estado: EstadoComida,
    onEstadoChange: (EstadoComida) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstadoComidaButton(
                estado = EstadoComida.COMPLETO,
                currentEstado = estado,
                onClick = { onEstadoChange(EstadoComida.COMPLETO) },
                text = "Todo",
                color = Color(0xFF4CAF50)
            )
            
            EstadoComidaButton(
                estado = EstadoComida.PARCIAL,
                currentEstado = estado,
                onClick = { onEstadoChange(EstadoComida.PARCIAL) },
                text = "Parte",
                color = Color(0xFFFFB74D)
            )
            
            EstadoComidaButton(
                estado = EstadoComida.RECHAZADO,
                currentEstado = estado,
                onClick = { onEstadoChange(EstadoComida.RECHAZADO) },
                text = "Nada",
                color = Color(0xFFEF5350)
            )
            
            EstadoComidaButton(
                estado = EstadoComida.NO_APLICABLE,
                currentEstado = estado,
                onClick = { onEstadoChange(EstadoComida.NO_APLICABLE) },
                text = "N/A",
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
fun EstadoComidaButton(
    estado: EstadoComida,
    currentEstado: EstadoComida,
    onClick: () -> Unit,
    text: String,
    color: Color
) {
    val isSelected = estado == currentEstado
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) color else Color.Transparent)
            .border(
                border = BorderStroke(1.dp, color),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ElevatedSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Registro guardado correctamente",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@Composable
fun HiltRegistroDiarioScreen(
    viewModel: RegistroDiarioViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    alumnoId: String,
    alumnoNombre: String,
    claseId: String,
    claseNombre: String,
    profesorId: String
) {
    // Cargamos los datos al montar la pantalla
    viewModel.cargarRegistroDiario(
        alumnoId = alumnoId,
        claseId = claseId, 
        profesorId = profesorId
    )
    
    RegistroDiarioScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        alumnoNombre = alumnoNombre,
        claseNombre = claseNombre
    )
}

@Preview(showBackground = true)
@Composable
fun RegistroDiarioScreenPreview() {
    UmeEguneroTheme {
        RegistroDiarioScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {},
            alumnoNombre = "Juan Pérez",
            claseNombre = "Infantil 3 años A"
        )
    }
}

@Composable
fun RegistroDiarioHeader(
    alumnoNombre: String,
    claseNombre: String,
    fecha: Date
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumnoNombre.firstOrNull()?.toString() ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = alumnoNombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Clase: $claseNombre",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GuardarRegistroButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Guardar registro",
                tint = Color.White
            )
        }
    }
} 