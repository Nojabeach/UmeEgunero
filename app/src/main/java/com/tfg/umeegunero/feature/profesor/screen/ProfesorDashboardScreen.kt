package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.feature.common.config.components.TemaSelector
import com.tfg.umeegunero.feature.common.config.components.TemaActual
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToRegistroActividad: (String) -> Unit = {},
    onNavigateToDetalleAlumno: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    alumnosPendientes: List<Alumno> = emptyList(),
    alumnos: List<Alumno> = emptyList(),
    mensajesNoLeidos: List<Triple<String, String, Boolean>> = emptyList(),
    totalMensajesNoLeidos: Int = 0,
    isLoading: Boolean = false,
    error: String? = null,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onCrearRegistroActividad: (String) -> Unit = {},
    onErrorDismissed: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Mostrar error si existe
    error?.let {
        // Aquí podrías mostrar un Snackbar o un diálogo con el error
        // y llamar a onErrorDismissed cuando se cierre
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Imagen de perfil del profesor
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Laura Martínez",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Profesora • Aula 2B",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                // Elementos del menú
                val menuItems = listOf(
                    "Inicio" to Icons.Default.Home,
                    "Mis Alumnos" to Icons.Default.Person,
                    "Historial" to Icons.Default.History,
                    "Mensajes" to Icons.AutoMirrored.Filled.Chat,
                    "Configuración" to Icons.Default.Settings
                )

                menuItems.forEachIndexed { index, (title, icon) ->
                    NavigationDrawerItem(
                        label = { Text(text = title) },
                        selected = selectedTab == index,
                        onClick = {
                            onTabSelected(index)
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider()

                // Botón de cerrar sesión
                NavigationDrawerItem(
                    label = { Text(text = "Cerrar Sesión") },
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (selectedTab) {
                                0 -> "Panel de Profesor"
                                1 -> "Mis Alumnos"
                                2 -> "Historial"
                                3 -> "Mensajes"
                                4 -> "Configuración"
                                else -> "Panel de Profesor"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Mostrar notificaciones */ }) {
                            BadgedBox(badge = { 
                                if (totalMensajesNoLeidos > 0) {
                                    Badge { Text(totalMensajesNoLeidos.toString()) }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF34C759), // Verde iOS para profesor
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        "Inicio" to Icons.Default.Home,
                        "Alumnos" to Icons.Default.Person,
                        "Actividad" to Icons.AutoMirrored.Filled.List,
                        "Mensajes" to Icons.AutoMirrored.Filled.Chat
                    )

                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (index == 3 && totalMensajesNoLeidos > 0) {
                                    BadgedBox(badge = { Badge { Text(totalMensajesNoLeidos.toString()) } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = title)
                                }
                            },
                            label = { Text(title) },
                            selected = selectedTab == index,
                            onClick = { onTabSelected(index) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == 0 && alumnosPendientes.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { 
                            // Crear nuevo registro para el primer alumno pendiente
                            alumnosPendientes.firstOrNull()?.let { alumno ->
                                onCrearRegistroActividad(alumno.dni)
                            }
                        },
                        containerColor = Color(0xFF34C759)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir registro",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            ProfesorDashboardContent(
                selectedTab = selectedTab,
                paddingValues = paddingValues,
                alumnosPendientes = alumnosPendientes,
                alumnos = alumnos,
                mensajesNoLeidos = mensajesNoLeidos,
                isLoading = isLoading,
                onNavigateToDetalleAlumno = onNavigateToDetalleAlumno,
                onNavigateToChat = onNavigateToChat,
                onCrearRegistroActividad = onCrearRegistroActividad
            )
        }
    }
}

@Composable
fun ProfesorDashboardContent(
    selectedTab: Int,
    paddingValues: PaddingValues,
    alumnosPendientes: List<Alumno> = emptyList(),
    alumnos: List<Alumno> = emptyList(),
    mensajesNoLeidos: List<Triple<String, String, Boolean>> = emptyList(),
    isLoading: Boolean = false,
    onNavigateToDetalleAlumno: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onCrearRegistroActividad: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedTab) {
            0 -> ProfesorHomeContent(
                alumnosPendientes = alumnosPendientes,
                onCrearRegistroActividad = onCrearRegistroActividad
            )
            1 -> MisAlumnosContent(
                alumnos = alumnos,
                onNavigateToDetalleAlumno = onNavigateToDetalleAlumno
            )
            2 -> HistorialContent()
            3 -> MensajesContent(
                mensajes = mensajesNoLeidos,
                onNavigateToChat = onNavigateToChat
            )
            4 -> ConfiguracionProfesorContent()
            else -> ProfesorHomeContent(
                alumnosPendientes = alumnosPendientes,
                onCrearRegistroActividad = onCrearRegistroActividad
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfesorHomeContent(
    alumnosPendientes: List<Alumno> = emptyList(),
    onCrearRegistroActividad: (String) -> Unit = {}
) {
    // TODO: Mejoras pendientes para la pantalla principal del profesor
    // - Implementar sistema de notificaciones importantes al inicio
    // - Añadir resumen de actividades pendientes del día
    // - Mostrar estadísticas de registros completados/pendientes
    // - Implementar acceso rápido a los alumnos más frecuentes
    // - Añadir widgets personalizables para información relevante
    // - Mostrar eventos o recordatorios del calendario escolar
    // - Implementar acceso a reuniones programadas con familiares
    // - Añadir sección de avisos o comunicados del centro
    // - Permitir visualización rápida de horario diario/semanal
    // - Mostrar indicadores de rendimiento del grupo/clase
    // - Implementar recordatorios de entrega de evaluaciones
    // - Añadir acceso a recursos didácticos personalizados
    // - Incluir resumen de comunicaciones pendientes con familias
    // - Mostrar alertas de situaciones especiales (alergias, medicación)
    
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")
    val formattedDate = remember { today.format(formatter).replaceFirstChar { it.uppercase() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado con fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de aula
        Text(
            text = "Tu aula: 2B - Educación Infantil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "15 alumnos a tu cargo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Estado del día
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Estado del día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusItem(
                        count = 15,
                        total = 15,
                        title = "Comidas",
                        icon = Icons.Default.Fastfood
                    )

                    StatusItem(
                        count = 10,
                        total = 15,
                        title = "Siestas",
                        icon = Icons.Default.Check
                    )

                    StatusItem(
                        count = 8,
                        total = 15,
                        title = "Informes",
                        icon = Icons.AutoMirrored.Filled.List
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de alumnos pendientes
        if (alumnosPendientes.isNotEmpty()) {
            Text(
                text = "Alumnos pendientes de registro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                items(alumnosPendientes) { alumno ->
                    AlumnoPendienteItem(
                        nombre = "${alumno.nombre} ${alumno.apellidos}",
                        onClick = { onCrearRegistroActividad(alumno.dni) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun StatusItem(
    count: Int,
    total: Int,
    title: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$count/$total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AlumnoPendienteItem(
    nombre: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear registro",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MisAlumnosContent(
    alumnos: List<Alumno> = emptyList(),
    onNavigateToDetalleAlumno: (String) -> Unit = {}
) {
    // TODO: Mejoras pendientes para la sección de alumnos
    // - Implementar filtrado por clase, edad o grupo
    // - Añadir búsqueda de alumnos por nombre
    // - Mostrar indicadores visuales de estado de asistencia (ausente, presente, etc.)
    // - Implementar vistas de asistencia diaria/semanal
    // - Añadir funcionalidad para generar informes individuales o grupales
    // - Permitir ordenación de la lista por diferentes criterios
    // - Implementar gestión de incidencias por alumno
    // - Añadir acceso directo a historial de cada alumno
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Alumnos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de alumnos
        if (alumnos.isEmpty()) {
            // Mostrar mensaje cuando no hay alumnos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alumnos asignados a tu clase",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(alumnos) { alumno ->
                    AlumnoListItem(
                        nombre = "${alumno.nombre} ${alumno.apellidos}",
                        onClick = { onNavigateToDetalleAlumno(alumno.dni) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun AlumnoListItem(
    nombre: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Aula 2B - 3 años",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistorialContent() {
    // TODO: Mejoras pendientes para la sección de historial
    // - Implementar filtrado por fechas, tipo de actividad o alumno
    // - Añadir exportación de datos a diferentes formatos (PDF, Excel)
    // - Mostrar gráficos y estadísticas de actividades
    // - Implementar búsqueda avanzada en el historial
    // - Añadir visualización por calendario de registros
    // - Permitir edición de registros anteriores (con marca de edición)
    // - Implementar comparativas entre periodos
    // - Añadir opción para crear informes personalizados
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Historial de Actividades",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun MensajesContent(
    mensajes: List<Triple<String, String, Boolean>> = emptyList(),
    onNavigateToChat: (String) -> Unit = {}
) {
    // TODO: Mejoras pendientes para la sección de mensajes
    // - Implementar filtrado de mensajes por origen o importancia
    // - Añadir búsqueda en el contenido de los mensajes
    // - Mostrar indicadores de estado de lectura y respuesta
    // - Implementar categorización automática de conversaciones
    // - Añadir plantillas de respuestas predefinidas
    // - Permitir adjuntar archivos o imágenes en los mensajes
    // - Implementar chat grupal para comunicación con múltiples familias
    // - Añadir opción para programar envío de mensajes
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mensajes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de chats
        if (mensajes.isEmpty()) {
            // Mostrar mensaje cuando no hay mensajes
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes mensajes nuevos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(mensajes) { (emisorId, texto, noLeido) ->
                    ChatItem(
                        nombre = emisorId, // Idealmente aquí mostrarías el nombre real del emisor
                        ultimoMensaje = texto,
                        noLeido = noLeido,
                        onClick = { onNavigateToChat(emisorId) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    nombre: String,
    ultimoMensaje: String,
    noLeido: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (noLeido) FontWeight.Bold else FontWeight.Medium
            )

            Text(
                text = ultimoMensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (noLeido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (noLeido) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun ConfiguracionProfesorContent() {
    ConfiguracionScreen(perfil = PerfilConfiguracion.PROFESOR)
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionProfesorPreviewNew() {
    UmeEguneroTheme {
        ConfiguracionScreen(perfil = PerfilConfiguracion.PROFESOR)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorDashboardPreview() {
    UmeEguneroTheme {
        ProfesorDashboardScreen(onLogout = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfesorDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ProfesorDashboardScreen(onLogout = {})
    }
}