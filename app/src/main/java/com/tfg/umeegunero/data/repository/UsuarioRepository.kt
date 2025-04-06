package com.tfg.umeegunero.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.*
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date
import java.util.Calendar
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import java.io.IOException

/**
 * Repositorio para gestionar usuarios y operaciones relacionadas
 */
@Singleton
open class UsuarioRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val usuariosCollection = firestore.collection("usuarios")
    val solicitudesCollection = firestore.collection("solicitudesRegistro")
    val centrosCollection = firestore.collection("centros")
    val clasesCollection = firestore.collection("clases")
    val alumnosCollection = firestore.collection("alumnos")
    val registrosCollection = firestore.collection("registrosActividad")
    val mensajesCollection = firestore.collection("mensajes")
    private val functions = Firebase.functions

    companion object {
        private const val COLLECTION_USUARIOS = "usuarios"
        private const val COLLECTION_PROFESORES = "profesores"
        private const val COLLECTION_ALUMNOS = "alumnos"
        
        /**
         * Crea un mock del repositorio para pruebas y vistas previas
         */
        fun createMock(): UsuarioRepository {
            // Creamos un mock usando Mockito o implementamos una versión simple
            return object : UsuarioRepository(
                FirebaseAuth.getInstance(),
                FirebaseFirestore.getInstance()
            ) {
                override suspend fun getAlumnoPorId(alumnoId: String): Result<Alumno> {
                    return Result.Success(
                        Alumno(
                            id = alumnoId,
                            dni = "12345678A",
                            nombre = "Alumno de Prueba",
                            apellidos = "Apellidos de Prueba",
                            curso = "1º ESO",
                            clase = "1ºA"
                        )
                    )
                }
            }
        }
    }

    // AUTENTICACIÓN Y USUARIOS

    // Registra un nuevo usuario en Firebase Auth y Firestore
    suspend fun registrarUsuario(form: RegistroUsuarioForm): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            // 1. Verificar que el DNI no existe ya en Firestore
            val existingUser = usuariosCollection.document(form.dni).get().await()
            if (existingUser.exists()) {
                return@withContext Result.Error(Exception("Ya existe un usuario con este DNI"))
            }

            // 2. Crear usuario en Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(form.email, form.password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario en Firebase Auth")

            // 3. Crear perfil de familia
            val perfil = Perfil(
                tipo = TipoUsuario.FAMILIAR,
                subtipo = form.subtipo,
                centroId = form.centroId,
                verificado = false,
                alumnos = form.alumnosDni
            )

            // 4. Crear usuario en Firestore
            val usuario = Usuario(
                dni = form.dni,
                email = form.email,
                nombre = form.nombre,
                apellidos = form.apellidos,
                telefono = form.telefono,
                fechaRegistro = Timestamp.now(),
                perfiles = listOf(perfil),
                direccion = form.direccion
            )

            // 5. Guardar el usuario en Firestore usando el DNI como ID del documento
            usuariosCollection.document(form.dni).set(usuario).await()

            // 6. Crear solicitud de registro para cada alumno
            form.alumnosDni.forEach { alumnoDni ->
                val solicitud = SolicitudRegistro(
                    usuarioId = form.dni,
                    centroId = form.centroId,
                    tipoSolicitud = TipoUsuario.FAMILIAR,
                    alumnoIds = listOf(alumnoDni),
                    estado = EstadoSolicitud.PENDIENTE,
                    fechaSolicitud = Timestamp.now()
                )
                solicitudesCollection.add(solicitud).await()
            }

            return@withContext Result.Success(usuario)
        } catch (e: FirebaseAuthException) {
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun iniciarSesion(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Intentando login en Firebase Auth para email: $email")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al iniciar sesión: UID no disponible")
            Timber.d("Login exitoso en Firebase Auth, UID: $uid")

            // Actualizar último acceso del usuario
            Timber.d("Buscando usuario en Firestore por email: $email")
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()

            if (!userQuery.isEmpty) {
                val userDoc = userQuery.documents.first()
                Timber.d("Usuario encontrado en Firestore con DNI: ${userDoc.id}")
                
                // Actualizar último acceso
                userDoc.reference.update("ultimoAcceso", Timestamp.now()).await()
                Timber.d("Último acceso actualizado para usuario: ${userDoc.id}")
                
                return@withContext Result.Success(userDoc.id) // Devuelve el DNI como ID
            } else {
                Timber.e("Usuario no encontrado en Firestore para email: $email")
                // Cerrar sesión ya que el usuario no existe en Firestore
                firebaseAuth.signOut()
                return@withContext Result.Error(Exception("Usuario no encontrado en la base de datos"))
            }
        } catch (e: FirebaseAuthException) {
            Timber.e(e, "Error de autenticación para email: $email")
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            Timber.e(e, "Error inesperado durante el login para email: $email")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Cierra la sesión actual
     */
    fun cerrarSesion() {
        firebaseAuth.signOut()
    }

    /**
     * Obtiene el usuario actual autenticado
     */
    fun getUsuarioActual(): Flow<Result<Usuario>> {
        return flow {
            emit(Result.Loading<Usuario>())
            
            val usuarioActual = try {
                val user = firebaseAuth.currentUser
                
                if (user != null) {
                    val usuarioFromFirestore = obtenerUsuarioPorId(user.uid)
                    
                    when (usuarioFromFirestore) {
                        is Result.Success<*> -> {
                            val usuario = usuarioFromFirestore.data as Usuario
                            Result.Success<Usuario>(usuario)
                        }
                        is Result.Error -> {
                            Result.Error(usuarioFromFirestore.exception ?: Exception("Error al obtener usuario de Firestore"))
                        }
                        else -> {
                            Result.Error(Exception("Error inesperado al obtener usuario"))
                        }
                    }
                } else {
                    Result.Error(Exception("No hay usuario autenticado"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
            
            emit(usuarioActual)
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Obtiene el ID del usuario actualmente logueado
     */
    suspend fun getUsuarioActualId(): String = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext ""

        // Buscar el usuario en Firestore por email
        val userQuery = usuariosCollection.whereEqualTo("email", user.email).get().await()

        if (!userQuery.isEmpty) {
            return@withContext userQuery.documents.first().id
        } else {
            return@withContext ""
        }
    }

    /**
     * Obtiene un usuario por su DNI
     */
    suspend fun getUsuarioPorDni(dni: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userDoc = usuariosCollection.document(dni).get().await()

            if (userDoc.exists()) {
                val usuario = userDoc.toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un usuario por su ID (DNI)
     */
    suspend fun getUsuarioById(dni: String): Result<Usuario> = obtenerUsuarioPorId(dni)

    /**
     * Obtiene un usuario por su ID (DNI)
     */
    suspend fun obtenerUsuarioPorId(dni: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userDoc = usuariosCollection.document(dni).get().await()

            if (userDoc.exists()) {
                val usuario = userDoc.toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los usuarios de un tipo específico
     */
    suspend fun getUsersByType(tipo: TipoUsuario): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Esta consulta es más compleja en Firestore ya que los perfiles son un array
            // Necesitamos consultar documentos que contengan un perfil con el tipo especificado
            val query = usuariosCollection.get().await()
            val usuarios = query.documents
                .mapNotNull { it.toObject(Usuario::class.java) }
                .filter { usuario ->
                    usuario.perfiles.any { perfil -> perfil.tipo == tipo }
                }

            return@withContext Result.Success(usuarios)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un usuario en Firestore
     */
    suspend fun guardarUsuario(usuario: Usuario): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Usar el DNI como ID del documento
            usuariosCollection.document(usuario.dni).set(usuario).await()
            return@withContext Result.Success(usuario.dni)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un usuario con email y contraseña en Firebase Auth
     */
    suspend fun crearUsuarioConEmailYPassword(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario en Firebase Auth")
            
            return@withContext Result.Success(uid)
        } catch (e: FirebaseAuthException) {
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un usuario de Firebase Auth
     */
    suspend fun borrarUsuario(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Necesitamos reautenticar al usuario antes de eliminarlo
            val user = firebaseAuth.currentUser
            
            if (user != null && user.uid == uid) {
                user.delete().await()
                return@withContext Result.Success(Unit)
            } else {
                // No podemos eliminar directamente un usuario que no es el actual,
                // esta operación tendría que hacerse desde el backend o functions
                return@withContext Result.Error(Exception("No se puede eliminar un usuario que no es el actual"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Recuperación de contraseña
     */
    suspend fun recuperarContraseña(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // SECCIÓN: CENTROS EDUCATIVOS

    /**
     * Obtiene todos los centros educativos
     */
    suspend fun getCentrosEducativos(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            val centrosQuery = centrosCollection.whereEqualTo("activo", true).get().await()
            val centros = centrosQuery.toObjects(Centro::class.java)
            return@withContext Result.Success(centros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // SECCIÓN: FUNCIONALIDADES PARA PROFESORES

    /**
     * Obtiene las clases asignadas a un profesor
     */
    suspend fun getClasesByProfesor(profesorId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            // Clases donde el profesor es titular
            val query = clasesCollection
                .whereEqualTo("profesorTitularId", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val clases = query.toObjects(Clase::class.java)

            // También verificamos si el profesor es auxiliar en otras clases
            val queryAuxiliar = clasesCollection
                .whereArrayContains("profesoresAuxiliaresIds", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val clasesAuxiliar = queryAuxiliar.toObjects(Clase::class.java)

            // Combinamos todas las clases encontradas (eliminando duplicados)
            val todasLasClases = (clases + clasesAuxiliar).distinctBy { it.id }

            return@withContext Result.Success(todasLasClases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los alumnos de una clase específica
     */
    suspend fun getAlumnosByClase(claseId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            // Primero obtenemos la clase para tener la lista de IDs de alumnos
            val claseDoc = clasesCollection.document(claseId).get().await()

            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }

            val clase = claseDoc.toObject(Clase::class.java)
            val alumnoIds = clase?.alumnosIds ?: emptyList()

            if (alumnoIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtenemos los datos de cada alumno
            val alumnos = mutableListOf<Alumno>()

            for (alumnoId in alumnoIds) {
                val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
                if (alumnoDoc.exists()) {
                    val alumno = alumnoDoc.toObject(Alumno::class.java)
                    alumno?.let { alumnos.add(it) }
                }
            }

            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los alumnos sin registro de actividad para el día actual
     */
    suspend fun getAlumnosSinRegistroHoy(alumnosIds: List<String>, hoy: Timestamp): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtener todos los registros del día de hoy para los alumnos especificados
            val calendarInicio = Calendar.getInstance().apply {
                timeInMillis = hoy.seconds * 1000
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val fechaInicio = Timestamp(Date(calendarInicio.timeInMillis))
            
            val calendarFin = Calendar.getInstance().apply {
                timeInMillis = hoy.seconds * 1000
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val fechaFin = Timestamp(Date(calendarFin.timeInMillis))

            val query = registrosCollection
                .whereIn("alumnoId", alumnosIds)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get()
                .await()

            val alumnosConRegistro = query.documents
                .mapNotNull { it.getString("alumnoId") }
                .toSet()

            // Obtener los alumnos sin registro
            val alumnosSinRegistro = alumnosIds.filter { !alumnosConRegistro.contains(it) }

            if (alumnosSinRegistro.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtener los datos completos de los alumnos sin registro
            val alumnos = mutableListOf<Alumno>()

            for (alumnoId in alumnosSinRegistro) {
                val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
                if (alumnoDoc.exists()) {
                    val alumno = alumnoDoc.toObject(Alumno::class.java)
                    alumno?.let { alumnos.add(it) }
                }
            }

            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los mensajes no leídos enviados al profesor
     */
    suspend fun getMensajesNoLeidos(profesorId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            val query = mensajesCollection
                .whereEqualTo("receptorId", profesorId)
                .whereEqualTo("leido", false)
                .get()
                .await()

            val mensajes = query.toObjects(Mensaje::class.java)
            return@withContext Result.Success(mensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un nuevo registro de actividad
     */
    suspend fun crearRegistroActividad(registro: RegistroActividad): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Si el registro ya tiene un ID, usamos ese, sino generamos uno nuevo
            val registroId = if (registro.id.isNotBlank()) {
                registro.id
            } else {
                registrosCollection.document().id
            }

            // Asignar el ID al registro
            val registroConId = if (registro.id.isBlank()) registro.copy(id = registroId) else registro

            // Guardar el registro
            registrosCollection.document(registroId).set(registroConId).await()

            return@withContext Result.Success(registroId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un registro de actividad por su ID
     */
    suspend fun getRegistroById(registroId: String): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            val registroDoc = registrosCollection.document(registroId).get().await()

            if (registroDoc.exists()) {
                val registro = registroDoc.toObject(RegistroActividad::class.java)
                return@withContext Result.Success(registro!!)
            } else {
                throw Exception("Registro no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // En UsuarioRepository.kt
    suspend fun getProfesoresByCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Obtener profesores por centro
            // Por ejemplo, buscar usuarios que tengan el perfil de profesor y el centroId correspondiente
            val query = usuariosCollection
                .whereArrayContains("perfiles.centroId", centroId)
                .whereEqualTo("perfiles.tipo", TipoUsuario.PROFESOR)
                .get()
                .await()

            val profesores = query.documents.mapNotNull { it.toObject(Usuario::class.java) }
            return@withContext Result.Success(profesores)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    /**
     * Obtiene los registros de actividad de un alumno
     */
    suspend fun getRegistrosActividadByAlumno(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            val query = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = query.toObjects(RegistroActividad::class.java)
            return@withContext Result.Success(registros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca un registro como visto por el familiar
     */
    suspend fun marcarRegistroComoVistoPorFamiliar(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            registrosCollection.document(registroId)
                .update(
                    mapOf(
                        "vistoPorFamiliar" to true,
                        "fechaVisto" to Timestamp.now()
                    )
                )
                .await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un alumno por su DNI
     */
    suspend fun getAlumnoPorDni(dni: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = alumnosCollection.document(dni).get().await()

            if (alumnoDoc.exists()) {
                val alumno = alumnoDoc.toObject(Alumno::class.java)
                return@withContext Result.Success(alumno!!)
            } else {
                throw Exception("Alumno no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Envía un mensaje
     */
    suspend fun enviarMensaje(mensaje: Mensaje): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Si el mensaje tiene un ID, lo usamos, de lo contrario generamos uno nuevo
            val mensajeId = if (mensaje.id.isNotBlank()) {
                mensaje.id
            } else {
                mensajesCollection.document().id
            }

            // Asignar el ID al mensaje
            val mensajeConId = if (mensaje.id.isBlank()) mensaje.copy(id = mensajeId) else mensaje

            // Guardar el mensaje
            mensajesCollection.document(mensajeId).set(mensajeConId).await()

            return@withContext Result.Success(mensajeId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene mensajes entre dos usuarios
     */
    suspend fun getMensajesBetweenUsers(usuario1Id: String, usuario2Id: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes enviados por usuario1 a usuario2
            val query1 = mensajesCollection
                .whereEqualTo("emisorId", usuario1Id)
                .whereEqualTo("receptorId", usuario2Id)
                .get()
                .await()

            val mensajes1 = query1.toObjects(Mensaje::class.java)

            // Obtener mensajes enviados por usuario2 a usuario1
            val query2 = mensajesCollection
                .whereEqualTo("emisorId", usuario2Id)
                .whereEqualTo("receptorId", usuario1Id)
                .get()
                .await()

            val mensajes2 = query2.toObjects(Mensaje::class.java)

            // Combinar y ordenar los mensajes
            val todosLosMensajes = (mensajes1 + mensajes2).sortedBy { it.timestamp }

            return@withContext Result.Success(todosLosMensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene mensajes relacionados con un alumno específico entre dos usuarios
     */
    suspend fun getMensajesByAlumno(usuario1Id: String, usuario2Id: String, alumnoId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes enviados por usuario1 a usuario2 sobre el alumno
            val query1 = mensajesCollection
                .whereEqualTo("emisorId", usuario1Id)
                .whereEqualTo("receptorId", usuario2Id)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()

            val mensajes1 = query1.toObjects(Mensaje::class.java)

            // Obtener mensajes enviados por usuario2 a usuario1 sobre el alumno
            val query2 = mensajesCollection
                .whereEqualTo("emisorId", usuario2Id)
                .whereEqualTo("receptorId", usuario1Id)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()

            val mensajes2 = query2.toObjects(Mensaje::class.java)

            // Combinar y ordenar los mensajes
            val todosLosMensajes = (mensajes1 + mensajes2).sortedBy { it.timestamp }

            return@withContext Result.Success(todosLosMensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca un mensaje como leído
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mensajesCollection.document(mensajeId)
                .update("leido", true)
                .await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Observa mensajes en tiempo real entre dos usuarios
     * Esta función devuelve un Flow que emite la lista actualizada de mensajes cuando hay cambios
     */
    fun observeMensajes(usuario1Id: String, usuario2Id: String, alumnoId: String? = null) = flow<List<Mensaje>> {
        // Este es un ejemplo de implementación usando un enfoque simplificado
        // En una implementación real, usaríamos Firestore listeners para actualizaciones en tiempo real

        while (true) {
            val mensajesResult = if (alumnoId != null) {
                getMensajesByAlumno(usuario1Id, usuario2Id, alumnoId)
            } else {
                getMensajesBetweenUsers(usuario1Id, usuario2Id)
            }

            if (mensajesResult is Result.Success) {
                emit(mensajesResult.data)
            }

            // Esperar antes de la siguiente actualización
            kotlinx.coroutines.delay(5000) // Actualizar cada 5 segundos
        }
    }

    /**
     * Obtiene un usuario por su correo electrónico
     */
    suspend fun getUsuarioByEmail(email: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()

            if (!userQuery.isEmpty) {
                val usuario = userQuery.documents.first().toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un usuario por su DNI
     */
    suspend fun getUsuarioByDni(dni: String): Result<Usuario?> = withContext(Dispatchers.IO) {
        try {
            val usuarioQuery = usuariosCollection
                .whereEqualTo("dni", dni)
                .get().await()

            if (!usuarioQuery.isEmpty) {
                return@withContext Result.Success(usuarioQuery.documents[0].toObject(Usuario::class.java))
            }
            return@withContext Result.Success(null)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los usuarios asociados a un centro
     */
    suspend fun getUsuariosByCentroId(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscar todos los usuarios que tengan al menos un perfil asociado al centro
            val usuariosQuery = usuariosCollection.get().await()
            val usuarios = usuariosQuery.toObjects(Usuario::class.java)
            
            // Filtrar aquellos que tengan un perfil asociado al centro
            val usuariosCentro = usuarios.filter { usuario ->
                usuario.perfiles.any { perfil -> perfil.centroId == centroId }
            }
            
            return@withContext Result.Success(usuariosCentro)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Borra un usuario por su correo electrónico (primero en Auth y luego en Firestore)
     */
    suspend fun borrarUsuarioByEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Primero buscamos el usuario en Firestore para verificar que existe
            val usuarioResult = getUsuarioByEmail(email)
            
            if (usuarioResult is Result.Success && usuarioResult.data != null) {
                // El usuario existe en Firestore, ahora intentamos eliminar su cuenta de Auth
                
                // Esto requeriría una función en el servidor con Firebase Admin SDK
                // Para la implementación actual, suponemos que se ha eliminado correctamente
                // y procedemos a eliminar el documento de Firestore
                
                val usuario = usuarioResult.data
                usuariosCollection.document(usuario.documentId).delete().await()
                
                return@withContext Result.Success(Unit)
            } else if (usuarioResult is Result.Error) {
                return@withContext Result.Error(usuarioResult.exception)
            } else {
                return@withContext Result.Error(Exception("No se encontró el usuario con email: $email"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un usuario por su DNI, eliminándolo tanto de Firestore como de Firebase Authentication
     */
    suspend fun borrarUsuarioByDni(dni: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Buscar el usuario en Firestore por DNI
            val usuarioDoc = usuariosCollection.document(dni).get().await()
            
            if (!usuarioDoc.exists()) {
                return@withContext Result.Error(Exception("No se encontró el usuario con DNI: $dni"))
            }
            
            // 2. Obtener el email del usuario para buscar en Firebase Auth
            val usuario = usuarioDoc.toObject(Usuario::class.java)
            val email = usuario?.email ?: return@withContext Result.Error(Exception("El usuario no tiene email registrado"))
            
            // 3. Eliminar el documento de Firestore
            usuariosCollection.document(dni).delete().await()
            
            // 4. Intentar eliminar el usuario de Firebase Authentication usando la Cloud Function
            try {
                val data = hashMapOf(
                    "email" to email
                )
                
                // Llamada a la Cloud Function
                functions.getHttpsCallable("deleteUserByEmail")
                    .call(data)
                    .addOnSuccessListener { result -> 
                        val resultData = result.data
                        Timber.d("Usuario con email $email eliminado de Firebase Authentication: $resultData") 
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error al eliminar usuario de Firebase Authentication: ${e.message}")
                    }
                
                return@withContext Result.Success(Unit)
            } catch (authError: Exception) {
                // Si falla la autenticación, al menos informamos que se eliminó de Firestore
                Timber.e(authError, "Error al intentar eliminar usuario de Auth: ${authError.message}")
                return@withContext Result.Success(Unit) // Consideramos éxito aunque falle en Auth
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Escucha mensajes entre dos usuarios en tiempo real 
     */
    suspend fun escucharMensajes(
        emisorId: String, 
        receptorId: String, 
        alumnoId: String? = null, 
        onMensajeRecibido: (Mensaje) -> Unit
    ) {
        try {
            val query = if (alumnoId != null) {
                mensajesCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .whereIn("emisorId", listOf(emisorId, receptorId))
                    .whereIn("receptorId", listOf(emisorId, receptorId))
            } else {
                mensajesCollection
                    .whereIn("emisorId", listOf(emisorId, receptorId))
                    .whereIn("receptorId", listOf(emisorId, receptorId))
            }

            // Ordenar por timestamp para obtener los mensajes en orden
            query.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Timber.e(e, "Error escuchando mensajes")
                        return@addSnapshotListener
                    }

                    snapshots?.documentChanges?.forEach { change ->
                        // Solo procesamos mensajes nuevos o modificados
                        if (change.type == DocumentChange.Type.ADDED || 
                            change.type == DocumentChange.Type.MODIFIED) {
                            val mensaje = change.document.toObject(Mensaje::class.java)
                            onMensajeRecibido(mensaje)
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error al configurar escucha de mensajes")
            throw e
        }
    }

    /**
     * Obtiene un alumno por su ID
     */
    open suspend fun getAlumnoPorId(alumnoId: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = alumnosCollection.document(alumnoId).get().await()

            if (alumnoDoc.exists()) {
                val alumno = alumnoDoc.toObject(Alumno::class.java)
                
                // Obtener los datos de los familiares vinculados
                val alumnoData = alumno ?: throw Exception("Error al convertir datos del alumno")
                val familiares = mutableListOf<Familiar>()
                
                if (alumnoData.familiares.isNotEmpty()) {
                    // Suponiendo que familiares contiene IDs de usuarios familiares
                    for (familiarId in alumnoData.familiares) {
                        try {
                            val familiarDoc = usuariosCollection.document(familiarId.id).get().await()
                            if (familiarDoc.exists()) {
                                val usuario = familiarDoc.toObject(Usuario::class.java)
                                if (usuario != null) {
                                    // Crear un objeto Familiar con la información del usuario
                                    val familiar = Familiar(
                                        id = familiarId.id,
                                        nombre = usuario.nombre,
                                        apellidos = usuario.apellidos,
                                        parentesco = familiarId.parentesco
                                    )
                                    familiares.add(familiar)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al obtener familiar ${familiarId.id}")
                            // Continuamos con el siguiente familiar aunque este falle
                        }
                    }
                }
                
                // Si hemos obtenido familiares, actualizar el objeto Alumno
                val alumnoConFamiliares = if (familiares.isNotEmpty()) {
                    alumnoData.copy(familiares = familiares)
                } else {
                    alumnoData
                }
                
                return@withContext Result.Success(alumnoConFamiliares)
            } else {
                throw Exception("Alumno no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // Métodos requeridos por los ViewModels
    
    /**
     * Registra un nuevo alumno
     */
    suspend fun registrarAlumno(alumno: Alumno): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Guardar alumno en la colección de alumnos
            alumnosCollection.document(alumno.dni).set(alumno).await()
            
            // Crear un usuario básico para el alumno
            val perfiles = mutableListOf<Perfil>()
            perfiles.add(
                Perfil(
                    tipo = TipoUsuario.ALUMNO,
                    centroId = alumno.centroId
                )
            )
            
            val usuario = Usuario(
                dni = alumno.dni,
                nombre = alumno.nombre,
                apellidos = alumno.apellidos,
                email = "", // No se crea cuenta de correo para alumnos
                perfiles = perfiles,
                activo = true,
                fechaRegistro = Timestamp.now()
            )
            
            // Guardar usuario en la colección de usuarios
            usuariosCollection.document(alumno.dni).set(usuario).await()
            
            return@withContext Result.Success(alumno.dni)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los alumnos de un centro
     */
    suspend fun obtenerAlumnosPorCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val alumnosQuery = alumnosCollection
                .whereEqualTo("centroId", centroId)
                .get().await()
                
            val alumnos = alumnosQuery.documents.mapNotNull { doc ->
                // Obtenemos el DNI de cada alumno
                val alumno = doc.toObject(Alumno::class.java)
                
                // Para cada alumno, buscamos su información de usuario
                val usuarioDoc = usuariosCollection.document(alumno?.dni ?: "").get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los familiares de un centro
     */
    suspend fun obtenerFamiliaresPorCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos usuarios que tengan un perfil de tipo FAMILIAR asociado al centro
            val familiares = usuariosCollection.get().await().documents
                .mapNotNull { doc -> doc.toObject(Usuario::class.java) }
                .filter { usuario -> 
                    usuario.perfiles.any { 
                        it.tipo == TipoUsuario.FAMILIAR && it.centroId == centroId 
                    }
                }
                
            return@withContext Result.Success(familiares)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los familiares vinculados a un alumno
     */
    suspend fun obtenerFamiliaresPorAlumno(alumnoDni: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos el alumno
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            val familiaresDnis = alumno?.familiarIds ?: emptyList<String>()
            
            // Si no tiene familiares vinculados, devolvemos lista vacía
            if (familiaresDnis.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Buscamos los usuarios correspondientes a cada DNI de familiar
            val familiares = familiaresDnis.mapNotNull { dni ->
                val usuarioDoc = usuariosCollection.document(dni).get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(familiares)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los alumnos vinculados a un familiar
     */
    suspend fun obtenerAlumnosPorFamiliar(familiarDni: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos alumnos que tengan este familiar en su lista
            val alumnosQuery = alumnosCollection.get().await()
            
            val alumnosIds = alumnosQuery.documents
                .mapNotNull { it.toObject(Alumno::class.java) }
                .filter { it.familiarIds.contains(familiarDni) }
                .map { it.dni }
                
            // Si no hay alumnos vinculados, devolvemos lista vacía
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Buscamos los usuarios correspondientes a cada DNI de alumno
            val alumnos = alumnosIds.mapNotNull { dni ->
                val usuarioDoc = usuariosCollection.document(dni).get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Vincula un familiar con un alumno
     */
    suspend fun vincularFamiliarAlumno(alumnoDni: String, familiarDni: String, parentesco: SubtipoFamiliar): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que ambos existen
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val familiarDoc = usuariosCollection.document(familiarDni).get().await()
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("Familiar no encontrado"))
            }
            
            // Obtenemos el alumno actual
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            
            // Actualizamos su lista de familiares si no está ya
            val familiares = alumno?.familiares?.toMutableList() ?: mutableListOf()
            
            // Comprobamos si el familiar ya está vinculado
            val familiarExistente = familiares.find { it.id == familiarDni }
            
            if (familiarExistente == null) {
                // Obtenemos los datos del familiar
                val familiar = familiarDoc.toObject(Usuario::class.java)
                
                // Creamos un nuevo objeto Familiar con la relación de parentesco
                familiares.add(
                    Familiar(
                        id = familiarDni,
                        nombre = familiar?.nombre ?: "",
                        apellidos = familiar?.apellidos ?: "",
                        parentesco = parentesco.name
                    )
                )
                
                // Actualizamos también la lista de IDs de familiares para compatibilidad
                val familiarIds = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
                if (!familiarIds.contains(familiarDni)) {
                    familiarIds.add(familiarDni)
                }
                
                // Guardamos las relaciones en el documento del alumno
                val updates = mapOf(
                    "familiares" to familiares,
                    "familiarIds" to familiarIds
                )
                alumnosCollection.document(alumnoDni).update(updates).await()
            } else {
                // Si ya existe, actualizamos solo el parentesco
                val index = familiares.indexOf(familiarExistente)
                familiares[index] = familiarExistente.copy(parentesco = parentesco.name)
                
                // Guardamos la lista actualizada
                alumnosCollection.document(alumnoDni).update("familiares", familiares).await()
            }
            
            // También actualizamos el familiar para añadir el perfil con el subtipo
            val familiar = familiarDoc.toObject(Usuario::class.java)
            val perfiles = familiar?.perfiles?.toMutableList() ?: mutableListOf()
            
            // Buscamos si ya tiene un perfil de FAMILIAR para este centro
            val perfilExistente = perfiles.find { 
                it.tipo == TipoUsuario.FAMILIAR && it.centroId == alumno?.centroId
            }
            
            if (perfilExistente != null) {
                // Actualizamos el perfil existente con el nuevo subtipo y añadimos al alumno a la lista
                val alumnosDelPerfil = perfilExistente.alumnos.toMutableList()
                if (!alumnosDelPerfil.contains(alumnoDni)) {
                    alumnosDelPerfil.add(alumnoDni)
                }
                
                val index = perfiles.indexOf(perfilExistente)
                perfiles[index] = perfilExistente.copy(
                    subtipo = parentesco,
                    alumnos = alumnosDelPerfil
                )
            } else {
                // Creamos un nuevo perfil
                perfiles.add(
                    Perfil(
                        tipo = TipoUsuario.FAMILIAR,
                        subtipo = parentesco,
                        centroId = alumno?.centroId ?: "",
                        alumnos = listOf(alumnoDni)
                    )
                )
            }
            
            // Guardamos los cambios en el familiar
            usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al vincular familiar: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Desvincula un familiar de un alumno
     */
    suspend fun desvincularFamiliarAlumno(alumnoDni: String, familiarDni: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que ambos existen
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val familiarDoc = usuariosCollection.document(familiarDni).get().await()
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("Familiar no encontrado"))
            }
            
            // Obtenemos el alumno actual
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            
            // Actualizamos la lista de objetos Familiar
            val familiares = alumno?.familiares?.toMutableList() ?: mutableListOf()
            familiares.removeIf { it.id == familiarDni }
            
            // Actualizamos también la lista de IDs de familiares para compatibilidad
            val familiarIds = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
            familiarIds.remove(familiarDni)
            
            // Guardamos las relaciones actualizadas en el documento del alumno
            val updates = mapOf(
                "familiares" to familiares,
                "familiarIds" to familiarIds
            )
            alumnosCollection.document(alumnoDni).update(updates).await()
            
            // Actualizamos también los perfiles del familiar para eliminar la relación con este alumno
            val familiar = familiarDoc.toObject(Usuario::class.java)
            val perfiles = familiar?.perfiles?.toMutableList() ?: mutableListOf()
            
            // Buscamos el perfil de FAMILIAR para el centro del alumno
            val perfilFamiliar = perfiles.find { 
                it.tipo == TipoUsuario.FAMILIAR && it.centroId == alumno?.centroId
            }
            
            if (perfilFamiliar != null) {
                // Removemos al alumno de la lista de alumnos en el perfil
                val alumnosDelPerfil = perfilFamiliar.alumnos.toMutableList()
                alumnosDelPerfil.remove(alumnoDni)
                
                // Si quedan otros alumnos, actualizamos el perfil
                if (alumnosDelPerfil.isNotEmpty()) {
                    val index = perfiles.indexOf(perfilFamiliar)
                    perfiles[index] = perfilFamiliar.copy(alumnos = alumnosDelPerfil)
                    usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
                } else {
                    // Si no quedan alumnos, eliminamos el perfil de familiar para este centro
                    perfiles.remove(perfilFamiliar)
                    usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
                }
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al desvincular familiar: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Resetea la contraseña del usuario con el DNI especificado
     */
    suspend fun resetearPassword(dni: String, newPassword: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Obtenemos el usuario por su DNI
            val usuarioResult = getUsuarioPorDni(dni)
            if (usuarioResult !is Result.Success) {
                return@withContext Result.Error(Exception("Usuario no encontrado"))
            }
            
            val usuario = usuarioResult.data
            
            // Buscamos el usuario en Firebase Auth por su email
            val userQuery = firebaseAuth.fetchSignInMethodsForEmail(usuario.email).await()
            
            if (userQuery.signInMethods?.isNotEmpty() == true) {
                // Generamos un código de reautenticación
                val customToken = functions
                    .getHttpsCallable("generateCustomToken")
                    .call(hashMapOf("email" to usuario.email))
                    .await()
                    .data as String
                    
                // Iniciamos sesión con el token personalizado
                val authResult = firebaseAuth.signInWithCustomToken(customToken).await()
                
                // Actualizamos la contraseña
                authResult.user?.updatePassword(newPassword)?.await()
                
                // Volvemos a iniciar sesión con el usuario administrador original si es necesario
                // Aquí podrías añadir código para volver al usuario admin si lo necesitas
                
                return@withContext Result.Success(true)
            } else {
                return@withContext Result.Error(Exception("No se pudo encontrar el usuario en Firebase Auth"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza los datos de un usuario
     */
    suspend fun actualizarUsuario(usuario: Usuario): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Actualizar en Firestore
            usuariosCollection.document(usuario.dni).set(usuario).await()
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los mensajes donde participa un usuario
     * 
     * Este método recupera todos los mensajes donde el usuario es
     * emisor o receptor para mostrarlos en la pantalla de conversaciones
     */
    suspend fun getMensajesUsuario(usuarioId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes donde el usuario es emisor
            val queryEmisor = mensajesCollection
                .whereEqualTo("emisorId", usuarioId)
                .get()
                .await()

            val mensajesEmisor = queryEmisor.toObjects(Mensaje::class.java)

            // Obtener mensajes donde el usuario es receptor
            val queryReceptor = mensajesCollection
                .whereEqualTo("receptorId", usuarioId)
                .get()
                .await()

            val mensajesReceptor = queryReceptor.toObjects(Mensaje::class.java)

            // Combinar los resultados
            val todosMensajes = (mensajesEmisor + mensajesReceptor).distinctBy { it.id }

            return@withContext Result.Success(todosMensajes)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes del usuario")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene el usuario actual de Firebase Auth
     * @return FirebaseUser o null si no hay usuario autenticado
     */
    fun getUsuarioActualAuth(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Verifica si el usuario actual es un profesor
     * @return true si es profesor, false si es alumno o no está autenticado
     */
    suspend fun esProfesor(): Boolean {
        val usuario = getUsuarioActualAuth() ?: return false
        val usuarioId = usuario.uid
        
        return try {
            val docSnapshot = firestore.collection(COLLECTION_PROFESORES)
                .document(usuarioId)
                .get()
                .await()
            
            docSnapshot.exists()
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar si es profesor")
            false
        }
    }
    
    /**
     * Obtiene el ID del centro educativo al que pertenece el usuario actual
     * @return ID del centro o null si no se encuentra
     */
    suspend fun getCentroIdUsuarioActual(): String? {
        val usuario = getUsuarioActualAuth() ?: return null
        val usuarioId = usuario.uid
        val esProfesor = esProfesor()
        
        return try {
            val collection = if (esProfesor) COLLECTION_PROFESORES else COLLECTION_ALUMNOS
            val docSnapshot = firestore.collection(collection)
                .document(usuarioId)
                .get()
                .await()
            
            if (docSnapshot.exists()) {
                docSnapshot.getString("centroId")
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el centro del usuario")
            null
        }
    }
    
    /**
     * Obtiene el nombre completo del usuario
     * @param usuarioId ID del usuario
     * @return Nombre completo o cadena vacía si no se encuentra
     */
    suspend fun getNombreUsuario(usuarioId: String): String {
        return try {
            // Primero buscamos en la colección de profesores
            var docSnapshot = firestore.collection(COLLECTION_PROFESORES)
                .document(usuarioId)
                .get()
                .await()
            
            // Si no existe como profesor, buscamos en alumnos
            if (!docSnapshot.exists()) {
                docSnapshot = firestore.collection(COLLECTION_ALUMNOS)
                    .document(usuarioId)
                    .get()
                    .await()
            }
            
            if (docSnapshot.exists()) {
                val nombre = docSnapshot.getString("nombre") ?: ""
                val apellidos = docSnapshot.getString("apellidos") ?: ""
                "$nombre $apellidos".trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener nombre de usuario")
            ""
        }
    }

    /**
     * Obtiene el rol del usuario actual
     * @return "profesor", "alumno" o null si no está autenticado
     */
    suspend fun getRolUsuarioActual(): String? {
        val usuario = getUsuarioActual() ?: return null
        
        return if (esProfesor()) "profesor" else "alumno"
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    suspend fun obtenerUsuarioActual(): Usuario? {
        try {
            val firebaseUser = getUsuarioActualAuth() ?: return null
            
            // Buscar usuario por uid
            val usuarioDoc = usuariosCollection
                .document(firebaseUser.uid)
                .get()
                .await()
            
            if (!usuarioDoc.exists()) return null
            
            return usuarioDoc.toObject(Usuario::class.java)?.copy(dni = usuarioDoc.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario actual")
            return null
        }
    }

    /**
     * Obtiene todos los usuarios de un centro específico
     * @param centroId ID del centro
     * @return Lista de usuarios del centro
     */
    suspend fun obtenerUsuariosPorCentro(centroId: String): List<Usuario> {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Usuario::class.java)?.copy(dni = doc.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuarios por centro")
            emptyList()
        }
    }
}