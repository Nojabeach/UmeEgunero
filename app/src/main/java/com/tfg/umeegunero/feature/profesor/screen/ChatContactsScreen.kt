package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.profesor.viewmodel.ChatContactsViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.FamiliarContacto
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorContacto
import com.tfg.umeegunero.ui.theme.ProfesorColor
import kotlinx.coroutines.delay

/**
 * Pantalla de contactos para iniciar un chat (versión mejorada)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContactsScreen(
    navController: NavController,
    chatRouteName: String
) {
    val viewModel: ChatContactsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Efecto para recargar los mensajes no leídos cuando la pantalla se vuelva activa
    LaunchedEffect(Unit) {
        // Cargar mensajes no leídos cada vez que la pantalla se muestra
        viewModel.cargarMensajesNoLeidos()
    }
    
    // Efecto para actualizar mensajes no leídos cada 30 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // 30 segundos
            viewModel.cargarMensajesNoLeidos()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Nuevo mensaje",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Selecciona un contacto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.filteredContacts.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(chatRouteName) },
                    containerColor = ProfesorColor,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Crear grupo"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = ProfesorColor
                        )
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar contactos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadContacts() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = ProfesorColor
                            )
                        ) {
                            Text(text = "Reintentar")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Barra de búsqueda
                        SearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it }
                        )
                        
                        // Para usuarios tipo familia, mostrar selector de hijos
                        AnimatedVisibility(
                            visible = uiState.userType == TipoUsuario.FAMILIAR && uiState.hijos.isNotEmpty()
                        ) {
                            HijosSelector(
                                hijos = uiState.hijos,
                                hijoSeleccionado = uiState.hijoSeleccionado,
                                onHijoSelected = { viewModel.seleccionarHijo(it) }
                            )
                        }
                        
                        // Para profesores y administradores, mostrar filtros
                        AnimatedVisibility(
                            visible = uiState.userType == TipoUsuario.PROFESOR || uiState.userType == TipoUsuario.ADMIN_CENTRO
                        ) {
                            FiltersSection(
                                cursos = uiState.availableCourses,
                                clases = uiState.availableClasses,
                                cursoSeleccionado = uiState.selectedCourseId,
                                claseSeleccionada = uiState.selectedClassId,
                                onCursoSelected = { viewModel.selectCourse(it) },
                                onClaseSelected = { viewModel.selectClass(it) }
                            )
                        }
                        
                        // Lista de contactos filtrada por búsqueda
                        val filteredContacts = if (searchQuery.isEmpty()) {
                            uiState.filteredContacts
                        } else {
                            uiState.filteredContacts.filter { contact ->
                                when (contact) {
                                    is ProfesorContacto -> {
                                        "${contact.nombre} ${contact.apellidos}".contains(searchQuery, ignoreCase = true)
                                    }
                                    is FamiliarContacto -> {
                                        "${contact.nombre} ${contact.apellidos}".contains(searchQuery, ignoreCase = true)
                                    }
                                    else -> false
                                }
                            }
                        }
                        
                        // Lista de contactos
                        ContactsList(
                            contacts = filteredContacts,
                            administratorContacts = uiState.administratorContacts.filter { contact ->
                                searchQuery.isEmpty() || "${contact.nombre} ${contact.apellidos}".contains(searchQuery, ignoreCase = true)
                            },
                            teacherContacts = uiState.teacherContacts.filter { contact ->
                                searchQuery.isEmpty() || "${contact.nombre} ${contact.apellidos}".contains(searchQuery, ignoreCase = true)
                            },
                            familyContacts = uiState.familyContacts.filter { contact ->
                                searchQuery.isEmpty() || "${contact.nombre} ${contact.apellidos}".contains(searchQuery, ignoreCase = true)
                            },
                            navController = navController,
                            chatRouteName = chatRouteName,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar contacto...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = ProfesorColor
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = ProfesorColor,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun HijosSelector(
    hijos: List<com.tfg.umeegunero.data.model.Alumno>,
    hijoSeleccionado: com.tfg.umeegunero.data.model.Alumno?,
    onHijoSelected: (com.tfg.umeegunero.data.model.Alumno) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Selecciona un hijo para ver sus profesores:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(hijos) { alumno ->
                val isSelected = hijoSeleccionado?.dni == alumno.dni
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ProfesorColor 
                           else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(2.dp, ProfesorColor) else null,
                    modifier = Modifier
                        .clickable { onHijoSelected(alumno) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White 
                                    else ProfesorColor.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alumno.nombre.first().toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ProfesorColor else ProfesorColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alumno.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FiltersSection(
    cursos: List<com.tfg.umeegunero.data.model.Curso>,
    clases: List<com.tfg.umeegunero.data.model.Clase>,
    cursoSeleccionado: String?,
    claseSeleccionada: String?,
    onCursoSelected: (String) -> Unit,
    onClaseSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "Filtrar contactos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Selector de cursos
        if (cursos.isNotEmpty()) {
            Text(
                text = "Por curso:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cursos) { curso ->
                    val isSelected = cursoSeleccionado == curso.id
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ProfesorColor 
                               else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp, 
                            if (isSelected) ProfesorColor else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clickable { onCursoSelected(curso.id) }
                    ) {
                        Text(
                            text = curso.nombre,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Selector de clases
        if (clases.isNotEmpty()) {
            Text(
                text = "Por clase:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(clases) { clase ->
                    val isSelected = claseSeleccionada == clase.id
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ProfesorColor 
                               else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp, 
                            if (isSelected) ProfesorColor else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clickable { onClaseSelected(clase.id) }
                    ) {
                        Text(
                            text = clase.nombre,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsList(
    contacts: List<Any>,
    administratorContacts: List<ProfesorContacto>,
    teacherContacts: List<ProfesorContacto>,
    familyContacts: List<FamiliarContacto>,
    navController: NavController,
    chatRouteName: String,
    viewModel: ChatContactsViewModel
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = ProfesorColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay contactos disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prueba con otros filtros o selecciona otro curso/clase",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sección de administradores
        if (administratorContacts.isNotEmpty()) {
            item {
                ContactSectionHeader(
                    title = "Administración",
                    icon = Icons.Default.AdminPanelSettings,
                    count = administratorContacts.size
                )
            }
            
            items(administratorContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = contact.descripcion ?: "Administrador del centro",
                    unreadCount = contact.unreadCount,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onContactClick = {
                        viewModel.startConversation(
                            contactId = contact.dni,
                            contactName = "${contact.nombre} ${contact.apellidos}",
                            navController = navController,
                            chatRouteName = chatRouteName
                        )
                    }
                )
            }
        }
        
        // Sección de profesores
        if (teacherContacts.isNotEmpty()) {
            item {
                ContactSectionHeader(
                    title = "Profesores",
                    icon = Icons.Default.School,
                    count = teacherContacts.size
                )
            }
            
            items(teacherContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = contact.descripcion ?: "Profesor",
                    unreadCount = contact.unreadCount,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onContactClick = {
                        viewModel.startConversation(
                            contactId = contact.dni,
                            contactName = "${contact.nombre} ${contact.apellidos}",
                            navController = navController,
                            chatRouteName = chatRouteName
                        )
                    }
                )
            }
        }
        
        // Sección de familias
        if (familyContacts.isNotEmpty()) {
            item {
                ContactSectionHeader(
                    title = "Familias",
                    icon = Icons.Default.People,
                    count = familyContacts.size
                )
            }
            
            items(familyContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = "Familia de ${contact.alumnoNombre ?: "Alumno"}",
                    unreadCount = contact.unreadCount,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onContactClick = {
                        viewModel.startConversation(
                            contactId = contact.dni,
                            contactName = "${contact.nombre} ${contact.apellidos}",
                            navController = navController,
                            chatRouteName = chatRouteName,
                            alumnoId = contact.alumnoId
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ContactSectionHeader(
    title: String,
    icon: ImageVector,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfesorColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ProfesorColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = ProfesorColor
            )
        }
    }
}

@Composable
fun ContactItem(
    name: String,
    description: String,
    unreadCount: Int,
    backgroundColor: Color,
    iconColor: Color,
    onContactClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale)
            .clickable { onContactClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
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
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del contacto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Badge de mensajes no leídos
            if (unreadCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = ProfesorColor,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Mensajes no leídos",
                        tint = ProfesorColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
} 