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
 * Esta interfaz forma parte del patrón Repository y sirve para abstraer las operaciones
 * de autenticación, permitiendo:
 * - Hacer testing unitario mediante mocks
 * - Seguir el principio de inversión de dependencias (SOLID)
 * - Desacoplar la lógica de autenticación de la implementación concreta (Firebase)
 * 
 * En posteriores versiones podrían implementarse otros proveedores de autenticación
 * sin necesidad de modificar el resto de la aplicación.
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 * @see AuthRepositoryImpl
 */
interface AuthRepository {
    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * Este método se encarga de recuperar toda la información del usuario
     * que ha iniciado sesión, no solo su ID o email, sino todos sus datos
     * almacenados en la base de datos.
     * 
     * @return El objeto Usuario con datos completos si hay sesión iniciada, null en caso contrario
     */
    suspend fun getCurrentUser(): Usuario?
    
    /**
     * Cierra la sesión del usuario actual.
     * 
     * Este método es responsable de finalizar la sesión actual del usuario,
     * eliminando sus tokens de autenticación y cualquier información de sesión.
     * 
     * @return true si la sesión se cerró correctamente, false si ocurrió algún error
     */
    suspend fun signOut(): Boolean
    
    /**
     * Envía un correo de recuperación de contraseña al email proporcionado.
     * 
     * Este método facilita el proceso de restablecimiento de contraseña cuando
     * un usuario la ha olvidado, enviando un email con instrucciones.
     * 
     * @param email Correo electrónico del usuario que desea recuperar su contraseña
     * @return Resultado encapsulado que indica éxito o error en el envío
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
    
    /**
     * Obtiene el usuario de Firebase actual.
     * 
     * A diferencia de getCurrentUser(), este método devuelve directamente el objeto
     * FirebaseUser, lo que puede ser útil en ciertos casos donde se necesita acceder
     * a métodos específicos de Firebase Auth.
     * 
     * @return El objeto FirebaseUser si hay un usuario autenticado, null en caso contrario
     */
    suspend fun getFirebaseUser(): FirebaseUser?
}

/**
 * Implementación de AuthRepository utilizando Firebase Auth.
 * 
 * Esta clase aplica el patrón Repository y se encarga de todas las operaciones
 * relacionadas con la autenticación de usuarios en la aplicación utilizando Firebase Auth.
 * 
 * Características principales:
 * - Utiliza corrutinas para operaciones asíncronas
 * - Maneja errores específicos de Firebase Auth
 * - Implementa logging para facilitar la depuración
 * - Se integra con el repositorio de usuarios para obtener información adicional
 * 
 * Esta clase se inyecta como singleton mediante Hilt para garantizar que
 * solo existe una instancia durante toda la ejecución de la aplicación.
 * 
 * @param firebaseAuth Instancia de FirebaseAuth inyectada automáticamente por Hilt
 * @param usuarioRepository Repositorio de usuarios inyectado para acceso a datos completos
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository
) : AuthRepository {

    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * Proceso:
     * 1. Verifica si hay un usuario autenticado en Firebase Auth
     * 2. Si existe, obtiene su email 
     * 3. Consulta en Firestore (mediante usuarioRepository) los datos completos
     * 4. Devuelve el objeto Usuario completo o null si hay algún problema
     * 
     * Este método es fundamental para la gestión de sesiones y acceso a datos
     * del usuario en toda la aplicación.
     * 
     * @return Objeto Usuario completo o null si no hay usuario autenticado o hay errores
     */
    override suspend fun getCurrentUser(): Usuario? {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val email = firebaseUser.email
                if (email != null) {
                    Timber.d("Firebase email: $email")
                    // Consultamos al repositorio para obtener los datos completos del usuario
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
            // Capturamos cualquier excepción no contemplada para evitar crashes
            Timber.e(e, "Error al obtener el usuario actual")
            null
        }
    }

    /**
     * Cierra la sesión del usuario actual
     * 
     * Utiliza Dispatchers.IO para ejecutar la operación en un hilo de fondo,
     * evitando bloquear el hilo principal. Este método es importante para 
     * la funcionalidad de logout y para mantener la seguridad de la aplicación.
     * 
     * @return True si se ha cerrado la sesión correctamente, False si ha ocurrido un error
     */
    override suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Firebase Auth se encarga de eliminar tokens y datos de sesión
            firebaseAuth.signOut()
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error al cerrar sesión")
            return@withContext false
        }
    }

    /**
     * Envía un correo de recuperación de contraseña al email proporcionado.
     * 
     * Este método implementa una buena práctica de seguridad: aunque el email
     * no exista en la base de datos, devuelve éxito para no revelar qué direcciones
     * están registradas (protección contra ataques de enumeración).
     * 
     * Proceso:
     * 1. Intenta enviar el email de recuperación mediante Firebase
     * 2. Maneja diferentes tipos de errores específicamente
     * 3. Devuelve un resultado encapsulado en la clase Result
     * 
     * @param email Correo electrónico del usuario
     * @return Resultado que indica éxito o contiene información del error
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Utilizamos await() para esperar la resolución de la tarea asíncrona
                firebaseAuth.sendPasswordResetEmail(email).await()
                Result.Success(true)
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidUserException -> {
                        // Evitamos dar pistas sobre qué emails están registrados
                        // Esta es una buena práctica de seguridad
                        Timber.d("Se intentó recuperar contraseña para un email no registrado: $email")
                        Result.Success(true)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        Timber.e(e, "Email con formato inválido: $email")
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
     * Este método es una forma directa de acceder al objeto FirebaseUser,
     * lo que puede ser necesario para ciertas operaciones específicas de Firebase.
     * 
     * Es útil cuando necesitamos acceder a propiedades o métodos propios
     * de FirebaseUser que no están mapeados en nuestro modelo Usuario.
     * 
     * @return El objeto FirebaseUser actual o null si no hay usuario autenticado
     */
    override suspend fun getFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
} 