package com.tfg.umeegunero.feature.centro.screen.academico

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.centro.viewmodel.AddCursoUiState
import com.tfg.umeegunero.feature.centro.viewmodel.AddCursoViewModel
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCursoScreen(
    viewModel: AddCursoViewModel,
    cursoId: String = "",
    centroId: String = "",
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Efecto para cargar el curso si estamos en modo edición
    LaunchedEffect(cursoId) {
        if (cursoId.isNotBlank()) {
            viewModel.loadCurso(cursoId)
        }
    }

    // Efecto para establecer el ID del centro
    LaunchedEffect(centroId) {
        if (centroId.isNotBlank()) {
            viewModel.setCentroId(centroId)
        }
    }

    // Efecto para mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Efecto para navegar de vuelta cuando se guarda con éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onCursoAdded()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Editar Curso" else "Añadir Curso",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de curso
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Curso",
                tint = CentroColor,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre del curso
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = viewModel::updateNombre,
                        label = { Text("Nombre del curso *") },
                        isError = uiState.nombreError != null,
                        supportingText = { uiState.nombreError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción
                    OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = viewModel::updateDescripcion,
                        label = { Text("Descripción *") },
                        isError = uiState.descripcionError != null,
                        supportingText = { uiState.descripcionError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rango de edad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edad mínima
                        OutlinedTextField(
                            value = uiState.edadMinima,
                            onValueChange = viewModel::updateEdadMinima,
                            label = { Text("Edad mínima *") },
                            isError = uiState.edadMinimaError != null,
                            supportingText = { uiState.edadMinimaError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Right) }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Edad máxima
                        OutlinedTextField(
                            value = uiState.edadMaxima,
                            onValueChange = viewModel::updateEdadMaxima,
                            label = { Text("Edad máxima *") },
                            isError = uiState.edadMaximaError != null,
                            supportingText = { uiState.edadMaximaError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Año académico
                    OutlinedTextField(
                        value = uiState.anioAcademico,
                        onValueChange = viewModel::updateAnioAcademico,
                        label = { Text("Año académico (ej: 2023-2024) *") },
                        isError = uiState.anioAcademicoError != null,
                        supportingText = { uiState.anioAcademicoError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Sección de años de nacimiento
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Años de nacimiento *",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Añade los años de nacimiento de los alumnos que pertenecen a este curso",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Button(
                                onClick = { viewModel.calcularAniosNacimientoAutomaticamente() },
                                enabled = uiState.edadMinima.isNotBlank() && uiState.edadMinimaError == null &&
                                         uiState.edadMaxima.isNotBlank() && uiState.edadMaximaError == null &&
                                         uiState.anioAcademico.isNotBlank() && uiState.anioAcademicoError == null,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Calcular")
                            }
                        }
                        
                        // Campo para añadir nuevo año
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.nuevoAnioNacimiento,
                                onValueChange = viewModel::updateNuevoAnioNacimiento,
                                label = { Text("Año de nacimiento") },
                                isError = uiState.nuevoAnioNacimientoError != null,
                                supportingText = { uiState.nuevoAnioNacimientoError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { 
                                        viewModel.addAnioNacimiento()
                                        focusManager.clearFocus()
                                    }
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { 
                                    viewModel.addAnioNacimiento()
                                    focusManager.clearFocus()
                                },
                                enabled = uiState.nuevoAnioNacimiento.isNotBlank() && uiState.nuevoAnioNacimientoError == null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir año",
                                    tint = if (uiState.nuevoAnioNacimiento.isNotBlank() && uiState.nuevoAnioNacimientoError == null) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                        
                        // Lista de años añadidos
                        if (uiState.aniosNacimiento.isEmpty()) {
                            Text(
                                text = "No hay años de nacimiento añadidos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.aniosNacimiento) { anio ->
                                    ElevatedFilterChip(
                                        selected = true,
                                        onClick = { },
                                        label = { Text(anio.toString()) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { viewModel.removeAnioNacimiento(anio) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Eliminar año",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Explicación sobre los años de nacimiento
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "¿Por qué son importantes los años de nacimiento?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "En entornos educativos rurales o con menor población, es común agrupar alumnos de diferentes edades en un mismo curso. Esta información permite al sistema identificar correctamente qué alumnos pertenecen a cada curso según su año de nacimiento.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Puedes calcular automáticamente los años basados en el rango de edad y el año académico, o añadirlos manualmente según las necesidades específicas de tu centro.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de guardar
            Button(
                onClick = viewModel::saveCurso,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditMode) "Actualizar Curso" else "Guardar Curso",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota sobre campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HiltAddCursoScreen(
    viewModel: AddCursoViewModel = hiltViewModel(),
    cursoId: String = "",
    centroId: String = "",
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    AddCursoScreen(
        viewModel = viewModel,
        cursoId = cursoId,
        centroId = centroId,
        onNavigateBack = onNavigateBack,
        onCursoAdded = onCursoAdded
    )
}

@Preview(name = "Light Mode")
@Composable
fun AddCursoScreenPreviewLight() {
    UmeEguneroTheme {
        Surface {
            AddCursoScreenPreviewContent()
        }
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddCursoScreenPreviewDark() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddCursoScreenPreviewContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCursoScreenPreviewContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    // Estado simulado para el preview
    val nombre = "1º de Primaria"
    val descripcion = "Primer curso de educación primaria"
    val edadMinima = "6"
    val edadMaxima = "7"
    val anioAcademico = "2023-2024"
    val aniosNacimiento = listOf(2017, 2018)
    val nuevoAnioNacimiento = ""
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir Curso",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de curso
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Curso",
                tint = CentroColor,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre del curso
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { },
                        label = { Text("Nombre del curso *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { },
                        label = { Text("Descripción *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rango de edad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edad mínima
                        OutlinedTextField(
                            value = edadMinima,
                            onValueChange = { },
                            label = { Text("Edad mínima *") },
                            modifier = Modifier.weight(1f)
                        )

                        // Edad máxima
                        OutlinedTextField(
                            value = edadMaxima,
                            onValueChange = { },
                            label = { Text("Edad máxima *") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Año académico
                    OutlinedTextField(
                        value = anioAcademico,
                        onValueChange = { },
                        label = { Text("Año académico (ej: 2023-2024) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Sección de años de nacimiento
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Años de nacimiento *",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Añade los años de nacimiento de los alumnos que pertenecen a este curso",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Button(
                                onClick = { },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Calcular")
                            }
                        }
                        
                        // Campo para añadir nuevo año
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nuevoAnioNacimiento,
                                onValueChange = { },
                                label = { Text("Año de nacimiento") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir año",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Lista de años añadidos
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(aniosNacimiento) { anio ->
                                ElevatedFilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text(anio.toString()) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar año",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Explicación sobre los años de nacimiento
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "¿Por qué son importantes los años de nacimiento?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "En entornos educativos rurales o con menor población, es común agrupar alumnos de diferentes edades en un mismo curso. Esta información permite al sistema identificar correctamente qué alumnos pertenecen a cada curso según su año de nacimiento.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Puedes calcular automáticamente los años basados en el rango de edad y el año académico, o añadirlos manualmente según las necesidades específicas de tu centro.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de guardar
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar Curso",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nota sobre campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 