# Guía de Despliegue - UmeEgunero

## Introducción

Esta guía proporciona los pasos necesarios para preparar y desplegar la aplicación UmeEgunero en entornos de prueba y producción. Se detallan los requisitos previos, la configuración del entorno, los pasos para generar el APK firmado y las recomendaciones para publicar la aplicación en Google Play Store.

## Requisitos Previos

### Herramientas y Software Necesario

1. **Android Studio**: Versión Arctic Fox (2021.3.1) o superior.
2. **JDK**: Versión 11 o superior.
3. **Git**: Para gestión del código fuente.
4. **Firebase CLI**: Para gestionar la configuración de Firebase.
5. **Cuenta de Desarrollador de Google Play**: Para publicar la aplicación en producción.

### Accesos y Credenciales

1. **Repositorio de Código**: Acceso al repositorio Git del proyecto.
2. **Consola de Firebase**: Acceso con permisos de administrador al proyecto Firebase.
3. **Keystore para Firma**: Archivo keystore y contraseñas para firmar la aplicación.
4. **Cuenta Google Play Console**: Acceso para gestionar la publicación de la aplicación.

## Configuración del Entorno de Desarrollo

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tfgcorp/umeegunero.git
cd umeegunero
```

### 2. Configurar Firebase

1. **Crear Proyecto en Firebase** (si aún no existe):
   - Acceder a [Firebase Console](https://console.firebase.google.com/)
   - Crear un nuevo proyecto o seleccionar uno existente
   - Añadir una aplicación Android con el paquete `com.tfg.umeegunero`

2. **Descargar y Configurar google-services.json**:
   - Descargar el archivo `google-services.json` desde la consola de Firebase
   - Colocarlo en la carpeta `app/` del proyecto

3. **Configurar Servicios de Firebase**:
   - Habilitar Authentication
   - Configurar Firestore Database
   - Habilitar Storage
   - Configurar Cloud Messaging

### 3. Configurar Variables de Entorno

Editar el archivo `local.properties` (no incluido en el control de versiones) para añadir:

```properties
sdk.dir=/ruta/al/android/sdk
keystore.path=/ruta/al/keystore.jks
keystore.password=contraseña_del_keystore
keystore.alias=alias_de_la_clave
keystore.alias.password=contraseña_del_alias
```

## Preparación para el Despliegue

### 1. Actualizar Versión de la Aplicación

Editar el archivo `app/build.gradle.kts` para actualizar:

```kotlin
android {
    defaultConfig {
        // Incrementar para cada nueva versión
        versionCode = ACTUAL_VERSION_CODE + 1
        versionName = "X.Y.Z" // Siguiendo SemVer
    }
}
```

### 2. Configurar Reglas de ProGuard

Revisar el archivo `app/proguard-rules.pro` para asegurar que las reglas son correctas:

```
# Reglas específicas para Firebase
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }

