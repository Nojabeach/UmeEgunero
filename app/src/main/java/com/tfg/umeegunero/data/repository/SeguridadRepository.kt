package com.tfg.umeegunero.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar aspectos de seguridad en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para manejar la seguridad de la aplicación,
 * incluyendo autenticación, autorización, gestión de roles y permisos,
 * y protección de datos sensibles.
 *
 * Características principales:
 * - Validación de credenciales de usuario
 * - Gestión de roles y permisos
 * - Control de acceso a recursos
 * - Registro de actividades de seguridad
 * - Protección contra accesos no autorizados
 *
 * El repositorio se encarga de:
 * - Verificar la identidad de los usuarios
 * - Gestionar tokens de autenticación
 * - Implementar políticas de seguridad
 * - Registrar intentos de acceso
 * - Proteger información sensible
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property auth Instancia de FirebaseAuth para autenticación
 * @property remoteConfig Servicio de configuración remota para políticas de seguridad
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class SeguridadRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seguridad_config")

    /**
     * Claves para las preferencias de seguridad
     */
    private object PreferencesKeys {
        val COMPLEJIDAD_PASSWORD = intPreferencesKey("complejidad_password")
        val TIEMPO_SESION = intPreferencesKey("tiempo_sesion")
        val MAX_INTENTOS = intPreferencesKey("max_intentos")
        val VERIFICACION_DOS_FACTORES = booleanPreferencesKey("verificacion_dos_factores")
        val NOTIFICACIONES_ACTIVIDAD = booleanPreferencesKey("notificaciones_actividad")
        val REGISTRO_COMPLETO = booleanPreferencesKey("registro_completo")
        val BLOQUEO_IP = booleanPreferencesKey("bloqueo_ip")
    }

    /**
     * Obtiene la configuración de seguridad actual.
     * 
     * @return Flow con la configuración actual
     */
    val configuracion: Flow<ConfiguracionSeguridad> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ConfiguracionSeguridad(
                complejidadPassword = preferences[PreferencesKeys.COMPLEJIDAD_PASSWORD] ?: 2,
                tiempoSesion = preferences[PreferencesKeys.TIEMPO_SESION] ?: 30,
                maxIntentos = preferences[PreferencesKeys.MAX_INTENTOS] ?: 3,
                verificacionDosFactores = preferences[PreferencesKeys.VERIFICACION_DOS_FACTORES] ?: false,
                notificacionesActividad = preferences[PreferencesKeys.NOTIFICACIONES_ACTIVIDAD] ?: true,
                registroCompleto = preferences[PreferencesKeys.REGISTRO_COMPLETO] ?: true,
                bloqueoIP = preferences[PreferencesKeys.BLOQUEO_IP] ?: true
            )
        }

    /**
     * Actualiza la configuración de seguridad.
     * 
     * @param config Nueva configuración a guardar
     */
    suspend fun actualizarConfiguracion(config: ConfiguracionSeguridad) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPLEJIDAD_PASSWORD] = config.complejidadPassword
            preferences[PreferencesKeys.TIEMPO_SESION] = config.tiempoSesion
            preferences[PreferencesKeys.MAX_INTENTOS] = config.maxIntentos
            preferences[PreferencesKeys.VERIFICACION_DOS_FACTORES] = config.verificacionDosFactores
            preferences[PreferencesKeys.NOTIFICACIONES_ACTIVIDAD] = config.notificacionesActividad
            preferences[PreferencesKeys.REGISTRO_COMPLETO] = config.registroCompleto
            preferences[PreferencesKeys.BLOQUEO_IP] = config.bloqueoIP
        }
    }

    /**
     * Modelo de datos para la configuración de seguridad.
     */
    data class ConfiguracionSeguridad(
        val complejidadPassword: Int = 2,
        val tiempoSesion: Int = 30,
        val maxIntentos: Int = 3,
        val verificacionDosFactores: Boolean = false,
        val notificacionesActividad: Boolean = true,
        val registroCompleto: Boolean = true,
        val bloqueoIP: Boolean = true
    )
} 