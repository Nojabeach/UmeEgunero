package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.PlantillaRegistroActividad
import com.tfg.umeegunero.data.model.TipoActividad
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las plantillas de registros de actividad
 * 
 * Esta clase se encarga de las operaciones CRUD relacionadas con las plantillas
 * que utilizan los profesores para agilizar la creación de registros de actividad.
 * 
 * @property firestore Instancia de Firestore para acceder a la base de datos
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@Singleton
class PlantillaRegistroRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLECCION_PLANTILLAS = "plantillas_actividad"
    }
    
    /**
     * Obtiene todas las plantillas disponibles para un profesor/centro
     * 
     * @param profesorId ID del profesor (opcional)
     * @param centroId ID del centro educativo (opcional)
     * @return Resultado con la lista de plantillas disponibles
     */
    suspend fun getPlantillas(
        profesorId: String? = null,
        centroId: String? = null
    ): Result<List<PlantillaRegistroActividad>> {
        return try {
            val query = firestore.collection(COLECCION_PLANTILLAS)
                .let { baseQuery ->
                    if (profesorId != null) {
                        // Plantillas del profesor o plantillas globales (null)
                        baseQuery.whereIn("profesorId", listOf(profesorId, null))
                    } else {
                        baseQuery
                    }
                }
                .let { withProfQuery ->
                    if (centroId != null) {
                        // Añadir filtro por centro
                        withProfQuery.whereIn("centroId", listOf(centroId, null))
                    } else {
                        withProfQuery
                    }
                }
            
            val snapshot = query.get().await()
            val plantillas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<PlantillaRegistroActividad>()
            }
            
            Result.Success(plantillas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener plantillas")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene plantillas filtradas por tipo de actividad
     * 
     * @param tipoActividad Tipo de actividad para filtrar
     * @param profesorId ID del profesor (opcional)
     * @param centroId ID del centro educativo (opcional)
     * @return Resultado con la lista de plantillas del tipo especificado
     */
    suspend fun getPlantillasPorTipo(
        tipoActividad: TipoActividad,
        profesorId: String? = null,
        centroId: String? = null
    ): Result<List<PlantillaRegistroActividad>> {
        return try {
            val query = firestore.collection(COLECCION_PLANTILLAS)
                .whereEqualTo("tipoActividad", tipoActividad.name)
                .let { baseQuery ->
                    if (profesorId != null) {
                        baseQuery.whereIn("profesorId", listOf(profesorId, null))
                    } else {
                        baseQuery
                    }
                }
                .let { withProfQuery ->
                    if (centroId != null) {
                        withProfQuery.whereIn("centroId", listOf(centroId, null))
                    } else {
                        withProfQuery
                    }
                }
            
            val snapshot = query.get().await()
            val plantillas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<PlantillaRegistroActividad>()
            }
            
            Result.Success(plantillas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener plantillas por tipo")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene una plantilla específica por su ID
     * 
     * @param plantillaId ID de la plantilla a obtener
     * @return Resultado con la plantilla encontrada
     */
    suspend fun getPlantilla(plantillaId: String): Result<PlantillaRegistroActividad> {
        return try {
            val docRef = firestore.collection(COLECCION_PLANTILLAS).document(plantillaId)
            val documento = docRef.get().await()
            
            val plantilla = documento.toObject<PlantillaRegistroActividad>()
                ?: throw Exception("Plantilla no encontrada")
            
            Result.Success(plantilla)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener plantilla: $plantillaId")
            Result.Error(e)
        }
    }
    
    /**
     * Guarda una nueva plantilla o actualiza una existente
     * 
     * @param plantilla Plantilla a guardar
     * @return Resultado con la plantilla guardada (incluyendo ID)
     */
    suspend fun guardarPlantilla(plantilla: PlantillaRegistroActividad): Result<PlantillaRegistroActividad> {
        return try {
            val docRef = if (plantilla.id.isNotEmpty()) {
                // Si tiene ID, actualizamos la existente
                firestore.collection(COLECCION_PLANTILLAS).document(plantilla.id)
            } else {
                // Si no tiene ID, creamos una nueva
                firestore.collection(COLECCION_PLANTILLAS).document()
            }
            
            val plantillaConId = if (plantilla.id.isEmpty()) {
                plantilla.copy(id = docRef.id)
            } else {
                plantilla
            }
            
            docRef.set(plantillaConId).await()
            Result.Success(plantillaConId)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar plantilla")
            Result.Error(e)
        }
    }
    
    /**
     * Elimina una plantilla
     * 
     * @param plantillaId ID de la plantilla a eliminar
     * @return Resultado indicando éxito o fracaso
     */
    suspend fun eliminarPlantilla(plantillaId: String): Result<Boolean> {
        return try {
            firestore.collection(COLECCION_PLANTILLAS)
                .document(plantillaId)
                .delete()
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar plantilla: $plantillaId")
            Result.Error(e)
        }
    }
    
    /**
     * Marca una plantilla como predeterminada y desmarca otras del mismo tipo
     * 
     * @param plantillaId ID de la plantilla a marcar como predeterminada
     * @param tipoActividad Tipo de actividad de la plantilla
     * @param profesorId ID del profesor propietario de la plantilla
     * @return Resultado indicando éxito o fracaso
     */
    suspend fun marcarComoPredeterminada(
        plantillaId: String,
        tipoActividad: TipoActividad,
        profesorId: String
    ): Result<Boolean> {
        return try {
            // 1. Obtener plantillas del mismo tipo y propietario
            val query = firestore.collection(COLECCION_PLANTILLAS)
                .whereEqualTo("tipoActividad", tipoActividad.name)
                .whereEqualTo("profesorId", profesorId)
                .whereEqualTo("esPredeterminada", true)
            
            val snapshot = query.get().await()
            
            // 2. Desmarcar las predeterminadas existentes
            for (doc in snapshot.documents) {
                if (doc.id != plantillaId) {
                    firestore.collection(COLECCION_PLANTILLAS)
                        .document(doc.id)
                        .update("esPredeterminada", false)
                        .await()
                }
            }
            
            // 3. Marcar la seleccionada como predeterminada
            firestore.collection(COLECCION_PLANTILLAS)
                .document(plantillaId)
                .update("esPredeterminada", true)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar plantilla como predeterminada: $plantillaId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene la plantilla predeterminada para un tipo de actividad y profesor
     * 
     * @param tipoActividad Tipo de actividad
     * @param profesorId ID del profesor
     * @return Resultado con la plantilla predeterminada o null si no existe
     */
    suspend fun getPlantillaPredeterminada(
        tipoActividad: TipoActividad,
        profesorId: String
    ): Result<PlantillaRegistroActividad?> {
        return try {
            val query = firestore.collection(COLECCION_PLANTILLAS)
                .whereEqualTo("tipoActividad", tipoActividad.name)
                .whereEqualTo("profesorId", profesorId)
                .whereEqualTo("esPredeterminada", true)
                .limit(1)
            
            val snapshot = query.get().await()
            val plantilla = snapshot.documents.firstOrNull()?.toObject<PlantillaRegistroActividad>()
            
            Result.Success(plantilla)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener plantilla predeterminada para: $tipoActividad")
            Result.Error(e)
        }
    }
} 