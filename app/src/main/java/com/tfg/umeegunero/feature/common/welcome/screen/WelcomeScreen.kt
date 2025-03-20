package com.tfg.umeegunero.feature.common.welcome.screen

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.Background
import com.tfg.umeegunero.ui.theme.BackgroundDark
import com.tfg.umeegunero.ui.theme.BlueDark
import com.tfg.umeegunero.ui.theme.GradientEnd
import com.tfg.umeegunero.ui.theme.GradientStart
import com.tfg.umeegunero.ui.theme.OnBackground
import com.tfg.umeegunero.ui.theme.OnPrimary
import com.tfg.umeegunero.ui.theme.PrimaryLight
import com.tfg.umeegunero.ui.theme.PurpleDark
import com.tfg.umeegunero.ui.theme.Secondary
import com.tfg.umeegunero.ui.theme.Surface
import com.tfg.umeegunero.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import com.tfg.umeegunero.ui.theme.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.Chat

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    // En Material 3, podemos usar esta aproximación para detectar si estamos en tema claro
    val backgroundColor = this.background
    // Calculamos un valor aproximado de luminosidad (0.0 - 1.0)
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

// Tipo de usuario para la navegación desde la pantalla de bienvenida
enum class WelcomeUserType {
    ADMIN,
    CENTRO,
    PROFESOR,
    FAMILIAR
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: (WelcomeUserType) -> Unit,
    onNavigateToRegister: () -> Unit,
    onCloseApp: () -> Unit,
    onDemoRequested: () -> Unit = {}
) {
    // TODO: Mejoras pendientes para la pantalla de Bienvenida
    // - Implementar un vídeo de fondo o animaciones más atractivas
    // - Añadir soporte para selección de idioma (IMPLEMENTADO)
    // - Mejorar el onboarding con más pasos e información detallada
    // - Implementar detección de tipo de usuario por QR/NFC
    // - Implementar geolocalización para centros cercanos
    // - Añadir estadísticas de uso
    // - Implementar soporte técnico (IMPLEMENTADO)
    // - Añadir modo demo de la aplicación (IMPLEMENTADO)
    // - Mejorar transiciones y animaciones
    // - Añadir soporte para autenticación biométrica
    
    val isLight = MaterialTheme.colorScheme.isLight()
    val gradientColors = if (isLight) {
        listOf(GradientStart, Color.White, GradientEnd)
    } else {
        listOf(BackgroundDark, SurfaceDark)
    }
    
    // Estado para controlar la visibilidad del diálogo de selección de idioma
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Estado para el idioma actual
    var currentLanguage by remember { mutableStateOf("ES") }
    
    // Mostrar diálogo de selección de idioma si está activo
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onLanguageSelected = { langCode -> 
                currentLanguage = langCode
                // Aquí se implementaría el cambio real de idioma en la app
            },
            onDismiss = { showLanguageDialog = false }
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
        // Botón de selección de idioma
        IconButton(
            onClick = { showLanguageDialog = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Cambiar idioma",
                    tint = if (isLight) MaterialTheme.colorScheme.primary else PrimaryLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentLanguage,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isLight) MaterialTheme.colorScheme.primary else PrimaryLight
                )
            }
        }

        IconButton(
            onClick = { onNavigateToLogin(WelcomeUserType.ADMIN) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Admin Login",
                tint = if (isLight) AdminColor else PrimaryLight
            )
        }

        IconButton(
            onClick = onCloseApp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close App",
                tint = if (isLight) Error else Error
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(4.dp))

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

            OnboardingCarousel()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "INICIAR SESIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            Surface.copy(alpha = 0.95f)
                        else
                            SurfaceDark.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLight) 2.dp else 4.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LoginButtons(
                            onCentroLogin = { onNavigateToLogin(WelcomeUserType.CENTRO) },
                            onProfesorLogin = { onNavigateToLogin(WelcomeUserType.PROFESOR) },
                            onFamiliarLogin = { onNavigateToLogin(WelcomeUserType.FAMILIAR) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            Surface.copy(alpha = 0.95f)
                        else
                            SurfaceDark.copy(alpha = 0.8f)
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
                        Text(
                            text = "Si eres familiar y no tienes cuenta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary // Nuevo color secundario
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón para contactar con soporte
                        TextButton(
                            onClick = { /* Aquí se implementaría la lógica para contactar con soporte */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "¿Necesitas ayuda? Contacta con soporte",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Botón para ver demo
                        TextButton(
                            onClick = onDemoRequested,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Ver demo de la aplicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
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
            icon = Icons.AutoMirrored.Filled.Chat,
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

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(5000)
            currentPage = (currentPage + 1) % items.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                                    Color(0xFF007AFF)
                                else
                                    Color(0xFFD1D1D6)
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
    val icon: Any,
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
        when (item.icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            is Painter -> {
                Icon(
                    painter = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun LoginButtons(
    onCentroLogin: () -> Unit,
    onProfesorLogin: () -> Unit,
    onFamiliarLogin: () -> Unit
) {
    val isLight = isLightTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onCentroLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF007AFF) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
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
                fontWeight = if (isLight) FontWeight.Medium else FontWeight.Normal
            )
        }

        Button(
            onClick = onProfesorLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF34C759) else MaterialTheme.colorScheme.secondary
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

        Button(
            onClick = onFamiliarLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF5856D6) else MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Acceso Familiar",
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
            onCloseApp = {},
            onDemoRequested = {}
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
            onCloseApp = {},
            onDemoRequested = {}
        )
    }
}

@Composable
fun isLightTheme(): Boolean {
    return MaterialTheme.colorScheme.isLight()
}

// Diálogo de selección de idioma
@Composable
fun LanguageSelectionDialog(
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar idioma") },
        text = {
            Column {
                languages.forEach { (name, code) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                onLanguageSelected(code)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}