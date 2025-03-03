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
class UsuarioRepository @Inject constructor(
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

    // SECCIÓN: AUTENTICACIÓN Y GESTIÓN DE USUARIOS

    /**
     * Registra un nuevo usuario en Firebase Auth y Firestore
     */
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
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al iniciar sesión")

            // Actualizar último acceso del usuario
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()

            if (!userQuery.isEmpty) {
                val userDoc = userQuery.documents.first()
                userDoc.reference.update("ultimoAcceso", Timestamp.now()).await()
                return@withContext Result.Success(userDoc.id) // Devuelve el DNI como ID
            } else {
                throw Exception("Usuario no encontrado en la base de datos")
            }
        } catch (e: FirebaseAuthException) {
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
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
}