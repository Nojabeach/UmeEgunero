package com.tfg.umeegunero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Contenido del navigation drawer
 */
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    onItemClick: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    // Cabecera del drawer
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "UmeEgunero",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Implementación simplificada para compilar
        Button(onClick = { onCloseDrawer() }) {
            Text("Cerrar menú")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = { onItemClick(AppScreens.Welcome.route) }) {
            Text("Ir a Inicio")
        }
    }
} 