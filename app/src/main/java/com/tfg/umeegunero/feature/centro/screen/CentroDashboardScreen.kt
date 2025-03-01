package com.tfg.umeegunero.feature.centro.screen

import android.content.res.Configuration
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToAddCurso: () -> Unit = {},
    onNavigateToEditCurso: (String) -> Unit = {},
    onNavigateToAddClase: () -> Unit = {},
    onNavigateToEditClase: (String) -> Unit = {},
    onNavigateToAddUser: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedItem by remember { mutableIntStateOf(0) }

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
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Centro Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Centro Educativo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Colegio San José",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                Spacer(modifier = Modifier.height(8.dp))

                // Elementos del menú
                val menuItems = listOf(
                    "Panel" to Icons.Default.Home,
                    "Profesores" to Icons.Default.Person,
                    "Alumnos" to Icons.Default.Group,
                    "Solicitudes" to Icons.Default.QuestionAnswer,
                    "Configuración" to Icons.Default.Settings
                )

                menuItems.forEachIndexed { index, (title, icon) ->
                    NavigationDrawerItem(
                        label = { Text(text = title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
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
                            when (selectedItem) {
                                0 -> "Panel de Centro"
                                1 -> "Profesores"
                                2 -> "Alumnos"
                                3 -> "Solicitudes"
                                4 -> "Configuración"
                                else -> "Panel de Centro"
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
                            BadgedBox(badge = { Badge { Text("3") } }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF007AFF),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        "Panel" to Icons.Default.Home,
                        "Profesores" to Icons.Default.Person,
                        "Alumnos" to Icons.Default.Group,
                        "Solicitudes" to Icons.Default.QuestionAnswer
                    )

                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (index == 3) {
                                    BadgedBox(badge = { Badge { Text("3") } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = title)
                                }
                            },
                            label = { Text(title) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index }
                        )
                    }
                }
            }
        ) { paddingValues ->
            CentroDashboardContent(
                selectedItem = selectedItem,
                paddingValues = paddingValues,
                onNavigateToAddCurso = onNavigateToAddCurso,
                onNavigateToEditCurso = onNavigateToEditCurso,
                onNavigateToAddClase = onNavigateToAddClase,
                onNavigateToEditClase = onNavigateToEditClase,
                onNavigateToAddUser = onNavigateToAddUser
            )
        }
    }
}

@Composable
fun CentroDashboardContent(
    selectedItem: Int,
    paddingValues: PaddingValues,
    onNavigateToAddCurso: () -> Unit,
    onNavigateToEditCurso: (String) -> Unit,
    onNavigateToAddClase: () -> Unit,
    onNavigateToEditClase: (String) -> Unit,
    onNavigateToAddUser: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedItem) {
            0 -> CentroPanelContent(onNavigateToAddCurso, onNavigateToEditCurso, onNavigateToAddClase, onNavigateToEditClase, onNavigateToAddUser)
            1 -> ProfesoresContent()
            2 -> AlumnosContent()
            3 -> SolicitudesContent()
            4 -> ConfiguracionContent()
            else -> CentroPanelContent(onNavigateToAddCurso, onNavigateToEditCurso, onNavigateToAddClase, onNavigateToEditClase, onNavigateToAddUser)
        }
    }
}

@Composable
fun CentroPanelContent(
    onNavigateToAddCurso: () -> Unit,
    onNavigateToEditCurso: (String) -> Unit,
    onNavigateToAddClase: () -> Unit,
    onNavigateToEditClase: (String) -> Unit,
    onNavigateToAddUser: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen del centro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dashboardItems = listOf(
                Triple("Profesores", "24", Icons.Default.Person),
                Triple("Alumnos", "342", Icons.Default.Group),
                Triple("Aulas", "18", Icons.Default.School),
                Triple("Solicitudes", "3", Icons.Default.QuestionAnswer)
            )

            items(dashboardItems) { (title, count, icon) ->
                DashboardCard(title = title, count = count, icon = icon)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Solicitudes recientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SolicitudesRecentesList()

        // Sección de Gestión Académica
        Text(
            text = "Gestión Académica",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                listOf(
                    Triple("Añadir Curso", Icons.Default.School, onNavigateToAddCurso),
                    Triple("Añadir Clase", Icons.Default.Group, onNavigateToAddClase)
                )
            ) { (title, icon, onClick) ->
                ElevatedCard(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Sección de Gestión de Usuarios
        Text(
            text = "Gestión de Usuarios",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                listOf(
                    Triple("Añadir Profesor", Icons.Default.Person, onNavigateToAddUser),
                    Triple("Solicitudes", Icons.Default.QuestionAnswer, {})
                )
            ) { (title, icon, onClick) ->
                ElevatedCard(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, count: String, icon: ImageVector) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SolicitudesRecentesList() {
    val solicitudes = remember {
        listOf(
            Triple("Ana García", "Familiar", "12/02/2024"),
            Triple("Roberto Sánchez", "Profesor", "10/02/2024"),
            Triple("Laura Martínez", "Familiar", "08/02/2024")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(solicitudes) { (nombre, tipo, fecha) ->
            SolicitudItem(nombre = nombre, tipo = tipo, fecha = fecha)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun SolicitudItem(nombre: String, tipo: String, fecha: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (tipo == "Familiar") Icons.Default.Person else Icons.Default.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row {
                Text(
                    text = tipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "• $fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = { /* Aprobar solicitud */ }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Aprobar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProfesoresContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Gestión de Profesores",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun AlumnosContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Gestión de Alumnos",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun SolicitudesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Solicitudes pendientes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SolicitudesRecentesList()
    }
}

@Composable
fun ConfiguracionContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Configuración del Centro",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CentroDashboardPreview() {
    UmeEguneroTheme {
        CentroDashboardScreen(onLogout = {}, onNavigateToAddCurso = {}, onNavigateToEditCurso = {}, onNavigateToAddClase = {}, onNavigateToEditClase = {}, onNavigateToAddUser = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CentroDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        CentroDashboardScreen(onLogout = {}, onNavigateToAddCurso = {}, onNavigateToEditCurso = {}, onNavigateToAddClase = {}, onNavigateToEditClase = {}, onNavigateToAddUser = {})
    }
}