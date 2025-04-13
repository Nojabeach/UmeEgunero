# UmeEgunero - Aplicación de Gestión Educativa

UmeEgunero es una aplicación Android desarrollada en Kotlin con Jetpack Compose que facilita la comunicación y gestión entre centros educativos, profesores, familiares y alumnos.

## Características principales

- **Autenticación y gestión de usuarios**: Sistema de inicio de sesión y registro para diferentes tipos de usuarios (administradores, centros, profesores y familiares).
- **Interfaz moderna con Jetpack Compose**: Diseño moderno y adaptable utilizando la biblioteca de UI declarativa de Android.
- **Arquitectura MVVM**: Separación clara entre la lógica de negocio y la interfaz de usuario.
- **Firebase como backend**: Firestore para almacenamiento de datos en la nube, autenticación de usuarios y notificaciones.
- **Inyección de dependencias con Hilt**: Gestión eficiente de dependencias.
- **Coroutines y Flow**: Para operaciones asíncronas y flujos de datos reactivos.
- **Modo Offline**: Sincronización robusta que permite trabajar sin conexión a internet.
- **Compatibilidad con Android 14**: Implementación completa de los requisitos para servicios en primer plano.

## Documentación

La documentación completa del proyecto está disponible en la carpeta `/docs`:

- **[Sprint Consolidado](./docs/Sprint_Consolidado.md)**: Resumen del estado actual del proyecto, módulos implementados y próximos pasos.
- **[Manual de Usuario](./docs/Manual_Usuario.md)**: Guía completa para usuarios finales sobre todas las funcionalidades de la aplicación.
- **[Documentación Técnica](./docs/Documentacion_Tecnica.md)**: Información detallada sobre la arquitectura, componentes principales y patrones de diseño.
- **[Guía de Despliegue](./docs/Guia_Despliegue.md)**: Instrucciones paso a paso para la preparación y despliegue de la aplicación.

## Estado actual del proyecto

El proyecto UmeEgunero se encuentra en fase de finalización, con un progreso estimado del 99% en desarrollo y 85% en pruebas básicas. Todas las funcionalidades principales están implementadas y se ha completado con éxito:

- ✅ Unificación de modelos Result/Resultado
- ✅ Optimización de la interfaz de usuario
- ✅ Implementación del sistema de firma digital
- ✅ Compatibilidad con Android 14 (API 34)
- ✅ Sistema de sincronización en modo offline
- ✅ Documentación completa del proyecto

## Requisitos técnicos

- Android Studio Meerkat | 2024.3.1 Patch 1
- Kotlin 1.9.22
- Java JDK 17
- Firebase (google-services.json en la carpeta app/)

## Configuración del proyecto

1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar el proyecto con los archivos Gradle
4. Ejecutar la aplicación

## Estructura del proyecto

La aplicación sigue una arquitectura MVVM con Clean Architecture:

- **app/src/main/java/com/tfg/umeegunero/**
  - **data/**: Capa de datos y modelos
  - **feature/**: Características organizadas por rol (profesor, familiar, admin)
  - **ui/**: Componentes reutilizables de UI
  - **util/**: Utilidades y clases auxiliares
  - **navigation/**: Navegación y rutas de la aplicación

## Funcionalidades principales

### Gestión de usuarios
- Registro y autenticación de diferentes tipos de usuarios
- Perfiles para administradores, profesores y familiares
- Gestión de centros educativos

### Comunicaciones
- Sistema de comunicados entre centro educativo y familias
- Firma digital de documentos importantes
- Historial y archivo de comunicaciones

### Calendario y eventos
- Calendario escolar compartido
- Gestión de eventos y actividades
- Sincronización con calendario del dispositivo

### Gestión de tareas
- Asignación de tareas a alumnos
- Seguimiento del progreso
- Entrega de tareas y calificaciones

### Actividades preescolares
- Catálogo de actividades para alumnos más pequeños
- Filtrado por estado y categoría
- Seguimiento del progreso del alumno

### Sincronización offline
- Trabajo sin conexión a internet
- Sincronización automática de cambios
- Gestión de conflictos

## Tecnologías utilizadas

- Kotlin
- Jetpack Compose
- Material Design 3
- Firebase (Firestore, Authentication, Storage, Cloud Messaging)
- Hilt para inyección de dependencias
- Coroutines y Flow
- Jetpack Navigation
- Room (para almacenamiento local)
- WorkManager (para sincronización)
- MockK (para testing)

## Requisitos

- Android 8.0 (API 26) o superior
- Recomendado: Android 14 (API 34) para funcionalidad completa
- Cuenta de Firebase configurada
- Android Studio Meerkat | 2024.3.1 Patch 1

## Licencia

Este proyecto está licenciado bajo la [Licencia MIT](https://opensource.org/licenses/MIT) - vea el archivo LICENSE para más detalles.

MIT License

Copyright (c) 2025 UmeEgunero

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

