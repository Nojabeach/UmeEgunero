# TODO: Implementación de Room en UmeEgunero

## Entidades para Room

### 1. Caché de datos remotos

```kotlin
// Ejemplo de entidad para almacenar centros educativos en caché local
@Entity(tableName = "centros")
data class CentroEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val ciudad: String,
    val provincia: String,
    val email: String,
    val telefono: String,
    val activo: Boolean,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
```

### 2. Datos de usuario y sesión

```kotlin
@Entity(tableName = "usuario_actual")
data class UsuarioSesionEntity(
    @PrimaryKey val id: String,
    val dni: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val tipoUsuario: String,
    val ultimoAcceso: Long = System.currentTimeMillis()
)
```

### 3. Historial y registros locales

```kotlin
@Entity(tableName = "registros_diarios_pendientes", 
        indices = [Index(value = ["alumnoId", "fecha"], unique = true)])
data class RegistroDiarioPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alumnoId: String,
    val fecha: Long,
    val desayuno: String,
    val comida: String,
    val merienda: String,
    val siesta: String,
    val deposiciones: Int,
    val observaciones: String,
    val sincronizado: Boolean = false
)
```

### 4. Relaciones entre entidades

```kotlin
@Entity(tableName = "profesor_alumno",
        primaryKeys = ["profesorId", "alumnoId"],
        foreignKeys = [
            ForeignKey(entity = ProfesorEntity::class,
                       parentColumns = ["id"],
                       childColumns = ["profesorId"]),
            ForeignKey(entity = AlumnoEntity::class,
                       parentColumns = ["id"],
                       childColumns = ["alumnoId"])
        ])
data class ProfesorAlumnoRelacionEntity(
    val profesorId: String,
    val alumnoId: String,
    val fechaAsignacion: Long = System.currentTimeMillis()
)
```

### 5. Notificaciones y mensajes

```kotlin
@Entity(tableName = "mensajes")
data class MensajeEntity(
    @PrimaryKey val id: String,
    val emisorId: String,
    val receptorId: String,
    val contenido: String,
    val fechaEnvio: Long,
    val leido: Boolean = false,
    val tipo: String // FAMILIAR_A_PROFESOR, PROFESOR_A_FAMILIAR, etc.
)
```

### 6. Sincronización eficiente

```kotlin
@Entity(tableName = "sincronizacion_pendiente")
data class SincronizacionPendienteEntity(
    @PrimaryKey val id: String,
    val tipoEntidad: String, // "registro", "mensaje", etc.
    val operacion: String, // "crear", "actualizar", "eliminar"
    val entidadId: String,
    val datosJson: String, // Datos serializados para sincronizar
    val intentos: Int = 0,
    val fechaCreacion: Long = System.currentTimeMillis()
)
```

## Estructura de implementación

### 1. Database

```kotlin
@Database(
    entities = [
        CentroEntity::class,
        UsuarioSesionEntity::class,
        RegistroDiarioPendienteEntity::class,
        ProfesorAlumnoRelacionEntity::class,
        MensajeEntity::class,
        SincronizacionPendienteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun centroDao(): CentroDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun registroDao(): RegistroDao
    abstract fun relacionesDao(): RelacionesDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun sincronizacionDao(): SincronizacionDao
}
```

### 2. DAOs

```kotlin
@Dao
interface CentroDao {
    @Query("SELECT * FROM centros WHERE activo = 1")
    fun getActiveCentros(): Flow<List<CentroEntity>>
    
    @Query("SELECT * FROM centros WHERE id = :id")
    suspend fun getCentroById(id: String): CentroEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(centros: List<CentroEntity>)
    
    @Update
    suspend fun update(centro: CentroEntity)
    
    @Query("DELETE FROM centros WHERE id = :id")
    suspend fun deleteCentro(id: String)
}
```

### 3. Repositorio con Single Source of Truth

