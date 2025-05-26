# Configuración de Google Apps Script - UmeEgunero

## 📋 Información General

Este documento contiene la configuración completa de Google Apps Script para el proyecto UmeEgunero, incluyendo webhooks, permisos y deployment.

## 🌐 Información del Proyecto

### Datos Básicos
- **Script ID:** (Generado automáticamente por Google)
- **Nombre del Proyecto:** UmeEgunero Webhook Handler
- **Tipo:** Web App
- **Runtime:** V8
- **Zona Horaria:** Europe/Madrid

### URL del Deployment
```
https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec
```

## 🔧 Configuración del Manifest (appsscript.json)

### Archivo Completo
```json
{
  "timeZone": "Europe/Madrid",
  "dependencies": {
    "enabledAdvancedServices": []
  },
  "exceptionLogging": "STACKDRIVER",
  "runtimeVersion": "V8",
  "oauthScopes": [
    "https://www.googleapis.com/auth/script.external_request",
    "https://www.googleapis.com/auth/firebase.messaging"
  ]
}
```

### Explicación de Configuraciones

#### Zona Horaria
```json
"timeZone": "Europe/Madrid"
```
- Configura la zona horaria para logs y timestamps
- Importante para sincronización con el sistema español

#### Runtime
```json
"runtimeVersion": "V8"
```
- Usa el motor JavaScript V8 moderno
- Soporte para ES6+ y mejor rendimiento

#### Logging
```json
"exceptionLogging": "STACKDRIVER"
```
- Envía logs de errores a Google Cloud Logging
- Facilita debugging y monitoreo

#### Permisos OAuth
```json
"oauthScopes": [
  "https://www.googleapis.com/auth/script.external_request",
  "https://www.googleapis.com/auth/firebase.messaging"
]
```

## 🔐 Configuración de Permisos

### Permisos Requeridos

#### 1. External Requests
```
https://www.googleapis.com/auth/script.external_request
```
- **Propósito:** Realizar llamadas HTTP a APIs externas
- **Uso:** Enviar notificaciones a Firebase Cloud Messaging
- **Necesario para:** `UrlFetchApp.fetch()`

#### 2. Firebase Messaging
```
https://www.googleapis.com/auth/firebase.messaging
```
- **Propósito:** Acceso a Firebase Cloud Messaging API
- **Uso:** Enviar notificaciones push a dispositivos
- **Necesario para:** Autenticación con FCM

### Configuración de Acceso

#### Web App Settings
```json
{
  "access": "Anyone",
  "executeAs": "User accessing the web app",
  "deploymentType": "HEAD"
}
```

- **Access:** `Anyone` - Permite webhooks desde Cloud Functions
- **Execute As:** `User accessing` - Ejecuta con permisos del propietario
- **Deployment:** `HEAD` - Siempre usa la versión más reciente

## 📡 Configuración de Webhooks

### Endpoint Principal
```
POST https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec
```

### Headers Esperados
```http
Content-Type: application/json
User-Agent: Firebase Cloud Functions
```

### Payload Structure
```javascript
{
  "messageId": "string",
  "senderId": "string", 
  "participantsIds": ["string"],
  "messageType": "CHAT|ANNOUNCEMENT|INCIDENT|etc",
  "messageContent": "string",
  "messageTitle": "string"
}
```

### Response Format
```javascript
// Éxito
{
  "success": true,
  "message": "Notificación procesada correctamente",
  "data": {
    "tokensProcessed": 5,
    "notificationsSent": 4,
    "errors": ["Error en token 3: Invalid token"]
  }
}

// Error
{
  "success": false,
  "error": "Payload inválido: falta messageId",
  "timestamp": "2025-05-26T15:31:37.000Z"
}
```

## 🔧 Configuración de Firebase Integration

### Variables de Configuración
```javascript
const FIREBASE_PROJECT_ID = "umeegunero";
const FCM_API_URL = `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`;
```

### Autenticación
```javascript
function getAccessToken() {
  try {
    // Obtener token OAuth automáticamente
    const token = ScriptApp.getOAuthToken();
    return token;
  } catch (error) {
    Logger.log(`Error obteniendo token: ${error.toString()}`);
    return null;
  }
}
```

### Estructura de Mensaje FCM
```javascript
const fcmMessage = {
  message: {
    token: "device_token_here",
    notification: {
      title: "Título de la notificación",
      body: "Contenido del mensaje"
    },
    data: {
      messageId: "msg_123",
      messageType: "CHAT",
      senderId: "user_456",
      click_action: "OPEN_CHAT"
    },
    android: {
      priority: "high",
      notification: {
        channel_id: "channel_chat"
      }
    },
    apns: {
      payload: {
        aps: {
          alert: {
            title: "Título",
            body: "Contenido"
          },
          sound: "default"
        }
      }
    }
  }
};
```

## 📊 Configuración de Logging

### Niveles de Log
```javascript
// Información general
Logger.log("Información general");

// Errores
console.error("Error crítico");

// Debug (solo en desarrollo)
console.log("Debug info");
```

