package com.tfg.umeegunero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tfg.umeegunero.data.dao.OperacionPendienteDao
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.local.dao.PendingSyncDao
import com.tfg.umeegunero.data.local.dao.PreferenciasDao
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.local.entity.PendingSyncEntity
import com.tfg.umeegunero.data.local.entity.PreferenciasEntity
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import com.tfg.umeegunero.data.local.util.Converters
import com.tfg.umeegunero.data.model.OperacionPendiente

/**
 * Base de datos local de la aplicación UmeEgunero.
 * 
 * Esta clase es un singleton que proporciona acceso a la base de datos.
 * Incluye entidades, DAOs y conversores de tipos necesarios para toda la aplicación.
 */
@Database(
    entities = [
        ChatMensajeEntity::class,
        ConversacionEntity::class,
        PreferenciasEntity::class,
        PendingSyncEntity::class,
        RegistroActividadEntity::class,
        OperacionPendiente::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para la entidad ChatMensajeEntity
     */
    abstract fun chatMensajeDao(): ChatMensajeDao
    
    /**
     * DAO para la entidad ConversacionEntity
     */
    abstract fun conversacionDao(): ConversacionDao
    
    /**
     * DAO para la entidad PreferenciasEntity
     */
    abstract fun preferenciasDao(): PreferenciasDao
    
    /**
     * DAO para la entidad PendingSyncEntity
     */
    abstract fun pendingSyncDao(): PendingSyncDao

    /**
     * DAO para la entidad RegistroActividadEntity
     */
    abstract fun registroActividadDao(): RegistroActividadDao
    
    /**
     * DAO para la entidad OperacionPendiente
     */
    abstract fun operacionPendienteDao(): OperacionPendienteDao
    
    companion object {
        const val DATABASE_NAME = "umeegunero_db"
        
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
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 