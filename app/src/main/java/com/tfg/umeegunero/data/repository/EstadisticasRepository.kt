package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las estadísticas generales de la aplicación en Firestore.
 *
 * Esta clase permite obtener información básica sobre el uso de la aplicación, como el número de usuarios activos,
 * sesiones promedio y tiempo promedio de uso, si estos datos siguen siendo relevantes para la app.
 *
 * Todas las funciones relacionadas con reportes de uso y características han sido eliminadas.
 */
@Singleton
class EstadisticasRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ESTADISTICAS = "estadisticas"
        private const val DOC_USO_APLICACION = "uso_aplicacion"
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
} 