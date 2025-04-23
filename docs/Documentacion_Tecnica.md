# Documentación Técnica - UmeEgunero

## Visión General de la Arquitectura

UmeEgunero es una aplicación Android desarrollada con Kotlin que sigue una arquitectura MVVM (Model-View-ViewModel) con principios de Clean Architecture. La aplicación utiliza Jetpack Compose para la interfaz de usuario y Firebase como backend principal.

## Estructura del Proyecto

```
com.tfg.umeegunero/
├── data/                          # Capa de datos
│   ├── model/                     # Modelos de datos y entidades
│   ├── repository/                # Implementaciones de repositorios
│   ├── datasource/                # Fuentes de datos (local y remota)
│   └── util/                      # Utilidades relacionadas con datos
├── di/                            # Inyección de dependencias (Hilt)
├── domain/                        # Capa de dominio
│   ├── repository/                # Interfaces de repositorio
│   ├── usecase/                   # Casos de uso
│   └── model/                     # Modelos de dominio
├── feature/                       # Características organizadas por módulos
│   ├── auth/                      # Autenticación
│   ├── admin/                     # Funcionalidades de administrador
│   ├── profesor/                  # Funcionalidades de profesor
│   ├── familiar/                  # Funcionalidades de familia
│   ├── common/                    # Pantallas compartidas entre varios perfiles
│   └── centro/                    # Gestión de centros
├── navigation/                    # Sistema de navegación
├── service/                       # Servicios en segundo plano
├── notification/                  # Sistema de notificaciones
├── ui/                            # Componentes de UI y temas
│   ├── theme/                     # Tema de la aplicación
│   └── components/                # Componentes reutilizables
└── util/                          # Utilidades generales
```

## Tecnologías Principales

### Frontend
- **Kotlin**: Lenguaje principal de desarrollo
- **Jetpack Compose**: Framework de UI declarativo
- **Material 3**: Guía de diseño y componentes
- **Coroutines + Flow**: Para operaciones asíncronas
- **Navigation Compose**: Navegación entre pantallas
- **Hilt**: Inyección de dependencias
- **ViewModel**: Gestión del estado de la UI
- **Room**: Base de datos local

### Backend
- **Firebase Auth**: Autenticación de usuarios
- **Cloud Firestore**: Base de datos principal
- **Firebase Storage**: Almacenamiento de archivos
- **Firebase Cloud Messaging**: Notificaciones push
- **Firebase Crashlytics**: Reportes de errores

## Modelos de Datos Principales

### Result/Resultado

La aplicación utiliza un modelo unificado para representar resultados de operaciones asíncronas:

```kotlin
/**
 * Representa el resultado de una operación asíncrona.
 *
 * @param T Tipo de datos en caso de éxito
 */
sealed class Result<out T> {
    /**
     * Estado de carga, cuando la operación está en progreso.
     */
    object Loading : Result<Nothing>()
    
    /**
     * Estado de éxito, cuando la operación se completa correctamente.
     * 
     * @property data Datos resultantes de la operación
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Estado de error, cuando la operación falla.
     * 
     * @property message Mensaje descriptivo del error
     * @property exception Excepción capturada (opcional)
     */
    data class Error(
        val message: String?,
        val exception: Throwable? = null
    ) : Result<Nothing>()
}
```

### Usuario

```kotlin
/**
 * Modelo que representa un usuario en el sistema.
 *
 * @property id Identificador único del usuario
 * @property nombre Nombre completo del usuario
 * @property email Correo electrónico
 * @property tipo Tipo de perfil (ADMIN, PROFESOR, FAMILIAR)
 * @property fotoPerfil URL de la foto de perfil (opcional)
 * @property idCentro Identificador del centro educativo al que pertenece
 * @property fechaRegistro Timestamp de registro
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val tipo: TipoUsuario,
    val fotoPerfil: String? = null,
    val idCentro: String,
    val fechaRegistro: Timestamp = Timestamp.now()
)

/**
 * Tipos de usuario disponibles en el sistema.
 */
enum class TipoUsuario {
    ADMIN, PROFESOR, FAMILIAR
}
```

