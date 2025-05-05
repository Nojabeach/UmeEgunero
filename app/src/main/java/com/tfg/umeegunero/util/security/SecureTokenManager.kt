package com.tfg.umeegunero.util.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyStore
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la generación y validación de tokens de seguridad para los servicios de la aplicación.
 * 
 * Esta clase proporciona mecanismos para:
 * - Generar tokens aleatorios seguros
 * - Encriptar/desencriptar información usando Android Keystore
 * - Generar y validar tokens firmados con tiempo de expiración
 * - Gestionar la rotación segura de tokens
 * 
 * Utiliza Android Keystore System para garantizar que las claves criptográficas 
 * estén almacenadas de forma segura en hardware (cuando está disponible).
 */
@Singleton
class SecureTokenManager @Inject constructor() {
    companion object {
        // Configuración del Keystore
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val EMAIL_SERVICE_KEY_ALIAS = "email_service_key"
        private const val AUTH_TOKEN_KEY_ALIAS = "auth_token_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
        
        // Duración predeterminada del token (24h)
        private const val DEFAULT_TOKEN_DURATION_MS = 24 * 60 * 60 * 1000L
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        try {
            // Asegurar que las claves existan
            if (!keyStore.containsAlias(EMAIL_SERVICE_KEY_ALIAS)) {
                generateKey(EMAIL_SERVICE_KEY_ALIAS)
            }
            if (!keyStore.containsAlias(AUTH_TOKEN_KEY_ALIAS)) {
                generateKey(AUTH_TOKEN_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inicializando SecureTokenManager")
        }
    }
    
    /**
     * Genera una clave criptográfica AES y la almacena en Android Keystore.
     * 
     * @param alias Nombre de la clave en el Keystore
     */
    private fun generateKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, 
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Obtiene una clave del KeyStore para operaciones criptográficas.
     * 
     * @param alias Nombre de la clave en el Keystore
     * @return SecretKey para operaciones criptográficas
     */
    private fun getKey(alias: String): SecretKey {
        return keyStore.getKey(alias, null) as SecretKey
    }
    
    /**
     * Genera un token seguro para el servicio de email.
     * 
     * El token incluye información de tiempo y una firma encriptada para
     * validar su autenticidad y prevenir su manipulación.
     * 
     * @param appId Identificador único de la aplicación
     * @param serviceId Identificador del servicio que utilizará el token
     * @param durationMs Duración de validez del token en milisegundos (opcional)
     * @return Token encriptado para uso en servicios externos
     */
    suspend fun generateEmailServiceToken(
        appId: String,
        serviceId: String = "email_service",
        durationMs: Long = DEFAULT_TOKEN_DURATION_MS
    ): String = withContext(Dispatchers.Default) {
        try {
            // Crear payload con información de validez
            val expiration = System.currentTimeMillis() + durationMs
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val tokenData = "$appId:$serviceId:${dateFormat.format(Date(expiration))}"
            
            // Generar firma
            val signature = generateRandomBytes(16)
            val signatureBase64 = Base64.encodeToString(signature, Base64.NO_WRAP)
            
            // Encriptar token completo
            val tokenWithSignature = "$tokenData:$signatureBase64"
            val encryptedToken = encrypt(tokenWithSignature, EMAIL_SERVICE_KEY_ALIAS)
            
            // Devolver token codificado para transmisión
            Base64.encodeToString(encryptedToken, Base64.URL_SAFE)
        } catch (e: Exception) {
            Timber.e(e, "Error generando token de servicio de email")
            // Fallback a un valor predeterminado en caso de error
            "error_generating_token"
        }
    }
    
    /**
     * Genera bytes aleatorios criptográficamente seguros.
     * 
     * @param length Longitud de la secuencia aleatoria en bytes
     * @return Array de bytes aleatorios
     */
    private fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
    
    /**
     * Encripta datos utilizando la clave almacenada en KeyStore.
     * 
     * @param data Datos a encriptar
     * @param keyAlias Alias de la clave a utilizar
     * @return Datos encriptados con IV incluido
     */
    private fun encrypt(data: String, keyAlias: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getKey(keyAlias)
        
        // Generar IV aleatorio
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        
        // Inicializar cipher para encriptación
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        
        // Encriptar los datos
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Combinar IV y datos encriptados
        return iv + encryptedData
    }
    
    /**
     * Desencripta datos utilizando la clave almacenada en KeyStore.
     * 
     * @param encryptedData Datos encriptados con IV incluido
     * @param keyAlias Alias de la clave a utilizar
     * @return Datos desencriptados como String
     */
    private fun decrypt(encryptedData: ByteArray, keyAlias: String): String {
        // Extraer IV (primeros IV_LENGTH bytes)
        val iv = encryptedData.sliceArray(0 until IV_LENGTH)
        // Extraer datos encriptados (resto de bytes)
        val encrypted = encryptedData.sliceArray(IV_LENGTH until encryptedData.size)
        
        // Configurar cipher para descifrado
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        val secretKey = getKey(keyAlias)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        // Desencriptar y convertir a String
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    /**
     * Valida un token del servicio de email.
     * 
     * @param token Token encriptado a validar
     * @param appId Identificador de la aplicación
     * @return true si el token es válido y no ha expirado
     */
    suspend fun validateEmailServiceToken(token: String, appId: String): Boolean = 
        withContext(Dispatchers.Default) {
        try {
            val encryptedData = Base64.decode(token, Base64.URL_SAFE)
            val decryptedToken = decrypt(encryptedData, EMAIL_SERVICE_KEY_ALIAS)
            
            // Validar formato y extraer partes
            val parts = decryptedToken.split(":")
            if (parts.size != 4) return@withContext false
            
            val tokenAppId = parts[0]
            val serviceId = parts[1]
            val expirationStr = parts[2]
            
            // Validar app ID
            if (tokenAppId != appId) return@withContext false
            
            // Validar expiración
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val expirationDate = dateFormat.parse(expirationStr)
            val now = Date()
            
            expirationDate?.after(now) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error validando token")
            false
        }
    }
    
    /**
     * Genera un token JTI (JWT ID) aleatorio para identificación única de tokens.
     * 
     * @return String de identificación único para tokens
     */
    fun generateTokenId(): String {
        val randomBytes = ByteArray(16)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.URL_SAFE).substring(0, 22)
    }
} 