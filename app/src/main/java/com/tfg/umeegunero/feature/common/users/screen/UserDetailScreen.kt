package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.users.viewmodel.UserDetailViewModel
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy

/**
 * Pantalla que muestra los detalles de un usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: String,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                ErrorMessage(
                    message = uiState.error ?: "Error desconocido",
                    onRetry = { viewModel.loadUser(userId) }
                )
            } else if (uiState.usuario != null) {
                UserDetailsContent(usuario = uiState.usuario!!)
            } else {
                Text(
                    text = "Usuario no encontrado",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun UserDetailsContent(usuario: Usuario) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con avatar e información principal
        UserHeader(usuario = usuario)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información de contacto
        SectionTitle(title = "Información de Contacto")
        
        ContactInfoItem(
            icon = Icons.Default.Email,
            label = "Email",
            value = usuario.email
        )
        
        if (!usuario.telefono.isNullOrBlank()) {
            ContactInfoItem(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = usuario.telefono ?: ""
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Detalles de la cuenta
        SectionTitle(title = "Detalles de la Cuenta")
        
        DetailItem(
            label = "DNI",
            value = usuario.dni
        )
        
        DetailItem(
            label = "Estado",
            value = if (usuario.activo) "Activo" else "Inactivo",
            valueColor = if (usuario.activo) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
        
        // Fecha de registro
        DetailItem(
            label = "Fecha de registro",
            value = formatTimestamp(usuario.fechaRegistro)
        )
        
        // Último acceso
        usuario.ultimoAcceso?.let {
            DetailItem(
                label = "Último acceso",
                value = formatTimestamp(it)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información de perfiles
        SectionTitle(title = "Perfiles")
        
        usuario.perfiles.forEach { perfil ->
            ProfileItem(perfil = perfil)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun UserHeader(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (usuario.avatarUrl?.isNotBlank() == true) {
                    // Cargar imagen real desde la URL con opciones optimizadas
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(usuario.avatarUrl)
                                .crossfade(false) // Desactivar animación para carga instantánea
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Avatar de ${usuario.nombre}",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Fallback a inicial del nombre cuando no hay imagen
                    Text(
                        text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nombre completo
            Text(
                text = "${usuario.nombre} ${usuario.apellidos}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Tipo principal de usuario
            val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.FAMILIAR
            Text(
                text = getTipoUsuarioLabel(tipoUsuario),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ContactInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileItem(perfil: com.tfg.umeegunero.data.model.Perfil) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Tipo de perfil
            Text(
                text = getTipoUsuarioLabel(perfil.tipo),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Información adicional del perfil
            if (perfil.centroId.isNotBlank()) {
                DetailItem(
                    label = "Centro ID",
                    value = perfil.centroId
                )
            }
            
            // Estado de verificación
            DetailItem(
                label = "Verificado",
                value = if (perfil.verificado) "Sí" else "No",
                valueColor = if (perfil.verificado) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            // Subtipo (si existe)
            perfil.subtipo?.let {
                DetailItem(
                    label = "Subtipo",
                    value = it.name
                )
            }
            
            // Alumnos (si hay)
            if (perfil.alumnos.isNotEmpty()) {
                Text(
                    text = "Alumnos asociados: ${perfil.alumnos.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
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
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

// Función helper para formatear timestamps
private fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

// Función helper para obtener la etiqueta del tipo de usuario
private fun getTipoUsuarioLabel(tipo: TipoUsuario): String {
    return when (tipo) {
        TipoUsuario.ADMIN_APP -> "Administrador de Aplicación"
        TipoUsuario.ADMIN_CENTRO -> "Administrador de Centro"
        TipoUsuario.PROFESOR -> "Profesor"
        TipoUsuario.FAMILIAR -> "Familiar"
        TipoUsuario.ALUMNO -> "Alumno"
        TipoUsuario.DESCONOCIDO -> "Desconocido"
        TipoUsuario.OTRO -> "Otro"
    }
} 