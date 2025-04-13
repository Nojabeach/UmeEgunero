package com.tfg.umeegunero.data.repository

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.util.FirmaDigitalUtil
import com.tfg.umeegunero.util.FirestoreQueryUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para la gestión de comunicados en Firestore
 */
@Singleton
class ComunicadoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreQueryUtil: FirestoreQueryUtil
) {
    
    private val coleccionComunicados = firestore.collection("comunicados")
    
    /**
     * Obtiene todos los comunicados con paginación
     * 
     * @param limit Número máximo de documentos a obtener
     * @param lastDocument Último documento de la página anterior (para paginación)
     * @return Resultado con la lista de comunicados o error
     */
    suspend fun getComunicados(
        limit: Long = 20,
        lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    ): Resultado<List<Comunicado>> {
        return try {
            val query = firestoreQueryUtil.getOptimizedComunicadosQuery(limit, lastDocument)
            val snapshot = firestoreQueryUtil.getQueryWithCache(query)
            
            val comunicados = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comunicado::class.java)
            }
            
            Resultado.Exito(comunicados)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener comunicados")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Obtiene comunicados por tipo de usuario
     * 
     * @param tipoUsuario Tipo de usuario para filtrar
     * @param limit Número máximo de documentos a obtener
     * @return Resultado con la lista de comunicados o error
     */
    suspend fun getComunicadosByTipoUsuario(
        tipoUsuario: String,
        limit: Long = 50
    ): Resultado<List<Comunicado>> {
        return try {
            val query = firestoreQueryUtil.getComunicadosByTipoUsuarioQuery(tipoUsuario, limit)
            val snapshot = firestoreQueryUtil.getQueryWithCache(query)
            
            val comunicados = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comunicado::class.java)
            }
            
            Resultado.Exito(comunicados)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener comunicados por tipo de usuario: $tipoUsuario")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Obtiene un comunicado por su ID
     * 
     * @param id ID del comunicado
     * @return Resultado con el comunicado o error
     */
    suspend fun getComunicadoById(id: String): Resultado<Comunicado> {
        return try {
            val doc = coleccionComunicados.document(id).get().await()
            val comunicado = doc.toObject(Comunicado::class.java)
            
            if (comunicado != null) {
                Resultado.Exito(comunicado)
            } else {
                Resultado.Error("Comunicado no encontrado", Exception("Comunicado no encontrado"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener comunicado por ID: $id")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Crea un nuevo comunicado
     * 
     * @param comunicado Comunicado a crear
     * @return Resultado con el ID del comunicado creado o error
     */
    suspend fun crearComunicado(comunicado: Comunicado): Resultado<String> {
        return try {
            val docRef = coleccionComunicados.add(comunicado).await()
            
            // Invalidar caché para consultas de comunicados
            firestoreQueryUtil.invalidateCache(
                firestoreQueryUtil.getOptimizedComunicadosQuery()
            )
            
            Resultado.Exito(docRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear comunicado")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Actualiza un comunicado existente
     * 
     * @param comunicado Datos actualizados del comunicado
     * @return Resultado con éxito o error
     */
    suspend fun actualizarComunicado(comunicado: Comunicado): Resultado<Unit> {
        return try {
            // Aseguramos que el ID no sea nulo
            val id = comunicado.id ?: return Resultado.Error("El comunicado no tiene ID", Exception("El comunicado no tiene ID"))
            
            coleccionComunicados.document(id).set(comunicado).await()
            
            // Invalidar caché para consultas de comunicados
            firestoreQueryUtil.invalidateCache(
                firestoreQueryUtil.getOptimizedComunicadosQuery()
            )
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar comunicado: ${comunicado.id}")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Elimina un comunicado
     * 
     * @param id ID del comunicado
     * @return Resultado con éxito o error
     */
    suspend fun eliminarComunicado(id: String): Resultado<Unit> {
        return try {
            coleccionComunicados.document(id).delete().await()
            
            // Invalidar caché para consultas de comunicados
            firestoreQueryUtil.invalidateCache(
                firestoreQueryUtil.getOptimizedComunicadosQuery()
            )
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar comunicado: $id")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Archiva un comunicado
     * 
     * @param id ID del comunicado
     * @return Resultado con éxito o error
     */
    suspend fun archivarComunicado(id: String): Resultado<Unit> {
        return try {
            coleccionComunicados.document(id)
                .update("archivado", true, "fechaArchivado", Timestamp.now())
                .await()
            
            // Invalidar caché para consultas de comunicados
            firestoreQueryUtil.invalidateCache(
                firestoreQueryUtil.getOptimizedComunicadosQuery()
            )
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al archivar comunicado: $id")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Registra la lectura de un comunicado por un usuario
     * 
     * @param comunicadoId ID del comunicado
     * @param usuarioId ID del usuario
     * @return Resultado con éxito o error
     */
    suspend fun registrarLectura(comunicadoId: String, usuarioId: String): Resultado<Unit> {
        return try {
            coleccionComunicados.document(comunicadoId)
                .update("lecturas.$usuarioId", Timestamp.now())
                .await()
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar lectura de comunicado: $comunicadoId por usuario: $usuarioId")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Confirma la lectura de un comunicado por un usuario
     * 
     * @param comunicadoId ID del comunicado
     * @param usuarioId ID del usuario
     * @return Resultado con éxito o error
     */
    suspend fun confirmarLectura(comunicadoId: String, usuarioId: String): Resultado<Unit> {
        return try {
            coleccionComunicados.document(comunicadoId)
                .update("confirmacionesLectura.$usuarioId", Timestamp.now())
                .await()
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al confirmar lectura de comunicado: $comunicadoId por usuario: $usuarioId")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Añade una firma digital a un comunicado
     * 
     * @param comunicadoId ID del comunicado
     * @param firmaBase64 Firma en formato Base64
     * @param usuarioId ID del usuario que firma (opcional)
     * @param timestamp Timestamp de la firma (opcional, por defecto es el momento actual)
     * @return Resultado con el comunicado actualizado o error
     */
    suspend fun añadirFirmaDigital(
        comunicadoId: String,
        firmaBase64: String,
        usuarioId: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Resultado<Comunicado> = withContext(Dispatchers.IO) {
        return@withContext try {
            val timestampFirebase = Timestamp(timestamp / 1000, ((timestamp % 1000) * 1000000).toInt())
            val usuarioFirmante = usuarioId ?: "sistema"
            
            // Guardar la firma en Firebase Storage usando FirmaDigitalUtil
            val firmaUrl = withContext(Dispatchers.IO) {
                FirmaDigitalUtil.guardarFirmaEnStorage(
                    bitmap = FirmaDigitalUtil.base64ABitmap(firmaBase64),
                    usuarioId = usuarioFirmante,
                    documentoId = comunicadoId
                )
            } ?: throw Exception("Error al guardar firma en Storage")
            
            // Generar hash de verificación
            val firmaHash = FirmaDigitalUtil.generarHashFirma(
                base64 = firmaBase64,
                usuarioId = usuarioFirmante,
                documentoId = comunicadoId,
                timestamp = timestamp
            )
            
            val actualizacion = mapOf(
                "firmaDigital" to firmaBase64,
                "firmaDigitalUrl" to firmaUrl,
                "firmaDigitalHash" to firmaHash,
                "firmaTimestamp" to timestampFirebase
            )
            
            coleccionComunicados.document(comunicadoId)
                .update(actualizacion)
                .await()
            
            // Obtener el comunicado actualizado
            val comunicado = coleccionComunicados.document(comunicadoId)
                .get()
                .await()
                .toObject(Comunicado::class.java)
            
            if (comunicado != null) {
                Resultado.Exito(comunicado)
            } else {
                Resultado.Error("No se pudo obtener el comunicado actualizado", Exception("No se pudo obtener el comunicado actualizado"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al añadir firma digital al comunicado $comunicadoId")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Añade una firma de destinatario a un comunicado
     * 
     * @param comunicadoId ID del comunicado
     * @param usuarioId ID del usuario que firma
     * @param firmaUrl URL de la firma en Firebase Storage
     * @return Resultado con el comunicado actualizado o error
     */
    suspend fun añadirFirmaDestinatario(
        comunicadoId: String,
        usuarioId: String,
        firmaUrl: String
    ): Resultado<Comunicado> {
        return try {
            val timestamp = Timestamp.now()
            
            // Obtener el comunicado actual
            val comunicado = coleccionComunicados.document(comunicadoId)
                .get()
                .await()
                .toObject(Comunicado::class.java)
            
            if (comunicado != null) {
                // Actualizar las firmas de destinatarios
                val firmasActualizadas = comunicado.firmasDestinatarios.toMutableMap()
                firmasActualizadas[usuarioId] = mapOf("url" to firmaUrl)
                
                val actualizacion = mapOf(
                    "firmasDestinatarios" to firmasActualizadas,
                    "ultimaActualizacion" to timestamp
                )
                
                coleccionComunicados.document(comunicadoId)
                    .update(actualizacion)
                    .await()
                
                // Obtener el comunicado actualizado
                val comunicadoActualizado = coleccionComunicados.document(comunicadoId)
                    .get()
                    .await()
                    .toObject(Comunicado::class.java)
                
                if (comunicadoActualizado != null) {
                    Resultado.Exito(comunicadoActualizado)
                } else {
                    Resultado.Error("No se pudo obtener el comunicado actualizado", Exception("No se pudo obtener el comunicado actualizado"))
                }
            } else {
                Resultado.Error("No se encontró el comunicado", Exception("No se encontró el comunicado"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al añadir firma de destinatario al comunicado $comunicadoId")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Obtiene estadísticas de lectura de un comunicado
     * 
     * @param comunicadoId ID del comunicado
     * @return Resultado con las estadísticas o error
     */
    suspend fun getEstadisticasLectura(comunicadoId: String): Resultado<Map<String, Any>> {
        return try {
            val comunicado = coleccionComunicados.document(comunicadoId)
                .get()
                .await()
                .toObject(Comunicado::class.java)
            
            if (comunicado != null) {
                // Usar el tamaño del mapa o lista correspondiente
                val tiposDestinatarios = comunicado.tiposDestinatarios.size
                val destinatariosCount = if (comunicado.destinatarios is String) 1 else (comunicado.destinatarios as? List<*>)?.size ?: 0
                val totalDestinatarios = if (tiposDestinatarios > 0) tiposDestinatarios else destinatariosCount
                
                val totalLecturas = comunicado.lecturas.size
                val totalConfirmaciones = comunicado.confirmacionesLectura?.size ?: 0
                
                val estadisticas = mapOf(
                    "totalDestinatarios" to totalDestinatarios,
                    "totalLecturas" to totalLecturas,
                    "totalConfirmaciones" to totalConfirmaciones,
                    "porcentajeLecturas" to (if (totalDestinatarios > 0) (totalLecturas.toFloat() / totalDestinatarios.toFloat() * 100) else 0f),
                    "porcentajeConfirmaciones" to (if (totalDestinatarios > 0) (totalConfirmaciones.toFloat() / totalDestinatarios.toFloat() * 100) else 0f)
                )
                
                Resultado.Exito(estadisticas)
            } else {
                Resultado.Error("Comunicado no encontrado", Exception("Comunicado no encontrado"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener estadísticas de lectura del comunicado: $comunicadoId")
            Resultado.Error(e.message, e)
        }
    }
    
    /**
     * Obtiene comunicados que requieren firma por parte de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param limit Número máximo de documentos a obtener
     * @return Resultado con la lista de comunicados o error
     */
    suspend fun getComunicadosRequierenFirma(
        usuarioId: String,
        limit: Long = 20
    ): Resultado<List<Comunicado>> {
        return try {
            val query = firestoreQueryUtil.getComunicadosRequierenFirmaQuery(usuarioId, limit)
            val snapshot = firestoreQueryUtil.getQueryWithCache(query)
            
            val comunicados = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comunicado::class.java)
            }
            
            Resultado.Exito(comunicados)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener comunicados que requieren firma para usuario: $usuarioId")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Añade una firma digital completa al comunicado
     *
     * @param comunicadoId ID del comunicado a firmar
     * @param usuarioId ID del usuario que firma
     * @param bitmapFirma Bitmap de la firma
     * @return Resultado de la operación
     */
    suspend fun firmarComunicado(
        comunicadoId: String,
        usuarioId: String,
        bitmapFirma: Bitmap
    ): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Convertir la firma a Base64
            val firmaBase64 = FirmaDigitalUtil.bitmapABase64(bitmapFirma)
            
            // 2. Guardar la firma en Firebase Storage
            val firmaUrl = FirmaDigitalUtil.guardarFirmaEnStorage(
                bitmap = bitmapFirma,
                usuarioId = usuarioId,
                documentoId = comunicadoId
            ) ?: throw Exception("Error al guardar firma en Storage")
            
            // 3. Generar timestamp
            val timestamp = Timestamp.now()
            val timestampLong = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
            
            // 4. Generar hash de la firma
            val firmaHash = FirmaDigitalUtil.generarHashFirma(
                base64 = firmaBase64,
                usuarioId = usuarioId,
                documentoId = comunicadoId,
                timestamp = timestampLong
            )
            
            // 5. Actualizar el documento en Firestore
            coleccionComunicados.document(comunicadoId)
                .update(
                    mapOf(
                        "firmaDigital" to firmaBase64,
                        "firmaDigitalUrl" to firmaUrl,
                        "firmaDigitalHash" to firmaHash,
                        "firmaTimestamp" to timestamp
                    )
                ).await()
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al firmar comunicado")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Verifica la autenticidad de la firma de un comunicado
     *
     * @param comunicadoId ID del comunicado a verificar
     * @return Resultado de la verificación (true si es auténtica)
     */
    suspend fun verificarFirmaComunicado(comunicadoId: String): Resultado<Boolean> = withContext(Dispatchers.IO) {
        try {
            val comunicadoResult = getComunicadoById(comunicadoId)
            val comunicado = when (comunicadoResult) {
                is Resultado.Exito -> comunicadoResult.datos
                is Resultado.Error -> throw comunicadoResult.excepcion ?: Exception("Error desconocido")
                is Resultado.Cargando -> throw Exception("Cargando datos...")
            }
            
            // Si el comunicado no tiene firma digital, retornamos false
            if (comunicado.firmaDigitalUrl.isNullOrEmpty() || 
                comunicado.firmaDigitalHash.isNullOrEmpty() || 
                comunicado.firmaTimestamp == null) {
                return@withContext Resultado.Exito(false)
            }
            
            // Verificamos la firma digital
            val esAutentica = FirmaDigitalUtil.verificarFirma(
                firmaHash = comunicado.firmaDigitalHash ?: "",
                base64 = comunicado.firmaDigital ?: "",
                documentoId = comunicado.id ?: "",
                usuarioId = comunicado.creadoPor,
                timestamp = comunicado.firmaTimestamp?.seconds?.times(1000)?.plus(comunicado.firmaTimestamp?.nanoseconds?.div(1000000) ?: 0) ?: 0
            )
            
            Resultado.Exito(esAutentica)
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar firma del comunicado")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Registra la firma digital de un destinatario en un comunicado
     *
     * @param comunicadoId ID del comunicado
     * @param usuarioId ID del usuario destinatario
     * @param bitmapFirma Bitmap de la firma
     * @return Resultado de la operación
     */
    suspend fun firmarComunicadoComoDestinatario(
        comunicadoId: String,
        usuarioId: String,
        bitmapFirma: Bitmap
    ): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Convertir la firma a Base64
            val firmaBase64 = FirmaDigitalUtil.bitmapABase64(bitmapFirma)
            
            // 2. Guardar la firma en Firebase Storage
            val firmaUrl = FirmaDigitalUtil.guardarFirmaEnStorage(
                bitmap = bitmapFirma,
                usuarioId = usuarioId,
                documentoId = comunicadoId
            ) ?: throw Exception("Error al guardar firma en Storage")
            
            // 3. Generar timestamp
            val timestamp = Timestamp.now()
            val timestampLong = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
            
            // 4. Generar hash de la firma
            val firmaHash = FirmaDigitalUtil.generarHashFirma(
                base64 = firmaBase64,
                usuarioId = usuarioId,
                documentoId = comunicadoId,
                timestamp = timestampLong
            )
            
            // 5. Crear el objeto de información de la firma
            val firmaInfo = mapOf(
                "base64" to firmaBase64,
                "url" to firmaUrl,
                "hash" to firmaHash,
                "timestamp" to timestamp
            )
            
            // 6. Actualizar el documento en Firestore
            coleccionComunicados.document(comunicadoId)
                .update("firmasDestinatarios.$usuarioId", firmaInfo)
                .await()
            
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al firmar comunicado como destinatario")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Verifica si un destinatario ha firmado un comunicado
     *
     * @param comunicadoId ID del comunicado
     * @param usuarioId ID del usuario destinatario
     * @return Resultado de la verificación (true si ha firmado)
     */
    suspend fun verificarFirmaDestinatario(
        comunicadoId: String,
        usuarioId: String
    ): Resultado<Boolean> = withContext(Dispatchers.IO) {
        try {
            val comunicadoResult = getComunicadoById(comunicadoId)
            val comunicado = when (comunicadoResult) {
                is Resultado.Exito -> comunicadoResult.datos
                is Resultado.Error -> throw comunicadoResult.excepcion ?: Exception("Error desconocido")
                is Resultado.Cargando -> throw Exception("Cargando datos...")
            }
            
            // Verificar si existe la firma del destinatario
            val haFirmado = comunicado.firmasDestinatarios.containsKey(usuarioId)
            
            Resultado.Exito(haFirmado)
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar firma del destinatario")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Obtiene todas las firmas digitales de los destinatarios de un comunicado
     *
     * @param comunicadoId ID del comunicado
     * @return Lista de pares <usuarioId, urlFirma>
     */
    suspend fun obtenerFirmasDestinatarios(
        comunicadoId: String
    ): Resultado<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val comunicadoResult = getComunicadoById(comunicadoId)
            val comunicado = when (comunicadoResult) {
                is Resultado.Exito -> comunicadoResult.datos
                is Resultado.Error -> throw comunicadoResult.excepcion ?: Exception("Error desconocido")
                is Resultado.Cargando -> throw Exception("Cargando datos...")
            }
            
            // Mapa para almacenar el ID del usuario y la URL de su firma
            val firmasUrls = mutableMapOf<String, String>()
            
            // Procesar cada entrada de firmas
            comunicado.firmasDestinatarios.forEach { (usuarioId, _) ->
                comunicado.firmasUrlsDestinatarios[usuarioId]?.let { url ->
                    firmasUrls[usuarioId] = url
                }
            }
            
            Resultado.Exito(firmasUrls)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener firmas de los destinatarios")
            Resultado.Error(e.message, e)
        }
    }

    /**
     * Añade una firma digital a un comunicado (compatibilidad con Result)
     * 
     * @param comunicadoId ID del comunicado
     * @param firmaBase64 Firma en formato Base64
     * @param usuarioId ID del usuario que firma (opcional)
     * @param timestamp Timestamp de la firma (opcional, por defecto es el momento actual)
     * @return Result con el comunicado actualizado o error
     */
    suspend fun addFirmaDigital(
        comunicadoId: String,
        firmaBase64: String,
        usuarioId: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): com.tfg.umeegunero.util.Result<Comunicado> {
        val resultado = añadirFirmaDigital(comunicadoId, firmaBase64, usuarioId, timestamp)
        return when (resultado) {
            is Resultado.Exito -> com.tfg.umeegunero.util.Result.Success(resultado.datos)
            is Resultado.Error -> com.tfg.umeegunero.util.Result.Error(resultado.excepcion)
            is Resultado.Cargando -> com.tfg.umeegunero.util.Result.Loading(resultado.datos)
        }
    }
} 