package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.EstadoComida

/**
 * Componente para mostrar un registro de actividad infantil en forma de tarjeta.
 *
 * Este componente es flexible y puede representar diferentes tipos de registros
 * como alimentación, descanso, higiene, etc. con un estilo consistente.
 *
 * @param titulo Título principal del registro
 * @param subtitulo Subtítulo o descripción corta
 * @param hora Hora del registro
 * @param descripcion Descripción detallada del registro
 * @param icono Icono representativo
 * @param iconoTint Color del icono
 * @param colorBorde Color del borde de la tarjeta
 * @param tipoIndicador Texto indicador opcional (ej: "Completo", "Alerta")
 * @param colorIndicador Color del indicador
 * @param onEditar Acción opcional que se ejecuta al hacer clic en editar
 * @param onEliminar Acción opcional que se ejecuta al hacer clic en eliminar
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun RegistroInfantilCard(
    titulo: String,
    subtitulo: String,
    hora: String,
    descripcion: String = "",
    icono: ImageVector,
    iconoTint: Color = MaterialTheme.colorScheme.primary,
    colorBorde: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    tipoIndicador: String? = null,
    colorIndicador: Color = MaterialTheme.colorScheme.primary,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado: icono, título y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = iconoTint.copy(alpha = 0.12f),
                    contentColor = iconoTint
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iconoTint
                )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.Top)
                ) {
                    Text(
                        text = hora,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            if (descripcion.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, top = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fila inferior: indicador de tipo y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicador de tipo (si existe)
                if (tipoIndicador != null) {
                    Surface(
                        color = colorIndicador.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(start = 40.dp)
                    ) {
                        Text(
                            text = tipoIndicador,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorIndicador,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier)
                }
                
                // Acciones
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onEditar != null) {
                        IconButton(
                            onClick = onEditar,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar registro",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (onEliminar != null) {
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar registro",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista previa del componente RegistroInfantilCard.
 */
@Preview(showBackground = true)
@Composable
fun RegistroInfantilCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RegistroInfantilCard(
                titulo = "Desayuno",
                subtitulo = "Ha comido bastante",
                hora = "08:30",
                descripcion = "Leche con cereales y una pieza de fruta. Ha mostrado buen apetito y ha tomado toda la leche.",
                icono = Icons.Default.CheckCircle,
                tipoIndicador = "Completo",
                onEditar = {},
                onEliminar = {}
            )
            
            RegistroInfantilCard(
                titulo = "Siesta",
                subtitulo = "Duración: 1h 15min",
                hora = "13:45",
                descripcion = "Ha dormido tranquilamente. Se ha despertado descansado.",
                icono = Icons.Default.CheckCircle,
                iconoTint = MaterialTheme.colorScheme.tertiary,
                colorBorde = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                onEditar = {}
            )
        }
    }
} 