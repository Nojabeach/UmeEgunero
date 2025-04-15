package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.CaracteristicaUsada
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las estadísticas de uso de la aplicación en Firestore.
 * 
 * Esta clase se encarga de realizar consultas y actualizaciones en la colección de estadísticas
 * de Firestore, permitiendo obtener información sobre el uso de la aplicación, como por ejemplo:
 * - Número de usuarios activos
 * - Características más utilizadas
 * - Tiempo promedio de uso
 * - Sesiones por usuario
 * 
 * Utiliza corrutinas para realizar operaciones asíncronas y devuelve los resultados
 * encapsulados en la clase [Result] para facilitar el manejo de errores.
 * 
 * @property firestore Instancia de FirebaseFirestore para acceder a la base de datos
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
@Singleton
class EstadisticasRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val COLLECTION_ESTADISTICAS = "estadisticas"
        private const val DOC_USO_APLICACION = "uso_aplicacion"
        private const val COLLECTION_CARACTERISTICAS = "caracteristicas"
    }
    
    /**
     * Obtiene las estadísticas generales de uso de la aplicación
     * 
     * @param periodo Periodo de tiempo para el que se quieren obtener las estadísticas
     * @return [Result] con los datos de usuarios activos, sesiones promedio y tiempo promedio
     */
    suspend fun obtenerEstadisticasUso(periodo: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val estadisticasRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)
            
            val estadisticasDoc = estadisticasRef.get().await()
            
            if (estadisticasDoc.exists()) {
                val resultado = mutableMapOf<String, Any>()
                
                // Obtener estadísticas generales según el periodo
                val campoUsuarios = when (periodo) {
                    "Última semana" -> "usuarios_activos_semana"
                    "Último trimestre" -> "usuarios_activos_trimestre"
                    "Último año" -> "usuarios_activos_anio"
                    else -> "usuarios_activos" // Por defecto último mes
                }
                
                val campoSesiones = when (periodo) {
                    "Última semana" -> "sesiones_promedio_semana"
                    "Último trimestre" -> "sesiones_promedio_trimestre"
                    "Último año" -> "sesiones_promedio_anio"
                    else -> "sesiones_promedio" // Por defecto último mes
                }
                
                val campoTiempo = when (periodo) {
                    "Última semana" -> "tiempo_promedio_sesion_semana"
                    "Último trimestre" -> "tiempo_promedio_sesion_trimestre"
                    "Último año" -> "tiempo_promedio_sesion_anio"
                    else -> "tiempo_promedio_sesion" // Por defecto último mes
                }
                
                resultado["usuariosActivos"] = estadisticasDoc.getLong(campoUsuarios) ?: 0
                resultado["sesionesPromedio"] = estadisticasDoc.getDouble(campoSesiones) ?: 0.0
                resultado["tiempoPromedioSesion"] = estadisticasDoc.getString(campoTiempo) ?: "0 min"
                resultado["ultimaActualizacion"] = estadisticasDoc.getTimestamp("ultima_actualizacion") ?: Timestamp.now()
                
                return@withContext Result.Success(resultado)
            } else {
                return@withContext Result.Error(Exception("No se encontraron estadísticas de uso"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener estadísticas de uso")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene las características más utilizadas de la aplicación
     * 
     * @param limite Número máximo de características a obtener
     * @return [Result] con la lista de características más utilizadas
     */
    suspend fun obtenerCaracteristicasUsadas(limite: Long = 6): Result<List<CaracteristicaUsada>> = withContext(Dispatchers.IO) {
        try {
            val caracteristicasRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)
                .collection(COLLECTION_CARACTERISTICAS)
                .orderBy("frecuencia", Query.Direction.DESCENDING)
                .limit(limite)
            
            val caracteristicasSnapshot = caracteristicasRef.get().await()
            val caracteristicasList = mutableListOf<CaracteristicaUsada>()
            
            for (doc in caracteristicasSnapshot.documents) {
                val nombre = doc.getString("nombre") ?: continue
                val frecuencia = doc.getLong("frecuencia")?.toInt() ?: 0
                val porcentaje = doc.getDouble("porcentaje")?.toFloat() ?: 0f
                
                caracteristicasList.add(CaracteristicaUsada(nombre, frecuencia, porcentaje))
            }
            
            return@withContext Result.Success(caracteristicasList)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener características usadas")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Registra el uso de una característica en Firestore
     * 
     * @param nombreCaracteristica Nombre de la característica utilizada
     * @return [Result] indicando si se ha registrado correctamente
     */
    suspend fun registrarUsoCaracteristica(nombreCaracteristica: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val caracteristicaRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)
                .collection(COLLECTION_CARACTERISTICAS)
                .whereEqualTo("nombre", nombreCaracteristica)
                .limit(1)
                .get()
                .await()
            
            if (caracteristicaRef.documents.isNotEmpty()) {
                val docRef = caracteristicaRef.documents.first().reference
                val frecuenciaActual = caracteristicaRef.documents.first().getLong("frecuencia") ?: 0
                
                docRef.update("frecuencia", frecuenciaActual + 1).await()
                
                // Actualizar porcentajes (esto requeriría recalcular todos los porcentajes)
                actualizarPorcentajesCaracteristicas()
                
                return@withContext Result.Success(true)
            } else {
                // Si no existe la característica, la creamos
                val nuevaCaracteristica = hashMapOf(
                    "nombre" to nombreCaracteristica,
                    "frecuencia" to 1,
                    "porcentaje" to 0.0 // Se actualizará después
                )
                
                firestore.collection(COLLECTION_ESTADISTICAS)
                    .document(DOC_USO_APLICACION)
                    .collection(COLLECTION_CARACTERISTICAS)
                    .add(nuevaCaracteristica)
                    .await()
                
                // Actualizar porcentajes
                actualizarPorcentajesCaracteristicas()
                
                return@withContext Result.Success(true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar uso de característica: $nombreCaracteristica")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Actualiza los porcentajes de todas las características basado en su frecuencia
     */
    private suspend fun actualizarPorcentajesCaracteristicas() {
        try {
            val caracteristicasRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)
                .collection(COLLECTION_CARACTERISTICAS)
                .get()
                .await()
            
            var totalUsos = 0L
            
            // Calcular total de usos
            for (doc in caracteristicasRef.documents) {
                totalUsos += doc.getLong("frecuencia") ?: 0
            }
            
            // Actualizar porcentajes
            for (doc in caracteristicasRef.documents) {
                val frecuencia = doc.getLong("frecuencia") ?: 0
                val porcentaje = if (totalUsos > 0) (frecuencia.toDouble() / totalUsos) * 100 else 0.0
                
                doc.reference.update("porcentaje", porcentaje).await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar porcentajes de características")
        }
    }
    
    /**
     * Genera datos ficticios para pruebas y demostración
     * 
     * @return [Result] indicando si se han generado correctamente los datos
     */
    suspend fun generarDatosFicticios(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val estadisticasRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)
            
            val estadisticasData = hashMapOf(
                "usuarios_activos" to 127L,
                "usuarios_activos_semana" to 85L,
                "usuarios_activos_trimestre" to 156L,
                "usuarios_activos_anio" to 203L,
                
                "sesiones_promedio" to 4.3,
                "sesiones_promedio_semana" to 3.8,
                "sesiones_promedio_trimestre" to 5.2,
                "sesiones_promedio_anio" to 6.7,
                
                "tiempo_promedio_sesion" to "12 min",
                "tiempo_promedio_sesion_semana" to "10 min",
                "tiempo_promedio_sesion_trimestre" to "15 min",
                "tiempo_promedio_sesion_anio" to "18 min",
                
                "ultima_actualizacion" to Timestamp.now()
            )
            
            estadisticasRef.set(estadisticasData).await()
            
            val caracteristicasRef = estadisticasRef.collection(COLLECTION_CARACTERISTICAS)
            
            // Eliminar datos anteriores si existen
            val caracteristicasExistentes = caracteristicasRef.get().await()
            for (doc in caracteristicasExistentes.documents) {
                doc.reference.delete().await()
            }
            
            // Crear datos ficticios de características
            val caracteristicas = listOf(
                mapOf("nombre" to "Gestión de centros", "frecuencia" to 245L, "porcentaje" to 28.5),
                mapOf("nombre" to "Comunicados", "frecuencia" to 186L, "porcentaje" to 21.7),
                mapOf("nombre" to "Calendario", "frecuencia" to 157L, "porcentaje" to 18.3),
                mapOf("nombre" to "Gestión de profesores", "frecuencia" to 132L, "porcentaje" to 15.4),
                mapOf("nombre" to "Gestión de clases", "frecuencia" to 89L, "porcentaje" to 10.4),
                mapOf("nombre" to "Reportes", "frecuencia" to 49L, "porcentaje" to 5.7)
            )
            
            for (caracteristica in caracteristicas) {
                caracteristicasRef.add(caracteristica).await()
            }
            
            Timber.d("Datos ficticios de estadísticas creados correctamente")
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al generar datos ficticios de estadísticas")
            return@withContext Result.Error(e)
        }
    }
} 