## Arquitectura MVVM

La aplicación sigue el patrón MVVM (Model-View-ViewModel):

### Modelo (Model)
- Representado por los repositorios y las fuentes de datos
- Encapsula la lógica de negocio y el acceso a datos
- Define las estructuras de datos utilizadas en la aplicación

### Vista (View)
- Implementada con Jetpack Compose
- Representa la interfaz de usuario
- Observa los cambios en el ViewModel y se actualiza en consecuencia
- No contiene lógica de negocio

### ViewModel
- Actúa como intermediario entre la Vista y el Modelo
- Mantiene el estado de la UI
- Procesa los eventos de la UI y ejecuta la lógica de negocio
- Expone Flows/StateFlows para que la Vista observe los cambios

## Arquitectura de Repositorios

Los repositorios proporcionan una API limpia para acceder a los datos:

```kotlin
/**
 * Interfaz para el repositorio de alumnos.
 */
interface AlumnoRepository {
    /**
     * Obtiene un alumno por su ID.
     *
     * @param id Identificador único del alumno
     * @return Un Flow con el resultado de la operación
     */
    fun obtenerAlumnoPorId(id: String): Flow<Result<Alumno>>
    
    /**
     * Obtiene todos los alumnos de una clase.
     *
     * @param idClase Identificador de la clase
     * @return Un Flow con el resultado de la operación
     */
    fun obtenerAlumnosPorClase(idClase: String): Flow<Result<List<Alumno>>>
    
    // Otros métodos...
}
```

## Inyección de Dependencias

Hilt se utiliza para la inyección de dependencias:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideCalendarioRepository(
        firestore: FirebaseFirestore,
        calendarioLocalDataSource: CalendarioLocalDataSource
    ): CalendarioRepository {
        return CalendarioRepositoryImpl(firestore, calendarioLocalDataSource)
    }
    
    // Otras dependencias...
}
```

## Gestión de Estados en la UI

La aplicación utiliza clases selladas para representar los estados de la UI:

```kotlin
/**
 * Estado de la interfaz de usuario para las pantallas.
 */
sealed class UiState<out T> {
    /**
     * Estado inicial o de carga.
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Estado de éxito con datos.
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Estado de error.
     */
    data class Error(val message: String?) : UiState<Nothing>()
}
```

## Sistema de Navegación

La navegación se implementa con Navigation Compose:

```kotlin
/**
 * Rutas de navegación para los módulos de la aplicación.
 */
sealed class NavigationRoutes(val route: String) {
    // Rutas de autenticación
    object Login : NavigationRoutes("login")
    object Registro : NavigationRoutes("registro")
    object RecuperarContrasena : NavigationRoutes("recuperar_contrasena")
    
    // Rutas de profesor
    object DashboardProfesor : NavigationRoutes("dashboard_profesor")
    object ListaAlumnos : NavigationRoutes("lista_alumnos")
    
    // Otras rutas...
}
```

## Sincronización y Modo Offline

La aplicación implementa un sistema de sincronización que permite el trabajo offline:

### Servicio de Sincronización

```kotlin
/**
 * Servicio en primer plano para sincronización de datos.
 * Gestiona la sincronización periódica y el manejo de operaciones pendientes.
 */
class SyncService : LifecycleService() {
    
    @Inject
    lateinit var syncRepository: SyncRepository
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Implementación del servicio...
}
```

### Almacenamiento Local

Room se utiliza para almacenar datos localmente:

```kotlin
/**
 * Base de datos local de la aplicación.
 */
