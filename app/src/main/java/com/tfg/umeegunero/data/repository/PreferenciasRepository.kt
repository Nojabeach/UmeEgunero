package com.tfg.umeegunero.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tfg.umeegunero.data.model.TemaPref
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Repositorio para gestionar las preferencias del usuario mediante DataStore
 */
@Singleton
class PreferenciasRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val temaKey = stringPreferencesKey("tema_preferencia")
    private val tokenFcmKey = stringPreferencesKey("fcm_token")
    private val notificacionesTareasKey = booleanPreferencesKey("notificaciones_tareas")
    private val notificacionesGeneralKey = booleanPreferencesKey("notificaciones_general")

    /**
     * Obtiene el tema actual de la aplicación
     */
    val temaPreferencia: Flow<TemaPref> = context.dataStore.data
        .map { preferences ->
            try {
                TemaPref.valueOf(preferences[temaKey] ?: TemaPref.SYSTEM.name)
            } catch (e: Exception) {
                TemaPref.SYSTEM
            }
        }

    /**
     * Guarda el tema seleccionado
     */
    suspend fun setTemaPreferencia(tema: TemaPref) {
        context.dataStore.edit { preferences ->
            preferences[temaKey] = tema.name
        }
    }
    
    /**
     * Guarda el token FCM del dispositivo
     */
    suspend fun guardarFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenFcmKey] = token
        }
    }
    
    /**
     * Obtiene el token FCM guardado
     */
    val fcmToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[tokenFcmKey] ?: ""
        }
    
    /**
     * Configura las preferencias de notificaciones para tareas
     */
    suspend fun setNotificacionesTareas(habilitadas: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificacionesTareasKey] = habilitadas
        }
    }
    
    /**
     * Obtiene la configuración de notificaciones para tareas
     */
    val notificacionesTareasFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[notificacionesTareasKey] ?: true // Habilitado por defecto
        }
    
    /**
     * Alias para compatibilidad con código existente
     */
    val notificacionesTareasHabilitadas: Flow<Boolean> = notificacionesTareasFlow
    
    /**
     * Configura las preferencias de notificaciones generales
     */
    suspend fun setNotificacionesGeneral(habilitadas: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificacionesGeneralKey] = habilitadas
        }
    }
    
    /**
     * Obtiene la configuración de notificaciones generales
     */
    val notificacionesGeneralFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[notificacionesGeneralKey] ?: true // Habilitado por defecto
        }
    
    /**
     * Alias para compatibilidad con código existente
     */
    val notificacionesGeneralHabilitadas: Flow<Boolean> = notificacionesGeneralFlow
} 