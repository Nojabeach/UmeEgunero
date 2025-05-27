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
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.util.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tfg.umeegunero.data.model.RegistroDiario
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Clase
import com.google.firebase.Timestamp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider

/**
 * Estado UI para la pantalla de listado de pre-registros diarios
 *
 * @property isLoading Indica si se están cargando datos
 * @property registros Lista de registros diarios
 * @property error Mensaje de error si lo hay
 * @property claseSeleccionadaId ID de la clase seleccionada para generar informe
 * @property showInformeDialog Indica si se debe mostrar el diálogo de informe
 * @property showEliminarDialog Indica si se debe mostrar el diálogo de confirmación de eliminación
 * @property registroIdAEliminar ID del registro que se quiere eliminar
 * @property totalAlumnosPresentes Contador de alumnos presentes en los registros
 * @property mensaje Mensaje de éxito o información para mostrar al usuario
 */
data class ListadoPreRegistroUiState(
    val isLoading: Boolean = false,
    val registros: List<RegistroDiario> = emptyList(),
    val error: String? = null,
    val claseSeleccionadaId: String? = null,
    val showInformeDialog: Boolean = false,
    val showEliminarDialog: Boolean = false,
    val registroIdAEliminar: String? = null,
    val totalAlumnosPresentes: Int = 0,
    val mensaje: String? = null
)

/**
 * ViewModel para gestionar la lógica de la pantalla de listado de pre-registros diarios
 */
