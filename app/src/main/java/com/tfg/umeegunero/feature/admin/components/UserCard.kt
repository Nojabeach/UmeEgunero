package com.tfg.umeegunero.feature.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.formatDate
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleActive: () -> Unit,
    isSelected: Boolean = false,
    onToggleSelection: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isActive = usuario.activo
    val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.ALUMNO
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (onToggleSelection != null) {
                    it.clickable { onToggleSelection(!isSelected) }
                } else {
                    it
                }
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surface
            }
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
                    // Checkbox de selección (opcional)
                    if (onToggleSelection != null) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection(it) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
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
fun UserCardPreview() {
    UmeEguneroTheme {
        val mockUsuario = Usuario(
            dni = "12345678A",
            email = "usuario@example.com",
            nombre = "Usuario",
            apellidos = "De Ejemplo",
            telefono = "600123456",
            fechaRegistro = Timestamp.now(),
            activo = true
        )
        
        UserCard(
            usuario = mockUsuario,
            onEdit = { },
            onDelete = { },
            onResetPassword = { },
            onToggleActive = { },
            isSelected = true,
            onToggleSelection = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 