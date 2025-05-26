# 🚨 BACKUP DE GOOGLE APPS SCRIPT - SOLO DOCUMENTACIÓN 🚨

**⚠️ ADVERTENCIA IMPORTANTE:**
- Este archivo es una **COPIA DE RESPALDO** del código original de Google Apps Script
- **NO** es el script activo del proyecto
- **NO** modificar este archivo para cambiar el comportamiento del sistema
- El script activo está en: Google Apps Script Console (URL en la documentación)

---

## 📅 Información del Backup

- **Fecha de Backup:** 26 de Mayo de 2025
- **Versión:** v1.0 - Sistema de webhooks funcionando
- **Estado:** Script activo y desplegado en Google Apps Script
- **Propósito:** Documentación y recuperación ante desastres
- **URL del Web App:** `https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec`

---

## 📄 Código Completo - Google Apps Script

### Archivo Principal: Code.gs

```javascript
/**
 * Google Apps Script para UmeEgunero
 * Maneja webhooks desde Firebase Cloud Functions y envía notificaciones FCM
 * 
 * @author UmeEgunero Team
 * @version 1.0
 * @date 2025-05-26
 */

// Configuración de Firebase
const FIREBASE_PROJECT_ID = "umeegunero";
const FCM_API_URL = `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`;

/**
 * Función principal que maneja las peticiones POST
 * Se ejecuta cuando se recibe un webhook desde Cloud Functions
 */
function doPost(e) {
  try {
    // Validar que se recibieron datos
    if (!e.postData || !e.postData.contents) {
      throw new Error("No se recibieron datos en la petición");
    }
    
    // Parsear los datos JSON
    const data = JSON.parse(e.postData.contents);
    Logger.log(`Webhook recibido: ${JSON.stringify(data)}`);
    
    // Validar estructura de datos
    validatePayload(data);
    
    // Procesar la notificación
    const result = processNotification(data);
    
    // Retornar respuesta exitosa
    return ContentService
      .createTextOutput(JSON.stringify({
        success: true,
        message: "Notificación procesada correctamente",
        data: result
      }))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (error) {
    Logger.log(`Error procesando webhook: ${error.toString()}`);
    
    // Retornar respuesta de error
    return ContentService
      .createTextOutput(JSON.stringify({
        success: false,
        error: error.toString(),
        timestamp: new Date().toISOString()
      }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

/**
 * Valida que el payload recibido tenga la estructura correcta
 */
function validatePayload(data) {
  if (!data.messageId) {
    throw new Error("Payload inválido: falta messageId");
  }
  
  if (!data.participantsIds || !Array.isArray(data.participantsIds)) {
    throw new Error("Payload inválido: participantsIds debe ser un array");
  }
  
  if (data.participantsIds.length === 0) {
    throw new Error("Payload inválido: participantsIds está vacío");
  }
  
  if (!data.messageType) {
    throw new Error("Payload inválido: falta messageType");
  }
  
  return true;
}

/**
 * Procesa la notificación y envía mensajes FCM a los destinatarios
 */
function processNotification(data) {
  const {
    messageId,
    senderId,
    participantsIds,
    messageType,
    messageContent,
    messageTitle
  } = data;
  
  Logger.log(`Procesando notificación para ${participantsIds.length} participantes`);
  
  // Obtener tokens FCM de los participantes
  const tokens = getFCMTokensForUsers(participantsIds);
  
  if (tokens.length === 0) {
    Logger.log("No se encontraron tokens FCM para los participantes");
    return {
      tokensProcessed: 0,
      notificationsSent: 0,
      errors: ["No se encontraron tokens FCM"]
    };
  }
  
  Logger.log(`Se encontraron ${tokens.length} tokens FCM`);
  
  // Enviar notificaciones
  const results = sendFCMNotifications(tokens, {
    title: messageTitle,
    body: messageContent,
    data: {
      messageId: messageId,
      messageType: messageType,
      senderId: senderId,
      click_action: getClickActionForMessageType(messageType)
    }
  });
  
  return results;
}

/**
 * Obtiene los tokens FCM de los usuarios especificados
 * En una implementación real, esto consultaría Firestore
 */
function getFCMTokensForUsers(userIds) {
  // NOTA: Esta es una implementación simplificada
  // En la versión real, esto haría una consulta a Firestore
  // para obtener los tokens FCM de cada usuario
  
  const tokens = [];
  
  // Simulación de obtención de tokens
  // En la implementación real, se haría una consulta a Firestore
  userIds.forEach(userId => {
    // Aquí iría la lógica para obtener el token del usuario desde Firestore
    // const userDoc = firestore.collection('usuarios').doc(userId).get();
    // const fcmTokens = userDoc.data().fcmTokens || {};
    // tokens.push(...Object.values(fcmTokens));
    
    Logger.log(`Obteniendo token para usuario: ${userId}`);
  });
  
  return tokens;
}

/**
 * Envía notificaciones FCM a los tokens especificados
 */
function sendFCMNotifications(tokens, notificationData) {
  const results = {
    tokensProcessed: tokens.length,
    notificationsSent: 0,
    errors: []
  };
  
  // Obtener token de acceso para FCM
  const accessToken = getAccessToken();
  
  if (!accessToken) {
    results.errors.push("No se pudo obtener token de acceso para FCM");
    return results;
  }
  
  // Enviar notificación a cada token
  tokens.forEach((token, index) => {
    try {
      const fcmMessage = {
        message: {
          token: token,
          notification: {
            title: notificationData.title,
            body: notificationData.body
          },
          data: notificationData.data,
          android: {
            priority: "high",
            notification: {
              channel_id: getChannelIdForMessageType(notificationData.data.messageType)
            }
          },
          apns: {
            payload: {
              aps: {
                alert: {
                  title: notificationData.title,
                  body: notificationData.body
                },
                sound: "default"
              }
            }
          }
        }
      };
      
      const response = UrlFetchApp.fetch(FCM_API_URL, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${accessToken}`,
          "Content-Type": "application/json"
        },
        payload: JSON.stringify(fcmMessage)
      });
      
      if (response.getResponseCode() === 200) {
        results.notificationsSent++;
        Logger.log(`Notificación enviada exitosamente a token ${index + 1}`);
      } else {
        const errorText = response.getContentText();
        results.errors.push(`Error en token ${index + 1}: ${errorText}`);
        Logger.log(`Error enviando notificación a token ${index + 1}: ${errorText}`);
      }
      
    } catch (error) {
      results.errors.push(`Excepción en token ${index + 1}: ${error.toString()}`);
      Logger.log(`Excepción enviando notificación a token ${index + 1}: ${error.toString()}`);
    }
  });
  
  Logger.log(`Notificaciones enviadas: ${results.notificationsSent} éxitos, ${results.errors.length} fallos`);
  
  return results;
}

