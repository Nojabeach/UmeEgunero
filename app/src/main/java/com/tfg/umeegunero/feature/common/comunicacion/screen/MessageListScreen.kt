package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

/**
 * Pantalla temporal para la lista de mensajes
 */
@Composable
fun MessageListScreen(
    navController: NavController,
    viewModel: ViewModel
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Sistema de mensajería en construcción")
    }
} 