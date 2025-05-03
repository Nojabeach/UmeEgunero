package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tfg.umeegunero.util.PantallaRegistrada

/**
 * Extensión de composable que añade automáticamente un registro de actividad de pantalla.
 * 
 * Esta función envuelve cada pantalla con el componente PantallaRegistrada para registrar
 * automáticamente cuándo se abre y se cierra cada pantalla, sin tener que modificar
 * el código de cada pantalla individual.
 * 
 * @param route Ruta de navegación
 * @param arguments Lista de argumentos de navegación
 * @param deepLinks Lista de deeplinks
 * @param nombrePantalla Nombre personalizado para la pantalla (si se omite, se usa la ruta)
 * @param content Contenido de la pantalla
 */
fun NavGraphBuilder.composableRegistrado(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    nombrePantalla: String? = null,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks
    ) { backStackEntry ->
        // Extraer un nombre legible de la ruta
        val pantallaId = nombrePantalla ?: obtenerNombrePantalla(route)
        
        // Obtener información adicional de los argumentos
        val info = obtenerInfoAdicionalDeRuta(backStackEntry)
        
        PantallaRegistrada(nombrePantalla = pantallaId, infoAdicional = info) {
            content(backStackEntry)
        }
    }
}

/**
 * Extrae un nombre legible de la ruta de navegación.
 */
private fun obtenerNombrePantalla(route: String): String {
    // Eliminar parámetros de ruta
    val rutaBase = route.split("?")[0]
    
    // Dividir por partes y tomar la primera o segunda si hay una separación
    val partes = rutaBase.split("/")
    
    // Si tiene formato "dashboard/seccion", mostrar "Seccion" 
    return if (partes.size > 1 && !partes[1].contains("{")) {
        partes[1].replaceFirstChar { it.uppercase() }.replace("_", " ")
    } else {
        // Convertir snake_case a Title Case
        partes[0].replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}

/**
 * Extrae información adicional de la entrada de navegación.
 */
private fun obtenerInfoAdicionalDeRuta(backStackEntry: NavBackStackEntry): String {
    val argumentos = backStackEntry.arguments ?: return ""
    val params = mutableListOf<String>()
    
    // Buscar argumentos de ruta comunes e incluirlos como información
    for (key in argumentos.keySet()) {
        when (key) {
            "dni", "centroId", "cursoId", "claseId", "eventoId", "alumnoId", "profesorId", "familiarId" -> {
                argumentos.getString(key)?.let { valor ->
                    if (valor.isNotEmpty()) {
                        params.add("$key: $valor")
                    }
                }
            }
        }
    }
    
    return params.joinToString(", ")
} 