package com.tfg.umeegunero.feature.common.config.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.feature.common.config.components.TemaActual
import com.tfg.umeegunero.feature.common.config.components.TemaSelector
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModelBase
import com.tfg.umeegunero.feature.common.config.viewmodel.IPreferenciasRepository
import com.tfg.umeegunero.feature.common.config.viewmodel.TestConfiguracionViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModelBase = hiltViewModel<ConfiguracionViewModel>(),
    perfil: PerfilConfiguracion = PerfilConfiguracion.SISTEMA
) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    
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
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            TemaSelector(
                temaSeleccionado = uiState.temaSeleccionado,
                onTemaSeleccionado = { viewModel.setTema(it) }
            )
            
            TemaActual(
                temaSeleccionado = uiState.temaSeleccionado
            )
            
            // Más configuraciones pendientes (tarjeta con las futuras funcionalidades)
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
            perfil = PerfilConfiguracion.ADMIN
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
            perfil = PerfilConfiguracion.PROFESOR
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
            perfil = PerfilConfiguracion.FAMILIAR
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