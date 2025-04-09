package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.CategoriaEtiqueta
import com.tfg.umeegunero.data.model.EtiquetaActividad
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar etiquetas personalizadas de actividades
 * 
 * Esta clase se encarga de las operaciones CRUD relacionadas con las etiquetas
 * que utilizan los profesores para categorizar y filtrar las actividades.
 * 
 * @property firestore Instancia de Firestore para acceder a la base de datos
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@Singleton
class EtiquetaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLECCION_ETIQUETAS = "etiquetas_actividad"
    }
    
    /**
     * Obtiene todas las etiquetas disponibles para un profesor/centro
     * 
     * @param profesorId ID del profesor (opcional)
     * @param centroId ID del centro educativo (opcional)
     * @param soloActivas Si es true, solo devuelve etiquetas activas
     * @return Resultado con la lista de etiquetas disponibles
     */
    suspend fun getEtiquetas(
        profesorId: String? = null,
        centroId: String? = null,
        soloActivas: Boolean = true
    ): Result<List<EtiquetaActividad>> {
        return try {
            val query = firestore.collection(COLECCION_ETIQUETAS)
                .let { baseQuery ->
                    if (profesorId != null) {
                        // Etiquetas del profesor o etiquetas globales (null)
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
                .let { withCentroQuery ->
                    if (soloActivas) {
                        // Solo devolver etiquetas activas
                        withCentroQuery.whereEqualTo("activa", true)
                    } else {
                        withCentroQuery
                    }
                }
            
            val snapshot = query.get().await()
            val etiquetas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<EtiquetaActividad>()
            }
            
            Result.Success(etiquetas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener etiquetas")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene etiquetas filtradas por categoría
     * 
     * @param categoria Categoría para filtrar
     * @param profesorId ID del profesor (opcional)
     * @param centroId ID del centro educativo (opcional)
     * @return Resultado con la lista de etiquetas de la categoría especificada
     */
    suspend fun getEtiquetasPorCategoria(
        categoria: CategoriaEtiqueta,
        profesorId: String? = null,
        centroId: String? = null
    ): Result<List<EtiquetaActividad>> {
        return try {
            val query = firestore.collection(COLECCION_ETIQUETAS)
                .whereEqualTo("categoria", categoria.name)
                .whereEqualTo("activa", true)
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
            val etiquetas = snapshot.documents.mapNotNull { doc ->
                doc.toObject<EtiquetaActividad>()
            }
            
            Result.Success(etiquetas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener etiquetas por categoría")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene una etiqueta específica por su ID
     * 
     * @param etiquetaId ID de la etiqueta a obtener
     * @return Resultado con la etiqueta encontrada
     */
    suspend fun getEtiqueta(etiquetaId: String): Result<EtiquetaActividad> {
        return try {
            val docRef = firestore.collection(COLECCION_ETIQUETAS).document(etiquetaId)
            val documento = docRef.get().await()
            
            val etiqueta = documento.toObject<EtiquetaActividad>()
                ?: throw Exception("Etiqueta no encontrada")
            
            Result.Success(etiqueta)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener etiqueta: $etiquetaId")
            Result.Error(e)
        }
    }
    
    /**
     * Guarda una nueva etiqueta o actualiza una existente
     * 
     * @param etiqueta Etiqueta a guardar
     * @return Resultado con la etiqueta guardada (incluyendo ID)
     */
    suspend fun guardarEtiqueta(etiqueta: EtiquetaActividad): Result<EtiquetaActividad> {
        return try {
            val docRef = if (etiqueta.id.isNotEmpty()) {
                // Si tiene ID, actualizamos la existente
                firestore.collection(COLECCION_ETIQUETAS).document(etiqueta.id)
            } else {
                // Si no tiene ID, creamos una nueva
                firestore.collection(COLECCION_ETIQUETAS).document()
            }
            
            val etiquetaConId = if (etiqueta.id.isEmpty()) {
                etiqueta.copy(id = docRef.id)
            } else {
                etiqueta
            }
            
            docRef.set(etiquetaConId).await()
            Result.Success(etiquetaConId)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar etiqueta")
            Result.Error(e)
        }
    }
    
    /**
     * Elimina una etiqueta (borrado lógico)
     * 
     * En lugar de eliminar físicamente la etiqueta, la marca como inactiva
     * para mantener la integridad referencial.
     * 
     * @param etiquetaId ID de la etiqueta a desactivar
     * @return Resultado indicando éxito o fracaso
     */
    suspend fun desactivarEtiqueta(etiquetaId: String): Result<Boolean> {
        return try {
            firestore.collection(COLECCION_ETIQUETAS)
                .document(etiquetaId)
                .update("activa", false)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al desactivar etiqueta: $etiquetaId")
            Result.Error(e)
        }
    }
    
    /**
     * Incrementa el contador de uso de una etiqueta
     * 
     * @param etiquetaId ID de la etiqueta cuyo contador se incrementará
     * @return Resultado indicando éxito o fracaso
     */
    suspend fun incrementarContador(etiquetaId: String): Result<Boolean> {
        return try {
            // Primero obtenemos la etiqueta actual para conocer su contador
            val etiquetaResult = getEtiqueta(etiquetaId)
            
            if (etiquetaResult is Result.Error) {
                return Result.Error(etiquetaResult.exception)
            }
            
            val etiqueta = (etiquetaResult as Result.Success).data
            val nuevoContador = etiqueta.contador + 1
            
            // Actualizamos el contador
            firestore.collection(COLECCION_ETIQUETAS)
                .document(etiquetaId)
                .update("contador", nuevoContador)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al incrementar contador de etiqueta: $etiquetaId")
            Result.Error(e)
        }
    }
} 