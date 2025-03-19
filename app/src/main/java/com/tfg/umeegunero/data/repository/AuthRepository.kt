package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

interface AuthRepository {
    suspend fun getCurrentUser(): Usuario?
    suspend fun signOut()
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
} 