package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Resultado
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la autenticación y los datos de usuario
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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
    suspend fun login(email: String, password: String): Resultado<FirebaseUser> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        authResult.user?.let {
            Resultado.Exito(it)
        } ?: Resultado.Error("Error al iniciar sesión", Exception("Error al iniciar sesión"))
    } catch (e: Exception) {
        Timber.e(e, "Error durante el login")
        Resultado.Error(e.message, e)
    }
    
    /**
     * Registra un nuevo usuario con correo electrónico y contraseña
     */
    suspend fun register(email: String, password: String, nombre: String): Resultado<FirebaseUser> = try {
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
            Resultado.Exito(user)
        } ?: Resultado.Error("Error al registrar usuario", Exception("Error al registrar usuario"))
    } catch (e: Exception) {
        Timber.e(e, "Error durante el registro")
        Resultado.Error(e.message, e)
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun logout(): Resultado<Unit> = try {
        auth.signOut()
        Resultado.Exito(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al cerrar sesión")
        Resultado.Error(e.message, e)
    }
    
    /**
     * Envía un correo de recuperación de contraseña
     */
    suspend fun resetPassword(email: String): Resultado<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resultado.Exito(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al enviar correo de recuperación")
        Resultado.Error(e.message, e)
    }
} 