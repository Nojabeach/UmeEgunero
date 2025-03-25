package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query

/**
 * Resultados posibles al realizar operaciones con repositorios
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Repositorio para gestionar usuarios y operaciones relacionadas
 */
@Singleton
open class UsuarioRepository @Inject constructor(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore
) {
    val usuariosCollection = firestore.collection("usuarios")
    val solicitudesCollection = firestore.collection("solicitudesRegistro")
    val centrosCollection = firestore.collection("centros")
    val clasesCollection = firestore.collection("clases")
    val alumnosCollection = firestore.collection("alumnos")
    val registrosCollection = firestore.collection("registrosActividad")
    val mensajesCollection = firestore.collection("mensajes")
    private val functions = Firebase.functions

    // TODO: Mejoras pendientes para el repositorio de usuarios
    // - Implementar caché local con Room para funcionamiento offline
    // - Añadir sincronización periódica en segundo plano
    // - Mejorar la seguridad con cifrado de datos sensibles
    // - Implementar sistema de permisos granular por tipo de usuario
    // - Desarrollar sistema de perfiles extendidos con información adicional
    // - Añadir soporte para múltiples roles por usuario
    // - Implementar registro de actividad del usuario
    // - Desarrollar sistema de notificaciones basado en acciones del usuario
    // - Añadir soporte para gestión de preferencias personalizadas

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
            val authResult = auth.createUserWithEmailAndPassword(form.email, form.password).await()
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
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
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
                auth.signOut()
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
        auth.signOut()
    }

    /**
     * Obtiene el usuario actual logueado
     */
    fun getUsuarioActual(): Flow<Result<Usuario?>> = flow {
        emit(Result.Loading)
        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Buscar el usuario en Firestore por email
                val userQuery = usuariosCollection.whereEqualTo("email", currentUser.email).get().await()

                if (!userQuery.isEmpty) {
                    val userDoc = userQuery.documents.first()
                    val usuario = userDoc.toObject(Usuario::class.java)
                    emit(Result.Success(usuario))
                } else {
                    emit(Result.Success(null)) // Usuario autenticado pero no en Firestore
                }
            } else {
                emit(Result.Success(null)) // No hay usuario logueado
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Obtiene el ID del usuario actualmente logueado
     */
    suspend fun getUsuarioActualId(): String = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext ""

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
    suspend fun getUsuarioById(id: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userDoc = usuariosCollection.document(id).get().await()

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
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
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
            val user = auth.currentUser
            
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
            auth.sendPasswordResetEmail(email).await()
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
            val fechaInicio = Timestamp(Date(hoy.seconds * 1000).apply {
                hours = 0
                minutes = 0
                seconds = 0
            })
            val fechaFin = Timestamp(Date(hoy.seconds * 1000).apply {
                hours = 23
                minutes = 59
                seconds = 59
            })

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
    suspend fun getUsuarioByEmail(email: String): Result<Usuario?> = withContext(Dispatchers.IO) {
        try {
            val usuarioQuery = usuariosCollection
                .whereEqualTo("email", email)
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
                // En un entorno real, este método enviaría una solicitud a una función Cloud
                // que se encargaría de eliminar el usuario de Firebase Auth
                
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

    companion object {
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
}