@HiltViewModel
class ListadoPreRegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener la fecha actual en formato yyyy-MM-dd
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaActual = dateFormat.format(Date())
                
                // Obtener usuario actual (profesor) 
                val usuarioActual = authRepository.getUsuarioActual()
                
                if (usuarioActual != null) {
                    // Obtener clases del profesor
                    val clasesResult = claseRepository.getClasesByProfesor(usuarioActual.dni)
                    
                    if (clasesResult is Result.Success<*>) {
                        val clases = clasesResult.data as List<Clase>
                        val todosLosRegistros = mutableListOf<RegistroDiario>()
                        var totalPresentes = 0
                        
                        // Para cada clase, obtener los registros de hoy
                        for (clase in clases) {
                            val registrosResult = registroDiarioRepository.getRegistrosDiariosPorClaseYFecha(
                                claseId = clase.id,
                                fecha = fechaActual
                            )
                            
                            if (registrosResult is Result.Success<*>) {
                                val registrosClase = registrosResult.data as List<RegistroDiario>
                                todosLosRegistros.addAll(registrosClase)
                                
                                // Contar alumnos presentes
                                val presentesEnClase = registrosClase.count { 
                                    it.presente == true 
                                }
                                totalPresentes += presentesEnClase
                                
                                Timber.d("Clase ${clase.nombre}: ${registrosClase.size} registros, $presentesEnClase presentes")
                            }
                        }
                        
                        _uiState.update { it.copy(
                            isLoading = false,
                            registros = todosLosRegistros,
                            totalAlumnosPresentes = totalPresentes
                        )}
                        
                        Timber.d("Cargados ${todosLosRegistros.size} registros totales, $totalPresentes alumnos presentes")
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Error al cargar las clases: ${(clasesResult as? Result.Error)?.exception?.message ?: "Error desconocido"}"
                        )}
                    }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al obtener el usuario actual: No hay sesión iniciada"
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros diarios")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )}
            }
        }
    }
    
    /**
     * Prepara la eliminación de un registro mostrando un diálogo de confirmación
     *
     * @param id Identificador del registro que se quiere eliminar
     */
    fun prepararEliminarRegistro(id: String) {
        _uiState.update { it.copy(
            showEliminarDialog = true,
            registroIdAEliminar = id
        ) }
    }
    
    /**
     * Cancela la eliminación de un registro cerrando el diálogo
     */
    fun cancelarEliminarRegistro() {
        _uiState.update { it.copy(
            showEliminarDialog = false,
            registroIdAEliminar = null
        ) }
    }
    
    /**
     * Elimina un registro diario después de confirmar
     */
    fun confirmarEliminarRegistro() {
        val idAEliminar = _uiState.value.registroIdAEliminar ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Primero verificar si el registro existe
                val registroExiste = registroDiarioRepository.verificarRegistroExiste(idAEliminar)
                
                if (!registroExiste) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "El registro ya no existe o ha sido eliminado",
                        showEliminarDialog = false,
                        registroIdAEliminar = null
                    ) }
                    return@launch
                }
                
                // Si existe, entonces intentar eliminarlo
                val resultado = registroDiarioRepository.eliminarRegistro(idAEliminar)
                
                if (resultado) {
                    // Actualizar la lista de registros excluyendo el eliminado
                    val nuevaLista = _uiState.value.registros.filter { it.id != idAEliminar }
                    
                    // Recalcular el total de presentes
                    val totalPresentes = nuevaLista.count { it.presente == true }
                    
                    _uiState.update { it.copy(
                        isLoading = false,
                        registros = nuevaLista,
                        totalAlumnosPresentes = totalPresentes,
                        mensaje = "Registro eliminado correctamente",
                        showEliminarDialog = false,
                        registroIdAEliminar = null
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al eliminar el registro",
                        showEliminarDialog = false,
                        registroIdAEliminar = null
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar registro diario: $idAEliminar")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error inesperado al eliminar: ${e.message}",
                    showEliminarDialog = false,
                    registroIdAEliminar = null
                ) }
            }
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
    
    /**
     * Limpia el mensaje de éxito o información
     */
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
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
    val context = LocalContext.current
    
    // Mostrar mensajes o errores como Toast
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarError()
        }
    }
    
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
        
        // Contador de alumnos presentes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alumnos presentes hoy:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Text(
                    text = "${uiState.totalAlumnosPresentes}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (uiState.registros.isEmpty()) {
            Text(
                text = "No hay registros disponibles para la fecha actual",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // Encabezado de la lista
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alumno",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Asistencia",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = "Acciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Lista de registros
            uiState.registros.forEach { registro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToDetalle(registro.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (registro.presente == true) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
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
                                // Intentamos mostrar el nombre completo del alumno en lugar de solo el ID
                                Text(
                                    text = registro.alumnoNombre ?: registro.alumnoId,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(registro.fecha.toDate()),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = { viewModel.mostrarDialogoInforme(registro.claseId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = "Generar informe",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (registro.presente == true) "Asistencia: Presente" else "Asistencia: Ausente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (registro.presente == true) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
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
                                    onClick = { viewModel.prepararEliminarRegistro(registro.id) }
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
    
    // Diálogo para confirmar eliminación de registro
    if (uiState.showEliminarDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarRegistro() },
            title = { Text("Eliminar Registro") },
            text = { Text("¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmarEliminarRegistro() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminarRegistro() }) {
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
            RegistroDiario(
                id = "1", 
                alumnoId = "alumno1",
                claseId = "clase_1a",
                fecha = Timestamp.now(),
                presente = true,
                observaciones = "Detalle registro 1"
            ),
            RegistroDiario(
                id = "2", 
                alumnoId = "alumno2",
                claseId = "clase_1b",
                fecha = Timestamp.now(),
                presente = false,
                observaciones = "Detalle registro 2"
            ),
            RegistroDiario(
                id = "3", 
                alumnoId = "alumno3",
                claseId = "clase_2a",
                fecha = Timestamp.now(),
                presente = true,
                observaciones = "Detalle registro 3"
            )
        )
        
        // Estado UI para la vista previa
        val uiState = ListadoPreRegistroUiState(
            isLoading = false,
            registros = registros,
            totalAlumnosPresentes = 2
        )
        
        // Crear un viewModel con estado fijo para la vista previa
        val viewModel = remember {
            object {
                val uiState = MutableStateFlow(uiState)
                fun mostrarDialogoInforme(claseId: String) {}
                fun ocultarDialogoInforme() {}
                fun prepararEliminarRegistro(id: String) {}
                fun cancelarEliminarRegistro() {}
                fun confirmarEliminarRegistro() {}
                fun limpiarMensaje() {}
                fun limpiarError() {}
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