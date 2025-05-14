package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListadoPreRegistroUiState(
    val isLoading: Boolean = false,
    val registros: List<RegistroDiario> = emptyList(),
    val error: String? = null
)

data class RegistroDiario(
    val id: String,
    val titulo: String,
    val fecha: String,
    val detalles: String
)

@HiltViewModel
class ListadoPreRegistroDiarioViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListadoPreRegistroUiState())
    val uiState: StateFlow<ListadoPreRegistroUiState> = _uiState.asStateFlow()
    
    init {
        cargarRegistros()
    }
    
    private fun cargarRegistros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulamos carga de datos
            val registrosSimulados = listOf(
                RegistroDiario("1", "Registro mañana", "12/05/2023", "Detalle registro 1"),
                RegistroDiario("2", "Registro tarde", "12/05/2023", "Detalle registro 2"),
                RegistroDiario("3", "Registro especial", "13/05/2023", "Detalle registro 3")
            )
            
            _uiState.update { it.copy(
                isLoading = false,
                registros = registrosSimulados
            )}
        }
    }
    
    fun eliminarRegistro(id: String) {
        viewModelScope.launch {
            val nuevaLista = _uiState.value.registros.filter { it.id != id }
            _uiState.update { it.copy(registros = nuevaLista) }
        }
    }
}

@Composable
fun ListadoPreRegistroDiarioScreen(
    viewModel: ListadoPreRegistroDiarioViewModel = hiltViewModel(),
    onNavigateToDetalle: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Listado de Pre-Registros Diarios",
            style = MaterialTheme.typography.headlineMedium
        )
        
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = registro.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = registro.fecha,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.eliminarRegistro(registro.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar registro"
                            )
                        }
                    }
                }
            }
        }
    }
} 