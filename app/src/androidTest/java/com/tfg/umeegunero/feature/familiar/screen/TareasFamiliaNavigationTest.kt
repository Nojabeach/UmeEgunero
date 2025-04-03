package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.PrioridadTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.familiar.viewmodel.TareasFamiliaViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de navegación para las pantallas de tareas de familiares
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TareasFamiliaNavigationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun navigateFromTareasList_ToTareaDetail() {
        // Este test es una versión simplificada que muestra cómo probar la navegación
        // En un test real, usaríamos mocks para los ViewModels y datos
        
        composeTestRule.setContent {
            UmeEguneroTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "tareas_list"
                ) {
                    composable("tareas_list") {
                        // Pantalla de lista de tareas simulada
                        TareasFamiliaScreenMock(
                            onTareaClick = { tareaId ->
                                navController.navigate("tarea_detail/$tareaId")
                            }
                        )
                    }
                    
                    composable(
                        route = "tarea_detail/{tareaId}",
                    ) { backStackEntry ->
                        val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
                        // Pantalla de detalle simulada
                        DetalleTareaSimulada(tareaId = tareaId)
                    }
                }
            }
        }
        
        // Verificar que estamos en la pantalla de lista
        composeTestRule.onNodeWithText("Tareas del Alumno").assertIsDisplayed()
        
        // Hacer clic en la primera tarea
        composeTestRule.onNodeWithText("Tarea 1").performClick()
        
        // Verificar que navegamos al detalle
        composeTestRule.onNodeWithText("Detalle de la tarea: tarea1").assertIsDisplayed()
    }
}

/**
 * Pantalla simulada para las pruebas
 */
@androidx.compose.runtime.Composable
fun TareasFamiliaScreenMock(
    onTareaClick: (String) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Tareas del Alumno") }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Lista simulada de tareas
            androidx.compose.material3.Button(
                onClick = { onTareaClick("tarea1") }
            ) {
                androidx.compose.material3.Text("Tarea 1")
            }
            
            androidx.compose.material3.Button(
                onClick = { onTareaClick("tarea2") }
            ) {
                androidx.compose.material3.Text("Tarea 2")
            }
        }
    }
}

/**
 * Pantalla de detalle simulada para las pruebas
 */
@androidx.compose.runtime.Composable
fun DetalleTareaSimulada(tareaId: String) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Detalle de Tarea") }
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("Detalle de la tarea: $tareaId")
        }
    }
} 