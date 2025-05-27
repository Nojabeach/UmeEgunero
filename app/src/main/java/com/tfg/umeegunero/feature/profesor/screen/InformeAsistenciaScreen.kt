/**
 * Pantalla de Informe de Asistencia
 *
 * Esta pantalla permite visualizar y generar informes detallados de asistencia 
 * para una clase específica en una fecha determinada. Muestra estadísticas de asistencia
 * y listados de alumnos presentes y ausentes.
 *
 * @author Maitane Ibañez Irazabal
 * @since 1.0.0
 */
package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.EstadoAsistencia
import com.tfg.umeegunero.data.model.RegistroAsistencia
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.DateUtils
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import java.time.ZoneId
import java.util.Date

/**
 * Estado UI para la pantalla de informe de asistencia
 */
data class InformeAsistenciaUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val clase: Clase? = null,
    val fecha: LocalDate = LocalDate.now(),
    val alumnosPresentes: List<Alumno> = emptyList(),
    val alumnosAusentes: List<Alumno> = emptyList(),
    val todosLosAlumnos: List<Alumno> = emptyList(),
    val porcentajeAsistencia: Float = 0f
)

/**
 * ViewModel para gestionar el informe de asistencia de alumnos
 */
@HiltViewModel
class InformeAsistenciaViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val asistenciaRepository: AsistenciaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InformeAsistenciaUiState())
    val uiState: StateFlow<InformeAsistenciaUiState> = _uiState.asStateFlow()
    
    /**
     * Carga los datos de asistencia para una clase y fecha específicas
     *
     * @param claseId ID de la clase para la que generar el informe
     * @param fecha Fecha opcional para el informe (por defecto es la fecha actual)
     */
    fun cargarInformeAsistencia(claseId: String, fecha: LocalDate? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Establecer la fecha
                val fechaInforme = fecha ?: LocalDate.now()
                _uiState.update { it.copy(fecha = fechaInforme) }
                
                // 1. Obtener datos de la clase
                when (val claseResult = claseRepository.getClaseById(claseId)) {
                    is Result.Success<*> -> {
                        val clase = claseResult.data as Clase
                        
                        // 2. Obtener todos los alumnos de la clase
                        try {
                            // Usamos el método que devuelve List<Alumno> directamente
                            val todosLosAlumnos = alumnoRepository.getAlumnosPorClase(claseId)
                            
                            // 3. Obtener registro de asistencia para la fecha específica
                            val fechaDate = Date.from(fechaInforme.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            val registroAsistencia = asistenciaRepository.obtenerRegistroAsistencia(
                                claseId = claseId,
                                fecha = fechaDate
                            )
                            
                            // Obtener IDs de alumnos presentes
                            val alumnosIdsPresentes = registroAsistencia?.estadosAsistencia
                                ?.filter { it.value == EstadoAsistencia.PRESENTE }
                                ?.map { it.key }
                                ?: emptyList()
                            
                            // Filtrar alumnos presentes
                            val alumnosPresentes = todosLosAlumnos.filter { alumno ->
                                alumnosIdsPresentes.contains(alumno.id)
                            }
                            
                            // Filtrar alumnos ausentes
                            val alumnosAusentes = todosLosAlumnos.filter { alumno ->
                                !alumnosIdsPresentes.contains(alumno.id)
                            }
                            
                            // Calcular porcentaje de asistencia
                            val porcentajeAsistencia = if (todosLosAlumnos.isNotEmpty()) {
                                alumnosPresentes.size.toFloat() / todosLosAlumnos.size.toFloat() * 100f
                            } else {
                                0f
                            }
                            
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    clase = clase,
                                    todosLosAlumnos = todosLosAlumnos,
                                    alumnosPresentes = alumnosPresentes,
                                    alumnosAusentes = alumnosAusentes,
                                    porcentajeAsistencia = porcentajeAsistencia
                                )
                            }
                        } catch (e: Exception) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "Error al cargar alumnos: ${e.message}"
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar la clase: ${claseResult.exception?.message}"
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar informe de asistencia")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Genera un PDF con el informe de asistencia
     */
    fun generarPdfInforme() {
        viewModelScope.launch {
            Timber.d("Generando PDF del informe de asistencia")
            // Implementación pendiente
        }
    }
    
    /**
     * Comparte el informe de asistencia
     */
    fun compartirInforme() {
        viewModelScope.launch {
            Timber.d("Compartiendo informe de asistencia")
            // Implementación pendiente
        }
    }
}

/**
 * Pantalla para mostrar el informe de asistencia de alumnos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeAsistenciaScreen(
    claseId: String,
    fecha: LocalDate? = null,
    onNavigateBack: () -> Unit,
    viewModel: InformeAsistenciaViewModel = hiltViewModel()
) {
    // Cargar datos del informe cuando la pantalla se muestra
    LaunchedEffect(claseId, fecha) {
        viewModel.cargarInformeAsistencia(claseId, fecha)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informe de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.generarPdfInforme() }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Generar PDF"
                        )
                    }
                    IconButton(onClick = { viewModel.compartirInforme() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir Informe"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Informe de Asistencia",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Clase: ${uiState.clase?.nombre ?: "Sin nombre"}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Fecha: ${uiState.fecha.format(dateFormatter)}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Estadísticas",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Text(
                                    text = "Total alumnos: ${uiState.todosLosAlumnos.size}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Presentes: ${uiState.alumnosPresentes.size}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Ausentes: ${uiState.alumnosAusentes.size}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Porcentaje de asistencia: ${String.format("%.1f", uiState.porcentajeAsistencia)}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    item {
                        Text(
                            text = "Alumnos Presentes (${uiState.alumnosPresentes.size} de ${uiState.todosLosAlumnos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        
                        if (uiState.alumnosPresentes.isNotEmpty()) {
                            Column {
                                uiState.alumnosPresentes.forEach { alumno ->
                                    Text(
                                        text = "• ${alumno.nombre} ${alumno.apellidos}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No hay alumnos presentes",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    item {
                        Text(
                            text = "Alumnos Ausentes (${uiState.alumnosAusentes.size} de ${uiState.todosLosAlumnos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        
                        if (uiState.alumnosAusentes.isNotEmpty()) {
                            Column {
                                uiState.alumnosAusentes.forEach { alumno ->
                                    Text(
                                        text = "• ${alumno.nombre} ${alumno.apellidos}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No hay alumnos ausentes",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista previa de la pantalla de informe de asistencia
 */
@Preview(showBackground = true)
@Composable
fun InformeAsistenciaScreenPreview() {
    UmeEguneroTheme {
        // Crear una clase ficticia para la vista previa
        val clase = Clase(
            id = "clase1",
            nombre = "3º de Primaria A",
            cursoId = "curso1"
        )
        
        // Crear una lista de alumnos ficticia
        val alumnos = listOf(
            Alumno(id = "1", nombre = "María", apellidos = "García López", dni = "12345678A"),
            Alumno(id = "2", nombre = "Juan", apellidos = "Martínez Sánchez", dni = "23456789B"),
            Alumno(id = "3", nombre = "Ana", apellidos = "Rodríguez Pérez", dni = "34567890C")
        )
        
        // Estado UI para la vista previa
        val uiState = InformeAsistenciaUiState(
            isLoading = false,
            clase = clase,
            todosLosAlumnos = alumnos,
            alumnosPresentes = alumnos.take(2),
            alumnosAusentes = alumnos.drop(2),
            porcentajeAsistencia = 66.67f
        )
        
        // Crear un viewModel con estado fijo para la vista previa
        val viewModel = object {
            val uiState = MutableStateFlow(uiState)
            fun cargarInformeAsistencia(claseId: String, fecha: LocalDate?) {}
            fun generarPdfInforme() {}
            fun compartirInforme() {}
        }
        
        // Mostrar la pantalla con los datos ficticios
        InformeAsistenciaScreen(
            claseId = "clase1",
            fecha = LocalDate.now(),
            onNavigateBack = {},
            viewModel = viewModel as InformeAsistenciaViewModel
        )
    }
} 