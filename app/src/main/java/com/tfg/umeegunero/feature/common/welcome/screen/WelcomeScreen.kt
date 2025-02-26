package com.tfg.umeegunero.feature.common.welcome.screen

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.delay

// Función auxiliar para determinar si estamos en modo claro u oscuro
@Composable
fun isLightTheme(): Boolean {
    // En Material 3, podemos usar esta aproximación para detectar si estamos en tema claro
    val backgroundColor = MaterialTheme.colorScheme.background
    // Calculamos un valor aproximado de luminosidad (0.0 - 1.0)
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

enum class UserType {
    SCHOOL,
    TEACHER,
    PARENT
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: (UserType) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdminLogin: () -> Unit,
    onCloseApp: () -> Unit
) {
    val isLight = isLightTheme()

    // Crear un gradiente elegante para el fondo, estilo iOS
    val gradientColors = if (!isLight) {
        // Gradiente para modo oscuro
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro, estilo iOS
        listOf(
            Color(0xFFF0F4FF), // Azul muy claro
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            Color(0xFFF0FAFF)  // Azul muy claro con tinte cyan
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Admin login button
        IconButton(
            onClick = onNavigateToAdminLogin,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Admin Login",
                tint = if (isLight) Color(0xFF007AFF) else MaterialTheme.colorScheme.primary
            )
        }

        // Close app button
        IconButton(
            onClick = onCloseApp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close App",
                tint = if (isLight) Color(0xFFFF3B30) else MaterialTheme.colorScheme.error
            )
        }

        // Main content (centered)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Distribuye el espacio uniformemente
        ) {
            // App logo and name at the top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp) // Reduced padding
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(70.dp) // Reduced size
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing

                Text(
                    text = "UmeEgunero",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Comunicación escolar",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Carousel
            OnboardingCarousel()

            // Buttons section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Login section with title
                Text(
                    text = "INICIAR SESIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Login buttons container
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            Color.White.copy(alpha = 0.95f)
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLight) 2.dp else 4.dp
                    ),
                    shape = RoundedCornerShape(16.dp) // Más redondeado, estilo iOS
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LoginButtons(
                            onSchoolLogin = { onNavigateToLogin(UserType.SCHOOL) },
                            onTeacherLogin = { onNavigateToLogin(UserType.TEACHER) },
                            onParentLogin = { onNavigateToLogin(UserType.PARENT) }
                        )
                    }
                }

                // Register section with divider
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 8.dp),
                    color = if (isLight)
                        Color(0xFFD1D1D6) // Gris claro iOS
                    else
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )

                // Register card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            Color.White.copy(alpha = 0.95f)
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLight) 2.dp else 4.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Texto informativo
                        Text(
                            text = "Si eres familiar y no tienes cuenta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isLight)
                                Color(0xFF8E8E93) // Gris texto iOS
                            else
                                MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        // Botón de registro
                        Button(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLight)
                                    Color(0xFFFF9500) // Naranja iOS
                                else
                                    MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HowToReg,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Regístrate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingCarousel() {
    val isLight = isLightTheme()

    val items = listOf(
        CarouselItem(
            icon = Icons.Default.School,
            title = "Comunicación Escuela-Familia",
            description = "Información diaria sobre la actividad de tu hijo/a en el centro educativo."
        ),
        CarouselItem(
            icon = Icons.Default.Chat,
            title = "Chat Privado",
            description = "Comunicación directa y segura con profesores para resolver cualquier duda."
        ),
        CarouselItem(
            icon = Icons.Default.Build,
            title = "Informes Diarios",
            description = "Control de comidas, descanso y actividades, siempre actualizado."
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    // Auto-scroll effect
    LaunchedEffect(key1 = true) {
        while(true) {
            delay(5000) // Change slide every 5 seconds
            currentPage = (currentPage + 1) % items.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carousel card with content
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(220.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (isLight) 2.dp else 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isLight)
                    Color.White.copy(alpha = 0.9f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp) // Esquinas más redondeadas, estilo iOS
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Contenido actual del carrusel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CarouselItemContent(item = items[currentPage])
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Pagination indicators
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { index ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == index) 20.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator width"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLight) {
                                if (currentPage == index)
                                    Color(0xFF007AFF) // Azul iOS
                                else
                                    Color(0xFFD1D1D6) // Gris claro iOS
                            } else {
                                if (currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            }
                        )
                        .width(width)
                        .height(8.dp)
                )
            }
        }
    }
}

data class CarouselItem(
    val icon: Any, // Can be ImageVector or Painter
    val title: String,
    val description: String
)

@Composable
fun CarouselItemContent(item: CarouselItem) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Icon
        when (item.icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp), // Smaller icon
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            is Painter -> {
                Icon(
                    painter = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp), // Smaller icon
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Less space

        // Title
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium, // Smaller text
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp)) // Less space

        // Description
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodySmall, // Smaller text
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 18.sp // Smaller line height
        )
    }
}

@Composable
fun LoginButtons(
    onSchoolLogin: () -> Unit,
    onTeacherLogin: () -> Unit,
    onParentLogin: () -> Unit
) {
    val isLight = isLightTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // School Login Button - Color azul iOS
        Button(
            onClick = onSchoolLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF007AFF) else MaterialTheme.colorScheme.primary // Azul iOS
            ),
            shape = RoundedCornerShape(12.dp) // Más redondeado, estilo iOS
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Acceso Centro",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLight) FontWeight.Medium else FontWeight.Normal // Fuente más ligera como iOS
            )
        }

        // Teacher Login Button - Color verde iOS
        Button(
            onClick = onTeacherLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF34C759) else MaterialTheme.colorScheme.secondary // Verde iOS
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Acceso Profesor",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLight) FontWeight.Medium else FontWeight.Normal
            )
        }

        // Parent Login Button - Color púrpura iOS
        Button(
            onClick = onParentLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF5856D6) else MaterialTheme.colorScheme.tertiary // Púrpura iOS
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Acceso Padres",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLight) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenLightPreview() {
    UmeEguneroTheme(darkTheme = false) {
        WelcomeScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            onNavigateToAdminLogin = {},
            onCloseApp = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        WelcomeScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            onNavigateToAdminLogin = {},
            onCloseApp = {}
        )
    }
}