/**
 * Módulo de gestión de administradores del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para listar y gestionar los
 * administradores del sistema, con diferentes niveles de acceso.
 */
package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.R
import com.tfg.umeegunero.components.UserCard
import com.tfg.umeegunero.feature.admin.viewmodel.ListAdministradoresViewModel
import com.tfg.umeegunero.ui.theme.AdminColor
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.rememberNavController
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Punto de entrada para la pantalla de lista de administradores con inyección de dependencias Hilt.
 * 
 * Este componente actúa como wrapper para la inyección del ViewModel a través de Hilt,
 * proporcionando una instancia del ViewModel a la pantalla principal.
 * 
 * @param onNavigateBack Callback para navegar hacia atrás
 * 
 * @see ListAdministradoresScreen
 * @see ListAdministradoresViewModel
 */
@Composable
fun HiltListAdministradoresScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ListAdministradoresViewModel = hiltViewModel()
    ListAdministradoresScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Pantalla principal para la gestión de administradores del sistema.
 * 
 * Esta pantalla proporciona una interfaz completa para visualizar y gestionar
 * los administradores del sistema, con diferentes niveles de acceso según
 * el tipo de administrador.
 * 
 * ## Características
 * - Lista de administradores con detalles
 * - Búsqueda en tiempo real
 * - Creación de nuevos administradores (solo ADMIN_APP)
 * - Visualización de detalles
 * 
 * ## Funcionalidades
 * - Filtrado por nombre o email
 * - Visualización de información de contacto
 * - Gestión de permisos
 * - Feedback visual de acciones
 * 
 * ## Seguridad
 * - Control de acceso basado en roles
 * - Validación de permisos
 * - Registro de acciones
 * 
 * @param viewModel ViewModel que gestiona la lógica de la lista de administradores
 * @param onNavigateBack Callback para navegar hacia atrás
 * 
 * @see ListAdministradoresViewModel
 * @see UserCard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAdministradoresScreen(
    viewModel: ListAdministradoresViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Determinar si el usuario puede crear administradores
    val puedeCrearAdmin = uiState.currentUser?.perfiles?.any { it.tipo.name == "ADMIN_APP" } == true

    // Mostrar mensaje de error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administradores") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
        ) {
            // Botón para crear administrador (solo ADMIN_APP)
            if (puedeCrearAdmin) {
                Button(
                    onClick = { /* TODO: Navegar a pantalla de creación de administrador */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Crear administrador")
                }
            }

            // Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar administradores") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            // Contenido principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AdminColor
                        )
                    }
                    uiState.filteredAdministradores.isEmpty() -> {
                        Text(
                            text = "No se encontraron administradores",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filteredAdministradores) { admin ->
                                UserCard(
                                    nombre = "${admin.nombre} ${admin.apellidos}",
                                    email = admin.email,
                                    telefono = admin.telefono ?: "No disponible",
                                    avatarUrl = admin.avatarUrl,
                                    tipoUsuario = admin.perfiles.firstOrNull()?.toString() ?: "Administrador",
                                    onClick = { /* Detalles del administrador si es necesario */ }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista previa de la pantalla de lista de administradores en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun ListAdministradoresScreenPreview() {
    UmeEguneroTheme {
        ListAdministradoresScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
}

/**
 * Vista previa de la pantalla de lista de administradores en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ListAdministradoresScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ListAdministradoresScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
} 