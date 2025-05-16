package com.tfg.umeegunero.admin

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.TipoUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Clase auxiliar para realizar tareas administrativas
 * como subir recursos estáticos a Firebase Storage
 */
class AdminTools(private val context: Context) {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = storage.reference
    
    // URL fija para el avatar de administrador
    private val ADMIN_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media"

    /**
     * Sube el avatar del administrador a Firebase Storage desde los assets
     * y actualiza todas las referencias en Firestore
     */
    fun subirAvatarAdministradorDesdeAssets(nombreArchivo: String = "images/AdminAvatar.png") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("Intentando cargar avatar de administrador desde assets: $nombreArchivo")
                
                // Crear archivo temporal desde los assets
                val archivoTemporal = File(context.cacheDir, "AdminAvatar.png")
                
                try {
                    // Abrir el stream del asset
                    context.assets.open(nombreArchivo).use { inputStream ->
                        // Escribir al archivo temporal
                        FileOutputStream(archivoTemporal).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    
                    Timber.d("Archivo de assets copiado a temporal: ${archivoTemporal.absolutePath}")
                    
                    // Convertir a Uri y subir
                    val uri = Uri.fromFile(archivoTemporal)
                    
                    // Subir a Firebase Storage
                    val avatarRef = storageRef.child("AdminAvatar.png")
                    
                    val uploadTask = avatarRef.putFile(uri)
                    uploadTask.addOnSuccessListener {
                        Timber.d("Archivo subido correctamente a Firebase Storage")
                        
                        // Obtener URL pública
                        avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val urlPublica = downloadUri.toString()
                            Timber.d("URL pública obtenida: $urlPublica")
                            
                            // Actualizar URLs en todos los usuarios administradores
                            actualizarUrlsAdministradores(urlPublica)
                            
                            // Limpiar archivo temporal
                            try {
                                if (archivoTemporal.exists()) {
                                    archivoTemporal.delete()
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al eliminar archivo temporal")
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error al subir archivo: ${exception.message}")
                    }
                } catch (e: IOException) {
                    Timber.e(e, "Error al acceder al asset: $nombreArchivo")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error general al subir avatar de administrador: ${e.message}")
            }
        }
    }

    /**
     * Sube el avatar del administrador a Firebase Storage
     * y actualiza todas las referencias en Firestore
     */
    fun subirAvatarAdministrador(rutaLocalArchivo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("Iniciando subida de avatar de administrador desde: $rutaLocalArchivo")
                
                // Verificar que el archivo existe
                val archivoLocal = File(rutaLocalArchivo)
                if (!archivoLocal.exists()) {
                    Timber.e("El archivo no existe en la ruta especificada: $rutaLocalArchivo")
                    return@launch
                }
                
                // Convertir a Uri
                val uri = Uri.fromFile(archivoLocal)
                
                // Subir a Firebase Storage
                val avatarRef = storageRef.child("AdminAvatar.png")
                
                val uploadTask = avatarRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    Timber.d("Archivo subido correctamente a Firebase Storage")
                    
                    // Obtener URL pública
                    avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val urlPublica = downloadUri.toString()
                        Timber.d("URL pública obtenida: $urlPublica")
                        
                        // Actualizar URLs en todos los usuarios administradores
                        actualizarUrlsAdministradores(urlPublica)
                    }
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "Error al subir archivo: ${exception.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error general al subir avatar de administrador: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza la URL del avatar para todos los usuarios administradores en Firestore
     */
    private fun actualizarUrlsAdministradores(urlAvatar: String) {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { resultado ->
                for (documento in resultado) {
                    // Intentar obtener el campo "perfiles"
                    val perfiles = documento.get("perfiles") as? List<Map<String, Any>> ?: continue
                    
                    // Verificar si alguno de los perfiles es ADMIN_APP
                    val esAdmin = perfiles.any { perfil ->
                        val tipoStr = perfil["tipo"] as? String
                        tipoStr == "ADMIN_APP"
                    }
                    
                    if (esAdmin) {
                        // Si es administrador, actualizar la URL del avatar
                        val dni = documento.id
                        Timber.d("Actualizando URL de avatar para administrador con DNI: $dni")
                        
                        firestore.collection("usuarios")
                            .document(dni)
                            .update("avatarUrl", urlAvatar)
                            .addOnSuccessListener {
                                Timber.d("URL de avatar actualizada para administrador con DNI: $dni")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al actualizar URL de avatar para administrador con DNI: $dni")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error al consultar usuarios: ${e.message}")
            }
    }

    /**
     * Sube el avatar del administrador a Firebase Storage desde los recursos raw
     * y actualiza todas las referencias en Firestore.
     * 
     * Este método es útil cuando la imagen está en la carpeta de recursos (R.drawable o R.raw)
     * en lugar de en los assets.
     */
    fun subirAvatarAdministradorDesdeRecursos() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("Intentando cargar avatar de administrador desde recursos...")
                
                // Crear archivo temporal para la imagen
                val archivoTemporal = File(context.cacheDir, "AdminAvatar.png")
                
                try {
                    // Si la imagen está en resources/images, necesitamos copiarla primero
                    val rutaRecursos = "/app/src/main/resources/images/AdminAvatar.png"
                    val archivoRecursos = File(rutaRecursos)
                    
                    if (archivoRecursos.exists()) {
                        Timber.d("Encontrado archivo en recursos: ${archivoRecursos.absolutePath}")
                        archivoRecursos.copyTo(archivoTemporal, overwrite = true)
                    } else {
                        // Intentar con la ruta absoluta
                        val rutaAbsoluta = "/Users/maitane/AndroidStudioProjects/UmeEgunero/app/src/main/resources/images/AdminAvatar.png"
                        val archivoAbsoluto = File(rutaAbsoluta)
                        
                        if (archivoAbsoluto.exists()) {
                            Timber.d("Encontrado archivo en ruta absoluta: $rutaAbsoluta")
                            archivoAbsoluto.copyTo(archivoTemporal, overwrite = true)
                        } else {
                            Timber.e("No se pudo encontrar el archivo AdminAvatar.png en los recursos")
                            return@launch
                        }
                    }
                    
                    Timber.d("Archivo copiado a temporal: ${archivoTemporal.absolutePath}")
                    
                    // Convertir a Uri y subir
                    val uri = Uri.fromFile(archivoTemporal)
                    
                    // Subir a Firebase Storage
                    val avatarRef = storageRef.child("AdminAvatar.png")
                    
                    val uploadTask = avatarRef.putFile(uri)
                    uploadTask.addOnSuccessListener {
                        Timber.d("Archivo subido correctamente a Firebase Storage")
                        
                        // Obtener URL pública
                        avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val urlPublica = downloadUri.toString()
                            Timber.d("URL pública obtenida: $urlPublica")
                            
                            // Actualizar URLs en todos los usuarios administradores
                            actualizarUrlsAdministradores(urlPublica)
                            
                            // Limpiar archivo temporal
                            try {
                                if (archivoTemporal.exists()) {
                                    archivoTemporal.delete()
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al eliminar archivo temporal")
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error al subir archivo: ${exception.message}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al copiar archivo desde recursos: ${e.message}")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error general al subir avatar de administrador desde recursos: ${e.message}")
            }
        }
    }
} 