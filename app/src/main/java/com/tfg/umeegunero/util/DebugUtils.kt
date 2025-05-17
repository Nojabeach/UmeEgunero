package com.tfg.umeegunero.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class DebugUtils @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {

    // Credenciales para el admin por defecto
    private val DEFAULT_ADMIN_EMAIL = "admin@eguneroko.com"
    private val DEFAULT_ADMIN_PASSWORD = "Lloreria2025"
    private val DEFAULT_ADMIN_DNI = "45678698P"

    /**
     * Comprueba si hay un admin en el sistema, si no lo crea
     */
    fun ensureAdminExists() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intento de login para ver si ya existe
                try {
                    Log.d("DebugUtils", "Probando login de admin...")
                    val authResult = FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        DEFAULT_ADMIN_EMAIL,
                        DEFAULT_ADMIN_PASSWORD
                    ).await()

                    // Si llega aquí, el admin ya existe
                    Log.d("DebugUtils", "Login OK, admin ya existe")

                    // Cerramos sesión para no liar al usuario
                    FirebaseAuth.getInstance().signOut()

                } catch (authError: Exception) {
                    // Si falla el login, probablemente no existe, vamos a crearlo
                    Log.d("DebugUtils", "No se pudo hacer login como admin: ${authError.message}")

                    try {
                        // Creamos el admin
                        createDefaultAdmin()
                    } catch (createError: Exception) {
                        // Si falla la creación, miramos si es por permisos o porque ya existe
                        Log.e("DebugUtils", "Error al crear admin: ${createError.message}")

                        // Plan B: comprobamos en Firestore directamente
                        try {
                            val adminDoc = usuarioRepository.getUsuarioPorDni(DEFAULT_ADMIN_DNI)
                            if (adminDoc is Result.Success) {
                                Log.d("DebugUtils", "Admin ya existe en Firestore")
                            } else {
                                Log.d("DebugUtils", "No se pudo verificar admin en Firestore")
                            }
                        } catch (e: Exception) {
                            Log.e("DebugUtils", "Error al verificar admin en Firestore: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DebugUtils", "Error general al verificar administradores", e)
            }
        }
    }

    /**
     * Crea el admin por defecto
     */
    private suspend fun createDefaultAdmin() {
        try {
            // 1. Crear usuario en Firebase Authentication
            val firebaseAuth = FirebaseAuth.getInstance()

            Log.d("DebugUtils", "Creando usuario en Authentication...")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                DEFAULT_ADMIN_EMAIL,
                DEFAULT_ADMIN_PASSWORD
            ).await()

            val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID del usuario creado")
            Log.d("DebugUtils", "Usuario creado en Authentication con UID: $uid")

            // 2. Crear perfil de administrador
            val perfil = Perfil(
                tipo = TipoUsuario.ADMIN_APP,
                verificado = true
            )

            // 3. Crear usuario administrador en Firestore
            val admin = Usuario(
                dni = DEFAULT_ADMIN_DNI,
                email = DEFAULT_ADMIN_EMAIL,
                nombre = "Administrador",
                apellidos = "Sistema",
                telefono = "+34600000000",
                fechaRegistro = Timestamp.now(),
                perfiles = listOf(perfil),
                activo = true
            )

            // 4. Guardar en Firestore
            Log.d("DebugUtils", "Guardando administrador en Firestore...")
            val savedResult = usuarioRepository.guardarUsuario(admin)

            when (savedResult) {
                is Result.Success -> {
                    Log.d("DebugUtils", "Administrador creado correctamente en Firestore")
                }
                is Result.Error -> {
                    Log.e("DebugUtils", "Error al guardar administrador en Firestore: ${savedResult.exception?.message}")
                    // Intentar eliminar el usuario de Firebase Auth si falló en Firestore
                    try {
                        firebaseAuth.currentUser?.delete()?.await()
                        Log.d("DebugUtils", "Usuario eliminado de Firebase Auth después de fallo en Firestore")
                    } catch (e: Exception) {
                        Log.e("DebugUtils", "Error al eliminar usuario de Firebase Auth: ${e.message}")
                    }
                    throw savedResult.exception ?: Exception("Error desconocido al guardar administrador")
                }
                else -> {
                    Log.e("DebugUtils", "Estado inesperado al guardar administrador")
                    throw Exception("Estado inesperado al guardar administrador")
                }
            }

            // 5. Cerramos sesión por seguridad
            firebaseAuth.signOut()
            Log.d("DebugUtils", "Sesión cerrada después de crear administrador")

        } catch (e: Exception) {
            Log.e("DebugUtils", "Error al crear administrador por defecto", e)
            throw e
        }
    }

    /**
     * Elimina todos los centros educativos de Firestore para resolver problemas de datos incompatibles
     * @return Un par con (éxito: Boolean, mensaje: String)
     */
    suspend fun purgarCentrosEducativos(): Pair<Boolean, String> {
        return try {
            Log.d("DebugUtils", "Iniciando purga de centros educativos...")
            
            // Referencia a la colección de centros
            val centrosCollection = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("centros")
            
            // Obtener todos los centros
            val querySnapshot = centrosCollection.get().await()
            
            if (querySnapshot.isEmpty) {
                Log.d("DebugUtils", "No se encontraron centros para purgar")
                return Pair(true, "No se encontraron centros para purgar. La base de datos ya está limpia.")
            }
            
            Log.d("DebugUtils", "Se encontraron ${querySnapshot.size()} centros para purgar")
            
            // Lista para guardar operaciones de escritura
            val batch = com.google.firebase.firestore.FirebaseFirestore.getInstance().batch()
            
            // Para cada centro, programar su eliminación
            querySnapshot.documents.forEach { doc ->
                val id = doc.id
                Log.d("DebugUtils", "Programando eliminación del centro con ID: $id")
                batch.delete(centrosCollection.document(id))
            }
            
            // Ejecutar todas las eliminaciones como una operación atómica
            batch.commit().await()
            
            Log.d("DebugUtils", "Purga completada con éxito. Se eliminaron ${querySnapshot.size()} centros.")
            Pair(true, "Purga completada con éxito. Se eliminaron ${querySnapshot.size()} centros.")
            
        } catch (e: Exception) {
            Log.e("DebugUtils", "Error durante la purga de centros: ${e.message}", e)
            Pair(false, "Error durante la purga de centros: ${e.message}")
        }
    }

    /**
     * Comprueba si existe algún admin_app en Firestore y, si no existe, crea uno con los datos de Maitane.
     * Solo debe ejecutarse en modo debug.
     */
    fun ensureDebugAdminApp() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Buscar si existe algún usuario con perfil ADMIN_APP
                val adminsResult = usuarioRepository.getAdministradores()
                val existeAdmin = adminsResult is Result.Success && adminsResult.data.isNotEmpty()
                if (existeAdmin) {
                    Timber.d("Ya existe al menos un usuario ADMIN_APP en Firestore")
                    return@launch
                }
                
                // Verificar también si existe algún administrador protegido por DNI
                val protectedAdminDNIs = listOf("45678698P")
                for (protectedDNI in protectedAdminDNIs) {
                    try {
                        val result = usuarioRepository.getUsuarioPorDni(protectedDNI)
                        if (result is Result.Success) {
                            Timber.d("Administrador protegido con DNI $protectedDNI encontrado. No se creará uno nuevo.")
                            return@launch
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al verificar administrador protegido con DNI $protectedDNI")
                    }
                }
                
                Timber.d("No existe ningún ADMIN_APP, creando usuario de debug...")
                // Crear perfil de admin
                val perfil = Perfil(
                    tipo = TipoUsuario.ADMIN_APP,
                    verificado = true
                )
                // Crear usuario admin
                val admin = Usuario(
                    dni = "45678698P",
                    email = "admin@eguneroko.com",
                    nombre = "Maitane",
                    apellidos = "",
                    telefono = "944831879",
                    fechaRegistro = com.google.firebase.Timestamp.now(),
                    perfiles = listOf(perfil),
                    activo = true
                )
                // Guardar en Firestore
                val saveResult = usuarioRepository.guardarUsuario(admin)
                if (saveResult is Result.Success) {
                    Timber.d("Usuario ADMIN_APP de debug creado correctamente en Firestore")
                } else if (saveResult is Result.Error) {
                    Timber.e(saveResult.exception, "Error al crear usuario ADMIN_APP de debug")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al crear usuario ADMIN_APP de debug")
            }
        }
    }
}