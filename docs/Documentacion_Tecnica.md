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
- **Coil**: Carga y caché de imágenes

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

La aplicación implementa un sistema de sincronización que permite el trabajo offline, usando Room para almacenamiento local y servicios de sincronización.

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

## Paginación y LazyLists

Se utilizan utilidades y componentes para paginación eficiente en listas y grids:

```kotlin
object PaginationUtils {
    data class PaginationState(
        val currentPage: Int = 0,
        val pageSize: Int = 20,
        val isLoading: Boolean = false,
        val isLastPage: Boolean = false,
        val totalItems: Int = 0
    )
    // ...
}
```

## Carga y Caché de Imágenes

Se utiliza Coil para la carga y caché de imágenes en la UI:

```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = "Foto de perfil",
    modifier = Modifier.size(64.dp)
)
```

## Preferencias y DataStore

El almacenamiento de preferencias y configuración de usuario se realiza con DataStore:

```kotlin
@Singleton
class PreferenciasRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ...
    val temaPreferencia: Flow<TemaPref> = context.dataStore.data
        .map { preferences ->
            try {
                TemaPref.valueOf(preferences[temaKey] ?: TemaPref.SYSTEM.name)
            } catch (e: Exception) {
                TemaPref.SYSTEM
            }
        }
    // ...
}
```

## Seguridad

- **Firebase Auth** y reglas de Firestore para autenticación y autorización.
- **Protección de datos sensibles**: Uso de HTTPS, roles y reglas de acceso.
- **No se ha encontrado uso de EncryptedSharedPreferences ni MasterKey en el código actual.**

## Pruebas

- Pruebas unitarias y de UI con JUnit y Compose Test.
- Ejemplo de test de UI:

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    // ...
}
```

## Accesibilidad y Buenas Prácticas

- Uso de `contentDescription` en imágenes y botones.
- Contraste adecuado y tamaños mínimos de touch targets.
- Internacionalización con `strings.xml` y soporte multilenguaje.

## Patrones de Diseño

- **Repository Pattern**: Abstracción de acceso a datos.
- **Factory Pattern**: Creación de ViewModels y objetos complejos.
- **Adapter Pattern**: Transformación de datos entre capas.
- **Observer Pattern**: StateFlow y Flow para sincronización UI-datos.
- **Singleton Pattern**: Instancias únicas de DAOs, repositorios y servicios críticos.
- **Strategy Pattern**: Estrategias de autenticación y lógica desacoplada.

## Optimización y Performance

- Uso de paginación para grandes conjuntos de datos.
- Caché de imágenes con Coil.
- Recomposición inteligente en Compose.
- LazyLists y LazyGrids para renderizado eficiente.

## Generación de Documentación

- Uso de Dokka para generar documentación a partir de comentarios KDoc.

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

## Testing y Cobertura

- Pruebas unitarias en ViewModel y lógica de dominio.
- Pruebas instrumentadas con Jetpack Compose Test.
- Cobertura mínima recomendada: 80% en lógica crítica.

## Integración y Seguridad con Firebase

- Uso de reglas de seguridad en Firestore.
- Validación de roles y permisos en el backend.

## Buenas Prácticas en Desarrollo Android

- Uso de Material 3 y Jetpack Compose en toda la UI.
- Arquitectura MVVM + Clean Architecture.
- Separación estricta de capas y responsabilidades.
- Inyección de dependencias con Hilt.
- Internacionalización (strings.xml) y soporte multilenguaje.
- Accesibilidad (contentDescription, contraste, tamaño mínimo de touch targets).

## Conclusión

Esta documentación técnica proporciona una visión general de la arquitectura y los principales componentes de UmeEgunero. La aplicación sigue las mejores prácticas de desarrollo para Android, utilizando tecnologías modernas y un enfoque arquitectónico que facilita la mantenibilidad y la escalabilidad.

Para una información más detallada sobre clases específicas, consulte la documentación generada por Dokka en el directorio `build/dokka`.