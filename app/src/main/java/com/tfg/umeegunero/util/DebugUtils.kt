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

    // Credenciales por defecto para el administrador
    private val DEFAULT_ADMIN_EMAIL = "admin@eguneroko.com"
    private val DEFAULT_ADMIN_PASSWORD = "Lloreria2025"
    private val DEFAULT_ADMIN_DNI = "12345678P"

    /**
     * Verifica si existe al menos un administrador, de lo contrario crea uno
     */
    fun ensureAdminExists() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Comprobar si ya existe un admin
                val result = usuarioRepository.getUsersByType(TipoUsuario.ADMIN_APP)

                if (result is com.tfg.umeegunero.data.repository.Result.Success && result.data.isEmpty()) {
                    Log.d("DebugUtils", "No existe ningún administrador. Creando uno por defecto...")
                    createDefaultAdmin()
                } else {
                    Log.d("DebugUtils", "Ya existe al menos un administrador. No es necesario crear uno.")
                }
            } catch (e: Exception) {
                Log.e("DebugUtils", "Error al verificar administradores", e)
            }
        }
    }

    /**admin
     * Crea un administrador por defecto en Authentication y Firestore
     */
    private suspend fun createDefaultAdmin() {
        try {
            // 1. Crear usuario en Firebase Authentication
            val firebaseAuth = FirebaseAuth.getInstance()

            // Verificar si el usuario ya existe en Auth
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(
                    DEFAULT_ADMIN_EMAIL,
                    DEFAULT_ADMIN_PASSWORD
                ).await()

                val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID del usuario creado")
                Log.d("DebugUtils", "Usuario creado en Authentication con UID: $uid")
            } catch (e: Exception) {
                Log.e("DebugUtils", "Error o usuario ya existe en Authentication", e)
                // Si falla, podría ser porque el usuario ya existe
            }

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
            val savedResult = usuarioRepository.guardarUsuario(admin)

            if (savedResult is com.tfg.umeegunero.data.repository.Result.Success) {
                Log.d("DebugUtils", "Administrador creado correctamente en Firestore")
            } else if (savedResult is com.tfg.umeegunero.data.repository.Result.Error) {
                Log.e("DebugUtils", "Error al guardar administrador en Firestore", savedResult.exception)
            }

            // 5. Cerrar sesión por seguridad
            firebaseAuth.signOut()

        } catch (e: Exception) {
            Log.e("DebugUtils", "Error al crear administrador", e)
        }
    }
}