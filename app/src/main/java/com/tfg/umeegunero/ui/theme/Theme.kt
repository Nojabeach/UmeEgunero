package com.tfg.umeegunero.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tfg.umeegunero.ui.theme.AppColors

// Usaremos los colores definidos en AppColors y CustomColors donde sea apropiado


private val LightColorScheme = lightColorScheme(
    primary = AppColors.PurplePrimary,
    onPrimary = AppColors.White,
    primaryContainer = AppColors.PurpleGrey80,
    onPrimaryContainer = AppColors.PurplePrimary,
    secondary = AppColors.PurpleSecondary,
    onSecondary = AppColors.White,
    secondaryContainer = AppColors.Pink80,
    onSecondaryContainer = AppColors.PurpleSecondary,
    tertiary = AppColors.PurpleTertiary,
    onTertiary = AppColors.White,
    background = AppColors.LightGrey,
    onBackground = AppColors.Black,
    surface = AppColors.White,
    onSurface = AppColors.Black,
    error = AppColors.Error,
    onError = AppColors.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PurpleGrey80,
    onPrimary = AppColors.PurplePrimary,
    primaryContainer = AppColors.PurplePrimary,
    onPrimaryContainer = AppColors.White,
    secondary = AppColors.Pink80,
    onSecondary = AppColors.PurpleSecondary,
    secondaryContainer = AppColors.PurpleSecondary,
    onSecondaryContainer = AppColors.White,
    tertiary = AppColors.Pink40,
    onTertiary = AppColors.White,
    background = AppColors.DarkGrey,
    onBackground = AppColors.White,
    surface = AppColors.NearBlack,
    onSurface = AppColors.White,
    error = AppColors.Error,
    onError = AppColors.White
)

@Composable
fun UmeEguneroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}