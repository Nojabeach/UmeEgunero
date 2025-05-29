# Gestión de Claves API en UmeEgunero

Este documento explica cómo se gestionan las claves API y otros datos sensibles en el proyecto UmeEgunero.

## Principio fundamental

**Las claves API y datos sensibles nunca deben subirse al control de versiones.**

## Configuración mediante local.properties

Todas las claves API y configuraciones sensibles se almacenan en el archivo `local.properties`, que está excluido del control de versiones mediante `.gitignore`.

### Pasos para configurar tu entorno

1. Copia el archivo `local.properties.example` a `local.properties`
2. Completa los valores reales para cada propiedad
3. Android Studio utilizará automáticamente estos valores

### Propiedades requeridas

```properties
# ImgBB
IMGBB_API_KEY=tu_clave_api_aqui

# SendGrid
SENDGRID_API_KEY=tu_clave_api_aqui
SENDGRID_FROM_EMAIL=tu_email_aqui
SENDGRID_FROM_NAME=Centro Educativo UmeEgunero

# URL del script de Google Apps Script
EMAIL_SCRIPT_URL=url_del_script_aqui

# Google Maps
GOOGLE_MAPS_API_KEY=tu_clave_api_aqui

# Firebase (obtener del google-services.json)
FIREBASE_API_KEY=tu_clave_api_aqui
FIREBASE_APPLICATION_ID=tu_id_aplicacion_aqui
FIREBASE_PROJECT_ID=umeegunero

# DNI del administrador principal
ADMIN_PRINCIPAL_DNI=dni_aqui

# Remote Config Password Key
REMOTE_CONFIG_PASSWORD_KEY=defaultAdminPassword
```

## Cómo se utilizan las claves en el código

Las claves se definen como campos de `BuildConfig` en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "IMGBB_API_KEY", "\"${localProperties.getProperty("IMGBB_API_KEY", "")}\"")
buildConfigField("String", "SENDGRID_API_KEY", "\"${localProperties.getProperty("SENDGRID_API_KEY", "")}\"")
// ... otras claves
```

Luego, en el código, se accede a estas claves mediante:

```kotlin
val apiKey = BuildConfig.IMGBB_API_KEY
```

## Seguridad adicional

- No incluyas claves API en constantes hardcodeadas
- No subas archivos de configuración con claves (como google-services.json)
- Utiliza siempre BuildConfig o métodos seguros para acceder a las claves
- Considera ofuscar las claves en releases de producción 