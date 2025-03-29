# UmeEgunero - Aplicación de Gestión Escolar

[![Android](https://img.shields.io/badge/Android-Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose) [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blueviolet.svg)](https://kotlinlang.org/) [![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

## ✨ Descripción del Proyecto

**UmeEgunero** es una aplicación Android moderna desarrollada como parte de un Trabajo Fin de Grado (TFG) en Desarrollo de Aplicaciones Multiplataforma (DAM). Su objetivo es mejorar la comunicación y gestión escolar entre centros educativos, profesores y familias, proporcionando funcionalidades intuitivas y adaptadas a cada perfil de usuario.

---

## 💡 Características Principales

### 🏢 Módulo de Administración
- ✅ Gestión de centros educativos
- ✅ Administración de usuarios y permisos
- ✅ Configuración global del sistema

### 🏫 Módulo de Centro Educativo
- ✅ Gestión de personal docente
- ✅ Administración de aulas y cursos
- ✅ Comunicaciones internas

### 🎨 Módulo de Profesorado
- ✅ Registro de asistencia
- ✅ Gestión de tareas y evaluaciones
- ✅ Comunicación con familias

### 👨‍👩‍👦 Módulo de Familiar
- ✅ Seguimiento del progreso del alumno
- ✅ Comunicación con profesores
- ✅ Acceso a calendarios y eventos

### 🔍 Módulo Común
- ✅ Autenticación y gestión de perfil
- ✅ Notificaciones y mensajería
- ✅ Pantallas de bienvenida y ayuda

---

## 💪 Arquitectura y Tecnologías

### ⚖️ Arquitectura
- 🔹 **MVVM (Model-View-ViewModel)** para una separación clara de responsabilidades.
- 🔹 **Clean Architecture**, estructurando el proyecto en capas bien definidas: `data`, `domain`, `presentation`.
- 🔹 **Feature Modularization**, organizando el código por funcionalidades para mayor mantenibilidad.

### ⚡ Tecnologías Clave
- 👨‍💻 **Kotlin** como lenguaje principal.
- 🌟 **Jetpack Compose** para UI moderna y reactiva.
- 🌬️ **Material Design 3** para diseño adaptable.
- 🌟 **Hilt** para inyección de dependencias.
- 🔐 **Room** para persistencia local.
- 🔧 **Firebase** para autenticación y base de datos en la nube.
- 🌟 **Corrutinas y Flow** para programación asíncrona.
- 🌟 **Retrofit** para consumo de APIs.
- 🌟 **WorkManager** para tareas en segundo plano.
- 🌟 **Navigation Compose** para navegación eficiente.

---

## 📁 Estructura del Proyecto

```bash
app/
├── src/
│   ├── main/
│   │   ├── java/com/tfg/umeegunero/
│   │   │   ├── data/            # Capa de datos
│   │   │   │   ├── local/       # Room, DataStore
│   │   │   │   ├── remote/      # Firebase, APIs
│   │   │   │   ├── repository/  # Implementaciones de repositorios
│   │   │   │   ├── model/       # Modelos de datos
│   │   │   │   ├── worker/      # WorkManager
│   │   │   │
│   │   │   ├── di/              # Inyección de dependencias con Hilt
│   │   │   ├── feature/         # Módulos organizados por rol
│   │   │   ├── navigation/      # Sistema de navegación
│   │   │   ├── ui/              # UI y temas
│   │   │   ├── util/            # Utilidades y extensiones
│   │   │   ├── MainActivity.kt  # Punto de entrada
│   │   │   └── UmeEguneroApp.kt # Clase Application
│   │   └── res/                 # Recursos
│   └── test/                    # Tests unitarios e instrumentados
├── build.gradle.kts             # Configuración de Gradle
└── proguard-rules.pro           # Reglas de ProGuard
```

---

## 🛠️ Configuración del Entorno de Desarrollo

### ⚡ Requisitos
- Android Studio **Hedgehog (2023.1.1)** o superior
- JDK **17**
- Gradle **8.9.0**
- Android SDK **35 (compilación) / 29 (mínimo)**

### ♻️ Pasos para Compilar y Ejecutar
```bash
# Clonar el repositorio
git clone https://github.com/tuusuario/UmeEgunero.git
cd UmeEgunero

# Abrir en Android Studio y sincronizar Gradle
# Ejecutar la app en un emulador o dispositivo físico
```

---

## 📃 Documentación Técnica

La documentación técnica se genera con **Dokka**:
```bash
./gradlew dokkaHtml
```
Salida en: `app/build/dokka/`

.[!NOTE].
> Esta documentación se actualiza automáticamente con cada commit en la rama principal.

---

## 👤 Autor

Desarrollado como TFG en DAM. Para más información, contacta a [maitaneibaira@gmail.com](mailto:maitaneibaira@gmail.com).

.[!TIP].
> Si te gusta el proyecto, dale una estrella ⭐ en GitHub.

