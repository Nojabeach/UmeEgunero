package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Componente que muestra una tarjeta de registro diario para educación infantil.
 *
 * Este componente presenta información sobre diferentes tipos de registros diarios (alimentación,
 * siesta, higiene o actividad) con un diseño de tarjeta amigable. Incluye un icono representativo,
 * hora del registro, título, descripción y acciones opcionales.
 *
 * @param titulo Título principal del registro
 * @param subtitulo Subtítulo o descripción corta del registro
 * @param hora Hora en que se realizó el registro
 * @param descripcion Descripción detallada del registro
 * @param icono Icono que representa visualmente el tipo de registro
 * @param iconoTint Color personalizado para el icono principal
 * @param colorBorde Color personalizado para el borde de la tarjeta
 * @param tipoIndicador Indicador del tipo o categoría del registro
 * @param colorIndicador Color personalizado para el indicador de tipo
 * @param onEditar Acción opcional que se ejecuta al hacer clic en editar
 * @param onEliminar Acción opcional que se ejecuta al hacer clic en eliminar
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun RegistroInfantilCard(
    titulo: String,
    subtitulo: String,
    hora: String,
    descripcion: String,
    icono: ImageVector,
    iconoTint: Color = MaterialTheme.colorScheme.primary,
    colorBorde: Color = MaterialTheme.colorScheme.outlineVariant,
    tipoIndicador: String? = null,
    colorIndicador: Color = MaterialTheme.colorScheme.primary,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono principal del registro
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconoTint,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Contenido principal: título y subtítulo
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Hora del registro
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
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
 * Componente especializado para mostrar un registro de alimentación infantil.
 *
 * @param registro Datos del registro de alimentación
 * @param onEditar Acción opcional que se ejecuta al hacer clic en editar
 * @param onEliminar Acción opcional que se ejecuta al hacer clic en eliminar
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun AlimentacionCard(
    registro: RegistroAlimentacion,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorPorTipo = when (registro.tipoComida) {
        TipoComida.DESAYUNO -> MaterialTheme.colorScheme.tertiary
        TipoComida.ALMUERZO -> MaterialTheme.colorScheme.primary
        TipoComida.COMIDA -> MaterialTheme.colorScheme.secondary
        TipoComida.MERIENDA -> MaterialTheme.colorScheme.tertiary
    }
    
    val iconoPorTipo = when (registro.tipoComida) {
        TipoComida.DESAYUNO -> Icons.Default.Fastfood
        TipoComida.ALMUERZO -> Icons.Default.Fastfood
        TipoComida.COMIDA -> Icons.Default.Restaurant
        TipoComida.MERIENDA -> Icons.Default.Fastfood
    }
    
    val textoNivelConsumo = when (registro.cantidadConsumida) {
        NivelConsumo.NADA -> "No ha comido nada"
        NivelConsumo.POCO -> "Ha comido poco"
        NivelConsumo.BIEN -> "Ha comido bien"
        NivelConsumo.TODO -> "Ha comido todo"
    }
    
    RegistroInfantilCard(
        titulo = when (registro.tipoComida) {
            TipoComida.DESAYUNO -> "Desayuno"
            TipoComida.ALMUERZO -> "Almuerzo"
            TipoComida.COMIDA -> "Comida"
            TipoComida.MERIENDA -> "Merienda"
        },
        subtitulo = textoNivelConsumo,
        hora = registro.hora,
        descripcion = registro.descripcion + if (registro.observaciones.isNotEmpty()) "\n\nObservaciones: ${registro.observaciones}" else "",
        icono = iconoPorTipo,
        iconoTint = colorPorTipo,
        colorBorde = colorPorTipo.copy(alpha = 0.5f),
        tipoIndicador = when (registro.cantidadConsumida) {
            NivelConsumo.NADA, NivelConsumo.POCO -> "Alerta"
            else -> null
        },
        colorIndicador = when (registro.cantidadConsumida) {
            NivelConsumo.NADA, NivelConsumo.POCO -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        },
        onEditar = onEditar,
        onEliminar = onEliminar,
        modifier = modifier
    )
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