package com.tfg.umeegunero.feature.common.welcome.screen

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

data class ButtonData(
    val text: String,
    val userType: String,
    val color: Color,
    val contentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onNavigateToLogin: (userType: String) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToAdminLogin: () -> Unit = {},
    onCloseApp: () -> Unit = {}
) {
    val pagerState = rememberPagerState { 4 }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Admin access button con efecto de elevación
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        ) {
            IconButton(
                onClick = { onNavigateToAdminLogin() },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.admin_access),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Close app button con efecto de elevación
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            )
        ) {
            IconButton(
                onClick = { onCloseApp() },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_app),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Logo y título de la app con estilo más atractivo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Círculo decorativo para el logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Conectando familias y escuelas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card para el onboarding con efecto de elevación y bordes redondeados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 8.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Onboarding Carousel dentro de la card
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        OnboardingPage(page = page)
                    }

                    // Pager Indicators con animación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(4) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val size by animateFloatAsState(
                                targetValue = if (isSelected) 12f else 8f,
                                label = "indicator_size_animation"
                            )

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(size.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }

                    // Botones de navegación del carrusel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                if (pagerState.currentPage > 0) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage > 0
                        ) {
                            Text(
                                text = stringResource(id = R.string.back),
                                color = if (pagerState.currentPage > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }

                        TextButton(
                            onClick = {
                                if (pagerState.currentPage < 3) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage < 3
                        ) {
                            Text(
                                text = stringResource(id = R.string.next),
                                color = if (pagerState.currentPage < 3)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acceso utilizando el patrón mejorado
            AccessButtons(
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToRegister = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccessButtons(
    onNavigateToLogin: (userType: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val buttonDataList = listOf(
        ButtonData(
            text = stringResource(R.string.school_admin_access),
            userType = "school",
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        ButtonData(
            text = stringResource(R.string.teacher_access),
            userType = "teacher",
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        ButtonData(
            text = stringResource(R.string.parent_access),
            userType = "parent",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttonDataList.forEach { buttonData ->
            ElevatedButton(
                onClick = { onNavigateToLogin(buttonData.userType) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = buttonData.color,
                    contentColor = buttonData.contentColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = buttonData.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedButton(
            onClick = { onNavigateToRegister() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.register_as_parent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val titles = listOf(
            stringResource(R.string.onboarding_title_1),
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_title_3),
            stringResource(R.string.onboarding_title_4)
        )

        val descriptions = listOf(
            stringResource(R.string.onboarding_desc_1),
            stringResource(R.string.onboarding_desc_2),
            stringResource(R.string.onboarding_desc_3),
            stringResource(R.string.onboarding_desc_4)
        )

        // Seleccionamos el icono según la página
        val icons = listOf(
            Icons.Default.School,      // Escuela para comunicación con el centro
            Icons.Default.GroupWork,   // Trabajo en grupo para seguimiento diario
            Icons.Default.Chat,        // Chat para la comunicación directa
            Icons.Default.WifiOff      // Funcionalidad sin internet
        )

        // Asegurarse de que page esté dentro del rango válido
        val safePageIndex = if (page in 0..3) page else 0

        val title = titles[safePageIndex]
        val description = descriptions[safePageIndex]
        val icon = icons[safePageIndex]

        // Diseño más atractivo para el indicador de página con iconos
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors[safePageIndex]
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Mostramos un ícono representativo de la característica
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Aumentamos el tamaño y mejoramos la visibilidad del título
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurface  // Mejoramos el contraste
        )

        // Aumentamos el tamaño y mejoramos la visibilidad de la descripción
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),  // Aumentamos la opacidad
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)  // Añadimos padding horizontal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    UmeEguneroTheme {
        WelcomeScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        WelcomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPagePreview() {
    UmeEguneroTheme {
        OnboardingPage(page = 0)
    }
}