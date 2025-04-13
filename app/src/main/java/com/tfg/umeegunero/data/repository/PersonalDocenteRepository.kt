package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.PersonalDocente
import com.tfg.umeegunero.data.model.Resultado
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Repositorio que gestiona las operaciones relacionadas con el personal docente en Firestore.
 */
class PersonalDocenteRepository @Inject constructor(
    @Named("personalDocenteCollection") private val personalDocenteCollection: CollectionReference,
    private val firestore: FirebaseFirestore
) {

    /**
     * Obtiene todos los profesores.
     * @param limit Límite de resultados por página.
     * @param lastDocumentId Identificador del último documento obtenido para paginación.
     * @return Flow con el resultado que contiene la lista de profesores.
     */
    fun getPersonalDocente(limit: Long = 50, lastDocumentId: String? = null): Flow<Resultado<List<PersonalDocente>>> = flow {
        try {
            emit(Resultado.Cargando())
            
            var query = personalDocenteCollection.limit(limit)
            
            if (lastDocumentId != null) {
                val lastDocument = personalDocenteCollection.document(lastDocumentId).get().await()
                query = query.startAfter(lastDocument)
            }
            
            val snapshot = query.get().await()
            val profesores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PersonalDocente::class.java)
            }
            
            emit(Resultado.Exito(profesores))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener personal docente")
            emit(Resultado.Error(e.message ?: "Error desconocido al obtener personal docente"))
        }
    }

    /**
     * Obtiene un profesor por su ID.
     * @param id Identificador del profesor.
     * @return Flow con el resultado que contiene el profesor.
     */
    fun getProfesorById(id: String): Flow<Resultado<PersonalDocente?>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val documento = personalDocenteCollection.document(id).get().await()
            val profesor = documento.toObject(PersonalDocente::class.java)
            
            emit(Resultado.Exito(profesor))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesor con ID: $id")
            emit(Resultado.Error(e.message ?: "Error desconocido al obtener profesor"))
        }
    }

    /**
     * Obtiene todos los profesores activos de un centro educativo.
     * @param centroId Identificador del centro educativo.
     * @return Flow con el resultado que contiene la lista de profesores.
     */
    fun getProfesoresByCentro(centroId: String): Flow<Resultado<List<PersonalDocente>>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val query = personalDocenteCollection
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("activo", true)
                
            val snapshot = query.get().await()
            val profesores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PersonalDocente::class.java)
            }
            
            emit(Resultado.Exito(profesores))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores del centro: $centroId")
            emit(Resultado.Error(e.message ?: "Error desconocido al obtener profesores por centro"))
        }
    }

    /**
     * Obtiene los profesores por especialidad.
     * @param especialidad Especialidad a filtrar.
     * @return Flow con el resultado que contiene la lista de profesores.
     */
    fun getProfesoresByEspecialidad(especialidad: String): Flow<Resultado<List<PersonalDocente>>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val query = personalDocenteCollection
                .whereArrayContains("especialidades", especialidad)
                .whereEqualTo("activo", true)
                
            val snapshot = query.get().await()
            val profesores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PersonalDocente::class.java)
            }
            
            emit(Resultado.Exito(profesores))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores por especialidad: $especialidad")
            emit(Resultado.Error(e.message ?: "Error desconocido al obtener profesores por especialidad"))
        }
    }

    /**
     * Crea o actualiza un profesor en Firestore.
     * @param profesor Datos del profesor a guardar.
     * @return Flow con el resultado de la operación que contiene el profesor guardado.
     */
    fun guardarProfesor(profesor: PersonalDocente): Flow<Resultado<PersonalDocente>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val document = if (profesor.id.isNotEmpty()) {
                personalDocenteCollection.document(profesor.id)
            } else {
                personalDocenteCollection.document()
            }
            
            document.set(profesor).await()
            val profesorGuardado = document.get().await().toObject(PersonalDocente::class.java)
                ?: throw Exception("Error al recuperar el profesor guardado")
            
            emit(Resultado.Exito(profesorGuardado))
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar profesor: ${profesor.id}")
            emit(Resultado.Error(e.message ?: "Error desconocido al guardar profesor"))
        }
    }

    /**
     * Elimina un profesor de Firestore.
     * @param id Identificador del profesor a eliminar.
     * @return Flow con el resultado de la operación.
     */
    fun eliminarProfesor(id: String): Flow<Resultado<Boolean>> = flow {
        try {
            emit(Resultado.Cargando())
            
            personalDocenteCollection.document(id).delete().await()
            
            emit(Resultado.Exito(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar profesor: $id")
            emit(Resultado.Error(e.message ?: "Error desconocido al eliminar profesor"))
        }
    }

    /**
     * Actualiza el estado de actividad de un profesor.
     * @param id Identificador del profesor.
     * @param activo Estado de actividad a establecer.
     * @return Flow con el resultado de la operación.
     */
    fun actualizarEstadoActividad(id: String, activo: Boolean): Flow<Resultado<Boolean>> = flow {
        try {
            emit(Resultado.Cargando())
            
            personalDocenteCollection.document(id)
                .update("activo", activo)
                .await()
            
            emit(Resultado.Exito(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar estado de actividad del profesor: $id")
            emit(Resultado.Error(e.message ?: "Error desconocido al actualizar estado de actividad"))
        }
    }

    /**
     * Asigna una clase a un profesor.
     * @param profesorId Identificador del profesor.
     * @param claseId Identificador de la clase.
     * @return Flow con el resultado de la operación.
     */
    fun asignarClase(profesorId: String, claseId: String): Flow<Resultado<Boolean>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val doc = personalDocenteCollection.document(profesorId).get().await()
            val profesor = doc.toObject(PersonalDocente::class.java)
                ?: throw Exception("No se encontró el profesor con ID: $profesorId")
            
            val clasesActualizadas = profesor.clasesAsignadas.toMutableList().also {
                if (!it.contains(claseId)) {
                    it.add(claseId)
                }
            }
            
            personalDocenteCollection.document(profesorId)
                .update("clasesAsignadas", clasesActualizadas)
                .await()
            
            emit(Resultado.Exito(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar clase $claseId al profesor $profesorId")
            emit(Resultado.Error(e.message ?: "Error desconocido al asignar clase"))
        }
    }

    /**
     * Desasigna una clase a un profesor.
     * @param profesorId Identificador del profesor.
     * @param claseId Identificador de la clase.
     * @return Flow con el resultado de la operación.
     */
    fun desasignarClase(profesorId: String, claseId: String): Flow<Resultado<Boolean>> = flow {
        try {
            emit(Resultado.Cargando())
            
            val doc = personalDocenteCollection.document(profesorId).get().await()
            val profesor = doc.toObject(PersonalDocente::class.java)
                ?: throw Exception("No se encontró el profesor con ID: $profesorId")
            
            val clasesActualizadas = profesor.clasesAsignadas.toMutableList().also {
                it.remove(claseId)
            }
            
            personalDocenteCollection.document(profesorId)
                .update("clasesAsignadas", clasesActualizadas)
                .await()
            
            emit(Resultado.Exito(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al desasignar clase $claseId del profesor $profesorId")
            emit(Resultado.Error(e.message ?: "Error desconocido al desasignar clase"))
        }
    }
} 