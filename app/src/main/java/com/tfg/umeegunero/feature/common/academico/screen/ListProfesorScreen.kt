package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListProfesorViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListProfesorUiState
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.navigation.compose.rememberNavController

/**
 * Pantalla que muestra el listado de profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProfesorScreen(
    viewModel: ListProfesorViewModel = hiltViewModel(),
    onNavigateToAddProfesor: () -> Unit,
    onNavigateToEditProfesor: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfesores()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Profesores") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Implementar navegación hacia atrás */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProfesor) {
                Icon(Icons.Default.Add, contentDescription = "Añadir profesor")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.profesores.isEmpty() -> {
                    Text(
                        text = "No hay profesores registrados",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.profesores) { profesor ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onNavigateToEditProfesor(profesor.dni) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${profesor.nombre} ${profesor.apellidos}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "DNI: ${profesor.dni}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProfesor(profesor.dni) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar profesor",
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
    }
}

@Composable
fun HiltListProfesorScreen(
    onNavigateToAddProfesor: () -> Unit,
    onNavigateToEditProfesor: (String) -> Unit
) {
    ListProfesorScreen(
        onNavigateToAddProfesor = onNavigateToAddProfesor,
        onNavigateToEditProfesor = onNavigateToEditProfesor
    )
}

@Preview(
    name = "ListProfesorScreen",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
fun ListProfesorScreenPreview() {
    UmeEguneroTheme {
        val navController = rememberNavController()
        ListProfesorScreen(
            onNavigateToAddProfesor = { /* TODO */ },
            onNavigateToEditProfesor = { /* TODO */ }
        )
    }
} 