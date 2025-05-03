package com.tfg.umeegunero.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import timber.log.Timber
import java.util.Date

/**
 * Componente para registrar en Logcat información sobre el ciclo de vida de una pantalla.
 * 
 * Registra en el log cuándo una pantalla se abre y cuándo se cierra, junto con el tiempo
 * que estuvo activa. Utiliza etiquetas visuales para facilitar la identificación en Logcat.
 * 
 * Ejemplo de uso:
 * ```
 * @Composable
 * fun PantallaLogin() {
 *     LogPantallas("Login")
 *     
 *     // Contenido de la pantalla
 * }
 * ```
 * 
 * @param nombrePantalla Nombre identificativo de la pantalla a registrar
 * @param infoAdicional Información adicional opcional para incluir en el log (ej. parámetros)
 */
@Composable
fun LogPantallas(nombrePantalla: String, infoAdicional: String = "") {
    val tiempoInicio = remember { Date().time }
    val etiquetaLog = remember { "PANTALLA_UME" }
    
    // Registrar cuando la pantalla se abre
    Timber.tag(etiquetaLog).e("▶️ ABIERTA: $nombrePantalla ${if (infoAdicional.isNotEmpty()) "($infoAdicional)" else ""}")
    
    // Registrar cuando la pantalla se cierra
    DisposableEffect(Unit) {
        onDispose {
            val duracion = (Date().time - tiempoInicio) / 1000 // Duración en segundos
            Timber.tag(etiquetaLog).e("⏹️ CERRADA: $nombrePantalla - Tiempo activa: ${duracion}s")
        }
    }
}

/**
 * Componente contenedor que registra automáticamente la actividad de la pantalla en Logcat.
 * 
 * Este componente encapsula cualquier pantalla y automatiza el proceso de registro,
 * permitiendo aplicar LogPantallas de manera global en Navigation.kt sin tener
 * que modificar cada pantalla individualmente.
 * 
 * Ejemplo de uso:
 * ```
 * composable(route) {
 *     PantallaRegistrada(nombrePantalla = "Login") {
 *         LoginScreen(...)
 *     }
 * }
 * ```
 * 
 * @param nombrePantalla Nombre identificativo de la pantalla a registrar
 * @param infoAdicional Información adicional opcional para incluir en el log
 * @param content Contenido de la pantalla a mostrar
 */
@Composable
fun PantallaRegistrada(
    nombrePantalla: String,
    infoAdicional: String = "",
    content: @Composable () -> Unit
) {
    LogPantallas(nombrePantalla, infoAdicional)
    content()
}

/**
 * Registra manualmente un evento específico de una pantalla en el Logcat.
 * 
 * Útil para registrar eventos importantes que ocurren dentro de una pantalla,
 * como navegación interna, cambios de estado significativos, etc.
 * 
 * @param nombrePantalla Nombre de la pantalla donde ocurre el evento
 * @param descripcionEvento Descripción corta del evento que se quiere registrar
 */
fun logEventoPantalla(nombrePantalla: String, descripcionEvento: String) {
    Timber.tag("EVENTO_PANTALLA_UME").e("$nombrePantalla: $descripcionEvento")
} 