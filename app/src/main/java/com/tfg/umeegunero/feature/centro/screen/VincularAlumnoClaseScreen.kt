package com.tfg.umeegunero.feature.centro.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.feature.centro.viewmodel.VincularAlumnoClaseViewModel
import com.tfg.umeegunero.feature.centro.viewmodel.NuevoAlumnoData
import com.tfg.umeegunero.feature.centro.viewmodel.VincularAlumnoClaseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import java.text.SimpleDateFormat
import java.util.Locale
import com.tfg.umeegunero.feature.centro.viewmodel.ModoVisualizacionAlumnos
import androidx.compose.foundation.shape.CircleShape

/**
 * Pantalla para vincular alumnos a clases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VincularAlumnoClaseScreen(
    onBack: () -> Unit,
    claseId: String? = null,
    centroId: String? = null,
    viewModel: VincularAlumnoClaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Inicializar con claseId o centroId si está disponible
    LaunchedEffect(claseId, centroId) {
        if (!claseId.isNullOrEmpty()) {
            // Inicializar con clase específica
            viewModel.inicializarConClase(claseId)
        } else if (!centroId.isNullOrEmpty()) {
            // Si no hay claseId, pero hay centroId, inicializar con centro
            viewModel.seleccionarCentro(centroId)
        }
    }
    
    // Manejo de mensajes y errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.limpiarError()
        }
    }
    
    LaunchedEffect(uiState.showSuccessMessage, uiState.mensaje) {
        if (uiState.showSuccessMessage && uiState.mensaje != null) {
            Toast.makeText(context, uiState.mensaje, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajeExito()
            // Esperamos un poco para que el usuario vea el mensaje
            delay(300)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular Alumnos a Clases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Recargar datos
                        scope.launch {
                            if (uiState.claseSeleccionada != null) {
                                viewModel.seleccionarClase(uiState.claseSeleccionada!!)
                                if (uiState.error == null) {
                                    viewModel.limpiarMensajeExito()
                                    viewModel.limpiarError()
                                    delay(300)
                                    viewModel.mostrarMensaje("Datos recargados correctamente")
                                }
                            } else if (uiState.cursoSeleccionado != null) {
                                viewModel.seleccionarCurso(uiState.cursoSeleccionado!!)
                            } else if (uiState.centroId.isNotEmpty()) {
                                viewModel.seleccionarCentro(uiState.centroId)
                            } else if (uiState.isAdminApp) {
                                // Esta función se implementará en el ViewModel
                                // viewModel.cargarTodosCentros()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar datos",
                            modifier = Modifier.size(size = 32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (uiState.claseSeleccionada != null) {
                FloatingActionButton(
                    onClick = { viewModel.mostrarDialogoCrearAlumno() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear nuevo alumno"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando datos...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            // Contenido principal de la pantalla - se implementará en otra función
            ContenidoPrincipal(
                uiState = uiState,
                viewModel = viewModel,
                paddingValues = paddingValues,
                scope = scope
            )
        }
    }
    
    // Diálogos - se implementarán en otra función
    MostrarDialogos(uiState, viewModel)
}

enum class DialogoTipo {
    VINCULAR, DESVINCULAR
}

/**
 * Contenido principal de la pantalla
 */
