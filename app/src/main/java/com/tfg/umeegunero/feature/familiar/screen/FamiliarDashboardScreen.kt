package com.tfg.umeegunero.feature.familiar.screen

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fastfood
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.tfg.umeegunero.feature.common.config.components.TemaSelector
import com.tfg.umeegunero.feature.common.config.components.TemaActual
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliarDashboardScreen(
    viewModel: FamiliarDashboardViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToDetalleRegistro: (String) -> Unit = {},
    onNavigateToDetalleHijo: (String) -> Unit = {},
    onNavigateToChat: (String, String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    var showHijosMenu by remember { mutableStateOf(false) }

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
                        // Imagen de perfil del familiar
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
                            text = uiState.familiar?.let { "${it.nombre} ${it.apellidos}" } ?: "Familiar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Familiar",
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
                    "Mis Hijos" to Icons.Default.Person,
                    "Actividades" to Icons.Default.CalendarToday,
                    "Mensajes" to Icons.AutoMirrored.Filled.Chat,
                    "Configuración" to Icons.Default.Settings
                )

                menuItems.forEachIndexed { index, (title, icon) ->
                    NavigationDrawerItem(
                        label = { Text(text = title) },
                        selected = uiState.selectedTab == index,
                        onClick = {
                            viewModel.setSelectedTab(index)
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
                            when (uiState.selectedTab) {
                                0 -> "Panel Familiar"
                                1 -> "Mis Hijos"
                                2 -> "Actividades"
                                3 -> "Mensajes"
                                4 -> "Configuración"
                                else -> "Panel Familiar"
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
                        Box {
                            IconButton(onClick = { showHijosMenu = true }) {
                                BadgedBox(badge = {
                                    if (uiState.registrosSinLeer > 0) {
                                        Badge { Text(uiState.registrosSinLeer.toString()) }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Seleccionar hijo"
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showHijosMenu,
                                onDismissRequest = { showHijosMenu = false }
                            ) {
                                uiState.hijos.forEach { hijo ->
                                    DropdownMenuItem(
                                        text = { Text("${hijo.nombre} ${hijo.apellidos}") },
                                        onClick = {
                                            viewModel.seleccionarHijo(hijo.dni)
                                            showHijosMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = {
                            viewModel.setSelectedTab(3)
                        }) {
                            BadgedBox(badge = {
                                if (uiState.totalMensajesNoLeidos > 0) {
                                    Badge { Text(uiState.totalMensajesNoLeidos.toString()) }
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
                        containerColor = FamiliarColor,
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
                        "Hijos" to Icons.Default.Person,
                        "Actividades" to Icons.Default.CalendarToday,
                        "Mensajes" to Icons.AutoMirrored.Filled.Chat
                    )

                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (index == 0 && uiState.registrosSinLeer > 0) {
                                    BadgedBox(badge = { Badge { Text(uiState.registrosSinLeer.toString()) } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else if (index == 3 && uiState.totalMensajesNoLeidos > 0) {
                                    BadgedBox(badge = { Badge { Text(uiState.totalMensajesNoLeidos.toString()) } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = title)
                                }
                            },
                            label = { Text(title) },
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) }
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            FamiliarDashboardContent(
                selectedTab = uiState.selectedTab,
                paddingValues = paddingValues,
                hijoSeleccionado = uiState.hijoSeleccionado,
                hijos = uiState.hijos,
                registrosActividad = uiState.registrosActividad,
                mensajesNoLeidos = uiState.mensajesNoLeidos,
                profesores = uiState.profesores,
                isLoading = uiState.isLoading,
                onNavigateToDetalleRegistro = onNavigateToDetalleRegistro,
                onNavigateToDetalleHijo = onNavigateToDetalleHijo,
                onNavigateToChat = onNavigateToChat,
                onMarcarRegistroComoVisto = { viewModel.marcarRegistroComoVisto(it) }
            )
        }
    }
}

@Composable
fun FamiliarDashboardContent(
    selectedTab: Int,
    paddingValues: PaddingValues,
    hijoSeleccionado: Alumno?,
    hijos: List<Alumno>,
    registrosActividad: List<RegistroActividad>,
    mensajesNoLeidos: List<Mensaje>,
    profesores: Map<String, Usuario>,
    isLoading: Boolean,
    onNavigateToDetalleRegistro: (String) -> Unit,
    onNavigateToDetalleHijo: (String) -> Unit,
    onNavigateToChat: (String, String?) -> Unit,
    onMarcarRegistroComoVisto: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedTab) {
            0 -> FamiliarHomeContent(
                hijoSeleccionado = hijoSeleccionado,
                registrosActividad = registrosActividad,
                onNavigateToDetalleRegistro = onNavigateToDetalleRegistro,
                onMarcarRegistroComoVisto = onMarcarRegistroComoVisto
            )
            1 -> MisHijosContent(
                hijos = hijos,
                onNavigateToDetalleHijo = onNavigateToDetalleHijo
            )
            2 -> ActividadesContent(
                hijoSeleccionado = hijoSeleccionado,
                registrosActividad = registrosActividad,
                onNavigateToDetalleRegistro = onNavigateToDetalleRegistro
            )
            3 -> MensajesContent(
                mensajes = mensajesNoLeidos,
                profesores = profesores,
                onNavigateToChat = onNavigateToChat
            )
            4 -> ConfiguracionFamiliarContent()
            else -> FamiliarHomeContent(
                hijoSeleccionado = hijoSeleccionado,
                registrosActividad = registrosActividad,
                onNavigateToDetalleRegistro = onNavigateToDetalleRegistro,
                onMarcarRegistroComoVisto = onMarcarRegistroComoVisto
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // Continuación de FamiliarDashboardScreen.kt

                CircularProgressIndicator(color = FamiliarColor)
            }
        }
    }
}

@Composable
fun FamiliarHomeContent(
    hijoSeleccionado: Alumno?,
    registrosActividad: List<RegistroActividad>,
    onNavigateToDetalleRegistro: (String) -> Unit,
    onMarcarRegistroComoVisto: (String) -> Unit
) {
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

        // Información del hijo seleccionado
        if (hijoSeleccionado != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${hijoSeleccionado.nombre} ${hijoSeleccionado.apellidos}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Centro: ${hijoSeleccionado.centroId}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Aula: ${hijoSeleccionado.aulaId}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            // Si no hay hijo seleccionado, mostramos un mensaje
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay ningún hijo seleccionado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Seleccione un hijo usando el menú superior",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Registros de actividad recientes
        Text(
            text = "Actividades recientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (registrosActividad.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades registradas aún",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn {
                val registrosOrdenados = registrosActividad.sortedByDescending { it.fecha }

                itemsIndexed(registrosOrdenados) { index, registro ->
                    RegistroActividadItem(
                        registro = registro,
                        onRegistroClick = {
                            onMarcarRegistroComoVisto(registro.id)
                            onNavigateToDetalleRegistro(registro.id)
                        }
                    )

                    if (index < registrosOrdenados.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RegistroActividadItem(
    registro: RegistroActividad,
    onRegistroClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onRegistroClick),
        colors = CardDefaults.cardColors(
            containerColor = if (!registro.vistoPorFamiliar)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono para el tipo de registro
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información del registro
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(registro.fecha),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Registro de actividades del día",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Mostrar indicadores de actividades
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (registro.comidas.primerPlato.descripcion.isNotBlank()) {
                        Badge(
                            modifier = Modifier.padding(end = 4.dp),
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        ) {
                            Text("Comida")
                        }
                    }

                    if (registro.siesta != null) {
                        Badge(
                            modifier = Modifier.padding(end = 4.dp),
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        ) {
                            Text("Siesta")
                        }
                    }

                    if (registro.necesidadesFisiologicas.pipi || registro.necesidadesFisiologicas.caca) {
                        Badge(
                            modifier = Modifier.padding(end = 4.dp),
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                        ) {
                            Text("Higiene")
                        }
                    }

                    if (registro.observaciones?.isNotEmpty() == true) {
                        Badge(
                            modifier = Modifier.padding(end = 4.dp),
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        ) {
                            Text("Notas")
                        }
                    }
                }
            }

            // Indicador de no leído
            if (!registro.vistoPorFamiliar) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("Nuevo")
                }
            }

            // Flecha para indicar navegación
            IconButton(onClick = onRegistroClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalle",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MisHijosContent(
    hijos: List<Alumno>,
    onNavigateToDetalleHijo: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis hijos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (hijos.isEmpty()) {
            // Mostrar mensaje cuando no hay hijos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay hijos registrados",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(hijos) { hijo ->
                    HijoListItem(
                        nombre = "${hijo.nombre} ${hijo.apellidos}",
                        centro = hijo.centroId,
                        aula = hijo.aulaId,
                        onClick = { onNavigateToDetalleHijo(hijo.dni) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun HijoListItem(
    nombre: String,
    centro: String,
    aula: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.first().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
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
                    text = "Centro: $centro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Aula: $aula",
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
}

@Composable
fun ActividadesContent(
    hijoSeleccionado: Alumno?,
    registrosActividad: List<RegistroActividad>,
    onNavigateToDetalleRegistro: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Actividades",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (hijoSeleccionado == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Seleccione un hijo para ver sus actividades",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        Text(
            text = "Actividades de ${hijoSeleccionado.nombre} ${hijoSeleccionado.apellidos}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (registrosActividad.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay actividades registradas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        // Agrupar registros por mes
        val registrosPorMes = registrosActividad
            .sortedByDescending { it.fecha }
            .groupBy { registro ->
                val date = registro.fecha.toDate()
                val calendar = java.util.Calendar.getInstance()
                calendar.time = date
                "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}"
            }

        LazyColumn {
            registrosPorMes.forEach { (mes, registros) ->
                item {
                    Text(
                        text = formatMes(mes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(registros) { registro ->
                    RegistroActividadCalendarItem(
                        registro = registro,
                        onRegistroClick = { onNavigateToDetalleRegistro(registro.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun RegistroActividadCalendarItem(
    registro: RegistroActividad,
    onRegistroClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRegistroClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fecha
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            val date = registro.fecha.toDate()
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date

            Text(
                text = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = obtenerNombreDia(calendar.get(java.util.Calendar.DAY_OF_WEEK)),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Resumen de actividades
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Registro diario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Indicadores de actividades
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (registro.comidas.primerPlato.descripcion.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = "Comida",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (registro.siesta != null) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Siesta",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Estado leído/no leído
        if (!registro.vistoPorFamiliar) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("Nuevo")
            }
        }
    }
}

@Composable
fun MensajesContent(
    mensajes: List<Mensaje>,
    profesores: Map<String, Usuario>,
    onNavigateToChat: (String, String?) -> Unit
) {
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

        if (mensajes.isEmpty()) {
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
            // Agrupamos los mensajes por emisor
            val mensajesPorEmisor = mensajes.groupBy { it.emisorId }

            LazyColumn {
                mensajesPorEmisor.forEach { (emisorId, mensajesEmisor) ->
                    item {
                        val nombreEmisor = profesores[emisorId]?.let { "${it.nombre} ${it.apellidos}" } ?: emisorId
                        ChatItem(
                            nombre = nombreEmisor,
                            ultimoMensaje = mensajesEmisor.maxByOrNull { it.timestamp }?.texto ?: "",
                            noLeido = mensajesEmisor.any { !it.leido },
                            onClick = { onNavigateToChat(emisorId, null) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
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
    onClick: () -> Unit
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
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
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
fun ConfiguracionFamiliarContent() {
    ConfiguracionScreen(perfil = PerfilConfiguracion.FAMILIAR)
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionFamiliarPreviewNew() {
    UmeEguneroTheme {
        ConfiguracionScreen(perfil = PerfilConfiguracion.FAMILIAR)
    }
}

// Funciones utilitarias

fun formatMes(mesKey: String): String {
    val (year, month) = mesKey.split("-")
    val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    return "${meses[month.toInt()]} de $year"
}

fun obtenerNombreDia(diaSemana: Int): String {
    return when (diaSemana) {
        java.util.Calendar.MONDAY -> "Lun"
        java.util.Calendar.TUESDAY -> "Mar"
        java.util.Calendar.WEDNESDAY -> "Mié"
        java.util.Calendar.THURSDAY -> "Jue"
        java.util.Calendar.FRIDAY -> "Vie"
        java.util.Calendar.SATURDAY -> "Sáb"
        java.util.Calendar.SUNDAY -> "Dom"
        else -> ""
    }
}

@Preview(showBackground = true)
@Composable
fun FamiliarDashboardPreview() {
    UmeEguneroTheme {
        FamiliarDashboardScreen(onLogout = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FamiliarDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        FamiliarDashboardScreen(onLogout = {})
    }
}