# Room en UmeEgunero

## Introducción

Este documento técnico detalla la implementación y uso de Room en la aplicación UmeEgunero, explicando su papel en la arquitectura de la aplicación, los componentes principales, y cómo se integra con Firebase Firestore para proporcionar una experiencia fluida incluso sin conexión.

## Propósito de Room en UmeEgunero

Room es una capa de abstracción sobre SQLite que permite el acceso a la base de datos local de manera robusta mientras aprovecha toda la potencia de SQLite. En UmeEgunero, Room se utiliza para:

1. **Soporte offline**: Permitir el funcionamiento de la aplicación sin conexión a internet
2. **Caché de datos**: Almacenar localmente los datos más utilizados para reducir llamadas a la red
3. **Sincronización en segundo plano**: Gestionar la sincronización de datos entre el dispositivo y Firebase Firestore
4. **Mejora de rendimiento**: Proporcionar acceso rápido a los datos más utilizados

## Arquitectura de Room en UmeEgunero

La implementación de Room en UmeEgunero sigue una arquitectura de tres capas:

1. **Entidades**: Clases que representan tablas en la base de datos
2. **DAOs (Data Access Objects)**: Interfaces que definen operaciones sobre la base de datos
3. **Base de datos**: Clase que define la configuración de la base de datos y proporciona acceso a los DAOs

### Diagrama de Arquitectura

