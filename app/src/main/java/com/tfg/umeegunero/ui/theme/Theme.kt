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

// Definición de colores base (ejemplo, ajustar según diseño original si se conoce)
private val PurplePrimary = Color(0xFF6750A4)
private val PurpleSecondary = Color(0xFF625B71)
private val PurpleTertiary = Color(0xFF7D5260)
private val PurpleError = Color(0xFFB3261E)

private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)

private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFF7D5260)

private val White = Color.White
private val Black = Color.Black
private val LightGrey = Color(0xFFF5F5F5)
private val DarkGrey = Color(0xFF1C1B1F)
private val NearBlack = Color(0xFF202124) // Un gris muy oscuro para fondos oscuros

// Usaremos los colores definidos en AppColors y CustomColors donde sea apropiado
// import com.tfg.umeegunero.ui.theme.AppColors // Importar si es necesario
// import com.tfg.umeegunero.ui.theme.CustomColors // Importar si es necesario

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary, // Ejemplo: Usar el púrpura definido
    onPrimary = White,       // Texto blanco sobre primario
    primaryContainer = PurpleGrey80, // Un contenedor claro relacionado con el primario
    onPrimaryContainer = PurplePrimary, // Texto primario sobre el contenedor
    secondary = PurpleSecondary, // Ejemplo: usar secundario definido
    onSecondary = White,         // Texto blanco sobre secundario
    secondaryContainer = Pink80, // Contenedor claro relacionado con secundario/terciario
    onSecondaryContainer = PurpleSecondary,
    tertiary = PurpleTertiary, // Ejemplo: usar terciario definido
    onTertiary = White,        // Texto blanco sobre terciario
    background = LightGrey,    // Fondo claro
    onBackground = Black,      // Texto negro sobre fondo claro
    surface = White,           // Superficie blanca
    onSurface = Black,         // Texto negro sobre superficie
    error = AppColors.Error, // Usar Error de AppColors
    onError = White          // Texto blanco sobre error
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleGrey80,   // Color primario más claro para tema oscuro
    onPrimary = PurplePrimary,  // Texto oscuro sobre primario claro
    primaryContainer = PurplePrimary, // Contenedor oscuro relacionado
    onPrimaryContainer = White,         // Texto claro sobre contenedor oscuro
    secondary = Pink80,         // Secundario más claro
    onSecondary = PurpleSecondary,// Texto oscuro sobre secundario claro
    secondaryContainer = PurpleSecondary, // Contenedor oscuro
    onSecondaryContainer = White,
    tertiary = Pink40,          // Terciario más oscuro
    onTertiary = White,
    background = DarkGrey,      // Fondo oscuro
    onBackground = White,        // Texto claro sobre fondo oscuro
    surface = NearBlack,       // Superficie muy oscura
    onSurface = White,           // Texto claro sobre superficie
    error = AppColors.Error, // Usar Error de AppColors
    onError = White            // Texto claro sobre error
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