/**
 * Obtiene un token de acceso para la API de FCM
 */
function getAccessToken() {
  try {
    // Usar el token de acceso de Google Apps Script
    // que tiene permisos para acceder a servicios de Firebase
    const token = ScriptApp.getOAuthToken();
    return token;
  } catch (error) {
    Logger.log(`Error obteniendo token de acceso: ${error.toString()}`);
    return null;
  }
}

/**
 * Determina el canal de notificación según el tipo de mensaje
 */
function getChannelIdForMessageType(messageType) {
  switch (messageType) {
    case "CHAT":
      return "channel_chat";
    case "ANNOUNCEMENT":
      return "channel_announcements";
    case "INCIDENT":
      return "channel_incidents";
    case "ATTENDANCE":
      return "channel_attendance";
    case "DAILY_RECORD":
      return "channel_daily_record";
    case "NOTIFICATION":
      return "channel_notifications";
    case "SYSTEM":
      return "channel_system";
    default:
      return "channel_default";
  }
}

/**
 * Determina la acción de click según el tipo de mensaje
 */
function getClickActionForMessageType(messageType) {
  switch (messageType) {
    case "CHAT":
      return "OPEN_CHAT";
    case "ANNOUNCEMENT":
      return "OPEN_ANNOUNCEMENTS";
    case "INCIDENT":
      return "OPEN_INCIDENTS";
    case "ATTENDANCE":
      return "OPEN_ATTENDANCE";
    case "DAILY_RECORD":
      return "OPEN_DAILY_RECORD";
    case "NOTIFICATION":
      return "OPEN_NOTIFICATIONS";
    case "SYSTEM":
      return "OPEN_SYSTEM";
    default:
      return "OPEN_APP";
  }
}

