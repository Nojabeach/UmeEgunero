package com.tfg.umeegunero.feature.common.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.ui.theme.AppColors
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel para la pantalla de cambio de tema
 */
@dagger.hilt.android.lifecycle.HiltViewModel
class CambiarTemaViewModel @javax.inject.Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : androidx.lifecycle.ViewModel() {
    // Obtener el tema actual como estado
    val temaActual = preferenciasRepository.temaPreferencia
    
    // Cambiar el tema de la aplicación
    fun cambiarTema(tema: TemaPref) {
        Timber.d("Cambiando tema a: $tema")
        viewModelScope.launch {
            preferenciasRepository.setTemaPreferencia(tema)
        }
    }
}

/**
 * Pantalla para cambiar el tema de la aplicación
 * 
 * @param navController Controlador de navegación para volver atrás
 * @param viewModel ViewModel con la lógica de negocio
 */
/**
 * Pantalla para cambiar el tema de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarTemaScreen(
    navController: NavController,
    viewModel: CambiarTemaViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val temaActual by viewModel.temaActual.collectAsState(initial = TemaPref.SYSTEM)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar tema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explicación
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Personaliza la apariencia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selecciona el tema que prefieras para la aplicación. Puedes elegir entre tema claro, oscuro o seguir la configuración del sistema.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Tema actual
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tema actual:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = when(temaActual) {
                                TemaPref.DARK -> "Oscuro"
                                TemaPref.LIGHT -> "Claro"
                                TemaPref.SYSTEM -> "Sistema"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Opciones de tema
            item {
                // Tema claro
                OpcionTemaCard(
                    titulo = "Tema claro",
                    descripcion = "Interfaz con fondo claro y textos oscuros",
                    icono = Icons.Default.BrightnessHigh,
                    iconoColor = AppColors.Yellow500,
                    seleccionado = temaActual == TemaPref.LIGHT,
                    onClick = {
                        scope.launch {
                            viewModel.cambiarTema(TemaPref.LIGHT)
                        }
                    }
                )
            }
            
            item {
                // Tema oscuro
                OpcionTemaCard(
                    titulo = "Tema oscuro",
                    descripcion = "Interfaz con fondo oscuro y textos claros",
                    icono = Icons.Default.DarkMode,
                    iconoColor = AppColors.Blue700,
                    seleccionado = temaActual == TemaPref.DARK,
                    onClick = {
                        scope.launch {
                            viewModel.cambiarTema(TemaPref.DARK)
                        }
                    }
                )
            }
            
            item {
                // Tema del sistema
                OpcionTemaCard(
                    titulo = "Según el sistema",
                    descripcion = "Utiliza el tema configurado en tu dispositivo",
                    icono = Icons.Default.Phone,
                    iconoColor = AppColors.Green500,
                    seleccionado = temaActual == TemaPref.SYSTEM,
                    onClick = {
                        scope.launch {
                            viewModel.cambiarTema(TemaPref.SYSTEM)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Tarjeta de opción de tema
 */
@Composable
fun OpcionTemaCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    iconoColor: Color,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (seleccionado) 2.dp else 0.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconoColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconoColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (seleccionado) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
} 