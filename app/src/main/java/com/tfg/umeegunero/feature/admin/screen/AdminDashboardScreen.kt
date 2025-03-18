package com.tfg.umeegunero.feature.admin.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.common.viewmodel.ConfiguracionUiState
import com.tfg.umeegunero.feature.common.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.theme.getNombreTema
import com.tfg.umeegunero.feature.common.components.TemaSelector
import com.tfg.umeegunero.feature.common.components.TemaActual
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToAddCentro: () -> Unit = {},
    onNavigateToEditCentro: (String) -> Unit = {},
    onNavigateToAddUser: () -> Unit = {},
    onNavigateToEditUsuario: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedItem by remember { mutableStateOf(0) }

    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // Efecto para cargar usuarios cuando se selecciona la pestaña correspondiente
    LaunchedEffect(selectedItem) {
        if (selectedItem == 1) {
            // Cargar usuarios cuando se selecciona esta pestaña
            viewModel.loadUsuarios()
        }
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
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Admin Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Administrador",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "admin@umeegunero.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                // Elementos del menú
                val menuItems = listOf(
                    "Centros Educativos" to Icons.Default.School,
                    "Usuarios" to Icons.Default.Person,
                    "Profesores" to Icons.Filled.School,
                    "Alumnos" to Icons.Default.Person,
                    "Vinculaciones" to Icons.Default.Link,
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (selectedItem) {
                                0 -> "Centros Educativos"
                                1 -> "Gestión de Usuarios"
                                2 -> "Gestión de Profesores"
                                3 -> "Gestión de Alumnos" 
                                4 -> "Vinculaciones"
                                5 -> "Configuración"
                                else -> "Panel de Administración"
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                // Solo mostrar FAB en las pantallas apropiadas
                when (selectedItem) {
                    0 -> { // Centros educativos
                        ExtendedFloatingActionButton(
                            text = { Text("Añadir Centro") },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                            onClick = onNavigateToAddCentro
                        )
                    }
                    1, 2, 3 -> { // Usuarios, Profesores, o Alumnos
                        ExtendedFloatingActionButton(
                            text = { Text("Añadir Usuario") },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                            onClick = onNavigateToAddUser
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            AdminDashboardContent(
                selectedItem = selectedItem,
                paddingValues = paddingValues,
                uiState = uiState,
                onEditCentro = onNavigateToEditCentro,
                onDeleteCentro = viewModel::deleteCentro,
                onRefresh = {
                    when (selectedItem) {
                        0 -> viewModel.loadCentros()
                        1 -> viewModel.loadUsuarios()
                    }
                },
                onEditUsuario = onNavigateToEditUsuario,
                onDeleteUsuario = viewModel::deleteUsuario
            )
        }
    }
}

@Composable
fun AdminDashboardContent(
    selectedItem: Int,
    paddingValues: PaddingValues,
    uiState: AdminDashboardUiState,
    onEditCentro: (String) -> Unit,
    onDeleteCentro: (String) -> Unit,
    onRefresh: () -> Unit,
    onEditUsuario: (String) -> Unit = {},
    onDeleteUsuario: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedItem) {
            0 -> CentrosEducativosContent(
                centros = uiState.centros,
                isLoading = uiState.isLoading,
                onEditCentro = onEditCentro,
                onDeleteCentro = onDeleteCentro,
                onRefresh = onRefresh
            )
            1 -> UsuariosContent(
                usuarios = uiState.usuarios,
                isLoading = uiState.isLoadingUsuarios,
                onEditUsuario = onEditUsuario,
                onDeleteUsuario = onDeleteUsuario,
                onRefresh = onRefresh
            )
            2 -> ProfesoresContent()
            3 -> AlumnosContent()
            4 -> VinculacionesContent()
            5 -> ConfiguracionContent()
            else -> CentrosEducativosContent(
                centros = uiState.centros,
                isLoading = uiState.isLoading,
                onEditCentro = onEditCentro,
                onDeleteCentro = onDeleteCentro,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
fun CentrosEducativosContent(
    centros: List<Centro>,
    isLoading: Boolean,
    onEditCentro: (String) -> Unit,
    onDeleteCentro: (String) -> Unit,
    onRefresh: () -> Unit
) {
    // TODO: Mejorar la gestión de centros educativos
    // - Añadir filtros de búsqueda por nombre, localidad o código postal
    // - Implementar paginación para manejar grandes cantidades de centros
    // - Añadir opción para ver detalles completos del centro
    // - Implementar confirmación antes de eliminar un centro
    // - Mostrar estadísticas resumidas (número de alumnos, profesores, etc.)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listado de Centros",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Botón de recargar
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostrar indicador de carga
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        // Mostrar lista de centros
        else if (centros.isNotEmpty()) {
            items(centros) { centro ->
                CentroEducativoItem(
                    centro = centro,
                    onEdit = { onEditCentro(centro.id) },
                    onDelete = { onDeleteCentro(centro.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        // Mostrar mensaje cuando no hay centros
        else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay centros educativos disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega un nuevo centro usando el botón '+' de abajo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CentroEducativoItem(
    centro: Centro,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = centro.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${centro.direccion.ciudad}, ${centro.direccion.provincia}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de acción
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun UsuariosContent(
    usuarios: List<Usuario>,
    isLoading: Boolean,
    onEditUsuario: (String) -> Unit,
    onDeleteUsuario: (String) -> Unit,
    onRefresh: () -> Unit
) {
    // TODO: Mejorar la gestión de usuarios
    // - Implementar filtrado por tipo de usuario (admin, profesor, familiar)
    // - Añadir búsqueda por nombre o email
    // - Permitir gestión masiva de usuarios
    // - Implementar reseteo de contraseñas
    // - Añadir función para desactivar temporalmente usuarios
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listado de Usuarios",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Botón de recargar
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostrar indicador de carga
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        // Mostrar lista de usuarios
        else if (usuarios.isNotEmpty()) {
            items(usuarios) { usuario ->
                UsuarioItem(
                    usuario = usuario,
                    onEdit = { onEditUsuario(usuario.dni) },
                    onDelete = { onDeleteUsuario(usuario.dni) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        // Mostrar mensaje cuando no hay usuarios
        else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay usuarios disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega un nuevo usuario usando el botón '+' de abajo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioItem(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Obtener el tipo de usuario primario
                val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.FAMILIAR
                val tipoText = when (tipoUsuario) {
                    TipoUsuario.ADMIN_APP -> "Administrador App"
                    TipoUsuario.ADMIN_CENTRO -> "Administrador Centro"
                    TipoUsuario.PROFESOR -> "Profesor"
                    TipoUsuario.FAMILIAR -> "Familiar"
                    else -> "Alumno"
                }

                Text(
                    text = "$tipoText • ${usuario.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de acción
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfesoresContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Mejoras pendientes para la pantalla de gestión de profesores
        // - Implementar listado completo con búsqueda avanzada
        // - Añadir vista de perfiles completos con experiencia y formación
        // - Mostrar estadísticas de evaluación docente por asignatura
        // - Implementar seguimiento de formación continua y certificaciones
        // - Añadir visualización de carga lectiva y horas complementarias
        // - Permitir gestión de ausencias y sustituciones
        // - Mostrar historial de comunicaciones con familias y centro
        // - Implementar sistema de evaluación de rendimiento
        // - Añadir funcionalidad para asignación optimizada a grupos
        // - Permitir vista de currículum vitae completo y actualización
        Text(
            text = "Gestión de Profesores - En desarrollo",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlumnosContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Mejoras pendientes para la pantalla de gestión de alumnos
        // - Implementar visualización de expedientes académicos completos
        // - Añadir sistema de seguimiento de evolución y progreso
        // - Mostrar historial médico y necesidades especiales
        // - Implementar gestión integrada de adaptaciones curriculares
        // - Añadir visualización de estructura familiar completa
        // - Permitir gestión de traslados entre centros educativos
        // - Mostrar historial de comportamiento e incidencias
        // - Implementar evaluación psicopedagógica y seguimiento
        // - Añadir funcionalidad para matrícula masiva y automática
        // - Permitir importación y exportación de datos académicos
        Text(
            text = "Gestión de Alumnos - En desarrollo",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VinculacionesContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Mejoras pendientes para la pantalla de vinculaciones
        // - Implementar visualización gráfica de relaciones familiares
        // - Añadir sistema de verificación de parentesco oficial
        // - Mostrar histórico de cambios en las vinculaciones
        // - Implementar gestión de permisos granulares por vinculación
        // - Añadir notificaciones automáticas de cambios importantes
        // - Permitir distintos niveles de acceso para diferentes familiares
        // - Mostrar información de contacto en caso de emergencia
        // - Implementar registro de accesos a información sensible
        // - Añadir función de transferencia de vinculación entre cuentas
        Text(
            text = "Gestión de Vinculaciones - En desarrollo",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ConfiguracionContent(
    viewModel: ConfiguracionViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        TemaSelector(
            temaSeleccionado = uiState.temaSeleccionado,
            onTemaSeleccionado = { viewModel.setTema(it) }
        )
        
        TemaActual(
            temaSeleccionado = uiState.temaSeleccionado
        )
        
        // Más configuraciones pendientes (tarjeta con las futuras funcionalidades)
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Próximamente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Gestión completa de parámetros del sistema\n" +
                           "• Paneles de administración de servicios cloud\n" +
                           "• Estadísticas de uso y rendimiento\n" +
                           "• Sistema de auditoría y registros de seguridad\n" +
                           "• Configuración de copias de seguridad\n" +
                           "• Personalización de la experiencia por defecto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ProfesorAlumnoVinculacionContent() {
    // Implementación básica para resolver el error de referencia
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vinculación Profesor-Alumno",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pantalla en desarrollo",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlumnoFamiliarVinculacionContent() {
    // Implementación básica para resolver el error de referencia
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vinculación Alumno-Familiar",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pantalla en desarrollo",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfesorCentroVinculacionContent() {
    // Implementación básica para resolver el error de referencia
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vinculación Profesor-Centro",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pantalla en desarrollo",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    // Crear un estado mock para el preview con centros de Getxo, Santurtzi y Berango
    val mockState = AdminDashboardUiState(
        centros = listOf(
            Centro(
                id = "1",
                nombre = "IES Artaza-Romo BHI",
                direccion = Direccion(ciudad = "Getxo", provincia = "Vizcaya / Bizkaia"),
                contacto = Contacto(telefono = "944633000", email = "secretaria@artazaromo.eus"),
                activo = true
            ),
            Centro(
                id = "2",
                nombre = "Colegio San José - Jesuitas",
                direccion = Direccion(ciudad = "Santurtzi", provincia = "Vizcaya / Bizkaia"),
                contacto = Contacto(telefono = "944831450", email = "secretaria@santurzibhi.net"),
                activo = true
            ),
            Centro(
                id = "3",
                nombre = "Berango Eskola HLHI",
                direccion = Direccion(ciudad = "Berango", provincia = "Vizcaya / Bizkaia"),
                contacto = Contacto(telefono = "946680953", email = "info@berangoeskola.eus"),
                activo = true
            )
        ),
        isLoading = false,
        error = null
    )

    UmeEguneroTheme {
        // Usar AdminDashboardContent directamente, sin el ViewModel
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Centros Educativos", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Añadir Centro") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                    onClick = {}
                )
            }
        ) { paddingValues ->
            AdminDashboardContent(
                selectedItem = 0,
                paddingValues = paddingValues,
                uiState = mockState,
                onEditCentro = {},
                onDeleteCentro = {},
                onRefresh = {},
                onEditUsuario = {},
                onDeleteUsuario = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardUsuariosPreview() {
    // Crear un estado mock para el preview con usuarios
    val mockState = AdminDashboardUiState(
        usuarios = listOf(
            Usuario(
                dni = "12345678A",
                email = "admin@umeegunero.com",
                nombre = "Administrador",
                apellidos = "App",
                telefono = "600111222",
                perfiles = listOf(Perfil(tipo = TipoUsuario.ADMIN_APP))
            ),
            Usuario(
                dni = "98765432Z",
                email = "director@artazaromo.eus",
                nombre = "María",
                apellidos = "López García",
                telefono = "600333444",
                perfiles = listOf(Perfil(tipo = TipoUsuario.ADMIN_CENTRO, centroId = "1"))
            ),
            Usuario(
                dni = "87654321B",
                email = "profesor@santurzibhi.net",
                nombre = "Juan",
                apellidos = "Martínez Ruiz",
                telefono = "600555666",
                perfiles = listOf(Perfil(tipo = TipoUsuario.PROFESOR, centroId = "2"))
            ),
            Usuario(
                dni = "76543210C",
                email = "familiar@gmail.com",
                nombre = "Laura",
                apellidos = "Sánchez Pérez",
                telefono = "600777888",
                perfiles = listOf(Perfil(tipo = TipoUsuario.FAMILIAR, subtipo = SubtipoFamiliar.MADRE))
            )
        ),
        isLoadingUsuarios = false
    )

    UmeEguneroTheme {
        // Usar AdminDashboardContent directamente, sin el ViewModel
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Añadir Usuario") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                    onClick = {}
                )
            }
        ) { paddingValues ->
            AdminDashboardContent(
                selectedItem = 1,
                paddingValues = paddingValues,
                uiState = mockState,
                onEditCentro = {},
                onDeleteCentro = {},
                onRefresh = {},
                onEditUsuario = {},
                onDeleteUsuario = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardVinculacionesPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Vinculaciones", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                VinculacionesContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorAlumnoVinculacionContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Vinculación Profesor-Alumno", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ProfesorAlumnoVinculacionContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlumnoFamiliarVinculacionContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Vinculación Alumno-Familiar", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AlumnoFamiliarVinculacionContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorCentroVinculacionContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Vinculación Profesor-Centro", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ProfesorCentroVinculacionContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesoresContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestión de Profesores", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ProfesoresContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlumnosContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestión de Alumnos", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AlumnosContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionContentPreview() {
    UmeEguneroTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Para la vista previa usamos un mockup sin ViewModel real
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Mock de componentes para la vista previa
                    TemaSelector(
                        temaSeleccionado = TemaPref.SYSTEM,
                        onTemaSeleccionado = { }
                    )
                    
                    TemaActual(
                        temaSeleccionado = TemaPref.SYSTEM
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Próximamente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "• Gestión completa de parámetros del sistema\n" +
                                       "• Paneles de administración de servicios cloud\n" +
                                       "• Estadísticas de uso y rendimiento\n" +
                                       "• Sistema de auditoría y registros de seguridad\n" +
                                       "• Configuración de copias de seguridad\n" +
                                       "• Personalización de la experiencia por defecto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}