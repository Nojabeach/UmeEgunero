package com.tfg.umeegunero.feature.profesor.registros.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.HistoricoRegistroDiarioViewModel
import com.tfg.umeegunero.ui.components.calendar.DateSelector
import com.tfg.umeegunero.ui.theme.ProfesorColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import android.content.Intent
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoRegistroDiarioScreen(
    navController: NavController,
    viewModel: HistoricoRegistroDiarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Estados para los menús desplegables
    var mostrarMenuClase by remember { mutableStateOf(false) }
    var mostrarMenuAlumno by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    
    // Estado para diálogos
    var mostrarDialogoExito by remember { mutableStateOf(false) }
    
    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarError()
            }
        }
    }
    
    // Efecto para manejar el éxito de exportación a PDF
    LaunchedEffect(uiState.exportPdfUri) {
        uiState.exportPdfUri?.let {
            mostrarDialogoExito = true
        }
    }
    
    // Función para compartir el PDF
    fun compartirPdf() {
        val intent = viewModel.crearIntentCompartirPDF()
        if (intent != null) {
            context.startActivity(Intent.createChooser(intent, "Compartir PDF de registros"))
            viewModel.limpiarExportPdfUri()
            mostrarDialogoExito = false
        } else {
            Toast.makeText(context, "Error al crear el archivo para compartir", Toast.LENGTH_SHORT).show()
        }
    }
    
    if (mostrarDialogoExito) {
        AlertDialog(
            onDismissRequest = { 
                mostrarDialogoExito = false
                viewModel.limpiarExportPdfUri()
            },
            title = { Text("PDF generado correctamente") },
            text = { Text("El PDF con los registros ha sido generado. ¿Desea compartirlo?") },
            confirmButton = {
                Button(onClick = { compartirPdf() }) {
                    Text("Compartir")
                }
            },
            dismissButton = {
                Button(onClick = { 
                    mostrarDialogoExito = false
                    viewModel.limpiarExportPdfUri()
                }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Registros Diarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refrescarDatos() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.alumnoSeleccionado != null && uiState.registros.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        viewModel.exportarRegistrosPDF(context)
                    },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Exportar") },
                    text = { Text("Exportar PDF") },
                    containerColor = ProfesorColor,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Filtros
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Filtros de búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de clase
                    Text(
                        text = "Clase",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarMenuClase = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (uiState.claseSeleccionada != null) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Class,
                                    contentDescription = null,
                                    tint = ProfesorColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.claseSeleccionada?.nombre ?: "Seleccionar clase",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (uiState.claseSeleccionada != null) FontWeight.Medium else FontWeight.Normal
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = mostrarMenuClase,
                            onDismissRequest = { mostrarMenuClase = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            uiState.clases.forEach { clase ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (uiState.claseSeleccionada?.id == clase.id) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(ProfesorColor, CircleShape)
                                                        .align(Alignment.CenterVertically)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = clase.nombre,
                                                fontWeight = if (uiState.claseSeleccionada?.id == clase.id) 
                                                    FontWeight.Bold 
                                                else 
                                                    FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.seleccionarClase(clase.id)
                                        mostrarMenuClase = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de alumno
                    Text(
                        text = "Alumno",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = uiState.claseSeleccionada != null) { 
                                    if (uiState.claseSeleccionada != null) {
                                        mostrarMenuAlumno = true
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (uiState.alumnoSeleccionado != null) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (uiState.claseSeleccionada != null) ProfesorColor else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.claseSeleccionada == null) {
                                        "Primero selecciona una clase"
                                    } else if (uiState.alumnoSeleccionado != null) {
                                        uiState.alumnoSeleccionado!!.nombre ?: "Alumno seleccionado"
                                    } else {
                                        "Seleccionar alumno"
                                    },
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (uiState.alumnoSeleccionado != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (uiState.claseSeleccionada != null) Color.Unspecified else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (uiState.claseSeleccionada != null) Color.Unspecified else Color.Gray
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = mostrarMenuAlumno,
                            onDismissRequest = { mostrarMenuAlumno = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            uiState.alumnos.forEach { alumno ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (uiState.alumnoSeleccionado?.id == alumno.id) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(ProfesorColor, CircleShape)
                                                        .align(Alignment.CenterVertically)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = alumno.nombre ?: "Alumno sin nombre",
                                                fontWeight = if (uiState.alumnoSeleccionado?.id == alumno.id) 
                                                    FontWeight.Bold 
                                                else 
                                                    FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.seleccionarAlumno(alumno.id)
                                        mostrarMenuAlumno = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de fecha
                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarCalendario = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = ProfesorColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                                            .format(uiState.fechaSeleccionada),
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (esHoy(uiState.fechaSeleccionada)) "Hoy" else 
                                            if (esAyer(uiState.fechaSeleccionada)) "Ayer" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    
                    if (mostrarCalendario) {
                        DateSelector(
                            selectedDate = uiState.fechaSeleccionada,
                            onDateSelected = { fecha -> 
                                viewModel.seleccionarFecha(fecha)
                                mostrarCalendario = false
                            },
                            onDismissRequest = { mostrarCalendario = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resumen de selección
            if (uiState.alumnoSeleccionado != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mostrando registros de ${uiState.alumnoSeleccionado?.nombre ?: ""} para el día ${
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.fechaSeleccionada)
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Lista de registros
            if (uiState.isLoading || uiState.isExporting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ProfesorColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.isExporting) "Generando PDF..." else "Cargando registros...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (uiState.registros.isEmpty()) {
                // Estado vacío mejorado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ProfesorColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.alumnoSeleccionado != null) {
                                "No hay registros para ${uiState.alumnoSeleccionado?.nombre} en la fecha seleccionada"
                            } else {
                                "Selecciona una clase y un alumno para ver sus registros"
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (uiState.alumnoSeleccionado != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Prueba a seleccionar otra fecha o comprueba que se hayan creado registros para este alumno",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(uiState.registros) { registro ->
                        RegistroDiarioCard(registro = registro)
                    }
                }
            }
        }
    }
}

@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    modifier: Modifier = Modifier
) {
    val fecha = SimpleDateFormat("HH:mm", Locale.getDefault()).format(registro.fecha.toDate())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registro a las $fecha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val createdByText = registro.creadoPor?.let { "Por: $it" } ?: ""
                if (createdByText.isNotEmpty()) {
                    Text(
                        text = createdByText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Comidas
            InfoSeccion(
                icon = Icons.Default.LocalDining,
                title = "Comidas",
                content = {
                    Column {
                        ComidaItem("Primer plato", registro.comidas.primerPlato.estadoComida)
                        ComidaItem("Segundo plato", registro.comidas.segundoPlato.estadoComida)
                        ComidaItem("Postre", registro.comidas.postre.estadoComida)
                        
                        if (!registro.observacionesComida.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Observaciones: ${registro.observacionesComida}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Siesta
            InfoSeccion(
                icon = Icons.Default.NightsStay,
                title = "Siesta",
                content = {
                    Column {
                        if (registro.haSiestaSiNo) {
                            val horaInicio = registro.horaInicioSiesta.ifEmpty { "No registrada" }
                            val horaFin = registro.horaFinSiesta.ifEmpty { "No registrada" }
                            
                            Text("El alumno ha dormido siesta", style = MaterialTheme.typography.bodyMedium)
                            Text("De $horaInicio a $horaFin", style = MaterialTheme.typography.bodyMedium)
                            
                            if (!registro.observacionesSiesta.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Observaciones: ${registro.observacionesSiesta}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        } else {
                            Text("El alumno no ha dormido siesta", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            )
            
            // Mostrar deposiciones si ha hecho caca
            if (registro.haHechoCaca) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.Default.Wc,
                    title = "Deposiciones",
                    content = {
                        Column {
                            Text(
                                "El alumno ha hecho ${registro.numeroCacas} deposiciones", 
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (!registro.observacionesCaca.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Observaciones: ${registro.observacionesCaca}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                )
            }
            
            // Materiales necesarios
            if (registro.necesitaPanales || registro.necesitaToallitas || registro.necesitaRopaCambio || 
                !registro.otroMaterialNecesario.isNullOrEmpty()) {
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.Default.DateRange,
                    title = "Material necesario",
                    content = {
                        Column {
                            if (registro.necesitaPanales) {
                                Text("• Pañales", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (registro.necesitaToallitas) {
                                Text("• Toallitas", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (registro.necesitaRopaCambio) {
                                Text("• Ropa de cambio", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!registro.otroMaterialNecesario.isNullOrEmpty()) {
                                Text("• ${registro.otroMaterialNecesario}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                )
            }
            
            // Observaciones generales
            if (!registro.observacionesGenerales.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.AutoMirrored.Filled.Subject,
                    title = "Observaciones generales",
                    content = {
                        Text(
                            text = registro.observacionesGenerales ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun InfoSeccion(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfesorColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.padding(start = 28.dp)) {
            content()
        }
    }
}

@Composable
fun ComidaItem(nombre: String, estado: EstadoComida) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (color, texto) = when (estado) {
            EstadoComida.COMPLETO -> Pair(Color.Green, "Completo")
            EstadoComida.PARCIAL -> Pair(Color.Yellow, "Parcial")
            EstadoComida.RECHAZADO -> Pair(Color.Red, "Rechazado")
            EstadoComida.NO_SERVIDO -> Pair(Color.Gray, "No servido")
            EstadoComida.NO_APLICABLE -> Pair(Color.Gray, "No aplicable")
            EstadoComida.SIN_DATOS -> Pair(Color.Gray, "Sin datos")
        }
        
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$nombre: $texto",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Función para determinar si una fecha es hoy
private fun esHoy(fecha: Date): Boolean {
    val hoy = java.util.Calendar.getInstance()
    val cal = java.util.Calendar.getInstance()
    cal.time = fecha
    
    return hoy.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
           hoy.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)
}

// Función para determinar si una fecha es ayer
private fun esAyer(fecha: Date): Boolean {
    val hoy = java.util.Calendar.getInstance()
    val ayer = java.util.Calendar.getInstance()
    ayer.add(java.util.Calendar.DAY_OF_YEAR, -1)
    
    val cal = java.util.Calendar.getInstance()
    cal.time = fecha
    
    return ayer.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
           ayer.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)
} 