@Database(
    entities = [
        AlumnoEntity::class,
        ComunicadoEntity::class,
        EventoEntity::class,
        // Otras entidades...
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun comunicadoDao(): ComunicadoDao
    abstract fun eventoDao(): EventoDao
    // Otros DAOs...
}
```

## Firma Digital

El sistema de firma digital para comunicados se implementa con:

```kotlin
/**
 * Utilidades para la firma digital de documentos.
 */
object FirmaDigitalUtil {
    
    /**
     * Genera una firma digital basada en datos del usuario y timestamp.
     *
     * @param userId ID del usuario que firma
     * @param documentId ID del documento firmado
     * @param timestamp Momento de la firma
     * @return Código hash que representa la firma
     */
    fun generarFirma(userId: String, documentId: String, timestamp: Long): String {
        // Implementación de la generación de firma...
    }
    
    /**
     * Verifica la validez de una firma digital.
     *
     * @param firma Firma a verificar
     * @param userId ID del usuario
     * @param documentId ID del documento
     * @param timestamp Timestamp de la firma
     * @return true si la firma es válida
     */
    fun verificarFirma(firma: String, userId: String, documentId: String, timestamp: Long): Boolean {
        // Implementación de la verificación...
    }
}
```

## Estilo de Código y Convenciones

- Nombres de clases en español
- Nombres de métodos en camelCase
- Comentarios KDoc para clases y métodos principales
- Máximo 100 caracteres por línea
- Propiedades inmutables cuando sea posible (val vs var)

## Pruebas

La aplicación incluye varias capas de pruebas:

### Pruebas Unitarias

```kotlin
/**
 * Pruebas para AlumnoRepositoryImpl.
 */
@RunWith(MockitoJUnitRunner::class)
class AlumnoRepositoryTest {
    
    @Mock
    lateinit var firestore: FirebaseFirestore
    
    @Mock
    lateinit var alumnoLocalDataSource: AlumnoLocalDataSource
    
    private lateinit var alumnoRepository: AlumnoRepository
    
    @Before
    fun setup() {
        alumnoRepository = AlumnoRepositoryImpl(firestore, alumnoLocalDataSource)
    }
    
    @Test
    fun `obtenerAlumnoPorId devuelve Success cuando la operación es exitosa`() {
        // Implementación de la prueba...
    }
    
    // Más pruebas...
}
```

### Pruebas de UI

```kotlin
/**
 * Pruebas de UI para la pantalla de login.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Before
    fun setup() {
        composeTestRule.setContent {
            UmeEguneroTheme {
                LoginScreen(
                    onNavigateToRegistro = {},
                    onNavigateToDashboard = {}
                )
            }
        }
    }
    
    @Test
    fun loginScreen_displaysAllRequiredElements() {
        // Verificación de elementos de la UI...
    }
    
    // Más pruebas...
}
```

## Gestión de Permisos

La aplicación requiere varios permisos para funcionar correctamente:

```xml
<!-- Permisos básicos -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Permisos para notificaciones -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Permisos para servicio en primer plano -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<!-- Otros permisos... -->
```

## Seguridad

### Autenticación y Autorización

UmeEgunero implementa un sistema completo de seguridad basado en Firebase Authentication y reglas personalizadas de Firestore:

#### Autenticación

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    
    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            // Verificar si la cuenta está activa
            val userDoc = firestore.collection("usuarios")
                .document(result.user!!.uid)
                .get().await()
            
            if (userDoc.exists() && userDoc.getBoolean("activo") == true) {
                emit(Result.Success(result.user!!))
            } else {
                auth.signOut()
                emit(Result.Error("La cuenta ha sido desactivada"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
    
    // Otros métodos de autenticación...
}
```

#### Reglas de Seguridad de Firestore

La aplicación utiliza reglas de seguridad detalladas en Firestore para proteger los datos:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Función para verificar rol de usuario
    function hasRole(role) {
      return request.auth != null && 
             exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.tipoUsuario == role;
    }
    
    // Función para verificar pertenencia a centro
    function belongsToCenter(centerId) {
      return request.auth != null && 
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.centroId == centerId;
    }
    
    // Reglas para colección de usuarios
    match /usuarios/{userId} {
      allow read: if request.auth != null && (request.auth.uid == userId || hasRole("ADMIN_APP"));
      allow create: if hasRole("ADMIN_APP") || hasRole("ADMIN_CENTRO");
      allow update: if request.auth != null && 
                    (request.auth.uid == userId || 
                     hasRole("ADMIN_APP") || 
                     (hasRole("ADMIN_CENTRO") && belongsToCenter(resource.data.centroId)));
      allow delete: if hasRole("ADMIN_APP");
    }
    
    // Reglas para centros educativos
    match /centros/{centroId} {
      allow read: if request.auth != null;
      allow write: if hasRole("ADMIN_APP");
      
      // Subcolección cursos
      match /cursos/{cursoId} {
        allow read: if request.auth != null && 
                   (hasRole("ADMIN_APP") || 
                    belongsToCenter(centroId));
        allow write: if hasRole("ADMIN_APP") || 
                    (hasRole("ADMIN_CENTRO") && belongsToCenter(centroId));
      }
      
      // Otras subcolecciones...
    }
    
    // Más reglas para otras colecciones...
  }
}
```

### Protección de Datos Sensibles

1. **Encriptación de Datos**:
   - Toda la comunicación con Firebase utiliza HTTPS
   - Datos sensibles como contraseñas nunca se almacenan en texto plano
   - Se utilizan Firebase Security Rules para restringir acceso a documentos

2. **Protección en la Aplicación**:
   - Información sensible como tokens de sesión se almacena en EncryptedSharedPreferences
   - No se permite captura de pantalla en pantallas con información sensible
   - Cierre automático de sesión por inactividad

```kotlin
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveSecureToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }
    
    fun getSecureToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
    
    fun clearSecureData() {
        encryptedPrefs.edit().clear().apply()
    }
}
```

## Rendimiento

Estrategias para optimizar el rendimiento:

1. **Paginación**: Para cargar grandes conjuntos de datos
2. **Caché de imágenes**: Utilizando Coil
3. **Recomposición inteligente**: Minimizando la recomposición en Compose
4. **LazyLists y LazyGrids**: Para renderizado eficiente

## Generación de Documentación

Este proyecto utiliza Dokka para generar la documentación a partir de los comentarios KDoc:

```gradle
plugins {
    id("org.jetbrains.dokka") version "1.8.10"
}

tasks.dokkaHtml.configure {
    outputDirectory.set(file("$buildDir/dokka"))
    
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            reportUndocumented.set(true)
            platform.set(org.jetbrains.dokka.Platform.jvm)
        }
    }
}
```

## Conclusión

Esta documentación técnica proporciona una visión general de la arquitectura y los principales componentes de UmeEgunero. La aplicación sigue las mejores prácticas de desarrollo para Android, utilizando tecnologías modernas y un enfoque arquitectónico que facilita la mantenibilidad y la escalabilidad.

Para una información más detallada sobre clases específicas, consulte la documentación generada por Dokka en el directorio `build/dokka`.

## Patrones de Diseño

UmeEgunero implementa varios patrones de diseño para mejorar la mantenibilidad, escalabilidad y robustez del código:

### Repository Pattern

El patrón Repository se utiliza para abstraer la lógica de acceso a datos, separando claramente las operaciones de persistencia de la lógica de negocio:

```kotlin
interface AlumnoRepository {
    fun obtenerAlumnoPorId(id: String): Flow<Result<Alumno>>
    fun obtenerAlumnosPorClase(idClase: String): Flow<Result<List<Alumno>>>
    fun registrarAlumno(alumno: Alumno): Flow<Result<String>>
    fun actualizarAlumno(alumno: Alumno): Flow<Result<Unit>>
    fun eliminarAlumno(id: String): Flow<Result<Unit>>
}

class AlumnoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataSource: AlumnoLocalDataSource
) : AlumnoRepository {
    // Implementación concreta que gestiona ambas fuentes de datos
}
```

### Factory Pattern

Se utiliza para la creación de instancias complejas, especialmente para ViewModels con parámetros específicos:

```kotlin
class AlumnoViewModelFactory(
    private val alumnoId: String,
    private val repository: AlumnoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlumnoViewModel::class.java)) {
            return AlumnoViewModel(alumnoId, repository) as T
        }
        throw IllegalArgumentException("ViewModel class desconocido")
    }
}
```

### Adapter Pattern

Implementado para transformar datos entre capas:

```kotlin
object AlumnoMapper {
    fun mapToEntity(domainModel: Alumno): AlumnoEntity {
        return AlumnoEntity(
            id = domainModel.id,
            nombre = domainModel.nombre,
            // Mapeo de atributos
        )
    }

    fun mapToDomain(entity: AlumnoEntity): Alumno {
        return Alumno(
            id = entity.id,
            nombre = entity.nombre,
            // Mapeo inverso
        )
    }
}
```

### Observer Pattern

Implementado a través de Kotlin Flows y StateFlow para mantener la UI sincronizada con los cambios de estado:

```kotlin
class ComunicadosViewModel @Inject constructor(
    private val repository: ComunicadoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ComunicadosUiState())
    val uiState: StateFlow<ComunicadosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerComunicados()
                .collect { result ->
                    // Actualiza el estado UI basado en el resultado
                    _uiState.update { currentState ->
                        when (result) {
                            is Result.Success -> currentState.copy(
                                comunicados = result.data,
                                isLoading = false
                            )
                            is Result.Error -> currentState.copy(
                                error = result.message,
                                isLoading = false
                            )
                            is Result.Loading -> currentState.copy(
                                isLoading = true
                            )
                        }
                    }
                }
        }
    }
}
```

### Singleton Pattern

Implementado a través de Hilt para proveer instancias únicas de servicios críticos:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
```

### Strategy Pattern

Utilizado para implementar diferentes estrategias de autenticación:

```kotlin
interface AuthStrategy {
    fun login(email: String, password: String): Flow<Result<User>>
}

class EmailPasswordAuthStrategy @Inject constructor(
    private val auth: FirebaseAuth
) : AuthStrategy {
    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            emit(Result.Success(result.user!!))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
```

## Seguridad

### Autenticación y Autorización

UmeEgunero implementa un sistema completo de seguridad basado en Firebase Authentication y reglas personalizadas de Firestore:

#### Autenticación

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    
    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            // Verificar si la cuenta está activa
            val userDoc = firestore.collection("usuarios")
                .document(result.user!!.uid)
                .get().await()
            
            if (userDoc.exists() && userDoc.getBoolean("activo") == true) {
                emit(Result.Success(result.user!!))
            } else {
                auth.signOut()
                emit(Result.Error("La cuenta ha sido desactivada"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
    
    // Otros métodos de autenticación...
}
```

#### Reglas de Seguridad de Firestore

La aplicación utiliza reglas de seguridad detalladas en Firestore para proteger los datos:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Función para verificar rol de usuario
    function hasRole(role) {
      return request.auth != null && 
             exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.tipoUsuario == role;
    }
    
    // Función para verificar pertenencia a centro
    function belongsToCenter(centerId) {
      return request.auth != null && 
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.centroId == centerId;
    }
    
    // Reglas para colección de usuarios
    match /usuarios/{userId} {
      allow read: if request.auth != null && (request.auth.uid == userId || hasRole("ADMIN_APP"));
      allow create: if hasRole("ADMIN_APP") || hasRole("ADMIN_CENTRO");
      allow update: if request.auth != null && 
                    (request.auth.uid == userId || 
                     hasRole("ADMIN_APP") || 
                     (hasRole("ADMIN_CENTRO") && belongsToCenter(resource.data.centroId)));
      allow delete: if hasRole("ADMIN_APP");
    }
    
    // Reglas para centros educativos
    match /centros/{centroId} {
      allow read: if request.auth != null;
      allow write: if hasRole("ADMIN_APP");
      
      // Subcolección cursos
      match /cursos/{cursoId} {
        allow read: if request.auth != null && 
                   (hasRole("ADMIN_APP") || 
                    belongsToCenter(centroId));
        allow write: if hasRole("ADMIN_APP") || 
                    (hasRole("ADMIN_CENTRO") && belongsToCenter(centroId));
      }
      
      // Otras subcolecciones...
    }
    
    // Más reglas para otras colecciones...
  }
}
```

### Protección de Datos Sensibles

1. **Encriptación de Datos**:
   - Toda la comunicación con Firebase utiliza HTTPS
   - Datos sensibles como contraseñas nunca se almacenan en texto plano
   - Se utilizan Firebase Security Rules para restringir acceso a documentos

2. **Protección en la Aplicación**:
   - Información sensible como tokens de sesión se almacena en EncryptedSharedPreferences
   - No se permite captura de pantalla en pantallas con información sensible
   - Cierre automático de sesión por inactividad

```kotlin
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveSecureToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }
    
    fun getSecureToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
    
    fun clearSecureData() {
        encryptedPrefs.edit().clear().apply()
    }
}
```

## Buenas Prácticas en Desarrollo Android

- Uso de Material 3 y Jetpack Compose en toda la UI.
- Arquitectura MVVM + Clean Architecture.
- Separación estricta de capas y responsabilidades.
- Inyección de dependencias con Hilt.
- Internacionalización (strings.xml) y soporte multilenguaje.
- Accesibilidad (contentDescription, contraste, tamaño mínimo de touch targets).

## Testing y Cobertura

- Pruebas unitarias en ViewModel y lógica de dominio.
- Pruebas instrumentadas con Jetpack Compose Test.
- Cobertura mínima recomendada: 80% en lógica crítica.

## Integración y Seguridad con Firebase

- Uso de reglas de seguridad en Firestore.
- Validación de roles y permisos en el backend.
- Manejo seguro de credenciales y datos personales.

## Tabla de Librerías Clave

| Librería               | Versión   | Uso principal           |
|------------------------|-----------|------------------------|
| Jetpack Compose        | 1.x       | UI declarativa         |
| Material 3             | 1.x       | Componentes UI         |
| Hilt                   | 2.x       | Inyección dependencias |
| Firebase (Auth, DB)    | Última    | Backend y auth         |
| Room                   | 2.x       | BD local               |
| Navigation Compose     | 2.x       | Navegación             |
| Coil                   | 2.x       | Carga de imágenes      |

## Internacionalización y Localización

- Uso de recursos en `strings.xml` para todos los textos.
- Soporte para español y euskera (añadir más idiomas si es necesario).

## Accesibilidad

- Uso de `contentDescription` para todos los elementos interactivos.
- Contraste mínimo de 4.5:1 para texto informativo.
- Tamaño mínimo de 48x48dp para touch targets.

## Optimización y Performance

- Uso de paginación para grandes conjuntos de datos.
- Caché de imágenes con Coil.
- Recomposición inteligente en Compose.
- LazyLists y LazyGrids para renderizado eficiente.

## Generación de Documentación

- Uso de Dokka para generar documentación a partir de comentarios KDoc.

## Patrones de Diseño

- Repository Pattern para abstraer lógica de acceso a datos.
- Factory Pattern para creación de instancias complejas.
- Adapter Pattern para transformar datos entre capas.
- Observer Pattern para mantener la UI sincronizada con cambios de estado.
- Singleton Pattern para proveer instancias únicas de servicios críticos.
- Strategy Pattern para implementar diferentes estrategias de autenticación.

## Seguridad

- Autenticación y autorización con Firebase Authentication y reglas de seguridad en Firestore.
- Protección de datos sensibles con encriptación y restricciones de acceso.
- Manejo seguro de credenciales y datos personales.