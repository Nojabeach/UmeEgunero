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
import androidx.compose.runtime.LaunchedEffect
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
import coil.request.CachePolicy
import coil.size.Size
import timber.log.Timber

// URL fija para administradores
private const val ADMIN_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media"

/**
 * Componente que muestra un avatar de usuario, ya sea con imagen o iniciales.
 * Para administradores, usa siempre una URL específica.
 */
@Composable
fun UserAvatar(
    imageUrl: String? = null,
    userName: String,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    // Logging para depuración
    LaunchedEffect(isAdmin, imageUrl) {
        Timber.d("UserAvatar - userName: $userName, isAdmin: $isAdmin, imageUrl: $imageUrl")
    }
    
    if (isAdmin) {
        // Si es administrador, usar el componente específico para administradores
        Timber.d("UsuarioEsAdmin: true - Usando AdminAvatar para $userName")
        AdminAvatar(
            userName = userName,
            size = size,
            borderWidth = borderWidth,
            borderColor = borderColor,
            modifier = modifier
        )
    } else {
        // Si no es administrador, usar el componente normal
        RegularUserAvatar(
            imageUrl = imageUrl,
            userName = userName,
            size = size,
            borderWidth = borderWidth,
            borderColor = borderColor,
            modifier = modifier
        )
    }
}

/**
 * Avatar específico para administradores que siempre muestra la imagen correcta
 */
@Composable
private fun AdminAvatar(
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
    
    // Usar la constante para la URL de administrador
    Timber.d("AdminAvatar - Usando URL constante: $ADMIN_AVATAR_URL")
    
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(ADMIN_AVATAR_URL)
            .size(Size.ORIGINAL)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    )
    
    when (val state = painter.state) {
        is AsyncImagePainter.State.Loading -> {
            Timber.d("AdminAvatar - Cargando imagen...")
            // Mientras carga, mostrar las iniciales
            AvatarWithInitials(userName, avatarModifier)
        }
        is AsyncImagePainter.State.Success -> {
            Timber.d("AdminAvatar - Imagen cargada con éxito")
            Image(
                painter = painter,
                contentDescription = "Avatar de administrador $userName",
                modifier = avatarModifier,
                contentScale = ContentScale.Crop
            )
        }
        is AsyncImagePainter.State.Error -> {
            Timber.e("AdminAvatar - Error al cargar imagen: ${state.result.throwable.message}")
            // Mostrar un avatar de color azul para administradores con iniciales
            Box(
                modifier = avatarModifier.background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(userName),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            Timber.d("AdminAvatar - Estado desconocido")
            AvatarWithInitials(userName, avatarModifier)
        }
    }
}

/**
 * Avatar estándar para usuarios regulares
 */
@Composable
private fun RegularUserAvatar(
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
    
    // Registrar la URL para depuración
    LaunchedEffect(imageUrl) {
        Timber.d("UserAvatar - Intentando cargar avatar con URL: $imageUrl")
    }
    
    // Si hay una URL de imagen, intentamos cargarla
    if (!imageUrl.isNullOrBlank()) {
        val context = LocalContext.current
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size.ORIGINAL)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        )
        
        // Verificar el estado del cargador de imágenes
        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
                // Mientras carga, mostrar las iniciales
                Timber.d("UserAvatar - Estado: Cargando imagen desde URL: $imageUrl")
                AvatarWithInitials(userName, avatarModifier)
            }
            is AsyncImagePainter.State.Success -> {
                Timber.d("UserAvatar - Estado: ÉXITO al cargar avatar desde URL: $imageUrl")
                Image(
                    painter = painter,
                    contentDescription = "Avatar de $userName",
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            }
            is AsyncImagePainter.State.Error -> {
                Timber.e("UserAvatar - Estado: ERROR al cargar avatar desde URL: $imageUrl, error: ${state.result.throwable.message}")
                // Si falla la carga, mostrar las iniciales
                AvatarWithInitials(userName, avatarModifier)
            }
            else -> {
                // Para otros estados, mostrar las iniciales
                Timber.d("UserAvatar - Estado desconocido para URL: $imageUrl")
                AvatarWithInitials(userName, avatarModifier)
            }
        }
    } else {
        // Si no hay URL, mostrar las iniciales
        Timber.d("UserAvatar - Sin URL, mostrando iniciales para: $userName")
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