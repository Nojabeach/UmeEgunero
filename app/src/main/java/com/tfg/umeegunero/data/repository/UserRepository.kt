package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la autenticación y los datos de usuario
 */
@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    private val usersCollection = firestore.collection("usuarios")
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    /**
     * Obtiene el ID del usuario actualmente autenticado
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Inicia sesión con correo electrónico y contraseña
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        authResult.user?.let {
            Result.Success(it)
        } ?: Result.Error(Exception("Error al iniciar sesión"))
    } catch (e: Exception) {
        Timber.e(e, "Error durante el login")
        Result.Error(e)
    }
    
    /**
     * Registra un nuevo usuario con correo electrónico y contraseña
     */
    suspend fun register(email: String, password: String, nombre: String): Result<FirebaseUser> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.let { user ->
            // Guardar información adicional del usuario en Firestore
            val userData = hashMapOf(
                "nombre" to nombre,
                "email" to email,
                "id" to user.uid,
                "fechaCreacion" to com.google.firebase.Timestamp.now()
            )
            usersCollection.document(user.uid).set(userData).await()
            Result.Success(user)
        } ?: Result.Error(Exception("Error al registrar usuario"))
    } catch (e: Exception) {
        Timber.e(e, "Error durante el registro")
        Result.Error(e)
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun logout(): Result<Unit> = try {
        auth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al cerrar sesión")
        Result.Error(e)
    }
    
    /**
     * Envía un correo de recuperación de contraseña
     */
    suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al enviar correo de recuperación")
        Result.Error(e)
    }
} 