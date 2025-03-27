package com.tfg.umeegunero.feature.common.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla genérica para funcionalidades aún no implementadas
 * 
 * @param title Título de la pantalla
 * @param onNavigateBack Función para volver atrás
 * @param onMenuClick Función para abrir el menú lateral
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DummyScreen(
    title: String,
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú principal",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Construction,
                contentDescription = "En construcción",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(bottom = 24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Esta funcionalidad se encuentra en desarrollo",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$title próximamente disponible",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DummyScreenPreview() {
    UmeEguneroTheme {
        DummyScreen(
            title = "Gestión de Cursos",
            onNavigateBack = {}
        )
    }
}

/**
 * Versión simplificada para cuando no necesitamos navegación hacia atrás
 */
@Composable
fun DummyScreen(title: String) {
    DummyScreen(
        title = title,
        onNavigateBack = {},
        onMenuClick = {}
    )
} 