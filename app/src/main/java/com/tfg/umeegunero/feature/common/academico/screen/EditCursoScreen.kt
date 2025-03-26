package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.EditCursoViewModel
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import com.tfg.umeegunero.ui.components.SnackbarHost
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCursoScreen(
    navController: NavController,
    viewModel: EditCursoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            snackbarHostState.showSnackbar(success)
            viewModel.clearSuccess()
            navController.navigateUp()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Curso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCurso() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        UmeEguneroTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    FormProgressIndicator(
                        currentStep = 1,
                        totalSteps = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextFieldWithError(
                            value = uiState.nombre,
                            onValueChange = { viewModel.updateNombre(it) },
                            label = { Text("Nombre del curso") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = false,
                            errorMessage = null
                        )

                        OutlinedTextFieldWithError(
                            value = uiState.descripcion,
                            onValueChange = { viewModel.updateDescripcion(it) },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = false,
                            errorMessage = null
                        )

                        OutlinedTextFieldWithError(
                            value = uiState.edadMinima.toString(),
                            onValueChange = { viewModel.updateEdadMinima(it.toIntOrNull() ?: 0) },
                            label = { Text("Edad mínima") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = false,
                            errorMessage = null
                        )

                        OutlinedTextFieldWithError(
                            value = uiState.edadMaxima.toString(),
                            onValueChange = { viewModel.updateEdadMaxima(it.toIntOrNull() ?: 0) },
                            label = { Text("Edad máxima") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = false,
                            errorMessage = null
                        )

                        OutlinedTextFieldWithError(
                            value = uiState.anioAcademico,
                            onValueChange = { viewModel.updateAnioAcademico(it) },
                            label = { Text("Año académico") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = false,
                            errorMessage = null
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activo")
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = uiState.activo,
                                onCheckedChange = { viewModel.updateActivo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
} 