### Estructura de Logs
```javascript
// Log de webhook recibido
Logger.log(`Webhook recibido: ${JSON.stringify(data)}`);

// Log de procesamiento
Logger.log(`Procesando notificación para ${participantsIds.length} participantes`);

// Log de resultados
Logger.log(`Notificaciones enviadas: ${successCount} éxitos, ${errorCount} fallos`);

// Log de errores
Logger.log(`Error en token ${index}: ${error.message}`);
```

### Acceso a Logs
1. **Google Apps Script Editor:**
   - Ir a "Ejecuciones" en el menú lateral
   - Seleccionar ejecución específica
   - Ver logs detallados

2. **Google Cloud Console:**
   - Ir a Cloud Logging
   - Filtrar por proyecto y recurso
   - Ver logs en tiempo real

## 🚀 Configuración de Deployment

### Proceso de Deployment

#### 1. Preparación
```bash
# Verificar código en el editor
# Ejecutar función de prueba
testWebhook();
```

#### 2. Deployment
1. Ir a "Desplegar" → "Nueva implementación"
2. Seleccionar tipo "Aplicación web"
3. Configurar descripción: "UmeEgunero Webhook Handler v1.0"
4. Configurar acceso: "Cualquiera"
5. Hacer clic en "Desplegar"

#### 3. Obtener URL
```javascript
// La URL se genera automáticamente
const deploymentUrl = "https://script.google.com/macros/s/[SCRIPT_ID]/exec";
```

#### 4. Actualizar Cloud Functions
```javascript
// En Cloud Functions, actualizar la variable
const APPS_SCRIPT_WEB_APP_URL = "nueva_url_aqui";
```

### Versionado
```javascript
// Información de versión en el código
const VERSION_INFO = {
  version: "1.0",
  lastUpdated: "2025-05-26",
  author: "UmeEgunero Team",
  description: "Webhook handler para notificaciones FCM"
};
```

## 🔄 Configuración de Integración

### Con Cloud Functions
```javascript
// En Cloud Functions
const response = await fetch(APPS_SCRIPT_WEB_APP_URL, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify(payload),
});
```

### Con Firebase
```javascript
// Configuración de proyecto Firebase
const firebaseConfig = {
  projectId: "umeegunero",
  messagingSenderId: "1045944201521"
};
```

## 📈 Configuración de Rendimiento

### Límites de Google Apps Script
- **Tiempo de ejecución:** 6 minutos máximo
- **Llamadas HTTP:** 20,000 por día
- **Tamaño de respuesta:** 50MB máximo
- **Concurrencia:** Limitada por Google

### Optimizaciones
```javascript
// Procesamiento en lotes
const BATCH_SIZE = 100;
for (let i = 0; i < tokens.length; i += BATCH_SIZE) {
  const batch = tokens.slice(i, i + BATCH_SIZE);
  processBatch(batch);
}

// Timeout para requests
const options = {
  method: "POST",
  headers: headers,
  payload: payload,
  muteHttpExceptions: true,
  timeout: 30 // 30 segundos
};
```

## 🛠️ Configuración de Desarrollo

### Variables de Entorno (Simuladas)
```javascript
// Configuración para diferentes entornos
const CONFIG = {
  development: {
    fcmUrl: "https://fcm.googleapis.com/v1/projects/umeegunero-dev/messages:send",
    logLevel: "DEBUG"
  },
  production: {
    fcmUrl: "https://fcm.googleapis.com/v1/projects/umeegunero/messages:send",
    logLevel: "INFO"
  }
};

const currentConfig = CONFIG.production; // Cambiar según entorno
```

### Funciones de Prueba
```javascript
// Función para probar el webhook
function testWebhook() {
  const testData = {
    messageId: "test-123",
    senderId: "test-user",
    participantsIds: ["user1", "user2"],
    messageType: "CHAT",
    messageContent: "Mensaje de prueba",
    messageTitle: "Prueba"
  };
  
  const result = processNotification(testData);
  Logger.log(`Resultado de prueba: ${JSON.stringify(result)}`);
}

// Función para obtener información del deployment
function getDeploymentInfo() {
  return {
    scriptId: ScriptApp.getScriptId(),
    url: "https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec",
    version: "1.0",
    lastUpdated: new Date().toISOString()
  };
}
```

## 🆘 Configuración de Troubleshooting

### Debugging
```javascript
// Habilitar debug detallado
const DEBUG_MODE = true;

function debugLog(message) {
  if (DEBUG_MODE) {
    Logger.log(`[DEBUG] ${message}`);
  }
}
```

### Manejo de Errores
```javascript
// Configuración de retry
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // 1 segundo

function sendWithRetry(url, options, retries = 0) {
  try {
    return UrlFetchApp.fetch(url, options);
  } catch (error) {
    if (retries < MAX_RETRIES) {
      Utilities.sleep(RETRY_DELAY);
      return sendWithRetry(url, options, retries + 1);
    }
    throw error;
  }
}
```

## 📅 Información de Versiones

- **Google Apps Script Runtime:** V8
- **Última actualización:** 26 de Mayo de 2025
- **Versión del código:** 1.0
- **Estado:** Activo y funcionando

## 📞 Contactos de Soporte

- **Google Apps Script:** https://developers.google.com/apps-script
- **Documentación:** https://developers.google.com/apps-script/reference
- **Stack Overflow:** Tag `google-apps-script`
- **Proyecto UmeEgunero:** Documentación interna del equipo 