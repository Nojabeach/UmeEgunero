package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.feature.common.academico.model.Clase
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Pantalla para la gestión de clases
 * Permite ver la lista de clases, añadir nuevas y editar existentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionClasesScreen(
    navController: NavController,
    cursoId: String
) {
    val clases = remember {
        listOf(
            Clase("1", "1ºA", "Grupo A", cursoId, "profesor1"),
            Clase("2", "1ºB", "Grupo B", cursoId, "profesor2"),
            Clase("3", "1ºC", "Grupo C", cursoId, "profesor3")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clases") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // En una implementación completa, navegaríamos a la pantalla de añadir clase
                    navController.navigate(AppScreens.Dummy.createRoute("Añadir Clase"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir clase"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(clases) { clase ->
                ClaseItem(
                    clase = clase,
                    onEditClick = {
                        // En una implementación completa, navegaríamos a la pantalla de editar clase
                        navController.navigate(AppScreens.Dummy.createRoute("Editar Clase: ${clase.nombre}"))
                    },
                    onItemClick = {
                        // Navegar a la pantalla de detalle de la clase
                        navController.navigate(AppScreens.Dummy.createRoute("Detalle de Clase: ${clase.nombre}"))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaseItem(
    clase: Clase,
    onEditClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = clase.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = clase.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar clase"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GestionClasesScreenPreview() {
    val clasesPreview = listOf(
        Clase(
            id = "1",
            nombre = "1ºA",
            descripcion = "Clase A de 1º curso",
            cursoId = "curso1",
            profesorId = "profesor1"
        ),
        Clase(
            id = "2",
            nombre = "1ºB",
            descripcion = "Clase B de 1º curso",
            cursoId = "curso1",
            profesorId = "profesor2"
        )
    )
    
    MaterialTheme {
        GestionClasesScreen(
            navController = rememberNavController(),
            cursoId = ""
        )
    }
} 