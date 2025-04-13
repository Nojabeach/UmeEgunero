package com.tfg.umeegunero.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy

/**
 * Clase de utilidad para gestionar las animaciones de transición entre pantallas
 * en la navegación de la aplicación.
 */
object NavAnimations {

    // Constantes de duración para las animaciones
    private const val ANIMATION_DURATION_MS = 500
    private const val SLIDE_DISTANCE = 300
    
    /**
     * Establece las animaciones de entrada y salida para transiciones entre pantallas.
     * 
     * @param backStackEntry Entrada actual de la pila de navegación
     * @param animationType Tipo de animación a aplicar (DASHBOARD o DETAIL)
     * @return Par con las animaciones de entrada y salida configuradas
     */
    fun setTransitionAnimations(
        backStackEntry: NavBackStackEntry?,
        animationType: AnimationType
    ): Pair<EnterTransition, ExitTransition> {
        return when (animationType) {
            AnimationType.DASHBOARD -> dashboardTransition()
            AnimationType.DETAIL -> detailTransition()
        }
    }
    
    /**
     * Animaciones para las transiciones a pantallas de tipo dashboard.
     * Utiliza un efecto de desvanecimiento con expansión para una experiencia fluida.
     * 
     * @return Par con las animaciones de entrada y salida
     */
    private fun dashboardTransition(): Pair<EnterTransition, ExitTransition> {
        val enterTransition = fadeIn(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + expandIn(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            expandFrom = Alignment.Center
        )
        
        val exitTransition = fadeOut(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + shrinkOut(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            shrinkTowards = Alignment.Center
        )
        
        return Pair(enterTransition, exitTransition)
    }
    
    /**
     * Animaciones para las transiciones a pantallas de detalle.
     * Utiliza un efecto de deslizamiento horizontal para indicar navegación a mayor profundidad.
     * 
     * @return Par con las animaciones de entrada y salida
     */
    private fun detailTransition(): Pair<EnterTransition, ExitTransition> {
        val enterTransition = slideInHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { SLIDE_DISTANCE }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
        
        val exitTransition = slideOutHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { -SLIDE_DISTANCE }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
        
        return Pair(enterTransition, exitTransition)
    }
    
    /**
     * Tipos de animación disponibles para las transiciones entre pantallas.
     */
    enum class AnimationType {
        /**
         * Animación para pantallas principales o dashboard, con efecto de expansión central.
         */
        DASHBOARD,
        
        /**
         * Animación para pantallas de detalle, con efecto de deslizamiento horizontal.
         */
        DETAIL
    }
} 