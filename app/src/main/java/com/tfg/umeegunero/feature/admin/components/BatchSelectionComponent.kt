package com.tfg.umeegunero.feature.admin.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Componente para la gestión de selecciones múltiples de usuarios
 */
@Composable
fun BatchSelectionComponent(
    usuarios: List<Usuario>,
    selectedUserIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onBatchResetPassword: (Set<String>) -> Unit,
    onBatchActivate: (Set<String>, Boolean) -> Unit,
    onBatchDelete: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selectedUserIds.isNotEmpty())
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título y botones de selección
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selección múltiple",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    // Seleccionar todos
                    IconButton(
                        onClick = { 
                            if (selectedUserIds.size == usuarios.size) {
                                // Si todos están seleccionados, deseleccionar todos
                                onSelectionChange(emptySet())
                            } else {
                                // Seleccionar todos
                                onSelectionChange(usuarios.map { it.dni }.toSet())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedUserIds.size == usuarios.size)
                                Icons.Default.SelectAll
                            else
                                Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (selectedUserIds.size == usuarios.size)
                                "Deseleccionar todos"
                            else
                                "Seleccionar todos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Invertir selección
                    IconButton(
                        onClick = { 
                            val allUserIds = usuarios.map { it.dni }.toSet()
                            val invertedSelection = allUserIds - selectedUserIds
                            onSelectionChange(invertedSelection)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipToBack,
                            contentDescription = "Invertir selección",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Información de selección actual
            AnimatedVisibility(
                visible = selectedUserIds.isNotEmpty(),
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "${selectedUserIds.size} usuario${if (selectedUserIds.size != 1) "s" else ""} seleccionado${if (selectedUserIds.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Acciones en lote
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Resetear contraseñas
                        OutlinedButton(
                            onClick = { onBatchResetPassword(selectedUserIds) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resetear")
                        }
                        
                        // Activar/desactivar
                        OutlinedButton(
                            onClick = { onBatchActivate(selectedUserIds, false) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desactivar")
                        }
                        
                        // Eliminar
                        OutlinedButton(
                            onClick = { showConfirmDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar múltiples usuarios
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("Confirmar eliminación múltiple") },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar los ${selectedUserIds.size} usuarios seleccionados? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchDelete(selectedUserIds)
                        showConfirmDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar todos")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BatchSelectionComponentPreview() {
    UmeEguneroTheme {
        val mockUsuarios = List(5) { index ->
            Usuario(
                dni = "1234567${index}A",
                nombre = "Usuario $index",
                apellidos = "Apellido",
                email = "usuario$index@example.com",
                telefono = "60000000$index",
                activo = index % 2 == 0
            )
        }
        
        var selectedIds by remember { mutableStateOf(setOf("12345670A", "12345672A")) }
        
        BatchSelectionComponent(
            usuarios = mockUsuarios,
            selectedUserIds = selectedIds,
            onSelectionChange = { selectedIds = it },
            onBatchResetPassword = { },
            onBatchActivate = { _, _ -> },
            onBatchDelete = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 