package com.tfg.umeegunero.feature.profesor.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.feature.profesor.models.Criterio
import com.tfg.umeegunero.feature.profesor.models.EvaluacionRubrica
import com.tfg.umeegunero.feature.profesor.models.Rubrica
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las rúbricas de evaluación y sus evaluaciones asociadas.
 * Proporciona métodos para crear, recuperar, actualizar y eliminar rúbricas y evaluaciones.
 */
@Singleton
class RubricaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLECCION_RUBRICAS = "rubricas"
        private const val COLECCION_EVALUACIONES = "evaluaciones_rubricas"
    }

    /**
     * Obtiene todas las rúbricas disponibles.
     * @return Flow con el resultado que contiene la lista de rúbricas
     */
    fun getRubricas(): Flow<Result<List<Rubrica>>> = flow {
        try {
            emit(Result.Loading())
            val snapshot = firestore.collection(COLECCION_RUBRICAS)
                .get()
                .await()
            
            val rubricas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Rubrica>()?.copy(id = doc.id)
            }
            
            emit(Result.Success(rubricas))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener rúbricas")
            emit(Result.Error(e))
        }
    }

    /**
     * Obtiene una rúbrica específica por su ID.
     * @param rubricaId ID de la rúbrica a obtener
     * @return Flow con el resultado que contiene la rúbrica
     */
    fun getRubrica(rubricaId: String): Flow<Result<Rubrica>> = flow {
        try {
            emit(Result.Loading())
            val doc = firestore.collection(COLECCION_RUBRICAS)
                .document(rubricaId)
                .get()
                .await()
                
            val rubrica = doc.toObject<Rubrica>()?.copy(id = doc.id)
            
            if (rubrica != null) {
                emit(Result.Success(rubrica))
            } else {
                emit(Result.Error(Exception("Rúbrica no encontrada")))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener rúbrica $rubricaId")
            emit(Result.Error(e))
        }
    }

    /**
     * Obtiene las rúbricas filtradas por asignatura.
     * @param asignatura Nombre de la asignatura para filtrar
     * @return Flow con el resultado que contiene la lista de rúbricas filtradas
     */
    fun getRubricasPorAsignatura(asignatura: String): Flow<Result<List<Rubrica>>> = flow {
        try {
            emit(Result.Loading())
            val snapshot = firestore.collection(COLECCION_RUBRICAS)
                .whereEqualTo("asignatura", asignatura)
                .get()
                .await()
            
            val rubricas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Rubrica>()?.copy(id = doc.id)
            }
            
            emit(Result.Success(rubricas))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener rúbricas para la asignatura $asignatura")
            emit(Result.Error(e))
        }
    }

    /**
     * Guarda una nueva rúbrica o actualiza una existente.
     * @param rubrica Rúbrica a guardar o actualizar
     * @return Flow con el resultado de la operación
     */
    fun guardarRubrica(rubrica: Rubrica): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading())
            
            if (!rubrica.esValida()) {
                emit(Result.Error(Exception("La rúbrica no es válida. Debe tener nombre, asignatura y al menos un criterio.")))
                return@flow
            }
            
            // Si la rúbrica ya tiene un ID, actualiza el documento existente
            val documentId = if (rubrica.id.isNotEmpty()) {
                firestore.collection(COLECCION_RUBRICAS)
                    .document(rubrica.id)
                    .set(rubrica)
                    .await()
                
                rubrica.id
            } else {
                // Si es una nueva rúbrica, crea un nuevo documento
                val docRef = firestore.collection(COLECCION_RUBRICAS)
                    .add(rubrica)
                    .await()
                
                docRef.id
            }
            
            emit(Result.Success(documentId))
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar rúbrica")
            emit(Result.Error(e))
        }
    }

    /**
     * Elimina una rúbrica específica por su ID.
     * @param rubricaId ID de la rúbrica a eliminar
     * @return Flow con el resultado de la operación
     */
    fun eliminarRubrica(rubricaId: String): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading())
            
            // Eliminar la rúbrica
            firestore.collection(COLECCION_RUBRICAS)
                .document(rubricaId)
                .delete()
                .await()
            
            // También eliminar todas las evaluaciones asociadas a esta rúbrica
            val evaluaciones = firestore.collection(COLECCION_EVALUACIONES)
                .whereEqualTo("rubricaId", rubricaId)
                .get()
                .await()
                
            for (doc in evaluaciones.documents) {
                firestore.collection(COLECCION_EVALUACIONES)
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            emit(Result.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar rúbrica $rubricaId")
            emit(Result.Error(e))
        }
    }

    /**
     * Guarda una evaluación de rúbrica para un alumno.
     * @param evaluacion Evaluación de rúbrica a guardar
     * @return Flow con el resultado de la operación
     */
    fun guardarEvaluacion(evaluacion: EvaluacionRubrica): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading())
            
            // Verificar que la rúbrica asociada existe
            val rubricaDoc = firestore.collection(COLECCION_RUBRICAS)
                .document(evaluacion.rubricaId)
                .get()
                .await()
                
            if (!rubricaDoc.exists()) {
                emit(Result.Error(Exception("La rúbrica asociada no existe")))
                return@flow
            }
            
            // Si la evaluación ya tiene un ID, actualiza el documento existente
            val documentId = if (evaluacion.id.isNotEmpty()) {
                firestore.collection(COLECCION_EVALUACIONES)
                    .document(evaluacion.id)
                    .set(evaluacion)
                    .await()
                
                evaluacion.id
            } else {
                // Si es una nueva evaluación, crea un nuevo documento
                val docRef = firestore.collection(COLECCION_EVALUACIONES)
                    .add(evaluacion)
                    .await()
                
                docRef.id
            }
            
            emit(Result.Success(documentId))
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar evaluación")
            emit(Result.Error(e))
        }
    }

    /**
     * Obtiene todas las evaluaciones para un alumno específico.
     * @param alumnoId ID del alumno
     * @return Flow con el resultado que contiene la lista de evaluaciones
     */
    fun getEvaluacionesPorAlumno(alumnoId: String): Flow<Result<List<EvaluacionRubrica>>> = flow {
        try {
            emit(Result.Loading())
            val snapshot = firestore.collection(COLECCION_EVALUACIONES)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()
            
            val evaluaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject<EvaluacionRubrica>()?.copy(id = doc.id)
            }
            
            emit(Result.Success(evaluaciones))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener evaluaciones para el alumno $alumnoId")
            emit(Result.Error(e))
        }
    }

    /**
     * Obtiene todas las evaluaciones para un alumno en un trimestre específico.
     * @param alumnoId ID del alumno
     * @param trimestreId ID del trimestre
     * @return Flow con el resultado que contiene la lista de evaluaciones
     */
    fun getEvaluacionesPorAlumnoYTrimestre(
        alumnoId: String, 
        trimestreId: Int
    ): Flow<Result<List<EvaluacionRubrica>>> = flow {
        try {
            emit(Result.Loading())
            val snapshot = firestore.collection(COLECCION_EVALUACIONES)
                .whereEqualTo("alumnoId", alumnoId)
                .whereEqualTo("trimestreId", trimestreId)
                .get()
                .await()
            
            val evaluaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject<EvaluacionRubrica>()?.copy(id = doc.id)
            }
            
            emit(Result.Success(evaluaciones))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener evaluaciones para el alumno $alumnoId en trimestre $trimestreId")
            emit(Result.Error(e))
        }
    }

    /**
     * Elimina una evaluación específica por su ID.
     * @param evaluacionId ID de la evaluación a eliminar
     * @return Flow con el resultado de la operación
     */
    fun eliminarEvaluacion(evaluacionId: String): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading())
            
            firestore.collection(COLECCION_EVALUACIONES)
                .document(evaluacionId)
                .delete()
                .await()
            
            emit(Result.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar evaluación $evaluacionId")
            emit(Result.Error(e))
        }
    }
} 