# UmeEgunero - Aplicación de Gestión Escolar

## Descripción General

UmeEgunero es una aplicación de gestión escolar completa diseñada para facilitar la comunicación y administración entre centros educativos, profesores y familias. La aplicación proporciona herramientas específicas para cada perfil de usuario, creando un ecosistema educativo integral.

## Arquitectura

La aplicación sigue una arquitectura MVVM (Model-View-ViewModel) combinada con Clean Architecture, organizando el código en capas claras y bien definidas:

- **Presentation Layer**: Implementada con Jetpack Compose
- **Domain Layer**: Contiene la lógica de negocio y casos de uso
- **Data Layer**: Gestiona el acceso a datos locales y remotos

## Tecnologías Principales

- **Kotlin**: Lenguaje principal de desarrollo
- **Jetpack Compose**: Framework UI declarativo
- **Material 3**: Diseño visual
- **Hilt**: Inyección de dependencias
- **Room**: Persistencia de datos local
- **Firebase**: Autenticación y almacenamiento en la nube
- **Coroutines y Flow**: Programación asíncrona

## Características por Perfil

### Administrador
- Gestión global del sistema
- Monitorización de estadísticas
- Configuración de centros educativos

### Centro Educativo
- Gestión de profesores y aulas
- Administración de cursos y clases
- Seguimiento académico

### Profesor
- Registro diario de alumnos
- Comunicación con familias
- Gestión de evaluaciones

### Familia
- Seguimiento del progreso de los alumnos
- Comunicación con profesores
- Consulta de información académica

## Documentación

Esta documentación ha sido generada con Dokka y personalizada para reflejar la identidad visual de UmeEgunero. Proporciona información detallada sobre las clases, funciones y componentes que conforman la aplicación.

---
Desarrollado por Maitane Ibañez Irazabal | 2024 