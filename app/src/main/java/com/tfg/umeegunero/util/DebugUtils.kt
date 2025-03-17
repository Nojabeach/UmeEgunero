package com.tfg.umeegunero.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugUtils @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {

    // Credenciales para el admin por defecto
    private val DEFAULT_ADMIN_EMAIL = "admin@eguneroko.com"
    private val DEFAULT_ADMIN_PASSWORD = "Lloreria2025"
    private val DEFAULT_ADMIN_DNI = "12345678P"

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
                            if (adminDoc is com.tfg.umeegunero.data.repository.Result.Success) {
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

            if (savedResult is com.tfg.umeegunero.data.repository.Result.Success) {
                Log.d("DebugUtils", "Administrador creado correctamente en Firestore")
            } else if (savedResult is com.tfg.umeegunero.data.repository.Result.Error) {
                Log.e("DebugUtils", "Error al guardar administrador en Firestore: ${savedResult.exception.message}")
                throw savedResult.exception
            }

            // 5. Cerramos sesión por seguridad
            firebaseAuth.signOut()

        } catch (e: Exception) {
            Log.e("DebugUtils", "Error al crear administrador por defecto", e)
            throw e
        }
    }
}