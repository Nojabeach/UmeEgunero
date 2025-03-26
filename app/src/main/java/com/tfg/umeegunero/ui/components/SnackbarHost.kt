package com.tfg.umeegunero.ui.components

import androidx.compose.material3.SnackbarHost as Material3SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Material3SnackbarHost(
        hostState = hostState,
        modifier = modifier
    )
} 