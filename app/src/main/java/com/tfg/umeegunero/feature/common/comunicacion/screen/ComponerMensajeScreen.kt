package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.Layout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoDestinatario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.ComponerMensajeViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.SpaceAround
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly

/**
 * Pantalla para componer un nuevo mensaje
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponerMensajeScreen(
    navController: NavController,
    destinatarioId: String? = null,
    viewModel: ComponerMensajeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Inicializar con destinatario si se proporciona
    LaunchedEffect(destinatarioId) {
        destinatarioId?.let {
            viewModel.cargarDestinatario(it)
        }
    }
    
    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }
    
    // Navegar atrás cuando el mensaje se envía correctamente
    LaunchedEffect(uiState.enviado) {
        if (uiState.enviado) {
            snackbarHostState.showSnackbar("Mensaje enviado correctamente")
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo mensaje") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.enviarMensaje() },
                        enabled = !uiState.enviando
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar mensaje"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.cargando) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Cargando datos..."
                )
            } else if (uiState.enviando) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Enviando mensaje..."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Sección de destinatario
                    item {
                        Text(
                            text = "Destinatario",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Selector de tipo de destinatario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TipoDestinatarioChip(
                                selected = uiState.tipoDestinatario == TipoDestinatario.INDIVIDUAL,
                                onClick = { viewModel.actualizarTipoDestinatario(TipoDestinatario.INDIVIDUAL) },
                                label = "Individual"
                            )
                            
                            TipoDestinatarioChip(
                                selected = uiState.tipoDestinatario == TipoDestinatario.GRUPO,
                                onClick = { viewModel.actualizarTipoDestinatario(TipoDestinatario.GRUPO) },
                                label = "Grupo"
                            )
                            
                            TipoDestinatarioChip(
                                selected = uiState.tipoDestinatario == TipoDestinatario.CLASE,
                                onClick = { viewModel.actualizarTipoDestinatario(TipoDestinatario.CLASE) },
                                label = "Clase"
                            )
                            
                            TipoDestinatarioChip(
                                selected = uiState.tipoDestinatario == TipoDestinatario.CENTRO,
                                onClick = { viewModel.actualizarTipoDestinatario(TipoDestinatario.CENTRO) },
                                label = "Centro"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Destinatario seleccionado (modo individual)
                    if (uiState.tipoDestinatario == TipoDestinatario.INDIVIDUAL) {
                        item {
                            if (uiState.destinatario != null) {
                                DestinatarioCard(
                                    usuario = uiState.destinatario!!,
                                    onRemove = {
                                        // Eliminar destinatario
                                    }
                                )
                            } else {
                                OutlinedTextField(
                                    value = uiState.textoFiltroBusqueda,
                                    onValueChange = { viewModel.actualizarFiltroBusqueda(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Buscar destinatario") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Buscar"
                                        )
                                    }
                                )
                                
                                if (uiState.textoFiltroBusqueda.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .heightIn(max = 200.dp)
                                                .padding(8.dp)
                                        ) {
                                            items(uiState.usuariosFiltrados) { usuario ->
                                                UsuarioItem(
                                                    usuario = usuario,
                                                    onClick = {
                                                        viewModel.seleccionarDestinatario(usuario)
                                                    }
                                                )
                                                
                                                Divider()
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Lista de destinatarios (modo grupo)
                    if (uiState.tipoDestinatario == TipoDestinatario.GRUPO) {
                        item {
                            OutlinedTextField(
                                value = uiState.textoFiltroBusqueda,
                                onValueChange = { viewModel.actualizarFiltroBusqueda(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Añadir destinatarios") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar"
                                    )
                                }
                            )
                            
                            if (uiState.textoFiltroBusqueda.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .heightIn(max = 200.dp)
                                            .padding(8.dp)
                                    ) {
                                        items(uiState.usuariosFiltrados) { usuario ->
                                            UsuarioItem(
                                                usuario = usuario,
                                                onClick = {
                                                    viewModel.agregarDestinatario(usuario)
                                                }
                                            )
                                            
                                            Divider()
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mostrar lista de destinatarios seleccionados
                            if (uiState.destinatarios.isNotEmpty()) {
                                Text(
                                    text = "Destinatarios seleccionados",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.destinatarios.forEach { usuario ->
                                        DestinatarioChip(
                                            nombre = usuario.nombre,
                                            onRemove = { viewModel.eliminarDestinatario(usuario) }
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    
                    // Selector de clase (modo clase)
                    if (uiState.tipoDestinatario == TipoDestinatario.CLASE) {
                        item {
                            Text(
                                text = "Seleccionar clase",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // Aquí iría un selector de clases
                            // (a implementar según necesidades específicas)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Asunto
                    item {
                        OutlinedTextField(
                            value = uiState.asunto,
                            onValueChange = { viewModel.actualizarAsunto(it) },
                            label = { Text("Asunto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Contenido
                    item {
                        OutlinedTextField(
                            value = uiState.contenido,
                            onValueChange = { viewModel.actualizarContenido(it) },
                            label = { Text("Contenido") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            maxLines = 10
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Adjuntos
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adjuntos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = {
                                    // Abrir selector de archivos
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Adjuntar archivo"
                                )
                            }
                        }
                        
                        if (uiState.adjuntos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            uiState.adjuntos.forEach { adjunto ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = adjunto.substringAfterLast('/'),
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    IconButton(
                                        onClick = { viewModel.eliminarAdjunto(adjunto) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar adjunto"
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    // Botón de enviar
                    item {
                        Button(
                            onClick = { viewModel.enviarMensaje() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.enviando
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text("Enviar mensaje")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chip para seleccionar el tipo de destinatario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoDestinatarioChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        }
    )
}

/**
 * Tarjeta de usuario destinatario
 */
