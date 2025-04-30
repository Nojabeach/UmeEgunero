/**
 * Módulo de gestión de profesorado del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para la gestión completa del
 * profesorado de un centro educativo, incluyendo altas, bajas,
 * modificaciones y asignación a clases.
 */
package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.GestionProfesoresViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import android.content.res.Configuration
import com.tfg.umeegunero.ui.components.*

/**
 * Pantalla principal para la gestión de profesores del centro.
 * 
 * Esta pantalla proporciona una interfaz completa para la administración
 * del profesorado, permitiendo realizar todas las operaciones de gestión
 * necesarias de forma intuitiva y eficiente.
 * 
 * ## Características
 * - Lista completa de profesores
 * - Formularios de alta y edición
 * - Asignación a clases
 * - Gestión de datos personales
 * 
 * ## Funcionalidades
 * - Alta de nuevos profesores
 * - Edición de datos existentes
 * - Asignación a grupos y materias
 * - Búsqueda y filtrado
 * 
 * ## Estados
 * - Carga de datos
 * - Lista vacía
 * - Formularios de edición
 * - Errores y mensajes
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona la lógica de profesores
 * 
 * @see GestionProfesoresViewModel
 * @see Usuario
 * @see Clase
 */
@Composable
fun GestionProfesoresScreen(
    navController: NavController,
    viewModel: GestionProfesoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados para formularios
    var nombreProfesor by remember { mutableStateOf("") }
    var apellidosProfesor by remember { mutableStateOf("") }
    var dniProfesor by remember { mutableStateOf("") }
    var emailProfesor by remember { mutableStateOf("") }
    var telefonoProfesor by remember { mutableStateOf("") }
    
    // Cargar datos de profesor en formulario cuando se selecciona
    LaunchedEffect(uiState.selectedProfesor, uiState.showEditProfesorDialog) {
        if (uiState.showEditProfesorDialog && uiState.selectedProfesor != null) {
            uiState.selectedProfesor?.let { profesor ->
                nombreProfesor = profesor.nombre
                apellidosProfesor = profesor.apellidos
                dniProfesor = profesor.dni
                emailProfesor = profesor.email
                telefonoProfesor = profesor.telefono ?: ""
            }
        }
    }
    
    // Reiniciar formulario al añadir nuevo profesor
    LaunchedEffect(uiState.showAddProfesorDialog) {
        if (uiState.showAddProfesorDialog) {
            nombreProfesor = ""
            apellidosProfesor = ""
            dniProfesor = ""
            emailProfesor = ""
            telefonoProfesor = ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Profesores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.mostrarDialogoAddProfesor() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Profesor"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.profesores.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay profesores registrados",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pulsa el botón + para añadir un profesor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(uiState.profesores) { profesor ->
                        ProfesorItemSimple(
                            profesor = profesor,
                            onProfesorClick = {
                                viewModel.seleccionarProfesor(profesor)
                                viewModel.mostrarDialogoEditProfesor()
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Para evitar que el FAB tape el último elemento
                    }
                }
            }
            
            // Mensajes de error
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        TextButton(onClick = { viewModel.limpiarError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(uiState.error ?: "Error desconocido")
                }
            }
            
            // Mensajes de éxito
            if (uiState.showSuccessMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.limpiarMensajeExito() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(uiState.showSuccessMessage ?: "")
                }
            }
        }
    }
    
    // Diálogo para añadir profesor
    if (uiState.showAddProfesorDialog) {
        ProfesorFormDialog(
            titulo = "Añadir Profesor",
            dni = dniProfesor,
            nombre = nombreProfesor,
            apellidos = apellidosProfesor,
            email = emailProfesor,
            telefono = telefonoProfesor,
            onDniChange = { dniProfesor = it },
            onNombreChange = { nombreProfesor = it },
            onApellidosChange = { apellidosProfesor = it },
            onEmailChange = { emailProfesor = it },
            onTelefonoChange = { telefonoProfesor = it },
            onConfirmar = {
                viewModel.crearProfesor(
                    dni = dniProfesor,
                    nombre = nombreProfesor,
                    apellidos = apellidosProfesor,
                    email = emailProfesor,
                    telefono = telefonoProfesor
                )
            },
            onCancelar = { viewModel.ocultarDialogoAddProfesor() }
        )
    }
    
    // Diálogo para editar profesor
    if (uiState.showEditProfesorDialog && uiState.selectedProfesor != null) {
        ProfesorFormDialog(
            titulo = "Editar Profesor",
            dni = dniProfesor,
            nombre = nombreProfesor,
            apellidos = apellidosProfesor,
            email = emailProfesor,
            telefono = telefonoProfesor,
            onDniChange = { dniProfesor = it },
            onNombreChange = { nombreProfesor = it },
            onApellidosChange = { apellidosProfesor = it },
            onEmailChange = { emailProfesor = it },
            onTelefonoChange = { telefonoProfesor = it },
            onConfirmar = {
                viewModel.actualizarProfesor(
                    dni = dniProfesor,
                    nombre = nombreProfesor,
                    apellidos = apellidosProfesor,
                    email = emailProfesor,
                    telefono = telefonoProfesor
                )
            },
            onCancelar = { viewModel.ocultarDialogoEditProfesor() },
            onEliminar = { viewModel.mostrarDialogoDeleteConfirm() },
            mostrarBotonEliminar = true,
            dniReadOnly = true
        )
    }
    
    // Diálogo de confirmación para eliminar profesor
    if (uiState.showDeleteConfirmDialog && uiState.selectedProfesor != null) {
        AlertDialog(
            onDismissRequest = { viewModel.ocultarDialogoDeleteConfirm() },
            title = { Text("Confirmar eliminación") },
            text = { 
                Text(
                    text = "¿Estás seguro de que deseas eliminar a ${uiState.selectedProfesor!!.nombre} ${uiState.selectedProfesor!!.apellidos}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarProfesor(uiState.selectedProfesor!!.dni)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.ocultarDialogoDeleteConfirm() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente que representa un profesor en la lista.
 * 
 * Este componente implementa la visualización de un profesor individual,
 * mostrando su información básica y permitiendo acceder a más detalles.
 * 
 * ## Características
 * - Datos básicos del profesor
 * - Acceso a edición
 * - Indicadores de estado
 * - Diseño Material Design 3
 * 
 * @param profesor Datos del profesor a mostrar
 * @param onProfesorClick Callback para gestionar el click
 * 
 * @see Usuario
 */
@Composable
fun ProfesorItemSimple(
    profesor: Usuario,
    onProfesorClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfesorClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profesor.nombre.firstOrNull()?.toString() ?: "P",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Información del profesor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "${profesor.nombre} ${profesor.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = profesor.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "DNI: ${profesor.dni}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono de navegación
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Diálogo de detalle del profesor
 */
@Composable
fun DetalleProfesorDialog(
    profesor: Usuario,
    clasesAsignadas: List<Clase>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAsignarClases: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado con título y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles del profesor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Avatar o iniciales
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profesor.nombre.firstOrNull()?.toString() ?: "P",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre completo
                Text(
                    text = "${profesor.nombre} ${profesor.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Información de contacto
                Spacer(modifier = Modifier.height(8.dp))
                
                DetailItem(label = "DNI", value = profesor.dni)
                DetailItem(label = "Email", value = profesor.email)
                DetailItem(label = "Teléfono", value = profesor.telefono)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Clases asignadas
                Text(
                    text = "Clases asignadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (clasesAsignadas.isEmpty()) {
                    Text(
                        text = "No tiene clases asignadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 120.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        clasesAsignadas.forEach { clase ->
                            Text(
                                text = clase.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón Editar
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    
                    // Botón Eliminar
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón para asignar clases
                Button(
                    onClick = onAsignarClases,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Asignar clases")
                }
            }
        }
    }
}

/**
 * Elemento para mostrar una etiqueta y su valor
 */
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Diálogo para el formulario de profesor
 */
@Composable
fun ProfesorFormDialog(
    titulo: String,
    dni: String,
    nombre: String,
    apellidos: String,
    email: String,
    telefono: String,
    onDniChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onEliminar: (() -> Unit)? = null,
    mostrarBotonEliminar: Boolean = false,
    dniReadOnly: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = dni,
                    onValueChange = onDniChange,
                    label = { Text("DNI") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !dniReadOnly,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = onApellidosChange,
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = onTelefonoChange,
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = nombre.isNotEmpty() && apellidos.isNotEmpty() && dni.isNotEmpty() && email.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (mostrarBotonEliminar && onEliminar != null) {
                    Button(
                        onClick = onEliminar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Eliminar")
                    }
                }
                
                OutlinedButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
            }
        }
    )
}

/**
 * Diálogo para asignar clases a un profesor
 */
@Composable
fun AsignarClasesDialog(
    clases: List<Clase>,
    clasesAsignadas: List<Clase>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val clasesSeleccionadasIds = remember { 
        mutableStateListOf<String>().apply {
            addAll(clasesAsignadas.map { it.id })
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Encabezado con título y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asignar clases",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (clases.isEmpty()) {
                    Text(
                        text = "No hay clases disponibles para asignar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Selecciona las clases a asignar:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Lista de clases con checkbox
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 250.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        clases.forEach { clase ->
                            val isSelected = clasesSeleccionadasIds.contains(clase.id)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (isSelected) {
                                            clasesSeleccionadasIds.remove(clase.id)
                                        } else {
                                            clasesSeleccionadasIds.add(clase.id)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            clasesSeleccionadasIds.add(clase.id)
                                        } else {
                                            clasesSeleccionadasIds.remove(clase.id)
                                        }
                                    }
                                )
                                
                                Column(
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = clase.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (clase.aula.isNotEmpty()) {
                                        Text(
                                            text = "Aula: ${clase.aula}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón Cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    // Botón Confirmar
                    Button(
                        onClick = { onConfirm(clasesSeleccionadasIds.toList()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

/**
 * Vista previa de la pantalla de gestión de profesores en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun GestionProfesoresScreenPreview() {
    UmeEguneroTheme {
        GestionProfesoresScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla de gestión de profesores en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun GestionProfesoresScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        GestionProfesoresScreen(
            navController = rememberNavController()
        )
    }
} 