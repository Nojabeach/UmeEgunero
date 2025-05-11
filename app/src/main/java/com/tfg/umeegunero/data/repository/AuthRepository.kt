package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import com.google.firebase.Timestamp

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
     * Cierra la sesión del usuario actual.
     * 
     * Alias para signOut() por compatibilidad con código en español.
     * 
     * @return true si la sesión se cerró correctamente, false si ocurrió algún error
     */
    suspend fun cerrarSesion(): Boolean
    
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
    
    /**
     * Marca un comunicado como leído por el usuario actual.
     * 
     * Este método registra que el usuario ha visto el comunicado, 
     * actualizando la lista de usuarios que lo han leído.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que indica éxito o error en la operación
     */
    suspend fun marcarComunicadoComoLeido(comunicadoId: String): Result<Unit>
    
    /**
     * Confirma la lectura de un comunicado por el usuario actual.
     * 
     * Este método registra que el usuario ha confirmado explícitamente 
     * que ha leído el comunicado y está al tanto de su contenido.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que indica éxito o error en la operación
     */
    suspend fun confirmarLecturaComunicado(comunicadoId: String): Result<Unit>
    
    /**
     * Verifica si el usuario actual ha leído un comunicado específico.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que contiene true si el usuario ha leído el comunicado, false en caso contrario
     */
    suspend fun haLeidoComunicado(comunicadoId: String): Result<Boolean>
    
    /**
     * Verifica si el usuario actual ha confirmado la lectura de un comunicado específico.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que contiene true si el usuario ha confirmado la lectura, false en caso contrario
     */
    suspend fun haConfirmadoLecturaComunicado(comunicadoId: String): Result<Boolean>

    /**
     * Obtiene el usuario actualmente autenticado en el sistema
     * 
     * @return Usuario autenticado o null si no hay ninguno
     */
    suspend fun getUsuarioActual(): Usuario?

    /**
     * Obtiene el ID del centro educativo al que pertenece el usuario actual
     */
    suspend fun getCurrentCentroId(): String

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * 
     * @return ID del usuario actual o null si no hay usuario autenticado
     */
    suspend fun getCurrentUserId(): String?

    /**
     * Elimina un usuario de Firebase Authentication por su email.
     * 
     * Este método utiliza Firebase Functions para eliminar de forma segura un usuario
     * mediante su correo electrónico. La eliminación se realiza en el servidor
     * para mantener la seguridad y evitar exposición de tokens o claves.
     *
     * @param email Dirección de correo electrónico del usuario a eliminar
     * @return Resultado de la operación
     */
    suspend fun deleteUserByEmail(email: String): Result<Unit>

    /**
     * Inicia sesión con email y contraseña.
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Resultado encapsulado que contiene el ID del usuario si el login es exitoso
     */
    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<String>
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
 * @param comunicadoRepository Repositorio de comunicados para gestionar las confirmaciones de lectura
 * @param firestore Instancia de FirebaseFirestore inyectada automáticamente por Hilt
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
    private val comunicadoRepository: ComunicadoRepository,
    private val firestore: FirebaseFirestore
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
     * Cierra la sesión del usuario actual.
     * 
     * Alias para signOut() por compatibilidad con código en español.
     * 
     * @return True si se ha cerrado la sesión correctamente, False si ha ocurrido un error
     */
    override suspend fun cerrarSesion(): Boolean = signOut()

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
    
    /**
     * Marca un comunicado como leído por el usuario actual.
     * 
     * Este método obtiene el ID del usuario actual y delega la operación
     * al repositorio de comunicados para registrar que el usuario ha visto el comunicado.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que indica éxito o error en la operación
     */
    override suspend fun marcarComunicadoComoLeido(comunicadoId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = getCurrentUser() ?: return@withContext Result.Error(
                    Exception("No hay usuario autenticado")
                )
                
                comunicadoRepository.marcarComoLeido(comunicadoId, usuario.documentId)
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar comunicado como leído")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Confirma la lectura de un comunicado por el usuario actual.
     * 
     * Este método obtiene el ID del usuario actual y delega la operación
     * al repositorio de comunicados para registrar que el usuario ha confirmado
     * explícitamente la lectura del comunicado.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que indica éxito o error en la operación
     */
    override suspend fun confirmarLecturaComunicado(comunicadoId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = getCurrentUser() ?: return@withContext Result.Error(
                    Exception("No hay usuario autenticado")
                )
                
                comunicadoRepository.confirmarLectura(comunicadoId, usuario.documentId)
            } catch (e: Exception) {
                Timber.e(e, "Error al confirmar lectura de comunicado")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Verifica si el usuario actual ha leído un comunicado específico.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que contiene true si el usuario ha leído 
     *         el comunicado, false en caso contrario
     */
    override suspend fun haLeidoComunicado(comunicadoId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = getCurrentUser() ?: return@withContext Result.Error(
                    Exception("No hay usuario autenticado")
                )
                
                comunicadoRepository.haLeidoComunicado(comunicadoId, usuario.documentId)
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar si el comunicado ha sido leído")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Verifica si el usuario actual ha confirmado la lectura de un comunicado específico.
     * 
     * @param comunicadoId Identificador del comunicado
     * @return Resultado encapsulado que contiene true si el usuario ha confirmado 
     *         la lectura, false en caso contrario
     */
    override suspend fun haConfirmadoLecturaComunicado(comunicadoId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = getCurrentUser() ?: return@withContext Result.Error(
                    Exception("No hay usuario autenticado")
                )
                
                comunicadoRepository.haConfirmadoLectura(comunicadoId, usuario.documentId)
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar si el comunicado ha sido confirmado")
                Result.Error(e)
            }
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado en el sistema
     * 
     * @return Usuario autenticado o null si no hay ninguno
     */
    override suspend fun getUsuarioActual(): Usuario? {
        val firebaseUser = getFirebaseUser() ?: return null
        
        return try {
            val userDoc = firestore.collection("usuarios")
                .whereEqualTo("uid", firebaseUser.uid)
                .limit(1)
                .get()
                .await()
                
            if (userDoc.isEmpty) {
                Timber.e("Usuario no encontrado en Firestore: ${firebaseUser.uid}")
                return null
            }
            
            // Convertir el documento a objeto Usuario
            val usuario = userDoc.documents.first().toObject(Usuario::class.java)
            
            if (usuario == null) {
                Timber.e("Error al convertir documento a Usuario: ${userDoc.documents.first().id}")
            }
            
            usuario
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario actual")
            null
        }
    }

    /**
     * Obtiene el ID del centro educativo al que pertenece el usuario actual
     */
    override suspend fun getCurrentCentroId(): String {
        val currentUser = getCurrentUser() ?: return ""
        return currentUser.perfiles.firstOrNull()?.centroId ?: ""
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * 
     * @return ID del usuario actual o null si no hay usuario autenticado
     */
    override suspend fun getCurrentUserId(): String? {
        return getCurrentUser()?.documentId
    }

    /**
     * Elimina un usuario de Firebase Authentication por su email.
     * 
     * Este método utiliza Firebase Functions para eliminar de forma segura un usuario
     * mediante su correo electrónico. La eliminación se realiza en el servidor
     * para mantener la seguridad y evitar exposición de tokens o claves.
     *
     * @param email Dirección de correo electrónico del usuario a eliminar
     * @return Resultado de la operación
     */
    override suspend fun deleteUserByEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Solicitando eliminación de usuario con email: $email")
        try {
            // Actualizado para usar Firebase Functions en lugar de Apps Script
            val data = hashMapOf(
                "action" to "deleteUser",
                "email" to email
            )
            
            val functions = Firebase.functions
            val result = functions
                .getHttpsCallable("manageUsers")
                .call(data)
                .await()
            
            val resultData = result.data as? Map<*, *>
            val success = resultData?.get("success") as? Boolean ?: false
            
            if (success) {
                Timber.d("Usuario eliminado correctamente: $email")
                return@withContext Result.Success(Unit)
            } else {
                val errorMessage = resultData?.get("error") as? String ?: "Error desconocido al eliminar usuario"
                Timber.e("Error al eliminar usuario: $errorMessage")
                return@withContext Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al ejecutar la función para eliminar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Inicia sesión con email y contraseña utilizando Firebase Auth.
     * 
     * Este método maneja el proceso de autenticación con Firebase, incluyendo:
     * - Validación de credenciales
     * - Manejo de errores específicos de Firebase Auth
     * - Logging para depuración
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Resultado encapsulado que contiene el ID del usuario si el login es exitoso
     */
    override suspend fun loginWithEmailAndPassword(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                Timber.d("Login exitoso para el usuario: ${user.email}")
                
                Result.Success(user.uid)
            } else {
                Timber.e("Login fallido: usuario nulo después de autenticación")
                Result.Error(Exception("Error de autenticación"))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e, "Usuario no encontrado o deshabilitado")
            Result.Error(Exception("Usuario no encontrado"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Credenciales inválidas")
            Result.Error(Exception("Credenciales inválidas"))
        } catch (e: Exception) {
            Timber.e(e, "Error durante el login")
            Result.Error(e)
        }
    }
} 