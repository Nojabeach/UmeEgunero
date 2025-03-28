package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Interfaz que define las operaciones relacionadas con la autenticación de usuarios.
 * 
 * Esta interfaz abstrae las operaciones de autenticación de Firebase Auth
 * para facilitar las pruebas unitarias y seguir el principio de inyección
 * de dependencias.
 * 
 * @author Estudiante 2º DAM
 */
interface AuthRepository {
    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * @return El objeto Usuario si hay un usuario autenticado, null en caso contrario
     */
    suspend fun getCurrentUser(): Usuario?
    
    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun signOut()
    
    /**
     * Envía un correo de recuperación de contraseña al email proporcionado.
     * 
     * @param email Correo electrónico del usuario
     * @return Resultado de la operación encapsulado en un objeto Result
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
    
    /**
     * Obtiene el usuario de Firebase actual.
     * 
     * @return El objeto FirebaseUser si hay un usuario autenticado, null en caso contrario
     */
    suspend fun getFirebaseUser(): FirebaseUser?
}

/**
 * Implementación de AuthRepository utilizando Firebase Auth.
 * 
 * Esta clase gestiona todas las operaciones relacionadas con la autenticación
 * de usuarios en la aplicación: inicio de sesión, registro, recuperación de contraseña, etc.
 * 
 * Utiliza Firebase Auth para la autenticación y se comunica con el repositorio
 * de usuarios para obtener información adicional.
 * 
 * @param firebaseAuth Instancia de FirebaseAuth inyectada por Hilt
 * @param usuarioRepository Repositorio de usuarios inyectado por Hilt
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository
) : AuthRepository {

    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * Primero verifica si hay un usuario autenticado en Firebase Auth,
     * luego busca sus datos completos en Firestore a través del UsuarioRepository.
     * 
     * @return Objeto Usuario con los datos completos del usuario o null si no hay usuario autenticado
     */
    override suspend fun getCurrentUser(): Usuario? {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val email = firebaseUser.email
                if (email != null) {
                    Timber.d("Firebase email: $email")
                    when (val result = usuarioRepository.getUsuarioByEmail(email)) {
                        is Result.Success -> {
                            Timber.d("Usuario encontrado por email: ${result.data.nombre}")
                            result.data
                        }
                        is Result.Error -> {
                            Timber.e(result.exception, "Error al obtener usuario por email")
                            null
                        }
                        else -> null
                    }
                } else {
                    Timber.d("El usuario autenticado no tiene email")
                    null
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

    /**
     * Cierra la sesión del usuario actual.
     * 
     * Utiliza el método signOut de Firebase Auth para cerrar la sesión.
     */
    override suspend fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Timber.e(e, "Error al cerrar sesión")
        }
    }

    /**
     * Envía un correo de recuperación de contraseña al email proporcionado.
     * 
     * Por seguridad, si el email no existe en la base de datos, igualmente
     * devuelve éxito para no dar información sobre qué emails están registrados.
     * 
     * @param email Correo electrónico del usuario
     * @return Resultado de la operación encapsulado en un objeto Result
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

    /**
     * Obtiene el usuario de Firebase actual.
     * 
     * @return El objeto FirebaseUser actual o null si no hay usuario autenticado
     */
    override suspend fun getFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
} 