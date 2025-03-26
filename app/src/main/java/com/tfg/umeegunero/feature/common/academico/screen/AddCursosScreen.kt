package com.tfg.umeegunero.feature.common.academico.screen

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
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursosViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddCursosUiState
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla para añadir o editar un curso
 * @param viewModel ViewModel que maneja la lógica de la pantalla
 * @param onNavigateBack Callback para volver a la pantalla anterior
 * @param onCursoAdded Callback que se ejecuta cuando se añade un curso exitosamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCursosScreen(
    viewModel: AddCursosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    // TODO: Mejoras pendientes para la pantalla de añadir/editar curso
    // - Implementar vista previa del curso con estructura completa
    // - Añadir sugerencias automáticas de asignaturas según el nivel
    // - Mostrar plantillas predefinidas para cursos estándar
    // - Implementar integración con currículo oficial por comunidad autónoma
    // - Añadir opción para duplicar curso existente como base
    // - Permitir importación y exportación de estructura de curso
    // - Mostrar estimación de carga lectiva y distribución horaria
    // - Implementar validación avanzada con sugerencias de corrección

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Efecto para cargar el curso si estamos en modo edición
    LaunchedEffect(viewModel.cursoId) {
        if (viewModel.cursoId.isNotBlank()) {
            viewModel.loadCurso(viewModel.cursoId)
        }
    }

    // ... existing code ...
}

@Composable
fun HiltAddCursosScreen(
    viewModel: AddCursosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCursoAdded: () -> Unit
) {
    AddCursosScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onCursoAdded = onCursoAdded
    )
}

@Preview(
    name = "AddCursosScreen Preview Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun AddCursosScreenPreviewLight() {
    AddCursosScreenPreviewContent()
}

@Preview(
    name = "AddCursosScreen Preview Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AddCursosScreenPreviewDark() {
    AddCursosScreenPreviewContent()
}

@Composable
private fun AddCursosScreenPreviewContent() {
    UmeEguneroTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AddCursosScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = {},
                onCursoAdded = {}
            )
        }
    }
} 