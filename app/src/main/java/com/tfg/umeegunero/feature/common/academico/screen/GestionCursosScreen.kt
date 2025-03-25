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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.model.Curso
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Pantalla para la gestión de cursos
 * Permite ver la lista de cursos, añadir nuevos y editar existentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCursosScreen(
    navController: NavController,
    centroId: String
) {
    val cursos = remember {
        listOf(
            Curso("1", "1º ESO", "Educación Secundaria Obligatoria - Primer curso"),
            Curso("2", "2º ESO", "Educación Secundaria Obligatoria - Segundo curso"),
            Curso("3", "3º ESO", "Educación Secundaria Obligatoria - Tercer curso"),
            Curso("4", "4º ESO", "Educación Secundaria Obligatoria - Cuarto curso")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cursos") },
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
                    // En una implementación completa, navegaríamos a la pantalla de añadir curso
                    navController.navigate(AppScreens.Dummy.createRoute("Añadir Curso"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir curso"
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
            items(cursos) { curso ->
                CursoItem(
                    curso = curso,
                    onEditClick = {
                        // En una implementación completa, navegaríamos a la pantalla de editar curso
                        navController.navigate(AppScreens.Dummy.createRoute("Editar Curso: ${curso.nombre}"))
                    },
                    onItemClick = {
                        // Navegar a la pantalla de gestión de clases para este curso
                        navController.navigate(AppScreens.GestionClases.createRoute(curso.id))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CursoItem(
    curso: Curso,
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
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = curso.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar curso"
                )
            }
        }
    }
} 