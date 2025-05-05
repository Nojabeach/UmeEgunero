package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

/**
 * Componente que muestra un avatar de usuario, ya sea con imagen o iniciales.
 *
 * @param imageUrl URL de la imagen de perfil (si está disponible)
 * @param userName Nombre del usuario para generar iniciales como fallback
 * @param size Tamaño del avatar en dp
 * @param borderWidth Ancho del borde (0dp para sin borde)
 * @param borderColor Color del borde
 * @param modifier Modificador para personalizar el avatar
 */
@Composable
fun UserAvatar(
    imageUrl: String? = null,
    userName: String,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (borderWidth > 0.dp) {
                Modifier.border(borderWidth, borderColor, CircleShape)
            } else {
                Modifier
            }
        )
    
    // Si hay una URL de imagen, intentamos cargarla
    if (!imageUrl.isNullOrBlank()) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .size(Size.ORIGINAL) // Cargar la imagen original
                .crossfade(true) // Efecto de transición
                .build()
        )
        
        // Verificar el estado del cargador de imágenes
        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = "Avatar de $userName",
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Si falla la carga, mostrar las iniciales
                AvatarWithInitials(userName, avatarModifier)
            }
        }
    } else {
        // Si no hay URL, mostrar las iniciales
        AvatarWithInitials(userName, avatarModifier)
    }
}

/**
 * Avatar con iniciales del usuario
 */
@Composable
private fun AvatarWithInitials(
    userName: String,
    modifier: Modifier = Modifier
) {
    // Generar color de fondo basado en el nombre
    val backgroundColor = getAvatarBackgroundColor(userName)
    
    // Obtener iniciales del nombre
    val initials = getInitials(userName)
    
    Box(
        modifier = modifier.background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Genera un color de fondo para el avatar basado en el nombre
 */
private fun getAvatarBackgroundColor(name: String): Color {
    // Colores predefinidos para avatares
    val colors = listOf(
        Color(0xFF1976D2), // Azul
        Color(0xFF388E3C), // Verde
        Color(0xFFE64A19), // Naranja
        Color(0xFF7B1FA2), // Morado
        Color(0xFFC2185B), // Rosa
        Color(0xFF00796B), // Teal
        Color(0xFF5D4037)  // Marrón
    )
    
    // Usar el hash del nombre para elegir un color
    val hash = name.hashCode()
    val index = ((hash % colors.size) + colors.size) % colors.size
    
    return colors[index]
}

/**
 * Obtiene las iniciales del nombre del usuario
 */
private fun getInitials(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .take(2)
        .ifEmpty { "?" }
} 