```kotlin
class AlumnoRepository @Inject constructor(
    private val alumnoDao: AlumnoDao,
    private val firestore: FirebaseFirestore,
    private val sincronizacionDao: SincronizacionDao
) {
    // Expone datos desde Room con actualizaciones en tiempo real
    fun getAlumnos(): Flow<List<Alumno>> = alumnoDao.getAlumnos()
        .map { it.map { entity -> entity.toDomainModel() } }
    
    // Función para forzar actualización desde Firestore
    suspend fun refreshAlumnos() {
        withContext(Dispatchers.IO) {
            try {
                val alumnos = firestore.collection("alumnos").get().await()
                    .toObjects(AlumnoDto::class.java)
                alumnoDao.insertAll(alumnos.map { it.toEntity() })
            } catch (e: Exception) {
                // Manejar error
                Timber.e(e, "Error al refrescar alumnos")
            }
        }
    }
    
    // Operaciones con sincronización automática
    suspend fun actualizarAlumno(alumno: Alumno) {
        withContext(Dispatchers.IO) {
            // Guarda localmente primero (para actualización inmediata de UI)
            alumnoDao.update(alumno.toEntity())
            
            try {
                // Luego sincroniza con backend
                firestore.collection("alumnos").document(alumno.id).set(alumno).await()
            } catch (e: Exception) {
                // Si falla, marca para sincronización posterior
                sincronizacionDao.insert(SincronizacionPendienteEntity(
                    id = UUID.randomUUID().toString(),
                    tipoEntidad = "alumno",
                    operacion = "actualizar",
                    entidadId = alumno.id,
                    datosJson = Gson().toJson(alumno)
                ))
            }
        }
    }
}
```

### 4. Mappers para convertir entre modelos

```kotlin
fun CentroEntity.toDomainModel(): Centro {
    return Centro(
        id = id,
        nombre = nombre,
        direccion = Direccion(
            ciudad = ciudad,
            provincia = provincia
        ),
        contacto = Contacto(
            telefono = telefono,
            email = email
        ),
        activo = activo
    )
}

fun Centro.toEntity(): CentroEntity {
    return CentroEntity(
        id = id,
        nombre = nombre,
        ciudad = direccion.ciudad,
        provincia = direccion.provincia,
        telefono = contacto.telefono,
        email = contacto.email,
        activo = activo
    )
}
```

### 5. Módulo Dagger/Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "umeegunero-db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideCentroDao(database: AppDatabase): CentroDao {
        return database.centroDao()
    }
    
    @Provides
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao {
        return database.usuarioDao()
    }
    
    // Resto de DAOs...
}
```

### 6. Sincronización en WorkManager

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizacionDao: SincronizacionDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Obtener todos los elementos pendientes de sincronización
        val pendientes = sincronizacionDao.getPendientes()
        
        // Procesar cada item pendiente
        pendientes.forEach { item ->
            try {
                when (item.tipoEntidad) {
                    "alumno" -> sincronizarAlumno(item)
                    "mensaje" -> sincronizarMensaje(item)
                    // otros tipos...
                }
                // Si se sincronizó correctamente, eliminar de pendientes
                sincronizacionDao.delete(item.id)
            } catch (e: Exception) {
                // Incrementar contador de intentos
                sincronizacionDao.incrementarIntentos(item.id)
                Timber.e(e, "Error sincronizando ${item.tipoEntidad} ${item.entidadId}")
                // Si han habido demasiados intentos, marcar como fallido permanentemente
                if (item.intentos >= MAX_SYNC_ATTEMPTS) {
                    // Lógica para manejar fallos permanentes
                }
            }
        }
        
        return Result.success()
    }
    
    private suspend fun sincronizarAlumno(item: SincronizacionPendienteEntity) {
        val alumno = Gson().fromJson(item.datosJson, Alumno::class.java)
        
        when (item.operacion) {
            "crear", "actualizar" -> {
                firestore.collection("alumnos").document(alumno.id).set(alumno).await()
            }
            "eliminar" -> {
                firestore.collection("alumnos").document(alumno.id).delete().await()
            }
        }
    }
    
    // Otras funciones de sincronización...
    
    companion object {
        private const val MAX_SYNC_ATTEMPTS = 5
        
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
                
            val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sync_data_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
            )
        }
    }
} 