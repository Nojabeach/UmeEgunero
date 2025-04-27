package com.tfg.umeegunero.util

import androidx.compose.ui.graphics.Color
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.ui.theme.AppColors

fun getUserColor(tipo: com.tfg.umeegunero.data.model.TipoUsuario?): Color = AppColors.getUserColor(tipo)
