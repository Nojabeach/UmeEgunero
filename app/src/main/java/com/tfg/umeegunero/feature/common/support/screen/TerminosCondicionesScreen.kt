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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla que muestra los términos y condiciones de la aplicación UmeEgunero.
 * 
 * Esta pantalla presenta de forma clara y organizada los términos legales que
 * los usuarios deben aceptar al registrarse en la aplicación.
 * 
 * @param onNavigateBack Callback para volver a la pantalla anterior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosCondicionesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Título principal
            Text(
                text = "Términos y Condiciones de Uso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Fecha de última actualización
            Text(
                text = "Última actualización: 1 de junio de 2023",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Introducción
            Text(
                text = "Bienvenido a UmeEgunero. Al utilizar nuestra aplicación, aceptas estos términos y condiciones. Por favor, léelos detenidamente antes de continuar.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sección 1: Aceptación de términos
            SeccionTerminos(
                titulo = "1. Aceptación de los Términos",
                contenido = "Al acceder y utilizar UmeEgunero, aceptas estar sujeto a estos términos y condiciones de uso. Si no estás de acuerdo con alguna parte de estos términos, no debes utilizar nuestra aplicación."
            )
            
            // Sección 2: Descripción del servicio
            SeccionTerminos(
                titulo = "2. Descripción del Servicio",
                contenido = "UmeEgunero es una plataforma educativa diseñada para facilitar la comunicación entre centros educativos, profesores y familias. La aplicación permite el seguimiento del progreso académico, la comunicación directa y la gestión de información relacionada con la educación de los alumnos."
            )
            
            // Sección 3: Registro y cuenta de usuario
            SeccionTerminos(
                titulo = "3. Registro y Cuenta de Usuario",
                contenido = "Para utilizar UmeEgunero, debes registrarte proporcionando información precisa y completa. Eres responsable de mantener la confidencialidad de tu cuenta y contraseña, y de todas las actividades que ocurran bajo tu cuenta."
            )
            
            // Sección 4: Privacidad y datos personales
            SeccionTerminos(
                titulo = "4. Privacidad y Datos Personales",
                contenido = "Tu privacidad es importante para nosotros. Recopilamos y procesamos datos personales de acuerdo con nuestra Política de Privacidad, que forma parte integral de estos términos. Al utilizar UmeEgunero, consientes el procesamiento de tus datos personales según lo descrito en nuestra Política de Privacidad."
            )
            
            // Sección 5: Uso aceptable
            SeccionTerminos(
                titulo = "5. Uso Aceptable",
                contenido = "Te comprometes a utilizar UmeEgunero solo para fines legales y de manera que no infrinja los derechos de otros usuarios. No está permitido el uso de la aplicación para actividades fraudulentas, maliciosas o que puedan dañar la reputación de UmeEgunero o de otros usuarios."
            )
            
            // Sección 6: Contenido generado por el usuario
            SeccionTerminos(
                titulo = "6. Contenido Generado por el Usuario",
                contenido = "Al publicar contenido en UmeEgunero, otorgas a la plataforma una licencia mundial, no exclusiva y libre de regalías para usar, reproducir, modificar, adaptar, publicar, traducir y distribuir dicho contenido en cualquier formato y medio."
            )
            
            // Sección 7: Limitación de responsabilidad
            SeccionTerminos(
                titulo = "7. Limitación de Responsabilidad",
                contenido = "UmeEgunero no será responsable por daños indirectos, incidentales, especiales, consecuentes o punitivos que resulten de tu uso o imposibilidad de usar la aplicación."
            )
            
            // Sección 8: Modificaciones de los términos
            SeccionTerminos(
                titulo = "8. Modificaciones de los Términos",
                contenido = "Nos reservamos el derecho de modificar estos términos en cualquier momento. Te notificaremos sobre cambios significativos mediante un aviso en la aplicación o por correo electrónico. El uso continuado de UmeEgunero después de dichos cambios constituye tu aceptación de los nuevos términos."
            )
            
            // Sección 9: Terminación
            SeccionTerminos(
                titulo = "9. Terminación",
                contenido = "Podemos terminar o suspender tu acceso a UmeEgunero inmediatamente, sin previo aviso, por cualquier razón, incluyendo, sin limitación, el incumplimiento de estos términos."
            )
            
            // Sección 10: Ley aplicable
            SeccionTerminos(
                titulo = "10. Ley Aplicable",
                contenido = "Estos términos se regirán e interpretarán de acuerdo con las leyes de España, sin tener en cuenta sus disposiciones sobre conflictos de leyes."
            )
            
            // Sección 11: Contacto
            SeccionTerminos(
                titulo = "11. Contacto",
                contenido = "Si tienes alguna pregunta sobre estos términos, por favor contáctanos en soporte@umeegunero.com."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón para aceptar
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Entendido")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Componente para mostrar una sección de los términos y condiciones
 */
@Composable
private fun SeccionTerminos(
    titulo: String,
    contenido: String
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = contenido,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 