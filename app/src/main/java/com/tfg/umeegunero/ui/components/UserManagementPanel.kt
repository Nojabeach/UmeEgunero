package com.tfg.umeegunero.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.PaginationUtils
import com.tfg.umeegunero.util.formatDate
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Filtros (expandible)
                AnimatedVisibility(
                    visible = showFilters,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Filtrar por tipo de usuario",
                            style = MaterialTheme.typography.titleSmall,
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
                                    label = { 
                                        Text(
                                            text = tipo.name.replace("_", " "),
                                            style = MaterialTheme.typography.bodySmall
                                        ) 
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Switch para mostrar/ocultar usuarios inactivos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mostrar usuarios inactivos",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Switch(
                                checked = showInactivos,
                                onCheckedChange = { showInactivos = it }
                            )
                        }
                    }
                }
            }
        }
        
        // Componente de exportación de datos
        ExportDataComponent(
            usuarios = filteredUsuarios,
            onExportCSV = { usuarios ->
                val csvContent = ExportUtils.generateCSVContent(usuarios)
                // Aquí iría la lógica para guardar el CSV
                // Por ejemplo, usando un intent para compartir o guardar
            },
            onExportPDF = { usuarios ->
                val pdfContent = ExportUtils.generatePDFContent(usuarios)
                // Aquí iría la lógica para generar y guardar el PDF
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Lista de usuarios
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Cargando usuarios..."
                )
            }
        } else if (filteredUsuarios.isEmpty()) {
            // Mensaje cuando no hay resultados
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isEmpty() && selectedTypes.isEmpty()) 
                            Icons.Default.PeopleAlt
                        else 
                            Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (searchQuery.isEmpty() && selectedTypes.isEmpty())
                            "No hay usuarios disponibles"
                        else
                            "No se encontraron resultados para tu búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (searchQuery.isEmpty() && selectedTypes.isEmpty())
                            "Los usuarios que añadas aparecerán aquí"
                        else
                            "Prueba a cambiar los términos de búsqueda o ajustar los filtros",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Lista de usuarios (usando LazyColumn para optimizar el rendimiento)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
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
    }
    
    // Diálogo de confirmación para eliminar un usuario
    if (showDeleteDialog && usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { 
                Text(
                    text = "¿Estás seguro de que deseas eliminar a ${usuarioSeleccionado?.nombre} ${usuarioSeleccionado?.apellidos}? Esta acción no se puede deshacer."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioSeleccionado?.let { onDelete(it) }
                        showDeleteDialog = false
                        usuarioSeleccionado = null
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
    
    // Diálogo para resetear contraseña de un usuario
    if (showResetPasswordDialog && usuarioSeleccionado != null) {
        var passwordVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        
        AlertDialog(
            onDismissRequest = { 
                showResetPasswordDialog = false
                newPassword = ""
            },
            title = { Text("Resetear contraseña") },
            text = { 
                Column {
                    Text(
                        text = "Introduce una nueva contraseña para ${usuarioSeleccionado?.nombre} ${usuarioSeleccionado?.apellidos}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Componente de generación de contraseñas
                    PasswordGenerator(
                        onPasswordGenerated = { generatedPassword ->
                            newPassword = generatedPassword
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo de contraseña con icono para mostrar/ocultar
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("Nueva contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioSeleccionado?.let { usuario ->
                            onResetPassword(usuario.dni, newPassword)
                        }
                        showResetPasswordDialog = false
                        newPassword = ""
                        usuarioSeleccionado = null
                    },
                    enabled = newPassword.length >= 6
                ) {
                    Text("Resetear")
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
        
        // Solicitar foco al aparecer
        LaunchedEffect(Unit) {
            delay(100) // Pequeño retraso para asegurar que el diálogo ya está visible
            focusRequester.requestFocus()
        }
    }
    
    // Diálogo para resetear contraseñas en lote
    if (showResetPasswordBatchDialog) {
        var passwordVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        
        AlertDialog(
            onDismissRequest = { 
                showResetPasswordBatchDialog = false
                newBatchPassword = ""
            },
            title = { Text("Resetear contraseñas en lote") },
            text = { 
                Column {
                    Text(
                        text = "Vas a resetear la contraseña de ${selectedUserIds.size} usuario${if (selectedUserIds.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Componente de generación de contraseñas
                    PasswordGenerator(
                        onPasswordGenerated = { generatedPassword ->
                            newBatchPassword = generatedPassword
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newBatchPassword,
                        onValueChange = { newBatchPassword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("Nueva contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Resetear todas las contraseñas seleccionadas
                        selectedUserIds.forEach { userId ->
                            onResetPassword(userId, newBatchPassword)
                        }
                        showResetPasswordBatchDialog = false
                        newBatchPassword = ""
                        selectedUserIds = emptySet()
                    },
                    enabled = newBatchPassword.length >= 6
                ) {
                    Text("Resetear todas")
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
        
        // Solicitar foco al aparecer
        LaunchedEffect(Unit) {
            delay(100) // Pequeño retraso para asegurar que el diálogo ya está visible
            focusRequester.requestFocus()
        }
    }
}

/**
 * Elemento individual de usuario para presentaciones cuando se requiere mostrar
 * un usuario sin todas las funcionalidades de la tarjeta completa.
 */
@Composable
fun UsuarioItem(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (!usuario.activo) 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else 
                    MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar o iniciales
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (usuario.perfiles.firstOrNull()?.tipo) {
                                TipoUsuario.ADMIN_APP, TipoUsuario.ADMIN_CENTRO -> MaterialTheme.colorScheme.tertiary
                                TipoUsuario.PROFESOR -> MaterialTheme.colorScheme.primary
                                TipoUsuario.FAMILIAR -> MaterialTheme.colorScheme.secondary
                                TipoUsuario.ALUMNO -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.primary
                            }
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
                
                // Información del usuario
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${usuario.nombre} ${usuario.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row {
                        Text(
                            text = usuario.fechaRegistro.let { formatDate(it) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        if (!usuario.activo) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Inactivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Acciones
                Row {
                    IconButton(
                        onClick = onToggleActive
                    ) {
                        Icon(
                            imageVector = if (usuario.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = if (usuario.activo) "Desactivar" else "Activar",
                            tint = if (usuario.activo) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onResetPassword
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Resetear contraseña",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    IconButton(
                        onClick = onEdit
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementPanelPreview() {
    UmeEguneroTheme {
        val mockUsuarios = List(5) { index ->
            Usuario(
                dni = "1234567${index}A",
                nombre = "Usuario $index",
                apellidos = "Apellido",
                email = "usuario$index@example.com",
                telefono = "60000000$index",
                activo = index % 2 == 0,
                fechaRegistro = Timestamp.now()
            )
        }
        
        Surface {
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
} 