@Composable
fun DestinatarioCard(
    usuario: Usuario,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario.nombre.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar destinatario"
                )
            }
        }
    }
}

/**
 * Elemento de usuario para la lista de búsqueda
 */
@Composable
fun UsuarioItem(
    usuario: Usuario,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar del usuario
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nombre.first().toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Información del usuario
        Column {
            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Chip para mostrar un destinatario seleccionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinatarioChip(
    nombre: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = { },
        label = { Text(nombre) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminar",
                modifier = Modifier.clickable(onClick = onRemove)
            )
        }
    )
}

/**
 * Layout para mostrar elementos en filas con ajuste automático
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rowConstraints = constraints.copy(minHeight = 0)
        val placeables = measurables.map { it.measure(rowConstraints) }
        val width = constraints.maxWidth
        
        var yPos = 0
        var xPos = 0
        var rowHeight = 0
        
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        val rowPlaceables = mutableListOf<MutableList<Pair<Int, Int>>>()
        var currentRowPlaceables = mutableListOf<Pair<Int, Int>>()
        
        placeables.forEach { placeable ->
            if (xPos + placeable.width > width) {
                rowWidths.add(xPos)
                rowHeights.add(rowHeight)
                rowPlaceables.add(currentRowPlaceables)
                
                xPos = 0
                yPos += rowHeight
                rowHeight = 0
                currentRowPlaceables = mutableListOf()
            }
            
            currentRowPlaceables.add(Pair(placeable.width, placeable.height))
            xPos += placeable.width
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        
        if (currentRowPlaceables.isNotEmpty()) {
            rowWidths.add(xPos)
            rowHeights.add(rowHeight)
            rowPlaceables.add(currentRowPlaceables)
        }
        
        val totalHeight = rowHeights.sum()
        
        layout(width, totalHeight) {
            var y = 0
            
            rowPlaceables.forEachIndexed { rowIndex, row ->
                val rowWidth = rowWidths[rowIndex]
                val rowHeight = rowHeights[rowIndex]
                
                var x = when (horizontalArrangement) {
                    Arrangement.End -> width - rowWidth
                    Arrangement.Center -> (width - rowWidth) / 2
                    Arrangement.Start -> 0
                    else -> 0
                }
                
                var placeableIndex = 0
                for (i in 0 until rowIndex) {
                    placeableIndex += rowPlaceables[i].size
                }
                
                row.forEachIndexed { index, (placeableWidth, placeableHeight) ->
                    val placeable = placeables[placeableIndex + index]
                    
                    val placeY = when (verticalArrangement) {
                        Arrangement.Top -> y
                        Arrangement.Bottom -> y + rowHeight - placeableHeight
                        Arrangement.Center -> y + (rowHeight - placeableHeight) / 2
                        Arrangement.Start -> y
                        else -> y
                    }
                    
                    placeable.place(x, placeY)
                    x += placeableWidth
                }
                
                y += rowHeight
            }
        }
    }
} 