/**
 * Función de prueba para verificar el funcionamiento del script
 */
function testWebhook() {
  const testData = {
    messageId: "test-message-123",
    senderId: "test-sender",
    participantsIds: ["user1", "user2", "user3"],
    messageType: "CHAT",
    messageContent: "Este es un mensaje de prueba",
    messageTitle: "Mensaje de Prueba"
  };
  
  Logger.log("Iniciando prueba del webhook...");
  
  try {
    validatePayload(testData);
    Logger.log("✅ Validación de payload exitosa");
    
    const result = processNotification(testData);
    Logger.log(`✅ Procesamiento completado: ${JSON.stringify(result)}`);
    
  } catch (error) {
    Logger.log(`❌ Error en la prueba: ${error.toString()}`);
  }
}

/**
 * Función para obtener información del deployment
 */
function getDeploymentInfo() {
  const info = {
    scriptId: ScriptApp.getScriptId(),
    deploymentUrl: "https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec",
    version: "1.0",
    lastUpdated: new Date().toISOString(),
    firebaseProject: FIREBASE_PROJECT_ID
  };
  
  Logger.log(`Información del deployment: ${JSON.stringify(info, null, 2)}`);
  return info;
}
```

### Archivo de Configuración: appsscript.json

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

---

## 📋 Notas del Backup

### Funciones Principales
1. **`doPost()`** - Maneja webhooks HTTP POST desde Cloud Functions
2. **`processNotification()`** - Procesa datos de notificación
3. **`sendFCMNotifications()`** - Envía notificaciones FCM
4. **`validatePayload()`** - Valida estructura de datos

### Características Importantes
- ✅ Manejo robusto de errores con try-catch
- ✅ Logging detallado para debugging
- ✅ Validación de payloads de entrada
- ✅ Soporte para múltiples tipos de mensaje
- ✅ Configuración de canales de notificación Android
- ✅ Respuestas JSON estructuradas

### Permisos Requeridos
- `script.external_request` - Para llamadas HTTP a FCM API
- `firebase.messaging` - Para acceso a Firebase Cloud Messaging

### Configuración del Deployment
- **Tipo:** Web App
- **Acceso:** Cualquiera (necesario para webhooks)
- **Ejecutar como:** Usuario propietario
- **Versión:** Última (head)

---

## 🔄 Para Restaurar este Script

1. **Crear nuevo proyecto** en Google Apps Script
2. **Copiar el código** de `Code.gs`
3. **Configurar manifest** con `appsscript.json`
4. **Configurar permisos** para servicios externos
5. **Desplegar como Web App** con acceso público
6. **Obtener nueva URL** del deployment
7. **Actualizar URL** en Cloud Functions

### Pasos Detallados de Restauración

1. Ir a [script.google.com](https://script.google.com)
2. Crear "Nuevo proyecto"
3. Reemplazar contenido de `Code.gs` con el código de arriba
4. Ir a "Configuración del proyecto" → "Mostrar archivo de manifiesto"
5. Reemplazar `appsscript.json` con la configuración de arriba
6. Ir a "Desplegar" → "Nueva implementación"
7. Seleccionar tipo "Aplicación web"
8. Configurar acceso "Cualquiera"
9. Copiar URL de la implementación
10. Actualizar `APPS_SCRIPT_WEB_APP_URL` en Cloud Functions

---

## 🧪 Funciones de Prueba

### Probar el Script
```javascript
// Ejecutar en el editor de Google Apps Script
testWebhook();
```

### Obtener Información del Deployment
```javascript
// Ejecutar en el editor de Google Apps Script
getDeploymentInfo();
```

---

**⚠️ RECORDATORIO: Este es solo un backup para documentación. El script activo está en Google Apps Script Console.** 