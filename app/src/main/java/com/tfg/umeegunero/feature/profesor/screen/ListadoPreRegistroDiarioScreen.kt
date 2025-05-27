/**
 * Pantalla de Listado de Pre-Registros Diarios
 *
 * Esta pantalla muestra un listado de los registros diarios de actividad
 * para los profesores, permitiendo acceder a detalles y generar informes
 * de asistencia a partir de ellos.
 *
 * @author Maitane Ibañez Irazabal
 * @since 1.0.0
 */
package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.time.LocalDate

/**
 * Estado UI para la pantalla de listado de pre-registros diarios
 *
 * @property isLoading Indica si se están cargando datos
 * @property registros Lista de registros diarios
 * @property error Mensaje de error si lo hay
 * @property claseSeleccionadaId ID de la clase seleccionada para generar informe
 * @property showInformeDialog Indica si se debe mostrar el diálogo de informe
 */
data class ListadoPreRegistroUiState(
    val isLoading: Boolean = false,
    val registros: List<RegistroDiario> = emptyList(),
    val error: String? = null,
    val claseSeleccionadaId: String? = null,
    val showInformeDialog: Boolean = false
)

/**
 * Modelo de datos para representar un registro diario
 *
 * @property id Identificador único del registro
 * @property titulo Título descriptivo del registro
 * @property fecha Fecha del registro en formato de texto
 * @property detalles Detalles adicionales del registro
 * @property claseId ID de la clase a la que pertenece el registro
 */
data class RegistroDiario(
    val id: String,
    val titulo: String,
    val fecha: String,
    val detalles: String,
    val claseId: String = "" // Añadido para vincular con la clase
)

/**
 * ViewModel para gestionar la lógica de la pantalla de listado de pre-registros diarios
 */
@HiltViewModel
class ListadoPreRegistroDiarioViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListadoPreRegistroUiState())
    val uiState: StateFlow<ListadoPreRegistroUiState> = _uiState.asStateFlow()
    
    init {
        cargarRegistros()
    }
    
    /**
     * Carga los registros diarios disponibles
     */
    private fun cargarRegistros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulamos carga de datos
            val registrosSimulados = listOf(
                RegistroDiario("1", "Registro mañana", "12/05/2023", "Detalle registro 1", "clase_1a"),
                RegistroDiario("2", "Registro tarde", "12/05/2023", "Detalle registro 2", "clase_1b"),
                RegistroDiario("3", "Registro especial", "13/05/2023", "Detalle registro 3", "clase_2a")
            )
            
            _uiState.update { it.copy(
                isLoading = false,
                registros = registrosSimulados
            )}
        }
    }
    
    /**
     * Elimina un registro diario
     *
     * @param id Identificador del registro a eliminar
     */
    fun eliminarRegistro(id: String) {
        viewModelScope.launch {
            val nuevaLista = _uiState.value.registros.filter { it.id != id }
            _uiState.update { it.copy(registros = nuevaLista) }
        }
    }
    
    /**
     * Muestra el diálogo para generar un informe de asistencia
     *
     * @param claseId ID de la clase para la que se generará el informe
     */
    fun mostrarDialogoInforme(claseId: String) {
        _uiState.update { it.copy(
            claseSeleccionadaId = claseId,
            showInformeDialog = true
        ) }
        Timber.d("Mostrando diálogo para generar informe de la clase: $claseId")
    }
    
    /**
     * Oculta el diálogo para generar un informe de asistencia
     */
    fun ocultarDialogoInforme() {
        _uiState.update { it.copy(showInformeDialog = false) }
    }
}

/**
 * Pantalla que muestra el listado de pre-registros diarios
 *
 * @param viewModel ViewModel que gestiona los datos de la pantalla
 * @param onNavigateToDetalle Callback para navegar al detalle de un registro
 * @param onNavigateToInformeAsistencia Callback para navegar al informe de asistencia
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoPreRegistroDiarioScreen(
    viewModel: ListadoPreRegistroDiarioViewModel = hiltViewModel(),
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToInformeAsistencia: (String, LocalDate?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado con título y botón de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Listado de Pre-Registros Diarios",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Botón para crear nuevo registro
            FloatingActionButton(
                onClick = { /* Acción para crear nuevo registro */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nuevo registro"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (uiState.registros.isEmpty()) {
            Text("No hay registros disponibles")
        } else {
            uiState.registros.forEach { registro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToDetalle(registro.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = registro.titulo,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = registro.fecha,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Row {
                                // Botón para generar informe de asistencia
                                IconButton(
                                    onClick = { viewModel.mostrarDialogoInforme(registro.claseId) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "Generar informe de asistencia",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Botón para eliminar registro
                                IconButton(
                                    onClick = { viewModel.eliminarRegistro(registro.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar registro",
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
    
    // Diálogo para confirmar generación de informe
    if (uiState.showInformeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.ocultarDialogoInforme() },
            title = { Text("Generar Informe de Asistencia") },
            text = { Text("¿Deseas generar un informe de asistencia para esta clase?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.claseSeleccionadaId?.let { claseId ->
                            onNavigateToInformeAsistencia(claseId, LocalDate.now())
                        }
                        viewModel.ocultarDialogoInforme()
                    }
                ) {
                    Text("Generar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.ocultarDialogoInforme() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Vista previa de la pantalla de listado de pre-registros diarios
 */
@Preview(showBackground = true)
@Composable
fun ListadoPreRegistroDiarioScreenPreview() {
    UmeEguneroTheme {
        // Crear registros ficticios para la vista previa
        val registros = listOf(
            RegistroDiario("1", "Registro mañana", "12/05/2023", "Detalle registro 1", "clase_1a"),
            RegistroDiario("2", "Registro tarde", "12/05/2023", "Detalle registro 2", "clase_1b"),
            RegistroDiario("3", "Registro especial", "13/05/2023", "Detalle registro 3", "clase_2a")
        )
        
        // Estado UI para la vista previa
        val uiState = ListadoPreRegistroUiState(
            isLoading = false,
            registros = registros
        )
        
        // Crear un viewModel con estado fijo para la vista previa
        val viewModel = remember {
            object {
                val uiState = MutableStateFlow(uiState)
                fun mostrarDialogoInforme(claseId: String) {}
                fun ocultarDialogoInforme() {}
                fun eliminarRegistro(id: String) {}
            }
        }
        
        // Mostrar la pantalla con los datos ficticios
        ListadoPreRegistroDiarioScreen(
            viewModel = viewModel as ListadoPreRegistroDiarioViewModel,
            onNavigateToDetalle = {},
            onNavigateToInformeAsistencia = { _, _ -> }
        )
    }
} 