# UmeEgunero - Aplicación de Gestión Escolar

## Descripción del Proyecto

**UmeEgunero** es una aplicación Android moderna desarrollada como TFG (Trabajo Fin de Grado) para el Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma (DAM). La aplicación está diseñada para facilitar la comunicación y gestión entre centros educativos, profesores y familias, ofreciendo diversas funcionalidades según el rol del usuario.

## Características Principales

La aplicación cuenta con módulos específicos para cada tipo de usuario:

### Módulo de Administración
- Gestión de centros educativos
- Administración de usuarios y permisos
- Configuración global del sistema

### Módulo de Centro Educativo
- Gestión de personal docente
- Administración de aulas y cursos
- Comunicaciones internas

### Módulo de Profesorado
- Registro de asistencia
- Gestión de tareas y evaluaciones
- Comunicación con familias

### Módulo de Familiar
- Seguimiento del progreso del alumno
- Comunicación con profesores
- Acceso a calendarios y eventos

### Módulo Común
- Autenticación y gestión de perfil
- Notificaciones y mensajería
- Pantallas de bienvenida y ayuda

## Arquitectura y Tecnologías

### Arquitectura
- **Patrón MVVM (Model-View-ViewModel)** para separación de responsabilidades
- **Clean Architecture** con capas bien definidas (data, domain, presentation)
- **Feature Modularization** para organizar el código por funcionalidades

### Tecnologías Principales
- **Kotlin** como lenguaje de programación principal
- **Jetpack Compose** para la interfaz de usuario
- **Material Design 3** para el diseño visual
- **Hilt** para inyección de dependencias
- **Room** para persistencia local
- **Firebase** para autenticación y base de datos en la nube
- **Corrutinas y Flow** para programación asíncrona
- **Retrofit** para comunicación con APIs
- **WorkManager** para tareas en segundo plano
- **Navigation Compose** para la navegación entre pantallas

## Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/tfg/umeegunero/
│   │   │   ├── data/            # Capa de datos
│   │   │   │   ├── local/       # Fuentes de datos locales (Room, DataStore)
│   │   │   │   ├── remote/      # Fuentes de datos remotas (Firebase, APIs)
│   │   │   │   ├── repository/  # Implementaciones de repositorios
│   │   │   │   ├── model/       # Modelos de datos
│   │   │   │   └── worker/      # WorkManager para tareas en segundo plano
│   │   │   │
│   │   │   ├── di/              # Módulos de inyección de dependencias con Hilt
│   │   │   │
│   │   │   ├── feature/         # Módulos de características organizados por rol
│   │   │   │   ├── admin/       # Funcionalidades de administración
│   │   │   │   ├── auth/        # Autenticación y registro
│   │   │   │   ├── centro/      # Gestión de centros educativos
│   │   │   │   ├── common/      # Componentes comunes entre módulos
│   │   │   │   ├── familiar/    # Funcionalidades para familiares
│   │   │   │   └── profesor/    # Funcionalidades para profesores
│   │   │   │
│   │   │   ├── navigation/      # Sistema de navegación con Compose Navigation
│   │   │   │
│   │   │   ├── ui/              # Componentes de UI y temas
│   │   │   │   ├── components/  # Componentes reutilizables de UI
│   │   │   │   └── theme/       # Tema de la aplicación (colores, tipografía)
│   │   │   │
│   │   │   ├── util/            # Utilidades y extensiones
│   │   │   │
│   │   │   ├── MainActivity.kt  # Punto de entrada de la aplicación
│   │   │   └── UmeEguneroApp.kt # Clase Application
│   │   │
│   │   └── res/                 # Recursos (imágenes, strings, etc.)
│   │
│   └── test/                    # Tests unitarios e instrumentados
│
├── build.gradle.kts             # Configuración de Gradle del módulo
└── proguard-rules.pro           # Reglas de ProGuard
```

## Configuración del Entorno de Desarrollo

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Gradle 8.9.0
- Android SDK 35 (compilación) y 29 (mínimo)

### Pasos para Compilar y Ejecutar
1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar el proyecto con los archivos Gradle
4. Ejecutar la aplicación en un emulador o dispositivo físico con API 29 o superior

## Documentación Técnica

La documentación técnica completa se genera con Dokka y está disponible en formato HTML. Para generarla, ejecute:

```bash
./gradlew dokkaHtml
```

La documentación generada estará disponible en: `app/build/dokka/`

## Autor

Realizado como TFG del Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma (2º DAM). 