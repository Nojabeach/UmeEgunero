# Configuración de Claves de API para UmeEgunero

Este documento describe cómo configurar correctamente las claves de API y credenciales para el proyecto UmeEgunero.

## Archivos Protegidos

Los siguientes archivos contienen información sensible y **NO deben ser subidos** a repositorios públicos:

- `app/google-services.json` - Configuración de Firebase
- `app/src/main/java/com/tfg/umeegunero/util/ApiConfigLocal.java` - Claves de API locales
- `sendgrid.env` - Variables de entorno para SendGrid

## Configuración de SendGrid

UmeEgunero utiliza SendGrid para el envío de emails. Para configurar SendGrid:

1. Crea una cuenta en [SendGrid](https://sendgrid.com/)
2. Genera una API Key en el panel de control de SendGrid
3. Crea un archivo `ApiConfigLocal.java` con el siguiente contenido:

```java
package com.tfg.umeegunero.util;

public class ApiConfigLocal {
    // SendGrid API Key
    public static final String SENDGRID_API_KEY = "TU_API_KEY_DE_SENDGRID";
    
    // URL del script de Google Apps Script
    public static final String EMAIL_SCRIPT_URL = "https://script.google.com/macros/s/TU_ID_DE_SCRIPT/exec";
}
```

## Configuración de Firebase

Para configurar Firebase:

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agrega una aplicación Android y descarga el archivo `google-services.json`
3. Coloca este archivo en el directorio `app/` del proyecto

## Configuración del Script de Email

UmeEgunero utiliza un Google Apps Script para el envío de emails. Para configurarlo:

1. Crea un nuevo script en [Google Apps Script](https://script.google.com/)
2. Implementa la lógica de envío de emails (consulta los ejemplos en la documentación)
3. Despliega el script como una aplicación web
4. Copia la URL del script en el archivo `ApiConfigLocal.java`

## Consideraciones de Seguridad

- **Nunca** subas archivos con claves API a repositorios públicos
- Utiliza variables de entorno para configurar claves en entornos CI/CD
- Considera usar Firebase Remote Config para gestionar claves de forma remota y segura

## Variables de Entorno

Para configurar las variables de entorno de SendGrid (útil para desarrollo local):

```bash
echo "export SENDGRID_API_KEY='TU_API_KEY'" > sendgrid.env
echo "sendgrid.env" >> .gitignore
source ./sendgrid.env
```

## Documentación Relacionada

- [Documentación de SendGrid para Java](https://github.com/sendgrid/sendgrid-java)
- [Documentación de Firebase para Android](https://firebase.google.com/docs/android/setup)
- [Guía de Google Apps Script](https://developers.google.com/apps-script) 