# UmeEgunero - Aplicación de Gestión Educativa

UmeEgunero es una aplicación Android desarrollada en Kotlin con Jetpack Compose que facilita la comunicación y gestión entre centros educativos, profesores, familiares y alumnos.

## Características principales

- **Autenticación y gestión de usuarios**: Sistema de inicio de sesión y registro para diferentes tipos de usuarios (administradores, centros, profesores y familiares).
- **Interfaz moderna con Jetpack Compose**: Diseño moderno y adaptable utilizando la biblioteca de UI declarativa de Android.
- **Arquitectura MVVM**: Separación clara entre la lógica de negocio y la interfaz de usuario.
- **Firebase como backend**: Firestore para almacenamiento de datos en la nube, autenticación de usuarios y notificaciones.
- **Inyección de dependencias con Hilt**: Gestión eficiente de dependencias.
- **Coroutines y Flow**: Para operaciones asíncronas y flujos de datos reactivos.

## Sprint 1: Gestión de Tareas para Familiares

El Sprint 1 se ha centrado en desarrollar las funcionalidades de gestión de tareas desde la perspectiva de los familiares:

### Funcionalidades Implementadas

1. **Visualización de tareas**:
   - Lista de tareas asignadas a los alumnos
   - Filtrado por estado (pendientes, en progreso, completadas, retrasadas)
   - Indicadores visuales de prioridad y estado

2. **Detalle de tareas**:
   - Información completa sobre cada tarea (título, descripción, fecha de entrega, etc.)
   - Visualización de archivos adjuntos por el profesor
   - Estado actual de la tarea y calificación (si está disponible)

3. **Revisión de tareas por familiares**:
   - Función para marcar tareas como revisadas
   - Posibilidad de añadir comentarios al revisar

4. **Entrega de tareas**:
   - Interfaz para enviar entregas en nombre de los alumnos
   - Soporte para adjuntar múltiples archivos
   - Comentarios para el profesor

### Estructura del código

El proyecto sigue una estructura modular basada en características:

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/tfg/umeegunero/
│   │   │   ├── data/
│   │   │   │   ├── model/          # Modelos de datos
│   │   │   │   ├── repository/     # Repositorios para acceso a datos
│   │   │   │   └── ...
│   │   │   ├── di/                 # Módulos de inyección de dependencias
│   │   │   ├── feature/
│   │   │   │   ├── familiar/       # Funcionalidades para familiares
│   │   │   │   │   ├── screen/     # Pantallas UI con Compose
│   │   │   │   │   ├── viewmodel/  # ViewModels específicos
│   │   │   │   │   └── ...
│   │   │   │   └── ...
│   │   │   ├── navigation/         # Configuración de navegación
│   │   │   └── ui/                 # Componentes de UI reutilizables
│   │   └── ...
│   ├── test/                       # Pruebas unitarias
│   └── androidTest/                # Pruebas instrumentadas
└── ...
```

### Modelos principales

**Tarea**: Representa una tarea asignada por un profesor a un alumno o clase.
```kotlin
data class Tarea(
    @DocumentId val id: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val claseId: String = "",
    val nombreClase: String = "",
    val alumnoId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaEntrega: Timestamp? = null,
    val adjuntos: List<String> = emptyList(),
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val prioridad: PrioridadTarea = PrioridadTarea.MEDIA,
    val revisadaPorFamiliar: Boolean = false,
    val fechaRevision: Timestamp? = null,
    val comentariosFamiliar: String = "",
    val calificacion: Double? = null,
    val feedbackProfesor: String = ""
)
```

**EntregaTarea**: Representa la entrega de una tarea por parte de un alumno.
```kotlin
data class EntregaTarea(
    @DocumentId
    val id: String = "",
    val tareaId: String = "",
    val alumnoId: String = "",
    val fechaEntrega: Timestamp = Timestamp.now(),
    val archivos: List<String> = emptyList(),
    val comentario: String = "",
    val calificacion: Float? = null,
    val comentarioProfesor: String? = null,
    val fechaCalificacion: Timestamp? = null
)
```

## Próximos pasos (Sprint 2)

- Mejora de la gestión de archivos (carga y descarga)
- Notificaciones para fechas de entrega próximas
- Funcionalidades avanzadas para profesores
- Calendario integrado con eventos académicos

## Tecnologías utilizadas

- Kotlin
- Jetpack Compose
- Firebase (Firestore, Authentication, Storage)
- Hilt para inyección de dependencias
- Coroutines y Flow
- Jetpack Navigation
- Room (para almacenamiento local)
- MockK (para testing)

## Requisitos

- Android 7.0 (API 24) o superior
- Cuenta de Firebase configurada
- Android Studio Arctic Fox o superior

