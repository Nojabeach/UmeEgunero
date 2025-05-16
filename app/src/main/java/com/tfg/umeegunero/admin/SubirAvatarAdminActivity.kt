package com.tfg.umeegunero.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import timber.log.Timber

/**
 * Actividad administrativa para subir el avatar del administrador a Firebase Storage
 * y actualizar las referencias en Firestore.
 *
 * Esta actividad es solo para uso durante el desarrollo y pruebas.
 */
class SubirAvatarAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UmeEguneroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SubirAvatarAdminScreen()
                }
            }
        }
    }
}

@Composable
fun SubirAvatarAdminScreen() {
    val context = LocalContext.current
    var estado by remember { mutableStateOf("Listo para subir") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Subir Avatar de Administrador",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = estado,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón para subir desde assets
                Button(
                    onClick = {
                        isLoading = true
                        estado = "Subiendo desde assets..."
                        
                        try {
                            // Iniciar subida desde assets
                            val adminTools = AdminTools(context)
                            adminTools.subirAvatarAdministradorDesdeAssets("images/AdminAvatar.png")
                            
                            estado = "Proceso iniciado desde assets. Revisa los logs para más detalles."
                            Timber.i("Solicitud de subida de avatar desde assets enviada")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al iniciar la subida desde assets: ${e.message}")
                            estado = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                ) {
                    Text("Subir Desde Assets")
                }
                
                // Separador
                Divider(Modifier.padding(vertical = 8.dp))
                
                // Botón para subir desde archivo local
                Button(
                    onClick = {
                        isLoading = true
                        estado = "Subiendo desde archivo local..."
                        
                        try {
                            // Ruta del archivo en los recursos
                            val rutaArchivo = "/Users/maitane/AndroidStudioProjects/UmeEgunero/app/src/main/resources/images/AdminAvatar.png"
                            
                            // Iniciar subida
                            val adminTools = AdminTools(context)
                            adminTools.subirAvatarAdministrador(rutaArchivo)
                            
                            estado = "Proceso iniciado desde archivo local. Revisa los logs para más detalles."
                            Timber.i("Solicitud de subida de avatar desde archivo local enviada")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al iniciar la subida desde archivo local: ${e.message}")
                            estado = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                ) {
                    Text("Subir Desde Archivo Local")
                }
            }
        }
    }
} 