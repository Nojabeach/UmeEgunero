package com.tfg.umeegunero.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.formatDate
import com.google.firebase.Timestamp
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleActive: () -> Unit,
    isSelected: Boolean,
    onToggleSelection: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotationAnimation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (!usuario.activo)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con nombre, email y checkbox de selección
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox para selección múltiple
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection(it) }
                )
                
                // Avatar (círculo con inicial)
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
                
                // Información básica del usuario
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
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DNI
                        Text(
                            text = usuario.dni,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        // Estado de activación
                        if (!usuario.activo) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Inactivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Botón para expandir/colapsar
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Mostrar menos" else "Mostrar más",
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }
            
            // Contenido expandible
            AnimatedContent(
                targetState = expanded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "expandableContent"
            ) { isExpanded ->
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Información adicional
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Teléfono
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = usuario.telefono.ifEmpty { "No disponible" },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Fecha de registro
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Registrado: ${formatDate(usuario.fechaRegistro)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Perfiles (tipos de usuario)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                if (usuario.perfiles.isEmpty()) {
                                    Text(
                                        text = "Sin perfiles asignados",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "Perfiles: " + usuario.perfiles.joinToString(", ") { 
                                            it.tipo.name.replace("_", " ") 
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Acciones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Activar/Desactivar
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
                            
                            // Resetear contraseña
                            IconButton(
                                onClick = onResetPassword
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Resetear contraseña",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            // Editar
                            IconButton(
                                onClick = onEdit
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Eliminar
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
    }
}

@Preview(showBackground = true)
@Composable
fun UserCardPreview() {
    UmeEguneroTheme {
        Surface {
            UserCard(
                usuario = Usuario(
                    dni = "12345678A",
                    nombre = "Juan",
                    apellidos = "Pérez García",
                    email = "juan.perez@example.com",
                    telefono = "600123456",
                    fechaRegistro = Timestamp.now(),
                    activo = true
                ),
                onEdit = {},
                onDelete = {},
                onResetPassword = {},
                onToggleActive = {},
                isSelected = false,
                onToggleSelection = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
} 