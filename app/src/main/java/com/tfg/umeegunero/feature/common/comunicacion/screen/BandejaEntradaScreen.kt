package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import timber.log.Timber

/**
 * Pantalla de bandeja de entrada del sistema de comunicación
 *
 * NOTA: Esta pantalla es una redirección a UnifiedInboxScreen.
 * Se mantiene por compatibilidad con navegaciones anteriores, pero en realidad
 * toda la funcionalidad de bandeja de entrada está unificada en UnifiedInboxScreen.
 * 
 * Para futuros desarrollos, usar directamente UnifiedInboxScreen en lugar de esta.
 */
@Composable
fun BandejaEntradaScreen(
    navController: NavController,
    mensajeSistema: String? = null
) {
    LaunchedEffect(Unit) {
        Timber.d("BandejaEntradaScreen: Redirigiendo a UnifiedInboxScreen")
    }
    
    // Simplemente redirigimos a la pantalla de bandeja unificada
    UnifiedInboxScreen(
        onNavigateToMessage = { messageId ->
            navController.navigate(AppScreens.MessageDetail.createRoute(messageId))
        },
        onNavigateToNewMessage = {
            navController.navigate(AppScreens.NewMessage.createRoute())
        },
        onBack = {
            navController.popBackStack()
        }
    )
} 