package com.tfg.umeegunero.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.DefaultAvatarsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para gestionar los avatares de los usuarios.
 *
 * Este servicio proporciona métodos para asignar avatares predeterminados
 * a los usuarios según su tipo, actualizar avatares existentes y
 * sincronizar los avatares de todos los usuarios.
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property defaultAvatarsManager Gestor de avatares predeterminados
 *
 * @author Maitane Ibañez Irazabal
 */
@Singleton
class AvatarService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val defaultAvatarsManager: DefaultAvatarsManager
) {
    private val usuariosCollection = firestore.collection("usuarios")
    
    /**
     * Asigna avatares predeterminados a todos los usuarios sin avatar.
     * @return Mapa con los DNIs de los usuarios actualizados y las URLs asignadas
     */
    suspend fun asignarAvataresPredeterminados(): Map<String, String> = withContext(Dispatchers.IO) {
        val resultados = mutableMapOf<String, String>()
        
        try {
            // Obtener todos los usuarios
            val snapshot = usuariosCollection.get().await()
            val usuarios = snapshot.toObjects(Usuario::class.java)
            
            Timber.d("Procesando ${usuarios.size} usuarios para asignar avatares predeterminados")
            
            for (usuario in usuarios) {
                // Si el usuario no tiene avatar o tiene un avatar vacío
                if (usuario.avatarUrl.isNullOrEmpty()) {
                    val tipoUsuario = obtenerTipoUsuarioPrincipal(usuario.perfiles)
                    
                    // Obtener URL de avatar predeterminada
                    val avatarUrl = defaultAvatarsManager.obtenerAvatarPredeterminado(tipoUsuario)
                    
                    if (avatarUrl.isNotEmpty()) {
                        // Actualizar usuario con la URL del avatar
                        usuariosCollection.document(usuario.dni).update("avatarUrl", avatarUrl).await()
                        
                        Timber.d("Avatar asignado a ${usuario.dni}: $avatarUrl")
                        resultados[usuario.dni] = avatarUrl
                    }
                }
            }
            
            Timber.d("Asignación de avatares completada. ${resultados.size} usuarios actualizados")
            return@withContext resultados
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar avatares predeterminados: ${e.message}")
            return@withContext resultados
        }
    }
    
    /**
     * Asigna un avatar predeterminado a un usuario específico.
     * @param dni DNI del usuario
     * @return URL del avatar asignado o cadena vacía si falla
     */
    suspend fun asignarAvatarPredeterminadoAUsuario(dni: String): String = withContext(Dispatchers.IO) {
        try {
            // Obtener el usuario
            val documentSnapshot = usuariosCollection.document(dni).get().await()
            if (!documentSnapshot.exists()) {
                Timber.w("No se encontró usuario con DNI: $dni")
                return@withContext ""
            }
            
            val usuario = documentSnapshot.toObject(Usuario::class.java)
            if (usuario == null) {
                Timber.w("Error al convertir documento a Usuario para DNI: $dni")
                return@withContext ""
            }
            
            // Si el usuario ya tiene avatar, devolver esa URL
            if (!usuario.avatarUrl.isNullOrEmpty()) {
                Timber.d("Usuario ya tiene avatar: ${usuario.avatarUrl}")
                return@withContext usuario.avatarUrl!!
            }
            
            // Obtener el tipo de usuario principal
            val tipoUsuario = obtenerTipoUsuarioPrincipal(usuario.perfiles)
            
            // Obtener URL de avatar predeterminada
            val avatarUrl = defaultAvatarsManager.obtenerAvatarPredeterminado(tipoUsuario)
            
            if (avatarUrl.isNotEmpty()) {
                // Actualizar usuario con la URL del avatar
                usuariosCollection.document(dni).update("avatarUrl", avatarUrl).await()
                Timber.d("Avatar asignado a $dni: $avatarUrl")
                return@withContext avatarUrl
            } else {
                Timber.w("No se pudo obtener avatar predeterminado para tipo: $tipoUsuario")
                return@withContext ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar avatar predeterminado a $dni: ${e.message}")
            return@withContext ""
        }
    }
    
    /**
     * Obtiene el tipo de usuario principal de una lista de perfiles.
     * @param perfiles Lista de perfiles del usuario
     * @return Tipo de usuario principal
     */
    private fun obtenerTipoUsuarioPrincipal(perfiles: List<Perfil>): TipoUsuario {
        // Determinar el tipo de usuario principal basado en prioridad
        val tiposPriorizados = listOf(
            TipoUsuario.ADMIN_APP,
            TipoUsuario.ADMIN_CENTRO,
            TipoUsuario.PROFESOR,
            TipoUsuario.FAMILIAR,
            TipoUsuario.ALUMNO
        )
        
        // Buscar el tipo más prioritario que tenga el usuario
        for (tipo in tiposPriorizados) {
            if (perfiles.any { it.tipo == tipo }) {
                return tipo
            }
        }
        
        // Si no se encuentra ninguno, usar el primero de la lista o DESCONOCIDO
        return perfiles.firstOrNull()?.tipo ?: TipoUsuario.DESCONOCIDO
    }
} 