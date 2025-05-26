package com.tfg.umeegunero.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import com.tfg.umeegunero.util.Converters

/**
 * Definición de la base de datos Room para la aplicación UmeEgunero.
 * 
 * Esta clase es un singleton que proporciona acceso a la base de datos.
 * Incluye entidades, daos y conversores de tipos necesarios.
 */
@Database(
    entities = [
        RegistroActividadEntity::class,
        ChatMensajeEntity::class,
        ConversacionEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Proporciona acceso al DAO para los registros de actividad.
     */
    abstract fun registroActividadDao(): RegistroActividadDao
    
    /**
     * Proporciona acceso al DAO para los mensajes de chat.
     */
    abstract fun chatMensajeDao(): ChatMensajeDao
    
    /**
     * Proporciona acceso al DAO para las conversaciones.
     */
    abstract fun conversacionDao(): ConversacionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos.
         * Si no existe, la crea utilizando el patrón singleton.
         * 
         * @param context El contexto de la aplicación
         * @return Instancia de la base de datos
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umeegunero_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 