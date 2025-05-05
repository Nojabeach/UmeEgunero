package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * Pantalla para componer un nuevo mensaje
 */
@Composable
fun ComponerMensajeScreen(
    navController: NavController,
    receiverId: String? = null,
    messageType: String? = null,
    viewModel: com.tfg.umeegunero.feature.common.comunicacion.viewmodel.NewMessageViewModel = hiltViewModel()
) {
    // Simplemente redirigimos a la nueva pantalla
    NewMessageScreen(
        receiverId = receiverId,
        messageType = messageType,
        onBack = {
            navController.popBackStack()
        },
        onMessageSent = {
            navController.popBackStack()
        }
    )
} 