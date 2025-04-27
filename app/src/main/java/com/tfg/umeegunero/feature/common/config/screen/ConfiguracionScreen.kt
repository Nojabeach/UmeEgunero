package com.tfg.umeegunero.feature.common.config.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.components.TemaActual
import com.tfg.umeegunero.ui.components.TemaSelector
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModelBase
import com.tfg.umeegunero.feature.common.config.viewmodel.IPreferenciasRepository
import com.tfg.umeegunero.feature.common.config.viewmodel.TestConfiguracionViewModel
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Tipos de perfil para la configuración
 */
enum class PerfilConfiguracion {
    ADMIN,
    PROFESOR,
    FAMILIAR,
    CENTRO,
    SISTEMA
}

/**
 * Pantalla de configuración común para todos los perfiles de usuario
 * Permite cambiar el tema de la aplicación y muestra las funcionalidades próximas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModelBase = hiltViewModel<ConfiguracionViewModel>(),
    perfil: PerfilConfiguracion = PerfilConfiguracion.SISTEMA,
    onNavigateBack: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Configurar el título según el perfil
    val titulo = when (perfil) {
        PerfilConfiguracion.ADMIN -> "Configuración Administrador"
        PerfilConfiguracion.PROFESOR -> "Configuración Profesor"
        PerfilConfiguracion.FAMILIAR -> "Configuración Familiar"
        PerfilConfiguracion.CENTRO -> "Configuración Centro"
        PerfilConfiguracion.SISTEMA -> "Configuración"
    }
    
    // Configurar las próximas características según el perfil
    val proximasCaracteristicas = when (perfil) {
        PerfilConfiguracion.ADMIN -> "• Gestión completa de parámetros del sistema\n" +
                "• Paneles de administración de servicios cloud\n" +
                "• Estadísticas de uso y rendimiento\n" +
                "• Sistema de auditoría y registros de seguridad\n" +
                "• Configuración de copias de seguridad\n" +
                "• Personalización de la experiencia por defecto"
        
        PerfilConfiguracion.PROFESOR -> "• Gestión de preferencias de notificaciones\n" +
                "• Personalización de la interfaz de evaluación\n" +
                "• Configuración de mensajería con familias\n" +
                "• Opciones avanzadas para informes académicos\n" +
                "• Integración con herramientas educativas externas\n" +
                "• Sincronización con calendarios académicos"
        
        PerfilConfiguracion.FAMILIAR -> "• Gestión de perfil familiar completo\n" +
                "• Opciones de privacidad y compartición de datos\n" +
                "• Configuración de notificaciones\n" +
                "• Opciones de accesibilidad\n" +
                "• Gestión de dispositivos autorizados\n" +
                "• Sincronización con calendario familiar"
        
        PerfilConfiguracion.CENTRO -> "• Gestión centralizada de credenciales\n" +
                "• Personalizacion de comunicaciones institucionales\n" +
                "• Configuración de calendario académico\n" +
                "• Gestión de permisos para personal docente\n" +
                "• Configuración de integración con sistemas externos\n" +
                "• Personalización de la imagen corporativa"
        
        PerfilConfiguracion.SISTEMA -> "• Opciones generales del sistema\n" +
                "• Preferencias de visualización\n" +
                "• Configuración de notificaciones\n" +
                "• Opciones de accesibilidad\n" +
                "• Gestión de datos personales\n" +
                "• Configuración de privacidad"
    }

    // Configurar el color según el perfil
    val topBarColor = when (perfil) {
        PerfilConfiguracion.ADMIN -> AdminColor
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú principal",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Efecto para mostrar errores
        LaunchedEffect(uiState.error) {
            uiState.error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Selector de tema
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tema de la aplicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = uiState.temaSeleccionado == TemaPref.SYSTEM,
                                onClick = { viewModel.setTema(TemaPref.SYSTEM) }
                            )
                            Text(
                                text = "Usar tema del sistema",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = uiState.temaSeleccionado == TemaPref.LIGHT,
                                onClick = { viewModel.setTema(TemaPref.LIGHT) }
                            )
                            Text(
                                text = "Tema claro",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = uiState.temaSeleccionado == TemaPref.DARK,
                                onClick = { viewModel.setTema(TemaPref.DARK) }
                            )
                            Text(
                                text = "Tema oscuro",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de próximas funcionalidades
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Próximamente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = proximasCaracteristicas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Preview para la pantalla de configuración
 */
@Preview(showSystemUi = true)
@Composable
fun ConfiguracionScreenPreview() {
    val viewModel = TestConfiguracionViewModel(FakePreferenciasRepository())
    UmeEguneroTheme {
        ConfiguracionScreen(
            viewModel = viewModel,
            onNavigateBack = {},
            onMenuClick = {}
        )
    }
}

/**
 * Preview para la pantalla de configuración de profesor
 */
@Preview(showSystemUi = true)
@Composable
fun ConfiguracionProfesorScreenPreview() {
    val viewModel = TestConfiguracionViewModel(FakePreferenciasRepository())
    UmeEguneroTheme {
        ConfiguracionScreen(
            viewModel = viewModel,
            onNavigateBack = {},
            onMenuClick = {}
        )
    }
}

/**
 * Preview para la pantalla de configuración de familiar
 */
@Preview(showSystemUi = true)
@Composable
fun ConfiguracionFamiliarScreenPreview() {
    val viewModel = TestConfiguracionViewModel(FakePreferenciasRepository())
    UmeEguneroTheme {
        ConfiguracionScreen(
            viewModel = viewModel,
            onNavigateBack = {},
            onMenuClick = {}
        )
    }
}

/**
 * Repositorio falso para el preview
 * Esta implementación es solo para el preview y simula el repositorio real
 */
private class FakePreferenciasRepository : IPreferenciasRepository {
    override val temaPreferencia: Flow<TemaPref> = MutableStateFlow(TemaPref.SYSTEM)
    
    override suspend fun setTemaPreferencia(tema: TemaPref) {
        (temaPreferencia as MutableStateFlow<TemaPref>).value = tema
    }
} 