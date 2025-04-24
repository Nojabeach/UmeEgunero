package com.tfg.umeegunero.util

import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Utilidades relacionadas con la obtención de información del usuario actual.
 *
 * Esta clase proporciona funciones para obtener datos consistentes del usuario
 * autenticado, especialmente el ID del centro educativo al que está asociado.
 * Las diferentes implementaciones están consolidadas aquí para garantizar
 * coherencia en toda la aplicación.
 */
object UsuarioUtils {
    
    /**
     * Obtiene el ID del centro educativo del usuario actual.
     * 
     * La función intenta obtener el centroId mediante varios métodos, en orden de prioridad:
     * 1. Busca un perfil de tipo ADMIN_CENTRO
     * 2. Busca un perfil de tipo PROFESOR
     * 3. Usa el método específico del repositorio de usuarios
     * 
     * @param authRepository Repositorio de autenticación para obtener usuario actual
     * @param usuarioRepository Repositorio de usuarios para obtener información completa
     * @return String con el ID del centro o null si no se encuentra
     */
    suspend fun obtenerCentroIdDelUsuarioActual(
        authRepository: AuthRepository,
        usuarioRepository: UsuarioRepository
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Obtener usuario actual de Firebase Auth
            val firebaseUser = authRepository.getCurrentUser() ?: return@withContext null
            val usuarioId = firebaseUser.documentId
            
            Timber.d("Obteniendo centroId para usuario: $usuarioId")
            
            // MÉTODO 1: Buscar en perfiles del usuario
            val usuarioResult = usuarioRepository.getUsuarioById(usuarioId)
            if (usuarioResult is com.tfg.umeegunero.util.Result.Success) {
                val usuario = usuarioResult.data
                val centroId = obtenerCentroIdDePerfiles(usuario)
                if (centroId != null) {
                    return@withContext centroId
                }
            } else if (firebaseUser.email != null) {
                // Intentar obtener por email si no se encontró por ID
                val usuarioEmailResult = usuarioRepository.getUsuarioByEmail(firebaseUser.email!!)
                if (usuarioEmailResult is com.tfg.umeegunero.util.Result.Success) {
                    val usuario = usuarioEmailResult.data
                    val centroId = obtenerCentroIdDePerfiles(usuario)
                    if (centroId != null) {
                        return@withContext centroId
                    }
                }
            }
            
            // MÉTODO 2: Usar método específico del repositorio
            try {
                val centroId = usuarioRepository.getCentroIdUsuarioActual()
                if (!centroId.isNullOrEmpty()) {
                    Timber.d("CentroId obtenido con método alternativo: $centroId")
                    return@withContext centroId
                }
            } catch (e: Exception) {
                Timber.e(e, "Error obteniendo centroId por método alternativo: ${e.message}")
            }
            
            // No se encontró centroId
            Timber.w("No se pudo obtener centroId para el usuario actual")
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centroId del usuario actual: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Extrae el centroId de los perfiles del usuario.
     * 
     * Busca primero un perfil de tipo ADMIN_CENTRO, y si no existe, 
     * busca un perfil de tipo PROFESOR.
     * 
     * @param usuario El objeto Usuario completo
     * @return String con el ID del centro o null si no se encuentra
     */
    private fun obtenerCentroIdDePerfiles(usuario: Usuario): String? {
        // Primero buscar perfil de ADMIN_CENTRO
        val perfilAdmin = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
        if (perfilAdmin != null && perfilAdmin.centroId.isNotEmpty()) {
            Timber.d("CentroId encontrado en perfil ADMIN_CENTRO: ${perfilAdmin.centroId}")
            return perfilAdmin.centroId
        }
        
        // Luego buscar perfil de PROFESOR
        val perfilProfesor = usuario.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
        if (perfilProfesor != null && perfilProfesor.centroId.isNotEmpty()) {
            Timber.d("CentroId encontrado en perfil PROFESOR: ${perfilProfesor.centroId}")
            return perfilProfesor.centroId
        }
        
        Timber.d("No se encontró centroId en ningún perfil del usuario")
        return null
    }
} 