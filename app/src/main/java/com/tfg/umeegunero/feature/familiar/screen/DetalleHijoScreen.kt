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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleHijoViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.tfg.umeegunero.util.formatDate
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Clases de datos para el preview
data class AlumnoDetalleModel(
    val dni: String,
    val nombre: String,
    val apellidos: String,
    val fechaNacimiento: String,
    val centroId: String,
    val centroNombre: String,
    val clase: String,
    val profesores: List<ProfesorSimple>,
    val direccion: String,
    val alergias: List<String>,
    val observaciones: String
)

data class ProfesorSimple(
    val id: String,
    val nombre: String,
    val asignaturas: List<String>
)

/**
 * Pantalla de detalle extendido de un hijo para familiares
 *
 * Muestra información ampliada del alumno seleccionado.
 *
 * @param navController Controlador de navegación para volver atrás
 * @param alumnoId ID del alumno a mostrar
 *
 * @author Equipo UmeEgunero
 * @version 4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHijoScreen(
    navController: NavController,
    alumnoId: String,
    viewModel: DetalleHijoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String?) -> Unit = { _, _ -> },
    onNavigateToRegistros: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Extraer el alumno para poder usarlo de forma segura
    val alumno = uiState.alumno

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
                        text = "Detalle del Alumno",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
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
            } else if (alumno == null) {
                // Mostrar mensaje de error si no hay alumno
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
                            text = "No se encontró al alumno",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                // Ahora alumno no es nulo, podemos usarlo directamente
                // Mostrar detalles del alumno
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Avatar e información básica
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = alumno.nombre.first().toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre completo
                            Text(
                                text = "${alumno.nombre} ${alumno.apellidos}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fecha de nacimiento
                            val fechaFormateada = if (alumno.fechaNacimiento != null && alumno.fechaNacimiento.isNotEmpty()) {
                                alumno.fechaNacimiento
                            } else {
                                "Fecha no disponible"
                            }
                            Text(
                                text = "Fecha de nacimiento: $fechaFormateada",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // Edad aproximada
                            Text(
                                text = "Edad: ${calcularEdad(alumno.fechaNacimiento)} años",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Información del centro educativo
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
                            Text(
                                text = "Centro Educativo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Información del centro
                            InfoRow(
                                icon = Icons.Default.School,
                                title = "Centro:",
                                value = uiState.centroNombre ?: alumno.centroId
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Información del aula
                            InfoRow(
                                icon = Icons.Default.Home,
                                title = "Aula:",
                                value = alumno.claseId
                            )

                            if (uiState.profesores.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Profesores",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                uiState.profesores.forEach { profesor ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${profesor.nombre} ${profesor.apellidos}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = { onNavigateToChat(profesor.dni, alumno.dni) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Message,
                                                contentDescription = "Enviar mensaje",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (profesor != uiState.profesores.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }

                    // Información médica y alergias
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
                            Text(
                                text = "Información Médica",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Alergias
                            if (alumno.alergias.isNotEmpty()) {
                                Text(
                                    text = "Alergias: ${alumno.alergias.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Medicación
                            if (alumno.medicacion.isNotEmpty()) {
                                Text(
                                    text = "Medicación: ${alumno.medicacion.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Observaciones médicas
                            Text(
                                text = "Observaciones médicas:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alumno.observacionesMedicas.takeIf { it.isNotEmpty() }
                                    ?: "No se han registrado observaciones médicas",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Información de contacto
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
                            Text(
                                text = "Información de Contacto",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Persona de contacto
                            uiState.familiar?.let { familiar ->
                                InfoRow(
                                    icon = Icons.Default.Person,
                                    title = "Contacto principal:",
                                    value = "${familiar.nombre} ${familiar.apellidos}"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                InfoRow(
                                    icon = Icons.Default.Phone,
                                    title = "Teléfono:",
                                    value = familiar.telefono
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                InfoRow(
                                    icon = Icons.Default.Email,
                                    title = "Email:",
                                    value = familiar.email
                                )
                            } ?: run {
                                Text(
                                    text = "No se ha encontrado información de contacto",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onNavigateToRegistros(alumno.dni) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Ver registros")
                        }

                        Button(
                            onClick = { /* Acción para editar información */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Editar información")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
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
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Calcula la edad a partir de la fecha de nacimiento
 */
private fun calcularEdad(fechaNacimiento: String?): Int {
    if (fechaNacimiento.isNullOrEmpty()) return 0

    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaNac = formato.parse(fechaNacimiento)
        val hoy = Calendar.getInstance()
        val fechaNacCal = Calendar.getInstance().apply {
            time = fechaNac ?: return 0
        }

        var edad = hoy.get(Calendar.YEAR) - fechaNacCal.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacCal.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        
        edad
    } catch (e: Exception) {
        0 // En caso de error, devuelve 0
    }
}

/**
 * Vista previa de la pantalla de detalle de hijo para familiares
 */
@Preview(showBackground = true)
@Composable
fun DetalleHijoScreenPreview() {
    DetalleHijoScreenPreviewContent()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetalleHijoScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        DetalleHijoScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHijoScreenPreviewContent() {
    // Mock de datos para el preview
    val alumnoMock = AlumnoDetalleModel(
        dni = "12345678X",
        nombre = "Martín",
        apellidos = "García López",
        fechaNacimiento = "01/01/2010",
        centroId = "centro1",
        centroNombre = "Colegio San José",
        clase = "4º de Primaria",
        profesores = listOf(
            ProfesorSimple(
                id = "prof1",
                nombre = "Ana Martínez",
                asignaturas = listOf("Matemáticas", "Ciencias")
            ),
            ProfesorSimple(
                id = "prof2",
                nombre = "Luis Fernández",
                asignaturas = listOf("Lengua", "Historia")
            )
        ),
        direccion = "Calle Principal 23, Bilbao",
        alergias = listOf("Nueces", "Pescado"),
        observaciones = "Es un niño muy activo y participativo en clase"
    )
    // UI simplificada para preview visual
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${alumnoMock.nombre} ${alumnoMock.apellidos}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información básica
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Foto del alumno
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Nombre y apellidos
                    Text(
                        text = "${alumnoMock.nombre} ${alumnoMock.apellidos}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // DNI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "DNI: ${alumnoMock.dni}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Edad y fecha de nacimiento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val fechaNacimientoStr = alumnoMock.fechaNacimiento.takeIf { it.isNotEmpty() } ?: "01/01/2010"
                        val edad = 4 // Mock fijo para preview
                        Text(
                            text = "$edad años · Nacimiento: $fechaNacimientoStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}