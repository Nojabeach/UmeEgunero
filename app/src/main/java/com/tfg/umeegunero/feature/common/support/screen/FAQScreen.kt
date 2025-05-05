package com.tfg.umeegunero.feature.common.support.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
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
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                    "No puedes crear una cuenta directamente. Tu centro educativo debe registrarte en el sistema. Una vez registrado, recibirás un correo con tus credenciales iniciales para acceder a la aplicación."
                ),
                FAQItem(
                    "¿Qué hago si olvido mi contraseña?",
                    "En la pantalla de inicio de sesión, toca en '¿Olvidaste tu contraseña?' y sigue las instrucciones. Recibirás un correo electrónico con un enlace para restablecerla."
                ),
                FAQItem(
                    "¿Cómo cambio mi información personal?",
                    "Accede al menú lateral y selecciona 'Perfil'. Allí encontrarás la opción para editar tu información personal como nombre, teléfono y foto de perfil."
                ),
                FAQItem(
                    "¿Es segura mi información personal?",
                    "Sí. UmeEgunero utiliza encriptación de datos y cumple con las normativas de protección de datos. Solo el personal autorizado del centro puede ver tu información completa."
                )
            )
        ),
        FAQCategory(
            title = "Sistema de Mensajería",
            icon = Icons.Default.Message,
            items = listOf(
                FAQItem(
                    "¿Cómo funciona el sistema de mensajería unificado?",
                    "El sistema centraliza todos los tipos de comunicación (notificaciones, chats, comunicados e incidencias) en una bandeja de entrada unificada. Puedes filtrar por tipo de mensaje y ver todas tus notificaciones en un solo lugar."
                ),
                FAQItem(
                    "¿Cómo envío un mensaje a un profesor?",
                    "En la pantalla de mensajes, toca el botón '+' para crear un nuevo mensaje. Selecciona al profesor como destinatario, escribe el asunto y contenido, y envíalo. También puedes adjuntar archivos si es necesario."
                ),
                FAQItem(
                    "¿Puedo ver si han leído mis mensajes?",
                    "Sí. En la pantalla de detalle del mensaje verás cuándo fue leído por el destinatario. Los mensajes no leídos aparecen destacados en la bandeja de entrada."
                ),
                FAQItem(
                    "¿Cómo filtro mis mensajes por tipo?",
                    "En la pantalla de mensajes, usa el selector de tipo ubicado en la parte superior para filtrar: Todos, Notificaciones, Chats, Comunicados o Incidencias."
                ),
                FAQItem(
                    "¿Puedo eliminar mensajes?",
                    "Sí. Desliza un mensaje hacia la izquierda en la bandeja de entrada o usa el botón de eliminar en la pantalla de detalle del mensaje."
                )
            )
        ),
        FAQCategory(
            title = "Perfil Profesor",
            icon = Icons.Default.School,
            items = listOf(
                FAQItem(
                    "¿Cómo veo la lista de mis alumnos?",
                    "En el dashboard de profesor, selecciona la opción 'Mis Alumnos'. Allí verás la lista completa de alumnos asignados a tu clase con sus datos básicos."
                ),
                FAQItem(
                    "¿Cómo completo el registro diario de un alumno?",
                    "Desde 'Registro Diario' en el dashboard, selecciona la fecha y los alumnos que deseas registrar. Completa la información sobre comidas, siesta, deposiciones y materiales necesarios. Finalmente, guarda el registro con el botón flotante."
                ),
                FAQItem(
                    "¿Puedo registrar varios alumnos a la vez?",
                    "Sí. En la pantalla de 'Registro Diario', puedes seleccionar múltiples alumnos y registrarlos simultáneamente. También puedes filtrar por asistencia para facilitar el proceso."
                ),
                FAQItem(
                    "¿Cómo sé si los familiares han visto los registros diarios?",
                    "Los registros más importantes generan notificaciones para los familiares. Puedes ver su estado en la sección de registros, donde se indica si han sido leídos."
                ),
                FAQItem(
                    "¿Puedo enviar comunicados a todos los familiares de mi clase?",
                    "Sí. Desde el sistema de mensajería, crea un nuevo mensaje, selecciona tipo 'Comunicado' y elige tu clase completa como destinatario para enviar información a todos los familiares a la vez."
                )
            )
        ),
        FAQCategory(
            title = "Perfil Familiar",
            icon = Icons.Default.People,
            items = listOf(
                FAQItem(
                    "¿Cómo veo las actividades diarias de mi hijo?",
                    "Accede a 'Registro Diario' desde el dashboard familiar para ver detalladamente la información sobre comidas, siestas, deposiciones y actividades realizadas por tu hijo cada día."
                ),
                FAQItem(
                    "¿Cómo me comunico con el profesor?",
                    "Usa el sistema de mensajería para enviar un mensaje directo al profesor. También puedes responder a cualquier mensaje o comunicado que hayas recibido."
                ),
                FAQItem(
                    "¿Puedo tener varios niños vinculados a mi cuenta?",
                    "Sí. Un familiar puede estar vinculado a varios alumnos. Solicita al centro educativo que realice la vinculación correspondiente y podrás gestionar todos tus hijos desde la misma cuenta."
                ),
                FAQItem(
                    "¿Cómo solicito vincularme a un alumno?",
                    "Desde tu perfil familiar, selecciona 'Solicitar Vinculación' e introduce el código de vinculación proporcionado por el centro. El centro educativo revisará y aprobará tu solicitud."
                ),
                FAQItem(
                    "¿Cómo configuro las notificaciones?",
                    "Ve a 'Configuración' en el menú lateral y selecciona 'Notificaciones'. Allí podrás personalizar qué tipo de notificaciones deseas recibir y cómo (push, email, etc.)."
                )
            )
        ),
        FAQCategory(
            title = "Perfil Administrador Centro",
            icon = Icons.Default.AdminPanelSettings,
            items = listOf(
                FAQItem(
                    "¿Cómo añado un nuevo profesor al sistema?",
                    "En el dashboard de administrador, selecciona 'Gestión de Usuarios' y luego 'Añadir Usuario'. Completa el formulario seleccionando el tipo 'Profesor' y asigna una clase si es necesario."
                ),
                FAQItem(
                    "¿Cómo gestiono las clases y cursos?",
                    "Accede a 'Gestión Académica' donde podrás crear, editar y eliminar cursos y clases. También podrás asignar profesores a cada clase y gestionar los alumnos por clase."
                ),
                FAQItem(
                    "¿Cómo apruebo solicitudes de vinculación familiar?",
                    "En el apartado 'Solicitudes de Vinculación' verás todas las solicitudes pendientes. Revisa cada solicitud y aprueba o rechaza según corresponda."
                ),
                FAQItem(
                    "¿Cómo envío un comunicado a todo el centro?",
                    "Desde el sistema de mensajería, crea un nuevo mensaje de tipo 'Comunicado', selecciona 'Todo el centro' como destinatario, completa la información y envíalo."
                ),
                FAQItem(
                    "¿Puedo ver estadísticas del centro?",
                    "Sí. En la sección 'Estadísticas' encontrarás información sobre asistencia, comunicaciones y actividad general del centro educativo presentada en gráficos interactivos."
                )
            )
        ),
        FAQCategory(
            title = "Soporte Técnico",
            icon = Icons.AutoMirrored.Filled.Help,
            items = listOf(
                FAQItem(
                    "¿Qué hago si la app no funciona correctamente?",
                    "Intenta cerrar la app completamente y volver a abrirla. Si el problema persiste, ve a Ajustes > Aplicaciones > UmeEgunero > Almacenamiento > Borrar caché, y reinicia la aplicación."
                ),
                FAQItem(
                    "¿Cómo reporto un error en la aplicación?",
                    "Ve a la sección 'Soporte Técnico' en el menú de configuración y describe detalladamente el problema. Si es posible, incluye capturas de pantalla para ayudarnos a diagnosticar el error."
                ),
                FAQItem(
                    "¿Qué información necesito para contactar con soporte?",
                    "Ten a mano tu DNI, el nombre de tu centro educativo y, si es posible, capturas de pantalla del error. Esto nos ayudará a resolver tu problema más rápidamente."
                ),
                FAQItem(
                    "¿Hay algún requisito mínimo para usar la app?",
                    "La app requiere Android 8.0 (API nivel 26) o superior. Además, necesitas una conexión a internet estable para sincronizar los datos correctamente."
                ),
                FAQItem(
                    "¿Dónde encuentro tutoriales sobre el uso de la aplicación?",
                    "En la sección 'Ayuda' del menú lateral encontrarás tutoriales y guías paso a paso sobre todas las funcionalidades principales de la aplicación."
                )
            )
        )
    )
} 