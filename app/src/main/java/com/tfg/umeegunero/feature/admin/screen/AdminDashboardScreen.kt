package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.tfg.umeegunero.feature.admin.screen.components.SectionHeader
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.feature.admin.screen.components.BotonAccion
import com.tfg.umeegunero.ui.theme.AcademicoColorDark
import com.tfg.umeegunero.data.model.Usuario
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion

/**
 * Dashboard del administrador de la aplicación
 * 
 * Esta pantalla presenta un panel de control completo para la administración
 * general de la aplicación, incluyendo gestión de centros, reportes, configuración
 * del sistema y comunicaciones.
 *
 * @param viewModel ViewModel que contiene la lógica de negocio del dashboard de administración
 * @param onNavigateToGestionUsuarios Callback para navegar a la gestión de usuarios
 * @param onNavigateToGestionCentros Callback para navegar a la gestión de centros
 * @param onNavigateToEstadisticas Callback para navegar a estadísticas
 * @param onNavigateToSeguridad Callback para navegar a seguridad
 * @param onNavigateToTema Callback para navegar a configuración de tema
 * @param onNavigateToEmailConfig Callback para navegar a configuración de email
 * @param onNavigateToNotificaciones Callback para navegar a notificaciones
 * @param onNavigateToComunicados Callback para navegar a comunicados
 * @param onNavigateToBandejaEntrada Callback para navegar a bandeja de entrada
 * @param onNavigateToComponerMensaje Callback para navegar a componer mensaje
 * @param onNavigateToSoporteTecnico Callback para navegar a soporte técnico
 * @param onNavigateToFAQ Callback para navegar a FAQ
 * @param onNavigateToTerminos Callback para navegar a términos y condiciones
 * @param onNavigateToLogout Callback para cerrar sesión
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateToGestionUsuarios: () -> Unit,
    onNavigateToGestionCentros: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onNavigateToSeguridad: () -> Unit,
    onNavigateToTema: () -> Unit,
    onNavigateToEmailConfig: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToComunicados: () -> Unit,
    onNavigateToBandejaEntrada: () -> Unit,
    onNavigateToComponerMensaje: () -> Unit,
    onNavigateToSoporteTecnico: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToTerminos: () -> Unit,
    onNavigateToLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showEmailConfigDialog by remember { mutableStateOf(false) }
    var showNotificacionesDialog by remember { mutableStateOf(false) }
    var showComunicadosDialog by remember { mutableStateOf(false) }
    var showBandejaEntradaDialog by remember { mutableStateOf(false) }
    var showComponerMensajeDialog by remember { mutableStateOf(false) }
    var showSoporteTecnicoDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showTerminosDialog by remember { mutableStateOf(false) }

    // Diálogos
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onNavigateToLogout()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cambiar tema") },
            text = { Text("¿Quieres cambiar el tema de la aplicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showThemeDialog = false
                        onNavigateToTema()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showEmailConfigDialog) {
        AlertDialog(
            onDismissRequest = { showEmailConfigDialog = false },
            title = { Text("Configurar email") },
            text = { Text("¿Quieres configurar el email de soporte?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmailConfigDialog = false
                        onNavigateToEmailConfig()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailConfigDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showNotificacionesDialog) {
        AlertDialog(
            onDismissRequest = { showNotificacionesDialog = false },
            title = { Text("Notificaciones") },
            text = { Text("¿Quieres gestionar las notificaciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificacionesDialog = false
                        onNavigateToNotificaciones()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificacionesDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showComunicadosDialog) {
        AlertDialog(
            onDismissRequest = { showComunicadosDialog = false },
            title = { Text("Comunicados") },
            text = { Text("¿Quieres ver los comunicados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComunicadosDialog = false
                        onNavigateToComunicados()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComunicadosDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showBandejaEntradaDialog) {
        AlertDialog(
            onDismissRequest = { showBandejaEntradaDialog = false },
            title = { Text("Bandeja de entrada") },
            text = { Text("¿Quieres ver tu bandeja de entrada?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBandejaEntradaDialog = false
                        onNavigateToBandejaEntrada()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBandejaEntradaDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showComponerMensajeDialog) {
        AlertDialog(
            onDismissRequest = { showComponerMensajeDialog = false },
            title = { Text("Nuevo mensaje") },
            text = { Text("¿Quieres componer un nuevo mensaje?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComponerMensajeDialog = false
                        onNavigateToComponerMensaje()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComponerMensajeDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showSoporteTecnicoDialog) {
        AlertDialog(
            onDismissRequest = { showSoporteTecnicoDialog = false },
            title = { Text("Soporte técnico") },
            text = { Text("¿Quieres acceder al soporte técnico?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSoporteTecnicoDialog = false
                        onNavigateToSoporteTecnico()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSoporteTecnicoDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showFAQDialog) {
        AlertDialog(
            onDismissRequest = { showFAQDialog = false },
            title = { Text("Preguntas frecuentes") },
            text = { Text("¿Quieres ver las preguntas frecuentes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFAQDialog = false
                        onNavigateToFAQ()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFAQDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showTerminosDialog) {
        AlertDialog(
            onDismissRequest = { showTerminosDialog = false },
            title = { Text("Términos y condiciones") },
            text = { Text("¿Quieres ver los términos y condiciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTerminosDialog = false
                        onNavigateToTerminos()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminosDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de administración") },
                actions = {
                    IconButton(onClick = { /* Navegar a perfil */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Editar perfil")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = AdminColor.copy(alpha = 0.1f)
    ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
        } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de bienvenida
                    item {
                        WelcomeCardV2(
                            nombre = uiState.currentUser?.nombre,
                            fecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
                        )
                    }

                    // Sección: Gestión Académica
                    item {
                        Text(
                            text = "Gestión Académica",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Primera fila: Centros y Usuarios
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Centros",
                                icono = Icons.Default.School,
                                descripcion = "Gestionar centros educativos",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToGestionCentros,
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Usuarios",
                                icono = Icons.Default.People,
                                descripcion = "Gestionar todos los perfiles de usuario",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToGestionUsuarios,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Segunda fila: Estadísticas
                    item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                            CategoriaCard(
                                titulo = "Estadísticas",
                                icono = Icons.Default.BarChart,
                                descripcion = "Ver estadísticas generales",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToEstadisticas,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Sección: Configuración
                    item {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    // Primera fila: Seguridad y Tema
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Seguridad",
                                icono = Icons.Default.Security,
                                descripcion = "Configurar aspectos de seguridad",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToSeguridad,
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Tema",
                                icono = Icons.Default.Palette,
                                descripcion = "Cambiar tema de la aplicación",
                                color = AdminColor,
                                onClick = { showThemeDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Segunda fila: Email y Notificaciones
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Email",
                                icono = Icons.Default.Email,
                                descripcion = "Configurar email de soporte",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { showEmailConfigDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Notificaciones",
                                icono = Icons.Default.Notifications,
                                descripcion = "Gestionar notificaciones",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { showNotificacionesDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sección: Comunicación y Soporte
                    item {
                        Text(
                            text = "Comunicación y Soporte",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    // Primera fila: Comunicados y Soporte técnico
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Comunicados",
                                icono = Icons.Default.Campaign,
                                descripcion = "Ver y gestionar comunicados",
                                color = AdminColor,
                                onClick = { showComunicadosDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Soporte técnico",
                                icono = Icons.Default.Help,
                                descripcion = "Acceder al soporte técnico",
                                color = AdminColor,
                                onClick = { showSoporteTecnicoDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Segunda fila: FAQ y Términos
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "FAQ",
                                icono = Icons.Default.QuestionAnswer,
                                descripcion = "Ver preguntas frecuentes",
                                color = AdminColor,
                                onClick = { showFAQDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Términos",
                                icono = Icons.Default.Description,
                                descripcion = "Ver términos y condiciones",
                                color = AdminColor,
                                onClick = { showTerminosDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grid de acciones rápidas para dashboards, usando el nuevo diseño compacto.
 */
@Composable
fun GridAccionesRapidas(
    onGestionCentros: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onConfiguracion: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BotonAccion(
            icono = Icons.Default.Business,
            texto = "Centros",
                onClick = onGestionCentros
            )
        BotonAccion(
            icono = Icons.Default.People,
            texto = "Usuarios",
                onClick = onGestionUsuarios
            )
        BotonAccion(
            icono = Icons.Default.Security,
            texto = "Configuración",
                onClick = onConfiguracion
            )
    }
}

/**
 * Tarjeta de bienvenida mejorada para el dashboard de administración
 */
@Composable
private fun WelcomeCardV2(
    nombre: String?,
    fecha: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!nombre.isNullOrBlank()) {
                Text(
                        text = "Bienvenido/a, $nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                    Text(
                        text = "Panel de administración",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                Text(
                        text = "Panel de administración",
                        style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                }
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AcademicoColorDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * ViewModel para la pantalla de administrador
 */
class AdminDashboardViewModel : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState
    
    fun logout() {
        // Implementación básica para compilar
    }
}

/**
 * Estado de la UI para la pantalla de administrador
 */
data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showListadoCentros: Boolean = false,
    val currentUser: Usuario? = null
)

/**
 * Previsualización profesional del dashboard de administrador.
 */
@Preview(showBackground = true)
@Composable
fun VistaPreviaDashboardAdmin() {
    UmeEguneroTheme {
        AdminDashboardScreen(
            onNavigateToGestionUsuarios = {},
            onNavigateToGestionCentros = {},
            onNavigateToEstadisticas = {},
            onNavigateToSeguridad = {},
            onNavigateToTema = {},
            onNavigateToEmailConfig = {},
            onNavigateToNotificaciones = {},
            onNavigateToComunicados = {},
            onNavigateToBandejaEntrada = {},
            onNavigateToComponerMensaje = {},
            onNavigateToSoporteTecnico = {},
            onNavigateToFAQ = {},
            onNavigateToTerminos = {},
            onNavigateToLogout = {}
        )
    }
}