package com.tfg.umeegunero.feature.admin.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.formatDate
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementPanel(
    usuarios: List<Usuario>,
    isLoading: Boolean,
    onDelete: (Usuario) -> Unit,
    onEdit: (Usuario) -> Unit,
    onResetPassword: (String, String) -> Unit,
    onToggleActive: (Usuario, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypes by remember { mutableStateOf(setOf<TipoUsuario>()) }
    var showFilters by remember { mutableStateOf(false) }
    var showInactivos by remember { mutableStateOf(false) }
    
    // Estado para selección múltiple
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }
    var showResetPasswordBatchDialog by remember { mutableStateOf(false) }
    var newBatchPassword by remember { mutableStateOf("") }
    
    // Estado para diálogos
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var newPassword by remember { mutableStateOf("") }
    
    // Obtener tipo principal de usuario
    fun getTipoUsuario(usuario: Usuario): TipoUsuario {
        return usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.ALUMNO
    }
    
    // Filtrado de usuarios
    val filteredUsuarios = usuarios.filter { usuario ->
        val tipoUsuario = getTipoUsuario(usuario)
        (selectedTypes.isEmpty() || tipoUsuario in selectedTypes) && 
        (searchQuery.isEmpty() || 
            usuario.nombre.contains(searchQuery, ignoreCase = true) || 
            usuario.apellidos.contains(searchQuery, ignoreCase = true) || 
            usuario.email.contains(searchQuery, ignoreCase = true) || 
            usuario.dni.contains(searchQuery, ignoreCase = true)) &&
        (showInactivos || usuario.activo)
    }.sortedWith(compareBy({ !it.activo }, { it.nombre }))

    Column(modifier = modifier.fillMaxSize()) {
        // Componente de selección y acciones por lotes
        BatchSelectionComponent(
            usuarios = filteredUsuarios,
            selectedUserIds = selectedUserIds,
            onSelectionChange = { selectedUserIds = it },
            onBatchResetPassword = { userIds ->
                // Mostrar diálogo para resetear contraseñas masivamente
                showResetPasswordBatchDialog = true
            },
            onBatchActivate = { userIds, activo ->
                // Activar/desactivar usuarios masivamente
                userIds.forEach { userId ->
                    filteredUsuarios.find { it.dni == userId }?.let { usuario ->
                        onToggleActive(usuario, activo)
                    }
                }
                // Limpiar selección
                selectedUserIds = emptySet()
            },
            onBatchDelete = { userIds ->
                // Eliminar usuarios masivamente
                userIds.forEach { userId ->
                    filteredUsuarios.find { it.dni == userId }?.let { usuario ->
                        onDelete(usuario)
                    }
                }
                // Limpiar selección
                selectedUserIds = emptySet()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Cabecera con buscador y filtros
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Título y botón de filtro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestión de Usuarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier.semantics { 
                            contentDescription = "Mostrar filtros de usuarios" 
                        }
                    ) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.FilterList else Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = if (selectedTypes.isNotEmpty() || showInactivos) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Buscar usuarios" },
                    placeholder = { Text("Buscar por nombre, email o DNI") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = { 
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                
                // Sección de filtros (expandible)
                AnimatedVisibility(
                    visible = showFilters,
                    enter = fadeIn(animationSpec = spring()),
                    exit = fadeOut(animationSpec = spring())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Filtrar por tipo:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Chips para filtrar por tipo de usuario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TipoUsuario.values().forEach { tipo ->
                                val isSelected = tipo in selectedTypes
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTypes = if (isSelected) {
                                            selectedTypes - tipo
                                        } else {
                                            selectedTypes + tipo
                                        }
                                    },
                                    label = { Text(tipo.name.replace("_", " ")) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Opción para mostrar usuarios inactivos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showInactivos = !showInactivos }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showInactivos,
                                onCheckedChange = { showInactivos = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mostrar usuarios inactivos",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        
        // Componente de exportación de datos
        ExportDataComponent(
            usuarios = filteredUsuarios,
            onExportCSV = { usuariosToExport ->
                // Aquí se implementaría la lógica para exportar a CSV
                // Por ejemplo, guardar el archivo en el almacenamiento
                val csvContent = ExportUtils.generateCSVContent(usuariosToExport)
                // En una implementación real, aquí se guardaría el archivo
            },
            onExportPDF = { usuariosToExport ->
                // Aquí se implementaría la lógica para exportar a PDF
                val pdfContent = ExportUtils.generatePDFContent(usuariosToExport)
                // En una implementación real, aquí se guardaría el archivo
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Estado de carga
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Lista de usuarios
        else if (filteredUsuarios.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsuarios) { usuario ->
                    UserCard(
                        usuario = usuario,
                        onEdit = { onEdit(usuario) },
                        onDelete = { 
                            usuarioSeleccionado = usuario
                            showDeleteDialog = true
                        },
                        onResetPassword = {
                            usuarioSeleccionado = usuario
                            showResetPasswordDialog = true
                        },
                        onToggleActive = { onToggleActive(usuario, !usuario.activo) },
                        isSelected = usuario.dni in selectedUserIds,
                        onToggleSelection = { isSelected ->
                            selectedUserIds = if (isSelected) {
                                selectedUserIds + usuario.dni
                            } else {
                                selectedUserIds - usuario.dni
                            }
                        }
                    )
                }
            }
        } 
        // Mensaje cuando no hay usuarios
        else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No se encontraron usuarios",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isNotEmpty() || selectedTypes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intenta con otros filtros o términos de búsqueda",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                searchQuery = ""
                                selectedTypes = emptySet()
                                showInactivos = false
                            }
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }
        }
        
        // Información del total de usuarios
        Text(
            text = "Mostrando ${filteredUsuarios.size} de ${usuarios.size} usuarios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
    
    // Diálogo para resetear contraseña
    if (showResetPasswordDialog && usuarioSeleccionado != null) {
        var showPassword by remember { mutableStateOf(false) }
        var passwordIsValid by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showResetPasswordDialog = false
                newPassword = ""
            },
            title = { Text("Resetear contraseña") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nueva contraseña para ${usuarioSeleccionado?.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Generador de contraseñas
                    PasswordGenerator(
                        onPasswordGenerated = { 
                            newPassword = it
                            passwordIsValid = it.length >= 6
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Campo para visualizar/editar la contraseña
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            passwordIsValid = it.length >= 6
                        },
                        label = { Text("Contraseña") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        supportingText = { 
                            if (newPassword.isNotEmpty() && !passwordIsValid) {
                                Text("La contraseña debe tener al menos 6 caracteres")
                            }
                        },
                        isError = newPassword.isNotEmpty() && !passwordIsValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioSeleccionado?.dni?.let { dni ->
                            onResetPassword(dni, newPassword)
                        }
                        showResetPasswordDialog = false
                        newPassword = ""
                    },
                    enabled = passwordIsValid
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showResetPasswordDialog = false
                        newPassword = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para confirmar eliminación
    if (showDeleteDialog && usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar a ${usuarioSeleccionado?.nombre}? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioSeleccionado?.let { onDelete(it) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para resetear contraseñas en lote
    if (showResetPasswordBatchDialog) {
        var showPassword by remember { mutableStateOf(false) }
        var passwordIsValid by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showResetPasswordBatchDialog = false
                newBatchPassword = ""
            },
            title = { Text("Resetear contraseñas en lote") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nueva contraseña para ${selectedUserIds.size} usuarios",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Generador de contraseñas
                    PasswordGenerator(
                        onPasswordGenerated = { 
                            newBatchPassword = it
                            passwordIsValid = it.length >= 6
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Campo para visualizar/editar la contraseña
                    OutlinedTextField(
                        value = newBatchPassword,
                        onValueChange = { 
                            newBatchPassword = it
                            passwordIsValid = it.length >= 6
                        },
                        label = { Text("Contraseña") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        supportingText = { 
                            if (newBatchPassword.isNotEmpty() && !passwordIsValid) {
                                Text("La contraseña debe tener al menos 6 caracteres")
                            }
                        },
                        isError = newBatchPassword.isNotEmpty() && !passwordIsValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Aplicar la misma contraseña a todos los usuarios seleccionados
                        selectedUserIds.forEach { dni ->
                            onResetPassword(dni, newBatchPassword)
                        }
                        showResetPasswordBatchDialog = false
                        newBatchPassword = ""
                        // Limpiar selección
                        selectedUserIds = emptySet()
                    },
                    enabled = passwordIsValid
                ) {
                    Text("Aplicar a todos")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showResetPasswordBatchDialog = false
                        newBatchPassword = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleActive: () -> Unit,
    isSelected: Boolean,
    onToggleSelection: (Boolean) -> Unit
) {
    val isActive = usuario.activo
    val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.ALUMNO
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con nombre e indicador de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar o iniciales
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isActive) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = usuario.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${usuario.nombre} ${usuario.apellidos}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isActive) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            if (!isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AssistChip(
                                    onClick = { },
                                    label = { 
                                        Text(
                                            "Inactivo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        ) 
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                        
                        Text(
                            text = usuario.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Indicador de tipo de usuario
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (tipoUsuario) {
                                TipoUsuario.ADMIN_APP, TipoUsuario.ADMIN_CENTRO -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                TipoUsuario.PROFESOR -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                TipoUsuario.FAMILIAR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                TipoUsuario.ALUMNO -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tipoUsuario.name.replace("_", " "),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (tipoUsuario) {
                            TipoUsuario.ADMIN_APP, TipoUsuario.ADMIN_CENTRO -> MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                            TipoUsuario.PROFESOR -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            TipoUsuario.FAMILIAR -> MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                            TipoUsuario.ALUMNO -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Datos adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // DNI
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DNI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = usuario.dni,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Fecha creación
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha registro",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (usuario.fechaRegistro != null) 
                            formatDate(usuario.fechaRegistro.seconds * 1000) 
                        else 
                            "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón activar/desactivar
                OutlinedIconButton(
                    onClick = onToggleActive,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = if (isActive) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Block else Icons.Default.Check,
                        contentDescription = if (isActive) "Desactivar usuario" else "Activar usuario"
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón resetear contraseña
                OutlinedIconButton(
                    onClick = onResetPassword,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Resetear contraseña"
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón editar
                OutlinedIconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar usuario"
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón eliminar
                OutlinedIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar usuario"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementPanelPreview() {
    UmeEguneroTheme {
        val mockUsuarios = listOf(
            Usuario(
                dni = "12345678A",
                email = "admin@example.com",
                nombre = "Administrador",
                apellidos = "Sistema",
                telefono = "600123456",
                fechaRegistro = Timestamp.now(),
                activo = true
            ),
            Usuario(
                dni = "87654321B",
                email = "profesor@example.com",
                nombre = "Profesor",
                apellidos = "Ejemplo",
                telefono = "600654321",
                fechaRegistro = Timestamp.now(),
                activo = true
            ),
            Usuario(
                dni = "11223344C",
                email = "familiar@example.com",
                nombre = "Familiar",
                apellidos = "Ejemplo",
                telefono = "600111222",
                fechaRegistro = Timestamp.now(),
                activo = false
            )
        )
        
        UserManagementPanel(
            usuarios = mockUsuarios,
            isLoading = false,
            onDelete = { },
            onEdit = { },
            onResetPassword = { _, _ -> },
            onToggleActive = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
} 