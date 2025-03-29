package com.tfg.umeegunero.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.AccessibilityUtils.accessibilityDescription
import com.tfg.umeegunero.util.AccessibilityUtils.accessibleClickable
import kotlinx.coroutines.launch

/**
 * Componente para mostrar una lista paginada de centros educativos
 */
@Composable
fun PaginatedCentrosList(
    centros: List<Centro>,
    isLoading: Boolean,
    onCentroClick: (Centro) -> Unit = {},
    onDeleteCentro: (String) -> Unit = {},
    pageSize: Int = 5,
    modifier: Modifier = Modifier
) {
    // Estado de paginación
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = if (centros.isEmpty()) 1 else ((centros.size - 1) / pageSize) + 1
    
    // Calcular elementos a mostrar en la página actual
    val paginatedCentros = remember(centros, currentPage, pageSize) {
        val startIndex = currentPage * pageSize
        val endIndex = minOf(startIndex + pageSize, centros.size)
        if (startIndex < centros.size) centros.subList(startIndex, endIndex) else emptyList()
    }
    
    // Estado para el LazyColumn
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && centros.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.semantics {
                    contentDescription = "Cargando centros educativos"
                }
            )
        } else if (centros.isEmpty()) {
            EmptyStateMessage()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Centros Educativos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { contentDescription = "Listado de centros educativos" }
                )
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(paginatedCentros) { centro ->
                        CentroItem(
                            centro = centro,
                            onClick = { onCentroClick(centro) },
                            onDelete = { onDeleteCentro(centro.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Paginador
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Solo mostrar paginación si hay más de una página
                    if (totalPages > 1) {
                        // Botón anterior
                        OutlinedButton(
                            onClick = { 
                                if (currentPage > 0) {
                                    currentPage--
                                    // Scroll al inicio cuando se cambia de página
                                    scope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            enabled = currentPage > 0,
                            modifier = Modifier.semantics {
                                contentDescription = "Página anterior"
                                if (currentPage == 0) {
                                    contentDescription = "Página anterior, botón deshabilitado"
                                }
                            }
                        ) {
                            Text("Anterior")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Número de página actual
                        Text(
                            text = "${currentPage + 1} de $totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                contentDescription = "Página ${currentPage + 1} de $totalPages"
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Botón siguiente
                        OutlinedButton(
                            onClick = { 
                                if (currentPage < totalPages - 1) {
                                    currentPage++
                                    // Scroll al inicio cuando se cambia de página
                                    scope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            enabled = currentPage < totalPages - 1,
                            modifier = Modifier.semantics {
                                contentDescription = "Página siguiente"
                                if (currentPage == totalPages - 1) {
                                    contentDescription = "Página siguiente, botón deshabilitado"
                                }
                            }
                        ) {
                            Text("Siguiente")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Elemento individual que muestra información de un centro educativo
 */
@Composable
fun CentroItem(
    centro: Centro,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Crear la dirección completa para compartir
    val direccionCompleta = "${centro.getDireccionCalle()}, ${centro.getDireccionNumero()}, ${centro.getDireccionCodigoPostal()}, ${centro.getDireccionCiudad()}"
    
    val contactoCompleto = "${centro.obtenerTelefono()}\n${centro.obtenerEmail()}"
    
    val accessibilityDescription = buildString {
        append("Centro educativo ${centro.nombre}. ")
        append("Dirección: $direccionCompleta. ")
        append("Teléfono: $contactoCompleto. ")
        if (centro.numProfesores > 0) {
            append("Tiene ${centro.numProfesores} profesores. ")
        } else {
            append("No tiene profesores asignados. ")
        }
        if (centro.numClases > 0) {
            append("Tiene ${centro.numClases} clases.")
        } else {
            append("No tiene clases creadas.")
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .accessibleClickable(
                description = accessibilityDescription,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = centro.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics {
                        contentDescription = "Eliminar centro ${centro.nombre}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dirección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = direccionCompleta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Teléfono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = contactoCompleto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clearAndSetSemantics {}
                ) {
                    Text(
                        text = "${centro.numProfesores} profesores",
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.clearAndSetSemantics {}
                ) {
                    Text(
                        text = "${centro.numClases} clases",
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mensaje que se muestra cuando no hay centros para mostrar
 */
@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .height(48.dp)
                .width(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "No hay centros educativos",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            text = "Usa el botón + para añadir un nuevo centro educativo",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaginatedCentrosListPreview() {
    UmeEguneroTheme {
        val mockCentros = List(10) { index ->
            Centro(
                id = "centro$index",
                nombre = "Centro Educativo $index",
                direccion = "Calle Principal $index, 28001, Madrid",
                telefono = "91${index}000000",
                email = "centro$index@example.com",
                numProfesores = index + 1,
                numClases = index * 2
            )
        }
        
        PaginatedCentrosList(
            centros = mockCentros,
            isLoading = false,
            onCentroClick = {},
            onDeleteCentro = {}
        )
    }
} 