package com.tfg.umeegunero.util

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase que gestiona los errores de la aplicación de forma centralizada
 * Permite un manejo uniforme de errores y proporciona mensajes amigables para el usuario
 */
@Singleton
class ErrorHandler @Inject constructor() {

    /**
     * Procesa una excepción y devuelve un mensaje de error amigable para el usuario
     * @param exception La excepción a procesar
     * @return Mensaje de error amigable para el usuario
     */
    fun procesarError(exception: Exception): String {
        // Registrar el error en Timber para depuración
        Timber.e(exception, "Error en la aplicación")
        
        // Devolver un mensaje amigable según el tipo de error
        return when (exception) {
            // Errores de Firebase Authentication
            is FirebaseAuthException -> procesarErrorAuth(exception)
            
            // Errores de Firestore
            is FirebaseFirestoreException -> procesarErrorFirestore(exception)
            
            // Errores de red
            is FirebaseNetworkException -> "No se pudo conectar al servidor. Verifica tu conexión a internet."
            
            // Cualquier otro error
            else -> "Se produjo un error inesperado: ${exception.localizedMessage ?: "Error desconocido"}"
        }
    }
    
    /**
     * Procesa errores específicos de Firebase Authentication
     */
    private fun procesarErrorAuth(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_CREDENTIAL" -> "Las credenciales proporcionadas no son válidas."
            "ERROR_USER_DISABLED" -> "Esta cuenta de usuario ha sido deshabilitada."
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD" -> "El correo electrónico o la contraseña son incorrectos."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está registrado."
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres."
            "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar al servidor. Verifica tu conexión a internet."
            else -> "Error de autenticación: ${exception.localizedMessage ?: exception.errorCode}"
        }
    }
    
    /**
     * Procesa errores específicos de Firestore
     */
    private fun procesarErrorFirestore(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> "El servicio no está disponible actualmente. Intenta más tarde."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "No tienes permisos para realizar esta acción."
            FirebaseFirestoreException.Code.NOT_FOUND -> "No se encontró el documento solicitado."
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "El documento ya existe."
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Has excedido el límite de solicitudes. Intenta más tarde."
            FirebaseFirestoreException.Code.CANCELLED -> "La operación fue cancelada."
            else -> "Error al acceder a la base de datos: ${exception.localizedMessage ?: exception.code.name}"
        }
    }
} 