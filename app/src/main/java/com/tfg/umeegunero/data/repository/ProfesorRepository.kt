package com.tfg.umeegunero.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario

/**
 * Repositorio para gestionar información de profesores en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * información de profesores, permitiendo una gestión integral de los recursos
 * humanos docentes dentro del sistema educativo.
 *
 * Características principales:
 * - Registro y gestión de perfiles de profesores
 * - Asignación de profesores a clases y cursos
 * - Gestión de roles y permisos
 * - Seguimiento de actividades y rendimiento
 * - Sincronización de información entre sistemas
 *
 * El repositorio permite:
 * - Crear y actualizar perfiles de profesores
 * - Gestionar asignaciones a clases
 * - Consultar información de profesores
 * - Mantener un directorio de personal docente
 * - Facilitar la comunicación y colaboración
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property centroRepository Repositorio de centros para obtener contexto
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class ProfesorRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository
) {
    private val usuariosCollection = firestore.collection("usuarios")
    
    /**
     * Obtiene un Usuario que tiene un perfil de PROFESOR por su DNI.
     *
     * @param dni DNI del usuario a buscar.
     * @return El objeto Usuario si se encuentra y tiene perfil de profesor, null en caso contrario.
     */
    suspend fun getUsuarioProfesorByDni(dni: String): Usuario? {
        return try {
            val documentSnapshot = usuariosCollection.document(dni).get().await()

            if (!documentSnapshot.exists()) {
                Timber.d("No se encontró usuario con DNI (documentId): $dni")
                return null
            }

            val usuario = documentSnapshot.toObject(Usuario::class.java)
            if (usuario == null) {
                Timber.d("No se pudo convertir el documento a Usuario para DNI: $dni")
                return null
            }

            val esProfesor = usuario.perfiles.any { it.tipo == TipoUsuario.PROFESOR }
            if (!esProfesor) {
                Timber.d("El usuario con DNI $dni no tiene un perfil de PROFESOR.")
                return null
            }
            
            // Devolver el objeto Usuario completo
            usuario
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario profesor por DNI: $dni")
            null
        }
    }

    /**
     * Obtiene un Usuario (que es profesor) por su ID de usuario (DNI).
     * Alias para getUsuarioProfesorByDni para mantener consistencia si se usa en otro lado.
     *
     * @param usuarioId DNI del usuario.
     * @return El Usuario si es profesor, null en caso contrario.
     */
    suspend fun getProfesorByUsuarioId(usuarioId: String): Usuario? {
        return getUsuarioProfesorByDni(usuarioId)
    }

    /**
     * Obtiene un Usuario (que es profesor) por su ID de documento (DNI).
     * Alias para getUsuarioProfesorByDni.
     *
     * @param profesorId DNI del profesor.
     * @return El Usuario si es profesor, null en caso contrario.
     */
    suspend fun getProfesorById(profesorId: String): Usuario? {
         return getUsuarioProfesorByDni(profesorId)
    }

    /**
     * Obtiene la lista de Usuarios que son profesores de un centro específico.
     *
     * @param centroId ID del centro.
     * @return Lista de objetos Usuario que son profesores del centro.
     */
    suspend fun getProfesoresByCentro(centroId: String): List<Usuario> {
        return try {
            // Nota: whereArrayContains tiene limitaciones con objetos. 
            // La forma más robusta es obtener todos los usuarios y filtrar en Kotlin,
            // o estructurar los perfiles de forma que se puedan consultar más directamente.
            // Por simplicidad y dado que el número de usuarios por centro podría no ser masivo,
            // se puede hacer un filtrado en cliente, pero para BBDD muy grandes, esto no es ideal.

            val querySnapshot = usuariosCollection.get().await() // Obtener todos los usuarios

            querySnapshot.documents.mapNotNull { document ->
                val usuario = document.toObject(Usuario::class.java)
                if (usuario != null) {
                    val esProfesorDelCentro = usuario.perfiles.any { 
                        it.tipo == TipoUsuario.PROFESOR && it.centroId == centroId 
                    }
                    if (esProfesorDelCentro) {
                        usuario // Devolver el objeto Usuario completo
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores por centro: $centroId")
            emptyList()
        }
    }
    
    /**
     * Elimina el rol de profesor de un usuario o lo marca como inactivo.
     * La eliminación completa de un usuario es una operación más delicada y 
     * podría gestionarse en UsuarioRepository.
     * Aquí, nos enfocaremos en la lógica de "eliminar como profesor".
     *
     * @param dniProfesor DNI del usuario profesor.
     * @param actualizarReferencias Si es true, actualiza referencias en clases y alumnos (puede ser complejo).
     * @return Resultado de la operación.
     */
    suspend fun eliminarRolProfesor(dniProfesor: String, actualizarReferencias: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userDocRef = usuariosCollection.document(dniProfesor)
            val documentSnapshot = userDocRef.get().await()

            if (!documentSnapshot.exists()) {
                return@withContext Result.Error(Exception("El usuario con DNI $dniProfesor no existe."))
            }

            val usuario = documentSnapshot.toObject(Usuario::class.java)
            if (usuario == null) {
                return@withContext Result.Error(Exception("No se pudo convertir el documento a Usuario."))
            }

            // Filtrar para quitar el perfil de profesor, o todos los perfiles de profesor si hay varios
            val perfilesActualizados = usuario.perfiles.filter { it.tipo != TipoUsuario.PROFESOR }
            
            // Si después de filtrar no quedan perfiles y consideras que el usuario debe desactivarse o eliminarse,
            // esa lógica podría ir aquí o delegarse.
            // Por ahora, solo actualizamos los perfiles.
            // Si el usuario ya no tiene perfiles de profesor, podría considerarse "ya no es profesor".

            userDocRef.update("perfiles", perfilesActualizados).await()
            Timber.d("Perfiles de profesor eliminados para el usuario con DNI: $dniProfesor")

            // La lógica de actualizarReferencias (desasignar de clases, alumnos) es compleja
            // y depende de cómo estén estructuradas esas relaciones.
            // Esto puede implicar llamadas a ClaseRepository y AlumnoRepository.
            if (actualizarReferencias) {
                Timber.d("Actualización de referencias para $dniProfesor iniciada...")
                // Ejemplo de desasignación de clases (simplificado):
                // val clasesDelProfesor = claseRepository.getClasesByProfesorId(dniProfesor) // Necesitaría un método así
                // clasesDelProfesor.forEach { claseRepository.desasignarProfesor(it.id) }
                
                // Ejemplo de desasignación de alumnos (simplificado):
                // val alumnosDelProfesor = alumnoRepository.getAlumnosByProfesorId(dniProfesor) // Necesitaría un método así
                // alumnosDelProfesor.forEach { alumnoRepository.desasignarProfesorDeAlumno(it.dni) }
                Timber.w("La actualización de referencias en eliminarRolProfesor necesita implementación detallada.")
            }

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar rol de profesor para DNI $dniProfesor: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
}

/**
 * Modelo de datos para un profesor
 *
 * @property id ID único del profesor
 * @property usuarioId ID del usuario asociado
 * @property nombre Nombre del profesor
 * @property apellidos Apellidos del profesor
 * @property claseId ID de la clase asignada
 * @property centroId ID del centro educativo
 * @property especialidad Especialidad del profesor
 * @property activo Indica si el profesor está activo
 */
data class Profesor(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val claseId: String = "",
    val centroId: String = "",
    val especialidad: String = "",
    val activo: Boolean = true
) 