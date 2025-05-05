package com.tfg.umeegunero.util.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para encriptar y desencriptar datos sensibles en la base de datos.
 * 
 * Esta clase proporciona métodos para proteger información confidencial
 * antes de almacenarla en Firebase Firestore y desencriptarla cuando
 * es recuperada. Utiliza AES-GCM con claves almacenadas en Android Keystore.
 * 
 * ## Características principales:
 * - Encriptación AES-GCM de 256 bits
 * - Almacenamiento seguro de claves en Android Keystore
 * - Generación de IV único para cada encriptación
 * - Compatibilidad con modelo asíncrono de Kotlin
 */
@Singleton
class DatabaseEncryptionUtil @Inject constructor() {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DATABASE_ENCRYPTION_KEY = "database_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
        
        // Campos que siempre deben ser encriptados
        val SENSITIVE_FIELDS = setOf(
            "dni",
            "email",
            "telefono",
            "direccion",
            "numeroSS", // Número Seguridad Social
            "condicionesMedicas",
            "codigoAcceso"
        )
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        // Asegurar que la clave existe
        if (!keyStore.containsAlias(DATABASE_ENCRYPTION_KEY)) {
            generateEncryptionKey()
        }
    }
    
    /**
     * Genera una clave de encriptación AES-256 y la almacena en Android Keystore.
     */
    private fun generateEncryptionKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                DATABASE_ENCRYPTION_KEY,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            
            Timber.d("Clave de encriptación generada correctamente")
        } catch (e: Exception) {
            Timber.e(e, "Error generando clave de encriptación")
        }
    }
    
    /**
     * Encripta una cadena de texto.
     * 
     * @param plaintext Texto a encriptar
     * @return Texto encriptado codificado en Base64, o null si ocurre un error
     */
    suspend fun encrypt(plaintext: String): String? = withContext(Dispatchers.Default) {
        if (plaintext.isBlank()) return@withContext plaintext
        
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = keyStore.getKey(DATABASE_ENCRYPTION_KEY, null) as SecretKey
            
            // Generar IV aleatorio
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            // Inicializar cipher para encriptación
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            
            // Encriptar los datos
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combinar IV y datos encriptados
            val combined = iv + encryptedBytes
            
            // Codificar en Base64 para almacenamiento
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e(e, "Error encriptando datos")
            null
        }
    }
    
    /**
     * Desencripta una cadena de texto.
     * 
     * @param encryptedText Texto encriptado codificado en Base64
     * @return Texto original desencriptado, o null si ocurre un error
     */
    suspend fun decrypt(encryptedText: String): String? = withContext(Dispatchers.Default) {
        if (encryptedText.isBlank()) return@withContext encryptedText
        
        try {
            // Decodificar Base64
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Extraer IV y datos encriptados
            val iv = encryptedData.sliceArray(0 until IV_LENGTH)
            val encrypted = encryptedData.sliceArray(IV_LENGTH until encryptedData.size)
            
            // Configurar cipher para descifrado
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            val secretKey = keyStore.getKey(DATABASE_ENCRYPTION_KEY, null) as SecretKey
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            // Desencriptar y convertir a String
            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Error desencriptando datos")
            null
        }
    }
    
    /**
     * Determina si un campo específico debe ser encriptado.
     * 
     * @param fieldName Nombre del campo
     * @return true si el campo debe ser encriptado
     */
    fun shouldEncryptField(fieldName: String): Boolean {
        return fieldName.lowercase() in SENSITIVE_FIELDS
    }
    
    /**
     * Procesa un mapa de datos para encriptar campos sensibles.
     * 
     * @param data Mapa con los datos a procesar
     * @return Mapa con los campos sensibles encriptados
     */
    suspend fun encryptSensitiveData(data: Map<String, Any?>): Map<String, Any?> {
        val result = data.toMutableMap()
        
        for ((key, value) in data) {
            if (value is String && shouldEncryptField(key)) {
                encrypt(value)?.let { encryptedValue ->
                    result[key] = encryptedValue
                }
            }
        }
        
        return result
    }
    
    /**
     * Procesa un mapa de datos para desencriptar campos sensibles.
     * 
     * @param data Mapa con los datos a procesar
     * @return Mapa con los campos sensibles desencriptados
     */
    suspend fun decryptSensitiveData(data: Map<String, Any?>): Map<String, Any?> {
        val result = data.toMutableMap()
        
        for ((key, value) in data) {
            if (value is String && shouldEncryptField(key)) {
                decrypt(value)?.let { decryptedValue ->
                    result[key] = decryptedValue
                }
            }
        }
        
        return result
    }
} 