package com.tfg.umeegunero.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Utilidades para implementar paginación en listas
 */
object PaginationUtils {
    
    /**
     * Estado para la paginación
     */
    data class PaginationState(
        val currentPage: Int = 0,
        val pageSize: Int = 20,
        val isLoading: Boolean = false,
        val isLastPage: Boolean = false,
        val totalItems: Int = 0
    ) {
        val totalPages: Int
            get() = if (totalItems % pageSize == 0) {
                totalItems / pageSize
            } else {
                (totalItems / pageSize) + 1
            }
            
        val canLoadMore: Boolean
            get() = !isLoading && !isLastPage
    }
    
    /**
     * Detecta cuando se debe cargar la siguiente página en un LazyListState
     */
    @Composable
    fun LazyListState.shouldLoadMore(
        buffer: Int = 5,
        onLoadMore: () -> Unit
    ) {
        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false
                
                lastVisibleItem.index >= layoutInfo.totalItemsCount - buffer
            }
        }
        
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                onLoadMore()
            }
        }
    }
    
    /**
     * Detecta cuando se debe cargar la siguiente página en un LazyGridState
     */
    @Composable
    fun LazyGridState.shouldLoadMore(
        buffer: Int = 10,
        onLoadMore: () -> Unit
    ) {
        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false
                
                lastVisibleItem.index >= layoutInfo.totalItemsCount - buffer
            }
        }
        
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                onLoadMore()
            }
        }
    }
    
    /**
     * Componente de paginador manual con botones
     */
    @Composable
    fun Paginator(
        currentPage: Int,
        totalPages: Int,
        onPageChange: (Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        if (totalPages <= 1) return
        
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón anterior
            OutlinedButton(
                onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                enabled = currentPage > 0
            ) {
                Text("Anterior")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Número de página actual
            Text(
                text = "${currentPage + 1} de $totalPages",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón siguiente
            OutlinedButton(
                onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1
            ) {
                Text("Siguiente")
            }
        }
    }
    
    /**
     * Indicador de carga para mostrar al final de una lista mientras se cargan más elementos
     */
    @Composable
    fun LoadingIndicator(
        isLoading: Boolean,
        modifier: Modifier = Modifier
    ) {
        if (isLoading) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Cargando más elementos...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    /**
     * Mensaje para cuando no hay más elementos para cargar
     */
    @Composable
    fun EndOfListMessage(
        isLastPage: Boolean,
        modifier: Modifier = Modifier
    ) {
        if (isLastPage) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay más elementos para mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 