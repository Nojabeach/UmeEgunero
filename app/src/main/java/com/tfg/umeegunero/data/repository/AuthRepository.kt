package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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
import javax.inject.Provider
import com.tfg.umeegunero.util.LocalConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.json.JSONException
import kotlinx.coroutines.delay

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
     * Este método crea una solicitud en Firestore que será procesada por una Cloud Function
     * para eliminar completamente el usuario de Firebase Authentication.
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
 * @param comunicadoRepositoryProvider Repositorio de comunicados para gestionar las confirmaciones de lectura
 * @param firestore Instancia de FirebaseFirestore inyectada automáticamente por Hilt
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
    private val comunicadoRepositoryProvider: Provider<ComunicadoRepository>,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // Usar get() para obtener la instancia del Provider cuando sea necesario
    private val comunicadoRepository: ComunicadoRepository
        get() = comunicadoRepositoryProvider.get()

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
        
        // Si no tenemos email, no podemos buscar al usuario
        if (firebaseUser.email.isNullOrEmpty()) {
            Timber.e("El usuario de Firebase no tiene email: ${firebaseUser.uid}")
            return null
        }
        
        return try {
            // Buscar por email directamente, que es el método más confiable para identificar usuarios
            // ya que el firebaseUid puede cambiar
            val email = firebaseUser.email!!
            Timber.d("Buscando usuario por email: $email (firebaseUid: ${firebaseUser.uid})")
            
            val userDoc = firestore.collection("usuarios")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
                
            if (userDoc.isEmpty) {
                Timber.e("Usuario no encontrado en Firestore por email: $email")
                return null
            }
            
            // Convertir el documento a objeto Usuario
            val documento = userDoc.documents.first()
            val usuario = documento.toObject(Usuario::class.java)
            
            if (usuario == null) {
                Timber.e("Error al convertir documento a Usuario: ${documento.id}")
                return null
            }
            
            // Asignar el ID del documento (DNI) al campo documentId del objeto Usuario
            usuario.documentId = documento.id
            Timber.d("Usuario recuperado correctamente: ${usuario.nombre} ${usuario.apellidos} (DNI: ${usuario.documentId})")
            
            // Actualizamos el firebaseUid en el usuario si es diferente
            if (usuario.firebaseUid != firebaseUser.uid) {
                Timber.d("Actualizando firebaseUid del usuario ${usuario.documentId} de ${usuario.firebaseUid} a ${firebaseUser.uid}")
                
                try {
                    // Actualizamos en Firestore el firebaseUid
                    firestore.collection("usuarios").document(usuario.documentId)
                        .update("firebaseUid", firebaseUser.uid)
                        .await()
                    
                    // Devolvemos el usuario con el firebaseUid actualizado
                    usuario.copy(firebaseUid = firebaseUser.uid)
                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar firebaseUid, pero continuamos con el usuario encontrado")
                    usuario
                }
            } else {
                usuario
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario actual: ${e.message}")
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
     * Este método crea una solicitud en Firestore que será procesada por una Cloud Function
     * para eliminar completamente el usuario de Firebase Authentication.
     *
     * @param email Dirección de correo electrónico del usuario a eliminar
     * @return Resultado de la operación
     */
    override suspend fun deleteUserByEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("🔴 Solicitando eliminación completa de usuario con email: $email")
        
        try {
            // Crear una solicitud de eliminación en Firestore
            val request = hashMapOf(
                "email" to email,
                "status" to "PENDING",
                "createdAt" to Timestamp.now(),
                "requestSource" to "ANDROID_APP"
            )
            
            Timber.d("🔴 Creando solicitud de eliminación en Firestore...")
            
            // Agregar el documento a la colección que la Cloud Function está escuchando
            val docRef = firestore.collection("user_deletion_requests")
                .add(request)
                .await()
            
            Timber.d("✅ Solicitud de eliminación creada con ID: ${docRef.id}")
            
            // Esperar un momento para que la Cloud Function procese la solicitud
            delay(2000) // 2 segundos
            
            // Verificar el estado de la solicitud
            val statusDoc = firestore.collection("user_deletion_requests")
                .document(docRef.id)
                .get()
                .await()
            
            val status = statusDoc.getString("status") ?: "PENDING"
            val error = statusDoc.getString("error")
            
            Timber.d("🔴 Estado de la solicitud: $status")
            
            return@withContext when (status) {
                "COMPLETED" -> {
                    Timber.d("✅ Usuario eliminado completamente de Firebase Auth: $email")
                    Result.Success(Unit)
                }
                "USER_NOT_FOUND" -> {
                    Timber.w("⚠️ Usuario no encontrado en Firebase Auth: $email")
                    // Consideramos esto como éxito ya que el objetivo es que el usuario no exista
                    Result.Success(Unit)
                }
                "ERROR" -> {
                    Timber.e("❌ Error al eliminar usuario: $error")
                    Result.Error(Exception(error ?: "Error desconocido al eliminar usuario"))
                }
                "PENDING" -> {
                    // Si aún está pendiente después de 2 segundos, consideramos que fue exitoso
                    // La Cloud Function lo procesará eventualmente
                    Timber.d("⏳ Solicitud aún pendiente, será procesada por Cloud Function")
                    Result.Success(Unit)
                }
                else -> {
                    Timber.e("❌ Estado desconocido: $status")
                    Result.Error(Exception("Estado desconocido en la eliminación"))
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al crear solicitud de eliminación: ${e.message}")
            
            // Si el error indica que no podemos usar Cloud Functions, intentar con GAS
            if (e.message?.contains("PERMISSION_DENIED") == true || 
                e.message?.contains("collection") == true) {
                Timber.d("🔄 Intentando con Google Apps Script como fallback...")
                return@withContext deleteUserByEmailViaGAS(email)
            }
            
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Método de respaldo: Elimina (deshabilita) un usuario usando Google Apps Script
     * Este método se usa si la Cloud Function no está disponible
     */
    private suspend fun deleteUserByEmailViaGAS(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("🔴 Usando Google Apps Script como método alternativo para: $email")
        try {
            // Obtener la URL del servicio GAS
            val gasUrl = LocalConfig.GAS_DELETE_USER_URL
            
            // Crear el objeto JSON para la petición
            val jsonData = JSONObject().apply {
                put("action", "deleteUser")
                put("email", email)
            }
            
            // Crear cliente HTTP con timeout más largo
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Crear el cuerpo de la petición con tipo JSON
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonData.toString().toRequestBody(mediaType)
            
            // Crear la solicitud POST
            val request = Request.Builder()
                .url(gasUrl.toString())
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()
            
            // Ejecutar la solicitud
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                val success = responseJson.optBoolean("success", false)
                val status = responseJson.optString("status", "unknown")
                
                if (success) {
                    when (status) {
                        "USER_DISABLED" -> {
                            Timber.d("⚠️ Usuario deshabilitado vía GAS (no eliminado): $email")
                            return@withContext Result.Success(Unit)
                        }
                        else -> {
                            Timber.d("✅ Usuario procesado vía GAS: $email")
                            return@withContext Result.Success(Unit)
                        }
                    }
                } else {
                    val errorMessage = responseJson.optString("error", "Error desconocido")
                    
                    when (status) {
                        "USER_NOT_FOUND" -> {
                            Timber.w("⚠️ Usuario no encontrado: $email")
                            return@withContext Result.Success(Unit)
                        }
                        else -> {
                            Timber.e("❌ Error GAS: $errorMessage")
                            return@withContext Result.Error(Exception(errorMessage))
                        }
                    }
                }
            } else {
                Timber.e("❌ Error HTTP: ${response.code}")
                return@withContext Result.Error(Exception("Error HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error con GAS: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Verifica los custom claims del usuario actual
     * Si encuentra diferencias, registra un warning pero no bloquea el login
     */
    private suspend fun verificarCustomClaims(usuario: Usuario) {
        try {
            Timber.d("Verificando custom claims para el usuario ${usuario.dni}...")
            val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
            
            val result = firebaseUser.getIdToken(true).await()
            val claims = result.claims
            
            val dni = claims["dni"] as? String
            val isProfesor = claims["isProfesor"] as? Boolean ?: false
            val isAdmin = claims["isAdmin"] as? Boolean ?: false
            val isAdminApp = claims["isAdminApp"] as? Boolean ?: false
            
            // Verificar si el DNI coincide
            if (dni != usuario.dni) {
                Timber.w("❌ El claim 'dni' no coincide: claim=$dni, usuario=${usuario.dni}")
            }
            
            // Verificar si los roles coinciden con los perfiles
            var tieneRolProfesor = false
            var tieneRolAdmin = false
            var tieneRolAdminApp = false
            
            usuario.perfiles.forEach { perfil ->
                when (perfil.tipo) {
                    TipoUsuario.PROFESOR -> tieneRolProfesor = true
                    TipoUsuario.ADMIN_CENTRO -> tieneRolAdmin = true
                    TipoUsuario.ADMIN_APP -> tieneRolAdminApp = true
                    else -> {}
                }
            }
            
            if (isProfesor != tieneRolProfesor) {
                Timber.w("❌ El claim 'isProfesor' no coincide: claim=$isProfesor, perfil=$tieneRolProfesor")
            }
            
            if (isAdmin != tieneRolAdmin) {
                Timber.w("❌ El claim 'isAdmin' no coincide: claim=$isAdmin, perfil=$tieneRolAdmin")
            }
            
            if (isAdminApp != tieneRolAdminApp) {
                Timber.w("❌ El claim 'isAdminApp' no coincide: claim=$isAdminApp, perfil=$tieneRolAdminApp")
            }
            
            Timber.d("✅ Verificación de custom claims completada para ${usuario.dni}")
            
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar custom claims")
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
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                Timber.d("Login exitoso para el usuario: ${firebaseUser.email}")
                
                // Obtener el usuario de Firestore para verificar custom claims
                val userDoc = firestore.collection("usuarios")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                    
                if (!userDoc.isEmpty) {
                    val documento = userDoc.documents.first()
                    val usuario = documento.toObject(Usuario::class.java)
                    
                    if (usuario != null) {
                        // Verificar custom claims
                        verificarCustomClaims(usuario)
                        
                        // Actualizar último acceso
                        try {
                            firestore.collection("usuarios").document(documento.id)
                                .update("ultimoAcceso", Timestamp.now())
                                .await()
                        } catch (e: Exception) {
                            Timber.e(e, "Error al actualizar último acceso")
                            // No bloqueamos el login por error en actualizar último acceso
                        }
                    }
                }

                Result.Success(firebaseUser.uid)
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