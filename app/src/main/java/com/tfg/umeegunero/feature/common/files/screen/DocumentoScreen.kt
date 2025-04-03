package com.tfg.umeegunero.feature.common.files.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.files.composable.VisorArchivo
import com.tfg.umeegunero.feature.common.files.viewmodel.DocumentoViewModel

/**
 * Pantalla para visualizar documentos
 *
 * @param navController Controlador de navegación
 * @param documentoUrl URL del documento a visualizar
 * @param documentoNombre Nombre opcional del documento
 * @param viewModel ViewModel para la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentoScreen(
    navController: NavController,
    documentoUrl: String,
    documentoNombre: String? = null,
    viewModel: DocumentoViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    
    // Inicializar ViewModel con URL y nombre
    LaunchedEffect(documentoUrl, documentoNombre) {
        viewModel.inicializar(documentoUrl, documentoNombre)
    }
    
    // Mostrar snackbar si hay mensaje de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.nombre ?: "Documento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.url.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "URL no válida o no especificada",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                VisorArchivo(
                    uiState = uiState,
                    onDescargar = { viewModel.descargarArchivo() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
} 