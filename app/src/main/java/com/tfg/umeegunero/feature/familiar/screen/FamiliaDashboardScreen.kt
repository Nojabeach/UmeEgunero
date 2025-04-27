package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.StatsOverviewCard
import com.tfg.umeegunero.ui.components.StatItem
import com.tfg.umeegunero.ui.components.StatsOverviewRow
import com.tfg.umeegunero.ui.components.charts.LineChart
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import android.content.res.Configuration
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.ui.components.CategoriaCardData
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCardBienvenida
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.foundation.lazy.grid.GridItemSpan


/**
 * Clase que representa una estadística para mostrar en el dashboard
 */
data class Estadistica(
    val value: String,
    val etiqueta: String
)

/**
 * Pantalla principal del dashboard para familiares
 * 
 * Esta pantalla muestra toda la información relevante para los familiares de los alumnos:
 * - Lista de hijos asociados a la cuenta
 * - Resumen de registros diarios recientes
 * - Métricas de actividad y progreso
 * - Acceso rápido a funcionalidades principales
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona los datos del dashboard
 * 
 * @author Equipo UmeEgunero
 * @version 4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaDashboardScreen(
    navController: NavController,
    viewModel: FamiliarDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Si ocurrió un error, mostramos un Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Variables para control de animaciones
    var showContent by remember { mutableStateOf(false) }
    
    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
        // Cargar datos del familiar al inicio
        viewModel.cargarDatosFamiliar()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Observamos si debemos navegar a la pantalla de welcome
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.FamiliarDashboard.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Dashboard Familiar",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de mensajes con badge para notificaciones
                    BadgedBox(
                        badge = {
                            if (uiState.totalMensajesNoLeidos > 0) {
                                Badge { Text(uiState.totalMensajesNoLeidos.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = {
                            try {
                                navController.navigate(AppScreens.ConversacionesFamilia.route)
                            } catch (e: Exception) {
                                // Nunca cerrar la app, solo mostrar error si ocurre
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Mensajes"
                            )
                        }
                    }
                    // Icono de perfil
                    IconButton(onClick = { navController.navigate(AppScreens.Perfil.route) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    }
                    // Cerrar sesión
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Mostrar un indicador de carga mientras se cargan los datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FamiliarColor)
            }
        } else {
            // Mostrar el contenido principal con animación cuando los datos estén cargados
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut()
            ) {
                val cards = listOf(
                    CategoriaCardData("Mensajes", "Consulta y responde mensajes de la escuela", Icons.AutoMirrored.Filled.Chat, MaterialTheme.colorScheme.primary, onClick = { navController.navigate(AppScreens.ConversacionesFamilia.route) }),
                    CategoriaCardData("Calendario", "Revisa eventos y fechas importantes", Icons.Default.CalendarMonth, MaterialTheme.colorScheme.tertiary, onClick = { navController.navigate(AppScreens.CalendarioFamilia.route) }),
                    CategoriaCardData("Historial", "Consulta el registro diario de tu hijo/a", Icons.AutoMirrored.Filled.Assignment, MaterialTheme.colorScheme.secondary, onClick = {
                        if (uiState.hijoSeleccionado != null) {
                            val alumnoId = uiState.hijoSeleccionado!!.dni
                            val alumnoNombre = uiState.hijoSeleccionado!!.nombre
                            navController.navigate(
                                AppScreens.ConsultaRegistroDiario.createRoute(
                                    alumnoId = alumnoId,
                                    alumnoNombre = alumnoNombre
                                )
                            )
                        }
                    }),
                    CategoriaCardData("Comunicados", "Lee los comunicados del centro", Icons.AutoMirrored.Filled.Announcement, MaterialTheme.colorScheme.primary, onClick = { navController.navigate(AppScreens.ComunicadosFamilia.route) }),
                    CategoriaCardData("Tareas", "Revisa y entrega tareas de tu hijo/a", Icons.AutoMirrored.Filled.Assignment, MaterialTheme.colorScheme.tertiary, onClick = { navController.navigate(AppScreens.TareasFamilia.route) }),
                    CategoriaCardData("Actividades", "Consulta y participa en actividades escolares", Icons.Default.PlayCircle, MaterialTheme.colorScheme.secondary, onClick = { navController.navigate(AppScreens.ActividadesPreescolar.route) })
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header de bienvenida
                    item(span = { GridItemSpan(2) }) {
                        CategoriaCardBienvenida(
                            data = CategoriaCardData(
                                titulo = "¡Hola, ${uiState.familiar?.nombre ?: "Familiar"}!",
                                descripcion = "Familia de ${uiState.hijoSeleccionado?.nombre ?: "Alumno"}\nCentro Infantil UmeEgunero\nÚltima actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                                icono = Icons.Default.ChildCare,
                                color = FamiliarColor,
                                onClick = {},
                                iconTint = Color.White,
                                border = true
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp, max = 100.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Información de bienvenida
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "¡Hola, ${uiState.familiar?.nombre ?: "Familiar"}}!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Familia de ${uiState.hijoSeleccionado?.nombre ?: "Alumno"}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Centro Infantil UmeEgunero",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(FamiliarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChildCare,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                    // Separador visual
                    item(span = { GridItemSpan(2) }) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    // Título de sección
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Accesos Rápidos",
                            style = MaterialTheme.typography.titleLarge,
                            color = FamiliarColor
                        )
                    }
                    // Cards de accesos rápidos
                    items(cards.size) { index ->
                        val card = cards[index]
                        CategoriaCard(
                            titulo = card.titulo,
                            descripcion = card.descripcion,
                            icono = card.icono,
                            color = FamiliarColor,
                            iconTint = card.iconTint,
                            border = true,
                            onClick = card.onClick,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    // Separador visual
                    item(span = { GridItemSpan(2) }) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    // Tarjeta de resumen de actividad diaria
                    if (uiState.hijoSeleccionado != null) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Resumen de Actividad",
                                style = MaterialTheme.typography.titleLarge,
                                color = FamiliarColor
                            )
                        }
                        item(span = { GridItemSpan(2) }) {
                            ResumenActividadCard(
                                alumno = uiState.hijoSeleccionado!!,
                                registrosActividad = uiState.registrosActividad,
                                onVerDetalles = { registroId ->
                                    navController.navigate(
                                        "detalle_registro/$registroId"
                                    )
                                }
                            )
                        }
                    }
                    // Separador visual
                    item(span = { GridItemSpan(2) }) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    // Notificaciones y comunicados recientes
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleLarge,
                            color = FamiliarColor
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        NotificacionesCard(
                            totalNoLeidas = uiState.registrosSinLeer,
                            onVerTodas = { navController.navigate(AppScreens.NotificacionesFamilia.route) }
                        )
                    }
                    // Espaciador final para scroll
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FamiliarColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WelcomeCard(nombreFamiliar: String, nombreHijo: String, modifier: Modifier = Modifier, onConfigClick: () -> Unit = {}) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onConfigClick),
        colors = CardDefaults.cardColors(
            containerColor = FamiliarColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información de bienvenida
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "¡Hola, $nombreFamiliar!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Familia de $nombreHijo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Centro Infantil UmeEgunero",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FamiliarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun HijosSelector(
    hijos: List<Alumno>,
    hijoSeleccionado: Alumno?,
    onHijoSelected: (Alumno) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Selecciona un hijo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(hijos) { hijo ->
                    val isSelected = hijo.dni == hijoSeleccionado?.dni
                    
                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .height(140.dp)
                            .clickable { onHijoSelected(hijo) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                FamiliarColor.copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Avatar/Inicial del alumno
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) FamiliarColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hijo.nombre.first().toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = hijo.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onVerMensajes: () -> Unit,
    onVerCalendario: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerComunicados: () -> Unit,
    onVerTareas: () -> Unit,
    onVerActividades: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
        modifier = Modifier.height(240.dp)
    ) {
        item {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                text = "Mensajes",
                onClick = onVerMensajes
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.CalendarMonth,
                text = "Calendario",
                onClick = onVerCalendario
            )
        }
        
        item {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Assignment,
                text = "Historial",
                onClick = onVerHistorial
            )
        }
        
        item {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Announcement,
                text = "Comunicados",
                onClick = onVerComunicados
            )
        }
        
        item {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Assignment,
                text = "Tareas",
                onClick = onVerTareas
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.PlayCircle,
                text = "Actividades",
                onClick = onVerActividades
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FamiliarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ResumenActividadCard(
    alumno: Alumno,
    registrosActividad: List<RegistroActividad>,
    onVerDetalles: (String) -> Unit
) {
    val ultimoRegistro = registrosActividad.maxByOrNull { it.fecha }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ultimoRegistro != null) {
                    val fecha = ultimoRegistro.fecha
                    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(fecha.seconds * 1000))
                    
                    Text(
                        text = "Último registro: $fechaFormateada",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = { 
                            onVerDetalles(ultimoRegistro.id)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FamiliarColor
                        )
                    ) {
                        Text("Ver detalle")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (ultimoRegistro != null) {
                // Mostrar información básica del registro en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActividadInfoItem(
                        icon = Icons.Default.Restaurant,
                        title = "Alimentación",
                        value = when {
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("COMPLETO") -> "Completa"
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("PARCIAL") -> "Parcial"
                            else -> "No servido"
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bedtime,
                        title = "Siesta",
                        value = if (ultimoRegistro.siesta?.duracion ?: 0 > 0) 
                            "${ultimoRegistro.siesta?.duracion} min" 
                        else 
                            "No"
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bathroom,
                        title = "Necesidades",
                        value = if (ultimoRegistro.necesidadesFisiologicas.caca) "Sí" else "No"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Observaciones
                if (!ultimoRegistro.observaciones.isNullOrBlank()) {
                    Text(
                        text = "Observaciones:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = ultimoRegistro.observaciones ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No hay observaciones para este registro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // No hay registros disponibles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros de actividad disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = FamiliarColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NotificacionesCard(
    totalNoLeidas: Int,
    onVerTodas: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalNoLeidas > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = totalNoLeidas.toString(),
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = if (totalNoLeidas > 0) 
                            "Tienes $totalNoLeidas ${if (totalNoLeidas == 1) "registro" else "registros"} sin leer"
                        else
                            "No hay notificaciones pendientes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (totalNoLeidas > 0) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onVerTodas,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FamiliarColor
                )
            ) {
                Text("Ver todas")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FamiliaDashboardScreenPreview() {
    UmeEguneroTheme {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FamiliaDashboardScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
}

