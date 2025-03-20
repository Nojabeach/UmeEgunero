package com.tfg.umeegunero.feature.common.welcome.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.tfg.umeegunero.ui.theme.*
import kotlinx.coroutines.delay

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    val backgroundColor = this.background
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
    onDemoRequested: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {}
) {
    // TODO: Mejoras pendientes para la pantalla de Bienvenida
    // - Implementar un vídeo de fondo o animaciones más atractivas
    // - Mejorar el onboarding con más pasos e información detallada
    // - Implementar detección de tipo de usuario por QR/NFC
    // - Implementar geolocalización para centros cercanos
    // - Añadir estadísticas de uso
    // - Implementar soporte técnico (IMPLEMENTADO)
    // - Añadir modo demo de la aplicación (IMPLEMENTADO)
    // - Mejorar transiciones y animaciones
    // - Añadir soporte para autenticación biométrica (UI IMPLEMENTADA)
    // - Añadir el poder mandar email a soporte local
    
    val isLight = MaterialTheme.colorScheme.isLight()
    val gradientColors = if (isLight) {
        listOf(GradientStart, Color.White, GradientEnd)
    } else {
        listOf(BackgroundDark, SurfaceDark)
    }
    
    // Estado para la lista desplazable
    val scrollState = rememberLazyListState()
    
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
        // Contenido principal desplazable
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 72.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo y título
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "UmeEgunero",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Comunicación escolar simplificada",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Carrusel de onboarding
            item {
                OnboardingCarousel()
            }

            // Botones de acceso
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "ACCESO A LA PLATAFORMA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight)
                                Surface.copy(alpha = 0.95f)
                            else
                                SurfaceDark.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isLight) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            LoginButtons(
                                onCentroLogin = { onNavigateToLogin(WelcomeUserType.CENTRO) },
                                onProfesorLogin = { onNavigateToLogin(WelcomeUserType.PROFESOR) },
                                onFamiliarLogin = { onNavigateToLogin(WelcomeUserType.FAMILIAR) }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight)
                                Surface.copy(alpha = 0.95f)
                            else
                                SurfaceDark.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isLight) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "¿Eres familiar y no tienes cuenta?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 12.dp),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = onNavigateToRegister,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Secondary
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Botones de soporte y demo (ahora visibles con desplazamiento)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            Surface.copy(alpha = 0.95f)
                        else
                            SurfaceDark.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLight) 4.dp else 6.dp
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Opciones adicionales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        // Botón para contactar con soporte
                        Button(
                            onClick = onNavigateToSupport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Soporte técnico local",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Espacio adicional al final para dispositivos más pequeños
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Botones de navegación en la parte superior (fuera del scroll y ajustados para la top bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 100.dp), // Aumentado para evitar la top bar
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón de acceso admin (izquierda)
            FloatingActionButton(
                onClick = { onNavigateToLogin(WelcomeUserType.ADMIN) },
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = if (isLight) AdminColor else PrimaryLight,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Admin Login",
                    tint = if (isLight) AdminColor else PrimaryLight
                )
            }
            
            // Botón para cerrar la aplicación (derecha)
            FloatingActionButton(
                onClick = onCloseApp,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = Error,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close App",
                    tint = Error
                )
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
                .fillMaxWidth()
                .height(240.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (isLight) 4.dp else 8.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isLight)
                    Color.White.copy(alpha = 0.95f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CarouselItemContent(item = items[currentPage])
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { index ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == index) 24.dp else 8.dp,
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
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
        verticalArrangement = Arrangement.Center
    ) {
        when (item.icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            is Painter -> {
                Icon(
                    painter = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var showBiometricInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onCentroLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { showBiometricInfo = true },
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }

        Button(
            onClick = onProfesorLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { showBiometricInfo = true },
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }

        Button(
            onClick = onFamiliarLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { showBiometricInfo = true },
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
    }

    if (showBiometricInfo) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBiometricInfo = false },
            title = { Text("Acceso Biométrico") },
            text = {
                Text(
                    "Para utilizar el acceso biométrico:\n\n" +
                    "1. Inicia sesión normalmente primero\n" +
                    "2. Ve a Configuración > Seguridad\n" +
                    "3. Activa la autenticación biométrica\n" +
                    "4. La próxima vez podrás acceder usando tu huella dactilar"
                )
            },
            confirmButton = {
                TextButton(onClick = { showBiometricInfo = false }) {
                    Text("Entendido")
                }
            }
        )
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
            onDemoRequested = {},
            onNavigateToSupport = {}
        )
    }
}

@Composable
fun isLightTheme(): Boolean {
    return MaterialTheme.colorScheme.isLight()
}

// Componente de selección de idioma integrado directamente en el LazyColumn
@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        languages.forEach { (name, code) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (currentLanguage == code)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                    .clickable { onLanguageSelected(code) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (currentLanguage == code)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (currentLanguage == code) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Diálogo de selección de idioma
@Composable
private fun LanguageSelectionDialog(
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