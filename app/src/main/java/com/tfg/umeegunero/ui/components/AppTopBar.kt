package com.tfg.umeegunero.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/**
 * Barra superior personalizada para la aplicación.
 * 
 * @param title Título que se mostrará en la barra
 * @param navigationIcon Icono para el botón de navegación
 * @param onNavigationClick Acción a ejecutar cuando se pulsa el botón de navegación
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navegar hacia atrás"
                    )
                }
            }
        }
    )
} 