package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }

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

                Divider()

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
                            selectedTab = index
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

                Divider()

                // Botón de cerrar sesión
                NavigationDrawerItem(
                    label = { Text(text = "Cerrar Sesión") },
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
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
                            BadgedBox(badge = { Badge { Text("5") } }) {
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
                        "Actividad" to Icons.Default.List,
                        "Mensajes" to Icons.AutoMirrored.Filled.Chat
                    )

                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (index == 3) {
                                    BadgedBox(badge = { Badge { Text("5") } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = title)
                                }
                            },
                            label = { Text(title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { /* Crear nuevo registro */ },
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
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
fun ProfesorDashboardContent(
    selectedTab: Int,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedTab) {
            0 -> ProfesorHomeContent()
            1 -> MisAlumnosContent()
            2 -> HistorialContent()
            3 -> MensajesContent()
            4 -> ConfiguracionContent()
            else -> ProfesorHomeContent()
        }
    }
}

@Composable
fun ProfesorHomeContent() {
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
                        icon = Icons.Default.CheckCircle
                    )

                    StatusItem(
                        count = 8,
                        total = 15,
                        title = "Informes",
                        icon = Icons.Default.List
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Alumnos pendientes de registro
        Text(
            text = "Pendientes de registro hoy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            val pendientes = listOf(
                "Ana García Torres",
                "Mario Sánchez López",
                "Lucía Fernández Pérez",
                "Pablo Rodríguez Martín",
                "Sofía Díaz Sánchez"
            )

            items(pendientes) { nombre ->
                AlumnoPendienteItem(nombre = nombre)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
fun AlumnoPendienteItem(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar del alumno
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Pendiente de informe diario",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        IconButton(onClick = { /* Crear registro para este alumno */ }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir registro",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MisAlumnosContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis alumnos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de alumnos
        LazyColumn {
            val alumnos = listOf(
                "Ana García Torres",
                "Mario Sánchez López",
                "Lucía Fernández Pérez",
                "Pablo Rodríguez Martín",
                "Sofía Díaz Sánchez",
                "Daniel Moreno Ruiz",
                "Julia González García",
                "Marcos Pérez Martín",
                "Claudia Serrano López",
                "Hugo Martín Sánchez",
                "Valeria Jiménez Díaz",
                "Adrián Torres Fernández",
                "Carmen Ruiz Gómez",
                "Álvaro Gómez Alonso",
                "Elena Álvarez Castro"
            )

            items(alumnos) { nombre ->
                AlumnoListItem(nombre = nombre)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun AlumnoListItem(nombre: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar del alumno
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
    }
}

@Composable
fun HistorialContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de actividades",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Fechas recientes
        LazyColumn {
            val fechas = listOf(
                "Hoy",
                "Ayer",
                "Lunes, 12 de febrero",
                "Viernes, 9 de febrero",
                "Jueves, 8 de febrero",
                "Miércoles, 7 de febrero"
            )

            items(fechas) { fecha ->
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Actividades de este día
                HistorialDiaItem(
                    titulo = "Registros de comida",
                    descripcion = "15 registros completados",
                    icono = Icons.Default.Fastfood
                )

                HistorialDiaItem(
                    titulo = "Registros de siesta",
                    descripcion = "10 registros completados",
                    icono = Icons.Default.CheckCircle
                )

                HistorialDiaItem(
                    titulo = "Informes diarios",
                    descripcion = "15 informes enviados a familias",
                    icono = Icons.Default.List
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

@Composable
fun HistorialDiaItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MensajesContent() {
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
        LazyColumn {
            val chats = listOf(
                Triple("Familia de Ana García", "¿Podría decirnos cómo ha estado Ana hoy?", true),
                Triple("Familia de Mario Sánchez", "Gracias por la información de ayer", false),
                Triple("Familia de Lucía Fernández", "¿A qué hora es la reunión del viernes?", true),
                Triple("Familia de Pablo Rodríguez", "¿Necesita traer algo especial mañana?", false),
                Triple("Familia de Sofía Díaz", "¿Sofía ha dormido bien durante la siesta?", true),
                Triple("Familia de Daniel Moreno", "El médico nos ha dicho que...", false),
                Triple("Centro Educativo", "Recordatorio: reunión mensual el viernes", false)
            )

            items(chats) { (nombre, mensaje, noLeido) ->
                ChatItem(nombre = nombre, ultimoMensaje = mensaje, noLeido = noLeido)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun ChatItem(
    nombre: String,
    ultimoMensaje: String,
    noLeido: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
fun ConfiguracionContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium
        )
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