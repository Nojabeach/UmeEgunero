package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.feature.profesor.viewmodel.StudentDetailViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.StudentDetailUiState
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla que muestra el detalle de un alumno
 * Permite visualizar toda la información relevante del alumno seleccionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    navController: NavController,
    alumnoId: String,
    viewModel: StudentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para cargar los datos del alumno al inicio
    LaunchedEffect(alumnoId) {
        viewModel.loadAlumno(alumnoId)
    }
    
    // Mostrar error en Snackbar si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Alumno") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
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
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(message = "Cargando información del alumno...")
            }
        } else if (uiState.alumno == null) {
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
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Alumno no encontrado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volver")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Información básica del alumno
                StudentHeader(alumno = uiState.alumno!!)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Información académica
                SectionTitle(title = "Información Académica")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow(
                    icon = Icons.Default.Class,
                    label = "Curso",
                    value = uiState.alumno!!.curso
                )
                
                InfoRow(
                    icon = Icons.Default.Groups,
                    label = "Clase",
                    value = uiState.alumno!!.clase
                )
                
                // Información médica y alergias
                if (uiState.alumno!!.alergias.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SectionTitle(title = "Alergias")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.alumno!!.alergias.forEach { alergia ->
                        ListItem(text = alergia)
                    }
                }
                
                // Información de medicación
                if (uiState.alumno!!.medicacion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SectionTitle(title = "Medicación")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.alumno!!.medicacion.forEach { med ->
                        ListItem(text = med)
                    }
                }
                
                // Observaciones médicas
                if (uiState.alumno!!.observacionesMedicas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SectionTitle(title = "Observaciones Médicas")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = uiState.alumno!!.observacionesMedicas,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Información de familiares
                if (uiState.alumno!!.familiares.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SectionTitle(title = "Familiares")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.alumno!!.familiares.forEach { familiar ->
                        FamiliarItem(familiar = familiar)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ListItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun FamiliarItem(familiar: Familiar) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "${familiar.nombre} ${familiar.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = familiar.parentesco,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StudentHeader(alumno: Alumno) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar (representado como un círculo con la inicial)
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = alumno.nombre.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nombre completo
        Text(
            text = "${alumno.nombre} ${alumno.apellidos}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // DNI
        Text(
            text = alumno.dni,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDetailScreenPreview() {
    UmeEguneroTheme {
        val mockAlumno = Alumno(
            id = "1",
            dni = "12345678A",
            nombre = "Carlos",
            apellidos = "Martínez López",
            email = "carlos@example.com",
            telefono = "666777888",
            fechaNacimiento = "12/05/2010",
            curso = "3º ESO",
            clase = "3ºB",
            alergias = listOf("Frutos secos", "Lactosa"),
            medicacion = listOf("Antihistamínico (si es necesario)"),
            observacionesMedicas = "Llevar siempre el inyector de adrenalina en la mochila.",
            familiares = listOf(
                Familiar("1", "Ana", "López García", "Madre"),
                Familiar("2", "Pedro", "Martínez Sánchez", "Padre")
            )
        )
        
        val mockViewModel = object : StudentDetailViewModel() {
            init {
                // Inicializamos el estado manualmente para la preview
                setStateForPreview(
                    StudentDetailUiState(
                        isLoading = false,
                        alumno = mockAlumno,
                        error = null
                    )
                )
            }
            
            override fun loadAlumno(alumnoId: String) {
                // No hacemos nada, solo usamos un estado predefinido
            }
        }
        
        StudentDetailScreen(
            navController = rememberNavController(),
            alumnoId = "1",
            viewModel = mockViewModel
        )
    }
} 