```
┌──────────────────────────────────────────────────────────────────┐
│                       Repositorios                                │
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────┐ │
│  │RegistroDiarioRepo│    │ConversacionRepo  │    │MensajeRepo   │ │
│  └────────┬─────────┘    └────────┬─────────┘    └───────┬──────┘ │
└───────────┼──────────────────────┼─────────────────────┼──────────┘
            │                      │                     │
            ▼                      ▼                     ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Room Database (AppDatabase)                   │
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────┐ │
│  │RegistroActividad │    │ConversacionDao   │    │ChatMensajeDao│ │
│  │Dao               │    │                  │    │              │ │
│  └────────┬─────────┘    └────────┬─────────┘    └───────┬──────┘ │
│           │                       │                      │         │
│  ┌────────▼─────────┐    ┌────────▼─────────┐    ┌──────▼───────┐ │
│  │RegistroActividad │    │ConversacionEntity│    │ChatMensaje   │ │
│  │Entity            │    │                  │    │Entity        │ │
│  └──────────────────┘    └──────────────────┘    └──────────────┘ │
└──────────────────────────────────────────────────────────────────┘
            ▲                      ▲                     ▲
            │                      │                     │
            ▼                      ▼                     ▼
┌──────────────────────────────────────────────────────────────────┐
│                      Firebase Firestore                           │
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────┐ │
│  │registrosActividad│    │conversaciones    │    │mensajes      │ │
│  └──────────────────┘    └──────────────────┘    └──────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## Componentes Principales

### 1. Base de Datos (AppDatabase)

La clase `AppDatabase` es un singleton que define la base de datos Room:

```kotlin
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
    abstract fun registroActividadDao(): RegistroActividadDao
    abstract fun chatMensajeDao(): ChatMensajeDao
    abstract fun conversacionDao(): ConversacionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
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
```

### 2. Entidades

Las entidades principales en UmeEgunero son:

#### RegistroActividadEntity

Representa los registros diarios de actividad de los alumnos, almacenando información sobre comidas, siesta, necesidades fisiológicas, etc.

```kotlin
@Entity(tableName = "registros_actividad")
data class RegistroActividadEntity(
    @PrimaryKey
    val id: String,
    val alumnoId: String,
    val alumnoNombre: String = "",
    val claseId: String = "",
    val fechaTimestamp: Long,
    // ... otros campos
    val isSynced: Boolean = false
)
```

#### ConversacionEntity

Representa las conversaciones entre usuarios de la aplicación:

```kotlin
@Entity(tableName = "conversaciones")
data class ConversacionEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val participantesJson: String,
    val ultimoMensajeTimestamp: Long,
    val isSynced: Boolean = false
)
```

#### ChatMensajeEntity

Almacena los mensajes individuales dentro de las conversaciones:

```kotlin
@Entity(tableName = "mensajes")
data class ChatMensajeEntity(
    @PrimaryKey
    val id: String,
    val conversacionId: String,
    val emisorId: String,
    val contenido: String,
    val timestampEnvio: Long,
    val estadoJson: String,
    val isSynced: Boolean = false
)
```

### 3. DAOs

Los DAOs definen las operaciones de base de datos:

#### RegistroActividadDao

```kotlin
@Dao
interface RegistroActividadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistroActividad(registro: RegistroActividadEntity): Long
    
    @Query("SELECT * FROM registros_actividad WHERE alumnoId = :alumnoId ORDER BY fechaTimestamp DESC")
    fun getRegistrosActividadByAlumno(alumnoId: String): Flow<List<RegistroActividadEntity>>
    
    @Query("SELECT * FROM registros_actividad WHERE isSynced = 0")
    suspend fun getUnsyncedRegistros(): List<RegistroActividadEntity>
    
    @Query("UPDATE registros_actividad SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int
    
    // ... otros métodos
}
```

## Mecanismo de Sincronización

UmeEgunero implementa un sistema de sincronización bidireccional entre Room y Firebase Firestore:

### 1. Sincronización Firestore → Room (Descarga)

- Se descarga información de Firestore y se almacena en Room
- Implementado mediante Workers y Sync Services que se ejecutan periódicamente
- Se priorizan los datos más relevantes (registros recientes, conversaciones activas)

### 2. Sincronización Room → Firestore (Subida)

- Los cambios realizados sin conexión se marcan con `isSynced = false`
- Un servicio de sincronización periódico identifica registros no sincronizados y los sube a Firestore
- Una vez completada la sincronización, los registros se marcan con `isSynced = true`

### 3. Resolución de Conflictos

- Implementación de estrategias de timestamp para identificar la versión más reciente
- Uso de transacciones Firestore para operaciones críticas
- Campos de metadatos para rastrear modificaciones

```kotlin
// Ejemplo de código para sincronizar registros no sincronizados
suspend fun sincronizarRegistrosPendientes() {
    val registrosNoSincronizados = registroActividadDao.getUnsyncedRegistros()
    
    for (registroEntity in registrosNoSincronizados) {
        try {
            // Convertir entidad a modelo de dominio
            val registro = registroEntity.toRegistroActividad()
            
            // Subir a Firestore
            val result = registroDiarioRepository.guardarRegistroEnFirestore(registro)
            
            if (result is Result.Success) {
                // Marcar como sincronizado en Room
                registroActividadDao.markAsSynced(registroEntity.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar registro ${registroEntity.id}")
        }
    }
}
```

## Convertidores de Tipos

Para manejar tipos complejos en Room, UmeEgunero utiliza convertidores de tipos:

```kotlin
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String = 
        if (value == null) "" else gson.toJson(value)
    
    // ... otros convertidores
}
```

## Seguridad y Prevención de Desbordamiento de Buffer

UmeEgunero implementa varias medidas para prevenir desbordamientos de buffer y problemas de seguridad:

1. **Validación de entrada**: Todos los datos se validan antes de ser almacenados
2. **Parámetros con nombre**: Uso de parámetros con nombre en consultas SQL para prevenir inyección SQL
3. **Limitación de tamaño**: Restricciones en el tamaño de campos para prevenir desbordamientos
4. **Transacciones atómicas**: Uso de transacciones para garantizar la integridad de los datos

## Integración con el Patrón Repository

Room se integra en la aplicación a través del patrón Repository, que abstrae el origen de los datos:

```kotlin
class RegistroDiarioRepositoryImpl(
    private val registroActividadDao: RegistroActividadDao,
    private val firestore: FirebaseFirestore
) : RegistroDiarioRepository {

    override fun getRegistrosActividadByAlumnoId(alumnoId: String): Flow<List<RegistroActividad>> {
        // Intenta obtener datos locales primero
        return registroActividadDao.getRegistrosActividadByAlumno(alumnoId)
            .map { entidades -> entidades.map { it.toRegistroActividad() } }
            .onStart {
                // Al mismo tiempo, actualiza los datos desde Firestore
                sincronizarRegistrosDesdeFirestore(alumnoId)
            }
    }
    
    private suspend fun sincronizarRegistrosDesdeFirestore(alumnoId: String) {
        // Obtener datos de Firestore y guardarlos en Room
        // Código de sincronización...
    }
    
    // ... otros métodos
}
```

## Conclusión

La implementación de Room en UmeEgunero proporciona una capa de persistencia local robusta que permite:

1. Funcionamiento offline completo
2. Sincronización transparente con Firestore
3. Alto rendimiento en operaciones de datos frecuentes
4. Experiencia de usuario fluida independientemente del estado de la conexión

Esta arquitectura híbrida aprovecha lo mejor de las bases de datos locales (Room) y en la nube (Firestore), proporcionando una experiencia óptima para todos los usuarios de la aplicación. 