# Módulos de UmeEgunero

## Módulo Principal: com.tfg.umeegunero

### Estructura de Paquetes

#### com.tfg.umeegunero.data
Contiene toda la lógica de acceso a datos, incluyendo modelos, repositorios y fuentes de datos.

##### com.tfg.umeegunero.data.model
Modelos de datos que representan las entidades del sistema educativo:
- **Alumno**: Información de estudiantes
- **Centro**: Datos de centros educativos  
- **Clase**: Información de clases y aulas
- **Curso**: Datos de cursos académicos
- **Usuario**: Información de usuarios del sistema
- **RegistroActividad**: Registros diarios de actividades
- **ActividadPreescolar**: Actividades específicas de educación infantil

##### com.tfg.umeegunero.data.repository
Repositorios que implementan el patrón Repository para acceso a datos:
- **AlumnoRepository**: Gestión de datos de alumnos
- **CentroRepository**: Gestión de centros educativos
- **ClaseRepository**: Gestión de clases y aulas
- **CursoRepository**: Gestión de cursos académicos
- **AuthRepository**: Autenticación y autorización
- **NotificacionRepository**: Gestión de notificaciones

##### com.tfg.umeegunero.data.local
Persistencia local con Room Database:
- **AppDatabase**: Base de datos principal
- **DAOs**: Objetos de acceso a datos
- **Entities**: Entidades locales

#### com.tfg.umeegunero.feature
Características y funcionalidades organizadas por perfil de usuario.

##### com.tfg.umeegunero.feature.admin
Funcionalidades específicas para administradores del sistema:
- **AdminDashboard**: Panel principal de administración
- **GestionCentros**: Gestión de centros educativos
- **EstadisticasGlobales**: Estadísticas del sistema

##### com.tfg.umeegunero.feature.centro
Funcionalidades para administradores de centro:
- **CentroDashboard**: Panel de control del centro
- **GestionProfesores**: Gestión de profesores
- **GestionAlumnos**: Gestión de alumnos
- **VinculacionFamiliar**: Vinculación de familias

##### com.tfg.umeegunero.feature.profesor
Funcionalidades para profesores:
- **ProfesorDashboard**: Panel del profesor
- **RegistroAsistencia**: Registro de asistencia
- **RegistroActividades**: Registro de actividades diarias
- **ComunicacionFamilias**: Comunicación con familias

##### com.tfg.umeegunero.feature.familiar
Funcionalidades para familias:
- **FamiliarDashboard**: Panel familiar
- **SeguimientoHijos**: Seguimiento de hijos
- **ConsultaRegistros**: Consulta de registros
- **ComunicacionCentro**: Comunicación con el centro

##### com.tfg.umeegunero.feature.common
Funcionalidades compartidas entre perfiles:
- **Autenticación**: Login y registro
- **Navegación**: Sistema de navegación
- **Configuración**: Ajustes de la aplicación
- **Comunicación**: Sistema de mensajería

#### com.tfg.umeegunero.util
Utilidades y helpers del sistema:
- **Result**: Wrapper para resultados de operaciones
- **Constants**: Constantes de la aplicación
- **Extensions**: Extensiones de Kotlin
- **Validators**: Validadores de datos

#### com.tfg.umeegunero.di
Inyección de dependencias con Hilt:
- **DatabaseModule**: Módulo de base de datos
- **NetworkModule**: Módulo de red
- **RepositoryModule**: Módulo de repositorios

#### com.tfg.umeegunero.navigation
Sistema de navegación de la aplicación:
- **Navigation**: Configuración de navegación
- **AppScreens**: Definición de pantallas
- **NavigationGraph**: Grafo de navegación

### Arquitectura

La aplicación sigue los principios de **Clean Architecture** y **MVVM**:

1. **Presentation Layer**: ViewModels y Composables
2. **Domain Layer**: Casos de uso y lógica de negocio  
3. **Data Layer**: Repositorios y fuentes de datos

### Tecnologías Principales

- **Jetpack Compose**: UI declarativa
- **Hilt**: Inyección de dependencias
- **Room**: Persistencia local
- **Firebase**: Backend y autenticación
- **Coroutines**: Programación asíncrona
- **Material 3**: Diseño visual

---
*Documentación generada automáticamente con Dokka* 