@Composable
fun ContenidoPrincipal(
    uiState: VincularAlumnoClaseUiState,
    viewModel: VincularAlumnoClaseViewModel,
    paddingValues: PaddingValues,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        // Selector de centro (solo visible para admin de app)
        if (uiState.isAdminApp) {
            AlumnoCentroSelector(
                centros = uiState.centros,
                centroSeleccionado = uiState.centroSeleccionado,
                onCentroSelected = { centro -> 
                    viewModel.seleccionarCentro(centro.id)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Solo mostrar el resto de la pantalla si hay un centro seleccionado
        if (uiState.centroId.isNotEmpty()) {
            // Selector de curso
            AlumnoCursoSelector(
                cursos = uiState.cursos,
                cursoSeleccionado = uiState.cursoSeleccionado,
                onCursoSelected = { curso -> 
                    if (curso.id.isNotEmpty()) {
                        // Seleccionar el curso y cargar sus clases
                        viewModel.seleccionarCurso(curso)
                        
                        // Indicar que hemos seleccionado un curso
                        scope.launch {
                            if (uiState.error == null) {
                                viewModel.mostrarMensaje("Curso ${curso.nombre} seleccionado")
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de clase (solo si hay curso seleccionado)
            if (uiState.cursoSeleccionado != null) {
                ClaseSelector(
                    clases = uiState.clases,
                    claseSeleccionada = uiState.claseSeleccionada,
                    onClaseSelected = { clase ->
                        viewModel.seleccionarClase(clase)
                        
                        scope.launch {
                            if (uiState.error == null) {
                                viewModel.mostrarMensaje("Clase ${clase.nombre} seleccionada")
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Contenido principal (lista de alumnos)
            if (uiState.claseSeleccionada != null) {
                // Contador de alumnos y capacidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alumnos en clase: ${uiState.alumnosVinculados.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (uiState.capacidadClase > 0) {
                        val isClaseLlena = uiState.alumnosVinculados.size >= uiState.capacidadClase
                        val color = if (isClaseLlena) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        
                        Text(
                            text = "Capacidad: ${uiState.alumnosVinculados.size}/${uiState.capacidadClase}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Barra de búsqueda y filtros
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Título de la sección
                    Text(
                        text = "Filtrar y buscar alumnos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Contenedor de filtros con fondo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Barra de búsqueda con icono y bordes redondeados
                            OutlinedTextField(
                                value = uiState.textoFiltroAlumnos,
                                onValueChange = { viewModel.actualizarFiltroAlumnos(it) },
                                placeholder = { Text("Buscar por nombre o DNI...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.textoFiltroAlumnos.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.actualizarFiltroAlumnos("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Limpiar búsqueda",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            
                            // Filtros en chips horizontales
                            Text(
                                text = "Mostrar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.modoVisualizacion == ModoVisualizacionAlumnos.TODOS,
                                    onClick = { viewModel.cambiarModoVisualizacion(ModoVisualizacionAlumnos.TODOS) },
                                    label = { Text("Todos") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                
                                FilterChip(
                                    selected = uiState.modoVisualizacion == ModoVisualizacionAlumnos.VINCULADOS,
                                    onClick = { viewModel.cambiarModoVisualizacion(ModoVisualizacionAlumnos.VINCULADOS) },
                                    label = { Text("Vinculados") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                                
                                FilterChip(
                                    selected = uiState.modoVisualizacion == ModoVisualizacionAlumnos.PENDIENTES,
                                    onClick = { viewModel.cambiarModoVisualizacion(ModoVisualizacionAlumnos.PENDIENTES) },
                                    label = { Text("Pendientes") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de alumnos con el filtro actualizado - nuevo diseño
            AlumnosList(
                alumnos = when (uiState.modoVisualizacion) {
                    ModoVisualizacionAlumnos.TODOS -> {
                        if (uiState.textoFiltroAlumnos.isEmpty()) 
                            uiState.alumnos 
                        else 
                            uiState.alumnos.filter { 
                                it.nombreCompleto.contains(uiState.textoFiltroAlumnos, ignoreCase = true) ||
                                it.dni.contains(uiState.textoFiltroAlumnos, ignoreCase = true)
                            }
                    }
                    ModoVisualizacionAlumnos.VINCULADOS -> {
                        if (uiState.textoFiltroAlumnos.isEmpty()) 
                            uiState.alumnosVinculados 
                        else 
                            uiState.alumnosVinculados.filter { 
                                it.nombreCompleto.contains(uiState.textoFiltroAlumnos, ignoreCase = true) ||
                                it.dni.contains(uiState.textoFiltroAlumnos, ignoreCase = true)
                            }
                    }
                    ModoVisualizacionAlumnos.PENDIENTES -> {
                        if (uiState.textoFiltroAlumnos.isEmpty()) 
                            uiState.alumnosDisponibles 
                        else 
                            uiState.alumnosDisponibles.filter { 
                                it.nombreCompleto.contains(uiState.textoFiltroAlumnos, ignoreCase = true) ||
                                it.dni.contains(uiState.textoFiltroAlumnos, ignoreCase = true)
                            }
                    }
                },
                alumnosVinculados = uiState.alumnosVinculados.map { it.dni },
                textoFiltro = uiState.textoFiltroAlumnos,
                onVincularClick = { alumno ->
                    viewModel.seleccionarAlumno(alumno)
                    viewModel.mostrarDialogoAsignar()
                },
                onDesvincularClick = { alumno ->
                    viewModel.seleccionarAlumno(alumno)
                    viewModel.mostrarDialogoConfirmarDesasignacion()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else if (uiState.isAdminApp && uiState.centros.isNotEmpty()) {
            // Mensaje de selección de centro para admin app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Selecciona un centro educativo para comenzar",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Como administrador de la aplicación, debes seleccionar primero un centro educativo para gestionar sus cursos, clases y alumnos.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Mensaje de selección
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.cursoSeleccionado != null)
                            "Selecciona una clase para gestionar sus alumnos"
                        else
                            "Selecciona un curso y una clase para gestionar los alumnos",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Muestra los diálogos de confirmación y creación de alumnos
 */
@Composable
fun MostrarDialogos(
    uiState: VincularAlumnoClaseUiState,
    viewModel: VincularAlumnoClaseViewModel
) {
    // Diálogo para vincular alumno
    if (uiState.showAsignarDialog) {
        DialogoConfirmacion(
            show = uiState.showAsignarDialog,
            onConfirm = { viewModel.asignarAlumnoAClase() },
            onDismiss = { viewModel.ocultarDialogoAsignar() },
            titulo = "Vincular alumno a clase",
            mensaje = "¿Estás seguro de que deseas vincular a ${uiState.alumnoSeleccionado?.nombreCompleto} a la clase ${uiState.claseSeleccionada?.nombre}?",
            confirmButtonText = "Vincular",
            tipo = DialogoTipo.VINCULAR
        )
    }
    
    // Diálogo para desvincular alumno
    if (uiState.showConfirmarDesasignacionDialog) {
        DialogoConfirmacion(
            show = uiState.showConfirmarDesasignacionDialog,
            onConfirm = { viewModel.desasignarAlumnoDeClase() },
            onDismiss = { viewModel.ocultarDialogoConfirmarDesasignacion() },
            titulo = "Desvincular alumno de clase",
            mensaje = "¿Estás seguro de que deseas desvincular a ${uiState.alumnoSeleccionado?.nombreCompleto} de la clase ${uiState.claseSeleccionada?.nombre}?",
            confirmButtonText = "Desvincular",
            tipo = DialogoTipo.DESVINCULAR
        )
    }
    
    // Diálogo para crear nuevo alumno
    if (uiState.showCrearAlumnoDialog) {
        DialogoCrearAlumno(
            nuevoAlumno = uiState.nuevoAlumno,
            onAlumnoChange = { viewModel.actualizarNuevoAlumno(it) },
            onConfirm = { viewModel.crearNuevoAlumno() },
            onDismiss = { viewModel.ocultarDialogoCrearAlumno() },
            isLoading = uiState.isLoading
        )
    }
}

/**
 * Diálogo de confirmación para vincular/desvincular alumnos
 */
@Composable
fun DialogoConfirmacion(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    titulo: String,
    mensaje: String,
    confirmButtonText: String,
    tipo: DialogoTipo
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = if (tipo == DialogoTipo.DESVINCULAR) 
                        Icons.Filled.RemoveCircle 
                    else 
                        Icons.Filled.AddCircle,
                    contentDescription = null,
                    tint = if (tipo == DialogoTipo.DESVINCULAR) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == DialogoTipo.DESVINCULAR) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo para crear un nuevo alumno
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearAlumno(
    nuevoAlumno: NuevoAlumnoData,
    onAlumnoChange: (NuevoAlumnoData) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Título
                Text(
                    text = "Crear nuevo alumno",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Formulario
                OutlinedTextField(
                    value = nuevoAlumno.nombre,
                    onValueChange = { 
                        onAlumnoChange(nuevoAlumno.copy(nombre = it, errorNombre = null))
                    },
                    label = { Text("Nombre") },
                    isError = nuevoAlumno.errorNombre != null,
                    supportingText = nuevoAlumno.errorNombre?.let {
                        { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nuevoAlumno.apellidos,
                    onValueChange = { 
                        onAlumnoChange(nuevoAlumno.copy(apellidos = it, errorApellidos = null))
                    },
                    label = { Text("Apellidos") },
                    isError = nuevoAlumno.errorApellidos != null,
                    supportingText = nuevoAlumno.errorApellidos?.let {
                        { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nuevoAlumno.dni,
                    onValueChange = { 
                        onAlumnoChange(nuevoAlumno.copy(dni = it, errorDni = null))
                    },
                    label = { Text("DNI/NIE") },
                    isError = nuevoAlumno.errorDni != null,
                    supportingText = nuevoAlumno.errorDni?.let {
                        { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nuevoAlumno.fechaNacimiento,
                    onValueChange = { 
                        onAlumnoChange(nuevoAlumno.copy(fechaNacimiento = it, errorFechaNacimiento = null))
                    },
                    label = { Text("Fecha de nacimiento (AAAA-MM-DD)") },
                    isError = nuevoAlumno.errorFechaNacimiento != null,
                    supportingText = nuevoAlumno.errorFechaNacimiento?.let {
                        { Text(it) }
                    } ?: {
                        Text("Formato: AAAA-MM-DD (por ejemplo: 2018-05-20)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading && 
                            nuevoAlumno.nombre.isNotEmpty() && 
                            nuevoAlumno.apellidos.isNotEmpty() &&
                            nuevoAlumno.dni.isNotEmpty() &&
                            nuevoAlumno.fechaNacimiento.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Crear")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Selector de centro educativo
 */
@Composable
fun AlumnoCentroSelector(
    centros: List<Centro>,
    centroSeleccionado: Centro?,
    onCentroSelected: (Centro) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Centro Educativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mostrar contador de centros
                Text(
                    text = "${centros.size} centros disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (centros.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // Solo expandir si hay centros
                        if (centros.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (centros.isEmpty()) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (centros.isEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (centros.isEmpty()) 
                            "No hay centros disponibles" 
                        else 
                            centroSeleccionado?.nombre ?: "Selecciona un centro educativo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (centros.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Icon(
                        imageVector = if (expanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) 
                            "Ocultar opciones" 
                        else 
                            "Mostrar opciones",
                        tint = if (centros.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded && centros.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                centros.forEach { centro ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = centro.nombre,
                                    fontWeight = if (centro.activo) FontWeight.Normal else FontWeight.Light
                                )
                                
                                if (!centro.activo) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            onCentroSelected(centro)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Selector de curso
 */
@Composable
fun AlumnoCursoSelector(
    cursos: List<Curso>,
    cursoSeleccionado: Curso?,
    onCursoSelected: (Curso) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Curso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mostrar contador de cursos con colores adecuados
                Text(
                    text = "${cursos.size} cursos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cursos.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // Solo expandir si hay cursos
                        if (cursos.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (cursos.isEmpty()) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (cursos.isEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cursos.isEmpty()) 
                            "No hay cursos disponibles" 
                        else 
                            cursoSeleccionado?.nombre ?: "Selecciona un curso",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (cursos.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = if (expanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) 
                            "Ocultar opciones" 
                        else 
                            "Mostrar opciones",
                        tint = if (cursos.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded && cursos.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                cursos.forEach { curso ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(curso.nombre)
                                
                                if (!curso.activo) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            onCursoSelected(curso)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Selector de clase
 */
@Composable
fun ClaseSelector(
    clases: List<Clase>,
    claseSeleccionada: Clase?,
    onClaseSelected: (Clase) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clase",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mostrar contador de clases con colores adecuados
                Text(
                    text = "${clases.size} clases disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (clases.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // Solo expandir si hay clases
                        if (clases.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (clases.isEmpty()) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (clases.isEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (clases.isEmpty()) 
                            "No hay clases disponibles" 
                        else 
                            claseSeleccionada?.nombre ?: "Selecciona una clase",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (clases.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = if (expanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) 
                            "Ocultar opciones" 
                        else 
                            "Mostrar opciones",
                        tint = if (clases.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded && clases.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                clases.forEach { clase ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(
                                    text = "Clase: ${clase.nombre}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Text(
                                    text = "Aula: ${clase.aula}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onClaseSelected(clase)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Lista de alumnos con opciones para vincular/desvincular
 */
@Composable
fun AlumnosList(
    alumnos: List<Alumno>,
    alumnosVinculados: List<String>,
    textoFiltro: String,
    onVincularClick: (Alumno) -> Unit,
    onDesvincularClick: (Alumno) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alumnos.isEmpty()) {
        // Mensaje cuando no hay alumnos
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (textoFiltro.isEmpty()) Icons.Default.Warning else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (textoFiltro.isEmpty()) 
                            MaterialTheme.colorScheme.secondary
                        else 
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (textoFiltro.isEmpty()) 
                            "No hay alumnos disponibles" 
                        else 
                            "No se encontraron resultados para '$textoFiltro'",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (textoFiltro.isEmpty()) 
                            "Puedes crear nuevos alumnos con el botón + en la esquina inferior derecha" 
                        else 
                            "Prueba con otros términos de búsqueda",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        // Lista de alumnos
        Column(modifier = modifier) {
            // Encabezado con contador de resultados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${alumnos.size} ${if (alumnos.size == 1) "alumno" else "alumnos"} encontrados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (textoFiltro.isNotEmpty()) {
                    Text(
                        text = "Filtro: \"$textoFiltro\"",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lista de alumnos
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    items(
                        items = alumnos,
                        key = { it.dni }
                    ) { alumno ->
                        val estaVinculado = alumnosVinculados.contains(alumno.dni)
                        
                        AlumnoItem(
                            alumno = alumno,
                            estaVinculado = estaVinculado,
                            onVincularClick = { onVincularClick(alumno) },
                            onDesvincularClick = { onDesvincularClick(alumno) }
                        )
                        
                        if (alumno != alumnos.last()) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(vertical = 4.dp),
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
 * Elemento de alumno en la lista
 */
@Composable
fun AlumnoItem(
    alumno: Alumno,
    estaVinculado: Boolean,
    onVincularClick: () -> Unit,
    onDesvincularClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar/Iniciales del alumno
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (estaVinculado) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (estaVinculado) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val iniciales = (alumno.nombre.firstOrNull()?.toString() ?: "") + 
                           (alumno.apellidos.split(" ").firstOrNull()?.firstOrNull()?.toString() ?: "")
                           
            Text(
                text = iniciales.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (estaVinculado) 
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Información del alumno
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = alumno.nombreCompleto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "DNI: ${alumno.dni}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Fecha de Nacimiento: ${alumno.fechaNacimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (estaVinculado) {
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Vinculado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón de acción (vincular o desvincular)
        FilledIconButton(
            onClick = if (estaVinculado) onDesvincularClick else onVincularClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (estaVinculado) 
                    MaterialTheme.colorScheme.errorContainer
                else 
                    MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = if (estaVinculado) 
                    Icons.Default.RemoveCircle
                else 
                    Icons.Default.AddCircle,
                contentDescription = if (estaVinculado) 
                    "Desvincular alumno" 
                else 
                    "Vincular alumno",
                tint = if (estaVinculado) 
                    MaterialTheme.colorScheme.error
                else 
                    MaterialTheme.colorScheme.primary
            )
        }
    }
} 