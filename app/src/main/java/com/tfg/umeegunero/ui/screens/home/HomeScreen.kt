package com.tfg.umeegunero.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.navigation.SYNC_SCREEN

const val HOME_SCREEN = "home_screen"

@Composable
fun HomeScreen(
    navController: NavController,
    // ... existing code ...
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_screen_title)) },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(SYNC_SCREEN) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.sync_screen_title)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Contenido principal de la pantalla
        // Aquí puedes añadir el contenido principal de tu pantalla de inicio
        // envuelto en un Box, Column o Row con el modificador padding(paddingValues)
    }
} 