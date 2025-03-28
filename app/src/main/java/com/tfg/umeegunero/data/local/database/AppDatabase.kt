package com.tfg.umeegunero.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import com.tfg.umeegunero.data.local.utils.Converters

/**
 * Clase principal de la base de datos de la aplicación utilizando Room.
 * 
 * Esta clase define las entidades que forman parte de la base de datos,
 * así como los DAOs disponibles para acceder a ella.
 *
 * @author Estudiante 2º DAM
 */
@Database(
    entities = [RegistroActividadEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO para los registros de actividad.
     */
    abstract fun registroActividadDao(): RegistroActividadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos siguiendo el patrón Singleton.
         * Si la instancia ya existe, la devuelve; de lo contrario, crea una nueva.
         *
         * @param context El contexto de la aplicación
         * @return Una instancia de AppDatabase
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ume_egunero_database"
                )
                .fallbackToDestructiveMigration() // En caso de cambio de versión, recrea la BD
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 