package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

// TODO: Mejoras pendientes para el repositorio de autenticación
// - Implementar autenticación biométrica (huella, FaceID)
// - Añadir soporte para autenticación con redes sociales (Google, Facebook)
// - Implementar sistema de tokens para mejorar seguridad
// - Desarrollar sistema de detección de intentos sospechosos
// - Añadir autenticación en dos factores
// - Implementar registro de sesiones activas
// - Desarrollar sistema de bloqueo temporal de cuentas
// - Mejorar el manejo de errores con respuestas más detalladas

interface AuthRepository {
    suspend fun getCurrentUser(): Usuario?
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository
) : AuthRepository {

    override suspend fun getCurrentUser(): Usuario? {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null && firebaseUser.email != null) {
                Timber.d("Firebase email: ${firebaseUser.email}")
                when (val result = usuarioRepository.getUsuarioByEmail(firebaseUser.email!!)) {
                    is Result.Success -> {
                        Timber.d("Usuario encontrado por email: ${result.data?.nombre}")
                        result.data
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener usuario por email")
                        null
                    }
                    else -> null
                }
            } else {
                Timber.d("No hay usuario autenticado en Firebase")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el usuario actual")
            null
        }
    }

    override suspend fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Timber.e(e, "Error al cerrar sesión")
        }
    }

    /**
     * Envía un correo de recuperación de contraseña
     * @param email Correo electrónico del usuario
     * @return Resultado de la operación
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Result.Success(true)
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidUserException -> {
                        // Evitamos dar pistas sobre qué emails están registrados
                        // Respondemos con éxito aunque el usuario no exista
                        Result.Success(true)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        Result.Error(Exception("El correo electrónico no es válido"))
                    }
                    else -> {
                        Timber.e(e, "Error al enviar email de recuperación")
                        Result.Error(e)
                    }
                }
            }
        }
    }
} 