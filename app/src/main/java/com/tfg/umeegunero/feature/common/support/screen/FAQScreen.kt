package com.tfg.umeegunero.feature.common.support.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

data class FAQItem(
    val question: String,
    val answer: String
)

data class FAQCategory(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val items: List<FAQItem>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FAQScreen(
    onNavigateBack: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val categories = getCategorias()
    var query by remember { mutableStateOf("") }
    var filteredCategories by remember { mutableStateOf(categories) }
    
    // Filtra las preguntas basadas en la consulta de búsqueda
    LaunchedEffect(query) {
        filteredCategories = if (query.isBlank()) {
            categories
        } else {
            val lowerQuery = query.lowercase()
            categories.mapNotNull { category ->
                val filteredItems = category.items.filter { item ->
                    item.question.lowercase().contains(lowerQuery) ||
                    item.answer.lowercase().contains(lowerQuery)
                }
                
                if (filteredItems.isNotEmpty()) {
                    category.copy(items = filteredItems)
                } else {
                    null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preguntas Frecuentes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar en preguntas frecuentes...") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Buscar"
                    ) 
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                )
            )
            
            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No se encontraron resultados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Intenta con otros términos o navega por categorías",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Mostrar número de resultados cuando hay búsqueda
                    if (query.isNotEmpty()) {
                        val totalResults = filteredCategories.sumOf { it.items.size }
                        Text(
                            text = "Se encontraron $totalResults resultados para \"$query\"",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    filteredCategories.forEach { category ->
                        FAQCategoryCard(category = category)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${category.items.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.primary
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
                    category.items.forEachIndexed { index, faq ->
                        FAQItem(faq)
                        if (index < category.items.size - 1) {
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
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Contraer" else "Expandir",
                tint = MaterialTheme.colorScheme.primary
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
                    .padding(start = 28.dp, top = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Enlace para más información (opcional)
                TextButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Más información")
                }
            }
        }
    }
}

private fun getCategorias(): List<FAQCategory> {
    return listOf(
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
                    "En la pantalla de inicio de sesión, haz clic en '¿Olvidaste tu contraseña?' y sigue las instrucciones para restablecerla a través de tu correo electrónico."
                ),
                FAQItem(
                    "¿Cómo activo la autenticación biométrica?",
                    "Ve a Configuración > Seguridad y activa la opción de autenticación biométrica. Asegúrate de tener configurada la huella dactilar en tu dispositivo."
                ),
                FAQItem(
                    "¿Cómo puedo cambiar mi correo electrónico?",
                    "Accede a tu perfil, selecciona la opción 'Editar perfil' y sigue las instrucciones para cambiar tu correo electrónico. Deberás verificar el nuevo correo."
                )
            )
        ),
        FAQCategory(
            title = "Funcionalidades",
            icon = Icons.Default.Functions,
            items = listOf(
                FAQItem(
                    "¿Cómo puedo ver el horario de mi hijo?",
                    "Accede a la sección 'Horario' en el menú principal. Allí podrás ver el horario completo y las actividades programadas para cada día de la semana."
                ),
                FAQItem(
                    "¿Cómo funciona el chat con los profesores?",
                    "El chat está disponible en la sección 'Comunicación'. Selecciona el profesor con quien deseas hablar y comienza la conversación. Puedes adjuntar archivos y fotos si es necesario."
                ),
                FAQItem(
                    "¿Puedo recibir notificaciones?",
                    "Sí, ve a Configuración > Notificaciones para personalizar qué tipo de notificaciones deseas recibir y cómo (email, push, etc.). También puedes establecer horarios de no molestar."
                ),
                FAQItem(
                    "¿Cómo puedo ver las calificaciones?",
                    "En la sección 'Académico' encontrarás todas las calificaciones, evaluaciones y comentarios de los profesores organizados por asignaturas y períodos."
                )
            )
        ),
        FAQCategory(
            title = "Seguridad y Privacidad",
            icon = Icons.Default.Security,
            items = listOf(
                FAQItem(
                    "¿Cómo se protegen mis datos?",
                    "Utilizamos encriptación de extremo a extremo y cumplimos con las normativas de protección de datos (RGPD). Todos los datos se almacenan en servidores seguros con certificación ISO 27001."
                ),
                FAQItem(
                    "¿Quién puede ver la información de mi hijo?",
                    "Solo los profesores asignados y el personal autorizado del centro pueden acceder a la información de tu hijo. Puedes revisar los permisos específicos en la sección de privacidad."
                ),
                FAQItem(
                    "¿Cómo puedo denunciar un problema de seguridad?",
                    "Contacta inmediatamente con nuestro equipo de seguridad a través de la sección de soporte técnico o escríbenos directamente a seguridad@umeegunero.com."
                ),
                FAQItem(
                    "¿Se comparten mis datos con terceros?",
                    "No compartimos datos personales con terceros sin tu consentimiento explícito. Puedes revisar nuestra política de privacidad completa en la aplicación."
                )
            )
        ),
        FAQCategory(
            title = "Soporte Técnico",
            icon = Icons.Default.Help,
            items = listOf(
                FAQItem(
                    "¿Qué hago si la app no funciona correctamente?",
                    "Intenta cerrar la app y volver a abrirla. Si el problema persiste, ve a Ajustes > Aplicaciones > UmeEgunero > Almacenamiento > Borrar caché, y reinicia la aplicación."
                ),
                FAQItem(
                    "¿Cómo puedo reportar un error?",
                    "Ve a la sección de soporte técnico y utiliza el formulario de contacto para describir el problema. Adjunta capturas de pantalla si es posible para ayudarnos a diagnosticar el error."
                ),
                FAQItem(
                    "¿Qué versión de Android necesito?",
                    "La app requiere Android 10 (API nivel 29) o superior para funcionar correctamente. Para iOS, se requiere iOS 13 o superior."
                ),
                FAQItem(
                    "¿Con qué frecuencia se actualiza la aplicación?",
                    "Publicamos actualizaciones mensuales con mejoras y correcciones. Las actualizaciones importantes se notifican dentro de la aplicación."
                ),
                FAQItem(
                    "¿Dónde puedo ver tutoriales sobre el uso de la aplicación?",
                    "En la sección de ayuda encontrarás tutoriales en video y guías paso a paso sobre todas las funcionalidades de la aplicación."
                )
            )
        ),
        FAQCategory(
            title = "Facturación y Pagos",
            icon = Icons.Default.Payment,
            items = listOf(
                FAQItem(
                    "¿Cómo puedo pagar las cuotas escolares?",
                    "En la sección 'Pagos' podrás ver las facturas pendientes y realizar pagos a través de los métodos configurados (tarjeta, domiciliación bancaria, etc.)."
                ),
                FAQItem(
                    "¿Recibo factura por los pagos?",
                    "Sí, todas las facturas se generan automáticamente y puedes encontrarlas en la sección 'Pagos > Historial'. También se envían a tu correo electrónico."
                ),
                FAQItem(
                    "¿Cómo añado un nuevo método de pago?",
                    "Ve a 'Pagos > Métodos de pago > Añadir nuevo' y sigue los pasos para registrar una nueva tarjeta o cuenta bancaria. Todos los datos se procesan de forma segura."
                ),
                FAQItem(
                    "¿Hay descuentos para hermanos?",
                    "Sí, las políticas de descuentos para hermanos se aplican automáticamente según la configuración del centro educativo. Puedes consultar los detalles en la sección de pagos."
                )
            )
        )
    )
} 