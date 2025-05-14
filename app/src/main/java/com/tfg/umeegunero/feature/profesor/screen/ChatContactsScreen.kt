package com.tfg.umeegunero.feature.profesor.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay

/**
 * Pantalla de contactos para iniciar un chat (versión temporal)
 */
@Composable
fun ChatContactsScreen(
    navController: NavController,
    chatRouteName: String
) {
    val viewModel: ChatContactsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
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
                title = { Text(text = "Contactos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.filteredContacts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { navController.navigate(chatRouteName) }
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
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
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
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.loadContacts() }) {
                            Text(text = "Reintentar")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Para usuarios tipo familia, mostrar selector de hijos
                        if (uiState.userType == TipoUsuario.FAMILIAR && uiState.hijos.isNotEmpty()) {
                            HijosSelector(
                                hijos = uiState.hijos,
                                hijoSeleccionado = uiState.hijoSeleccionado,
                                onHijoSelected = { viewModel.seleccionarHijo(it) }
                            )
                        }
                        
                        // Para profesores y administradores, mostrar filtros
                        if (uiState.userType == TipoUsuario.PROFESOR || uiState.userType == TipoUsuario.ADMIN_CENTRO) {
                            FiltersSection(
                                cursos = uiState.availableCourses,
                                clases = uiState.availableClasses,
                                cursoSeleccionado = uiState.selectedCourseId,
                                claseSeleccionada = uiState.selectedClassId,
                                onCursoSelected = { viewModel.selectCourse(it) },
                                onClaseSelected = { viewModel.selectClass(it) }
                            )
                        }
                        
                        // Lista de contactos
                        ContactsList(
                            contacts = uiState.filteredContacts,
                            administratorContacts = uiState.administratorContacts,
                            teacherContacts = uiState.teacherContacts,
                            familyContacts = uiState.familyContacts,
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
fun HijosSelector(
    hijos: List<com.tfg.umeegunero.data.model.Alumno>,
    hijoSeleccionado: com.tfg.umeegunero.data.model.Alumno?,
    onHijoSelected: (com.tfg.umeegunero.data.model.Alumno) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Selecciona un hijo para ver sus profesores:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(hijos) { alumno ->
                val isSelected = hijoSeleccionado?.dni == alumno.dni
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { onHijoSelected(alumno) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alumno.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Filtrar contactos:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Selector de cursos
        if (cursos.isNotEmpty()) {
            Text(
                text = "Curso:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cursos) { curso ->
                    val isSelected = cursoSeleccionado == curso.id
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                               else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onCursoSelected(curso.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = curso.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        
        // Selector de clases
        if (clases.isNotEmpty()) {
            Text(
                text = "Clase:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(clases) { clase ->
                    val isSelected = claseSeleccionada == clase.id
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                               else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onClaseSelected(clase.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clase.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
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
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay contactos disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prueba con otros filtros o selecciona otro curso/clase",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp) // Espacio para no tapar el FAB
    ) {
        // Sección de administradores
        if (administratorContacts.isNotEmpty()) {
            item {
                ContactSectionHeader(
                    title = "Administración",
                    icon = Icons.Default.AdminPanelSettings
                )
            }
            
            items(administratorContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = "Administrador",
                    unreadCount = contact.unreadCount,
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
                    icon = Icons.Default.School
                )
            }
            
            items(teacherContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = contact.descripcion ?: "Profesor",
                    unreadCount = contact.unreadCount,
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
                    icon = Icons.Default.People
                )
            }
            
            items(familyContacts) { contact ->
                ContactItem(
                    name = "${contact.nombre} ${contact.apellidos}",
                    description = "Familia de ${contact.alumnoNombre ?: "Alumno"}",
                    unreadCount = contact.unreadCount,
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
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContactItem(
    name: String,
    description: String,
    onContactClick: () -> Unit,
    unreadCount: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onContactClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Iniciar chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 