package com.tfg.umeegunero.feature.common.dashboard.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.dashboard.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.navigation.NavigationDrawerContent
import com.tfg.umeegunero.navigation.NavigationStructure
import kotlinx.coroutines.launch

@Composable
fun CentroDashboardScreen(
    navController: NavController,
    viewModel: CentroDashboardViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    navController = navController,
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    navItems = NavigationStructure.getCentroNavItems()
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard Centro Educativo") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Manejo de navegación
                LaunchedEffect(navController.currentBackStackEntry?.destination?.route) {
                    val route = navController.currentBackStackEntry?.destination?.route
                    when {
                        route?.startsWith(AppScreens.GestionCursos.route) == true -> {
                            val centroId = route.split("/").lastOrNull()
                            if (centroId != null) {
                                viewModel.navigateToGestionCursos(centroId)
                            }
                        }
                        route?.startsWith(AppScreens.GestionClases.route) == true -> {
                            val cursoId = route.split("/").lastOrNull()
                            if (cursoId != null) {
                                viewModel.navigateToGestionClases(cursoId)
                            }
                        }
                    }
                }
            }
        }
    }
} 