package com.tfg.umeegunero.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Usuario

/**
 * Componente para manejar la selección por lotes de usuarios.
 * Proporciona opciones para seleccionar todos los usuarios, resetear contraseñas en masa,
 * activar/desactivar usuarios y eliminar usuarios seleccionados.
 * 
 * @param usuarios Lista de usuarios filtrada actualmente visible.
 * @param selectedUserIds Conjunto de IDs de usuarios seleccionados actualmente.
 * @param onSelectionChange Callback llamado cuando cambia la selección de usuarios.
 * @param onBatchResetPassword Callback llamado cuando se solicita un reseteo masivo de contraseñas.
 * @param onBatchActivate Callback llamado cuando se solicita activar o desactivar usuarios en masa.
 * @param onBatchDelete Callback llamado cuando se solicita eliminar usuarios en masa.
 * @param modifier Modificador opcional para el componente.
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
    val isSelectionMode = selectedUserIds.isNotEmpty()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = isSelectionMode,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Cabecera con contador de selección y botón para cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedUserIds.size} seleccionados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedUserIds.size == usuarios.size && usuarios.isNotEmpty(),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        // Seleccionar todos
                                        onSelectionChange(usuarios.map { it.dni }.toSet())
                                    } else {
                                        // Deseleccionar todos
                                        onSelectionChange(emptySet())
                                    }
                                }
                            )
                            
                            Text(
                                text = "Todos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { onSelectionChange(emptySet()) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar selección",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones de acción para operaciones en lote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón para resetear contraseñas
                    Button(
                        onClick = { onBatchResetPassword(selectedUserIds) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resetear")
                    }
                    
                    // Botón para activar usuarios
                    Button(
                        onClick = { onBatchActivate(selectedUserIds, true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar")
                    }
                    
                    // Botón para desactivar usuarios
                    Button(
                        onClick = { onBatchActivate(selectedUserIds, false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desactivar")
                    }
                    
                    // Botón para eliminar usuarios
                    Button(
                        onClick = { onBatchDelete(selectedUserIds) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
} 