# Reglas para Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Reglas para Jetpack Compose
-keepclasseswithmembers class androidx.compose.** { *; }
```

### 3. Preparar Recursos para Diferentes Densidades

Verificar que las imágenes y recursos están disponibles en diferentes densidades:
- `drawable-mdpi`
- `drawable-hdpi`
- `drawable-xhdpi`
- `drawable-xxhdpi`
- `drawable-xxxhdpi`

## Generación de APK/Bundle para Diferentes Entornos

### 1. Entorno de Desarrollo

```bash
./gradlew assembleDebug
```

El APK se generará en: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Entorno de Pruebas

```bash
./gradlew assembleQa
```

El APK se generará en: `app/build/outputs/apk/qa/app-qa.apk`

### 3. Entorno de Producción (APK)

```bash
./gradlew assembleRelease
```

El APK firmado se generará en: `app/build/outputs/apk/release/app-release.apk`

### 4. Entorno de Producción (Bundle)

```bash
./gradlew bundleRelease
```

El Bundle se generará en: `app/build/outputs/bundle/release/app-release.aab`

## Verificación y Pruebas

### 1. Verificar la Firma del APK

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### 2. Pruebas en Dispositivos Reales

Instalar y probar la aplicación en dispositivos reales con diferentes:
- Versiones de Android (especialmente Android 14)
- Tamaños de pantalla
- Densidades de píxeles
- Fabricantes (Samsung, Xiaomi, Google, etc.)

### 3. Validar Permisos y Funcionalidades

Verificar que todos los permisos y funcionalidades funcionan correctamente:
- Notificaciones
- Sincronización en segundo plano
- Almacenamiento de archivos
- Comunicación con Firebase

## Despliegue en Google Play Store

### 1. Preparar Recursos para Google Play Store

- **Icono de la Aplicación**: 512x512 px, PNG o JPEG, máx. 1MB
- **Imagen de Característica Gráfica**: 1024x500 px, PNG o JPEG
- **Capturas de Pantalla**: Al menos 2 para cada tipo de dispositivo
- **Vídeo Promocional**: Opcional, enlace a YouTube

### 2. Crear Ficha de la Aplicación

1. Acceder a [Google Play Console](https://play.google.com/console)
2. Crear una nueva aplicación o seleccionar la existente
3. Configurar:
   - Detalles del producto
   - Clasificación de contenido
   - Precio y distribución
   - Configuración de la aplicación

### 3. Subir el APK/Bundle

1. En Google Play Console, ir a "Versiones de la aplicación"
2. Seleccionar "Producción", "Pruebas abiertas", "Pruebas cerradas" o "Pruebas internas"
3. Subir el archivo APK o Bundle
4. Completar la información de la versión (notas de lanzamiento)

### 4. Revisión y Publicación

1. Completar toda la información requerida
2. Verificar que no hay problemas en el panel de Google Play Console
3. Enviar para revisión
4. Una vez aprobada, publicar la aplicación

## Checklist de Publicación en Google Play

- [ ] Incrementar versión y nombre de versión
- [ ] Firmar APK/AAB con keystore de producción
- [ ] Revisar permisos solicitados
- [ ] Probar la app en dispositivos reales y emuladores
- [ ] Revisar strings y metadatos
- [ ] Subir capturas de pantalla y descripción
- [ ] Completar clasificación de contenido
- [ ] Revisar políticas de privacidad y protección de datos

## Recomendaciones para el TFG

- Documentar todos los pasos y decisiones de despliegue.
- Adjuntar capturas de la app publicada y validación en Play Console.
- Incluir en la memoria referencias a esta guía y checklist.

## Monitorización Post-Despliegue

### 1. Monitorizar Firebase Analytics

Verificar en la consola de Firebase:
- Número de usuarios activos
- Errores y fallos
- Uso de funcionalidades
- Rendimiento de la aplicación

### 2. Monitorizar Google Play Console

Verificar en Google Play Console:
- Valoraciones y reseñas
- Errores ANR (Application Not Responding)
- Instalaciones y desinstalaciones
- Conversión de usuarios

### 3. Configurar Alertas

Configurar alertas para:
- Caídas en la valoración de la aplicación
- Aumento de errores
- Problemas de rendimiento
- Seguridad y vulnerabilidades

## Procedimiento de Rollback

En caso de detectar problemas críticos:

### 1. Suspender el Despliegue

En Google Play Console:
1. Ir a "Versiones de la aplicación"
2. Seleccionar la versión problemática
3. Elegir "Detener lanzamiento"

### 2. Volver a una Versión Anterior

1. En Google Play Console, ir a "Versiones de la aplicación"
2. Seleccionar una versión anterior estable
3. Elegir "Lanzar a producción"

### 3. Comunicar a los Usuarios

Notificar a los usuarios sobre el problema y la solución a través de:
- Redes sociales
- Correo electrónico
- Dentro de la aplicación (si es posible)

## Anexos

### Anexo I: Estructura de Directorios

```
umeegunero/
├── app/                           # Módulo principal de la aplicación
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tfg/umeegunero/  # Código fuente
│   │   │   ├── res/               # Recursos
│   │   │   └── AndroidManifest.xml
│   │   ├── debug/                 # Configuración para debug
│   │   ├── qa/                    # Configuración para QA
│   │   └── release/               # Configuración para release
│   ├── build.gradle.kts           # Configuración de construcción
│   └── proguard-rules.pro         # Reglas de ProGuard
├── build.gradle.kts               # Configuración global
├── gradle/                        # Wrapper de Gradle
├── settings.gradle.kts            # Configuración de módulos
└── firebase/                      # Configuración de Firebase
```

### Anexo II: Checklist de Despliegue

- [ ] Versión actualizada en build.gradle.kts
- [ ] google-services.json configurado correctamente
- [ ] Keystore disponible y configurado
- [ ] Reglas de ProGuard verificadas
- [ ] Recursos optimizados para todas las densidades
- [ ] Permisos correctamente declarados en AndroidManifest.xml
- [ ] Pruebas realizadas en múltiples dispositivos
- [ ] Metadatos de Google Play Store actualizados
- [ ] Recursos visuales preparados para Google Play
- [ ] Notas de la versión escritas y traducidas