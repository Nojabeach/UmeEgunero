package com.tfg.umeegunero.feature.common.support.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

data class FAQItem(
    val question: String,
    val answer: String
)

data class FAQCategory(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val items: List<FAQItem>
)

@Composable
fun FAQScreen(
    onNavigateBack: () -> Unit
) {
    val categories = listOf(
        FAQCategory(
            title = "Cuenta y Acceso",
            icon = Icons.Default.AccountCircle,
            items = listOf(
                FAQItem(
                    "¿Cómo puedo crear una cuenta?",
                    "Para crear una cuenta, ve a la pantalla de registro y sigue los pasos indicados. Necesitarás proporcionar tu email, contraseña y datos personales básicos."
                ),
                FAQItem(
                    "¿Qué hago si olvido mi contraseña?",
                    "En la pantalla de inicio de sesión, haz clic en '¿Olvidaste tu contraseña?' y sigue las instrucciones para restablecerla."
                ),
                FAQItem(
                    "¿Cómo activo la autenticación biométrica?",
                    "Ve a Configuración > Seguridad y activa la opción de autenticación biométrica. Asegúrate de tener configurada la huella dactilar en tu dispositivo."
                )
            )
        ),
        FAQCategory(
            title = "Funcionalidades",
            icon = Icons.Default.Functions,
            items = listOf(
                FAQItem(
                    "¿Cómo puedo ver el horario de mi hijo?",
                    "Accede a la sección 'Horario' en el menú principal. Allí podrás ver el horario completo y las actividades programadas."
                ),
                FAQItem(
                    "¿Cómo funciona el chat con los profesores?",
                    "El chat está disponible en la sección 'Comunicación'. Selecciona el profesor con quien deseas hablar y comienza la conversación."
                ),
                FAQItem(
                    "¿Puedo recibir notificaciones?",
                    "Sí, ve a Configuración > Notificaciones para personalizar qué tipo de notificaciones deseas recibir."
                )
            )
        ),
        FAQCategory(
            title = "Seguridad y Privacidad",
            icon = Icons.Default.Security,
            items = listOf(
                FAQItem(
                    "¿Cómo se protegen mis datos?",
                    "Utilizamos encriptación de extremo a extremo y cumplimos con las normativas de protección de datos (RGPD)."
                ),
                FAQItem(
                    "¿Quién puede ver la información de mi hijo?",
                    "Solo los profesores asignados y el personal autorizado del centro pueden acceder a la información de tu hijo."
                ),
                FAQItem(
                    "¿Cómo puedo denunciar un problema de seguridad?",
                    "Contacta inmediatamente con nuestro equipo de soporte técnico a través de la sección de ayuda."
                )
            )
        ),
        FAQCategory(
            title = "Soporte",
            icon = Icons.Default.Help,
            items = listOf(
                FAQItem(
                    "¿Qué hago si la app no funciona correctamente?",
                    "Intenta cerrar la app y volver a abrirla. Si el problema persiste, contacta con soporte técnico."
                ),
                FAQItem(
                    "¿Cómo puedo reportar un error?",
                    "Ve a la sección de soporte técnico y utiliza el formulario de contacto para describir el problema."
                ),
                FAQItem(
                    "¿Qué versión de Android necesito?",
                    "La app requiere Android 10 (API nivel 29) o superior para funcionar correctamente."
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preguntas Frecuentes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            categories.forEach { category ->
                FAQCategoryCard(category = category)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FAQCategoryCard(category: FAQCategory) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    category.items.forEach { faq ->
                        FAQItem(faq)
                        if (faq != category.items.last()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FAQItem(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Contraer" else "Expandir"
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 