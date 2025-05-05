package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Pantalla de bandeja de entrada del sistema de comunicación
 */
@Composable
fun BandejaEntradaScreen(
    navController: NavController,
    mensajeSistema: String? = null
) {
    // Simplemente redirigimos a la pantalla de bandeja unificada
    UnifiedInboxScreen(
        onNavigateToMessage = { messageId ->
            navController.navigate("message_detail/$messageId")
        },
        onNavigateToNewMessage = {
            navController.navigate("new_message")
        },
        onBack = {
            navController.popBackStack()
        }
    )
} 