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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHijoScreen(
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
                            imageVector = Icons.Default.ArrowBack,
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
                            alumno.fechaNacimiento?.let { fecha ->
                                Text(
                                    text = "Fecha de nacimiento: ${formatDate(fecha)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                // Edad aproximada
                                Text(
                                    text = "Edad: ${calcularEdad(fecha)} años",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    // Información del centro educativo
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
                                value = alumno.aulaId
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
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Información médica y alergias
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
                                text = "Información Médica",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Alergias
                            Text(
                                text = "Alergias:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (alumno.alergias?.isNotEmpty() == true)
                                    alumno.alergias.joinToString(", ")
                                else
                                    "No se han registrado alergias",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Medicación
                            Text(
                                text = "Medicación:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alumno.medicacion?.takeIf { it.isNotBlank() }
                                    ?: "No se ha registrado medicación",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Observaciones médicas
                            Text(
                                text = "Observaciones médicas:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alumno.observacionesMedicas?.takeIf { it.isNotBlank() }
                                    ?: "No se han registrado observaciones médicas",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Información de contacto
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

fun formatDate(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
}

fun calcularEdad(fechaNacimiento: Timestamp): Int {
    val fechaNac = fechaNacimiento.toDate()
    val today = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    calendar.time = fechaNac

    var edad = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)

    if (today.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)) {
        edad--
    }

    return edad
}

@Preview(showBackground = true)
@Composable
fun DetalleHijoScreenPreview() {
    UmeEguneroTheme {
        DetalleHijoScreen(
            onNavigateBack = {},
            viewModel = TODO(),
            onNavigateToChat = TODO(),
            onNavigateToRegistros = TODO()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetalleHijoScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        DetalleHijoScreen(
            onNavigateBack = {},
            viewModel = TODO(),
            onNavigateToChat = TODO(),
            onNavigateToRegistros = TODO()
        )
    }
}