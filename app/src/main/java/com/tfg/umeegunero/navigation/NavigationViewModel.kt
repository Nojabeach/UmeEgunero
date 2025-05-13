package com.tfg.umeegunero.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.tfg.umeegunero.data.model.MessageType
import timber.log.Timber

/**
 * Comando de navegación para la navegación basada en eventos
 * Este enfoque permite desacoplar la navegación de las actividades/fragmentos
 */
sealed class NavigationCommand {
    /**
     * Navega a una nueva ruta
     */
    data class NavigateTo(val route: String) : NavigationCommand()
    
    /**
     * Retrocede a la pantalla anterior
     */
    object NavigateBack : NavigationCommand()
    
    /**
     * Navega a una nueva ruta y borra toda la pila de navegación
     */
    data class NavigateToWithClearBackstack(val route: String) : NavigationCommand()
}

/**
 * ViewModel que maneja la navegación entre pantallas.
 * Proporciona un flujo de comandos de navegación para ser observado por el componente de navegación.
 */
@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    
    private val _navigationCommands = MutableSharedFlow<NavigationCommand>()
    val navigationCommands: SharedFlow<NavigationCommand> = _navigationCommands.asSharedFlow()
    
    /**
     * Navega a una ruta específica
     */
    suspend fun navigateTo(route: String) {
        Timber.d("Navegando a: $route")
        _navigationCommands.emit(NavigationCommand.NavigateTo(route))
    }
    
    /**
     * Retrocede a la pantalla anterior
     */
    suspend fun navigateBack() {
        Timber.d("Navegando hacia atrás")
        _navigationCommands.emit(NavigationCommand.NavigateBack)
    }
    
    /**
     * Navega a una ruta y borra la pila de navegación
     */
    suspend fun navigateToWithClearBackstack(route: String) {
        Timber.d("Navegando a: $route con limpieza de pila")
        _navigationCommands.emit(NavigationCommand.NavigateToWithClearBackstack(route))
    }
    
    /**
     * Maneja la navegación desde una notificación
     */
    suspend fun handleNotificationNavigation(messageId: String, messageType: String?) {
        Timber.d("Manejando navegación desde notificación - messageId: $messageId, tipo: $messageType")
        
        when (messageType) {
            MessageType.CHAT.name -> {
                // Para mensajes de chat, navegar a la conversación
                _navigationCommands.emit(
                    NavigationCommand.NavigateTo(
                        AppScreens.MessageDetail.createRoute(messageId)
                    )
                )
            }
            MessageType.ANNOUNCEMENT.name -> {
                // Para comunicados, navegar al detalle del comunicado
                _navigationCommands.emit(
                    NavigationCommand.NavigateTo(
                        AppScreens.DetalleComunicado.createRoute(messageId)
                    )
                )
            }
            MessageType.INCIDENT.name, 
            MessageType.ATTENDANCE.name,
            MessageType.DAILY_RECORD.name,
            MessageType.NOTIFICATION.name,
            MessageType.SYSTEM.name -> {
                // Para otros tipos, navegar al detalle del mensaje unificado
                _navigationCommands.emit(
                    NavigationCommand.NavigateTo(
                        AppScreens.MessageDetail.createRoute(messageId)
                    )
                )
            }
            else -> {
                // Si no se especifica el tipo o no se reconoce, ir a la bandeja de entrada unificada
                _navigationCommands.emit(
                    NavigationCommand.NavigateTo(
                        AppScreens.UnifiedInbox.route
                    )
                )
            }
        }
    }
} 