package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tfg.umeegunero.ui.screens.home.HOME_SCREEN
import com.tfg.umeegunero.ui.screens.sync.SyncScreen

const val SYNC_SCREEN = "sync_screen"

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = HOME_SCREEN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing code ...
        
        composable(SYNC_SCREEN) {
            SyncScreen()
        }
        
        // ... existing code ...
    }
} 