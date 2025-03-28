package com.tfg.umeegunero.feature.common.academico.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListProfesorViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListProfesorUiState
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.ui.tooling.preview.Preview
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla que muestra el listado de profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProfesorScreen(
    viewModel: ListProfesorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddProfesor: () -> Unit,
    onNavigateToEditProfesor: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.loadProfesores()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profesores") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
    onNavigateBack: () -> Unit,
    onNavigateToAddProfesor: () -> Unit,
    onNavigateToEditProfesor: (String) -> Unit
) {
    ListProfesorScreen(
        onNavigateBack = onNavigateBack,
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
            onNavigateBack = {},
            onNavigateToAddProfesor = {},
            onNavigateToEditProfesor = {}
        )
    }
} 