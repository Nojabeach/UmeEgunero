# Backup Cloud Functions - index.js (VERSIÓN FINAL)
**Actualizado: 26 Mayo 2025, 18:13**
**URL GAS v9**: https://script.google.com/macros/s/AKfycbwf5p65kfWFkAS1EghW9d5s4QfajlHKw1HZuFAnVMIlTzw07nR42U6dK9fXO2OB8X4O3A/exec

## Estado: ✅ DESPLEGADO Y FUNCIONANDO

### Cloud Functions Activas:
1. `notifyOnNewSolicitudVinculacion` - Notifica a administradores
2. `notifyOnSolicitudVinculacionUpdated` - Notifica a familiares  
3. `notifyOnNewUnifiedMessage` - Mensajes unificados
4. `notifyOnNewMessage` - Compatibilidad anterior

⚠️ **Archivo activo**: `/Users/maitane/UmeEguneroFirebaseFunctions/functions/index.js`

---

## 📄 Código Completo - index.js

```javascript
// const functions = require("firebase-functions");
const {onDocumentCreated, onDocumentUpdated} = require("firebase-functions/v2/firestore");
// Importante: Necesitamos un módulo para hacer llamadas HTTP.
// 'node-fetch' es común, pero las Cloud Functions v2 tienen fetch global (experimental?)
// Para asegurar compatibilidad, usaremos node-fetch v2 (require).
// ¡Asegúrate de añadirlo a package.json!
const fetch = require("node-fetch");
const admin = require("firebase-admin");

admin.initializeApp();

// --- ¡IMPORTANTE! REEMPLAZA ESTA URL ---
// Pega aquí la URL de tu Google Apps Script Web App desplegada (la última que obtuviste)
const APPS_SCRIPT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbwf5p65kfWFkAS1EghW9d5s4QfajlHKw1HZuFAnVMIlTzw07nR42U6dK9fXO2OB8X4O3A/exec"; // <-- ACTUALIZADA - Versión 9 del 26 may 2025
// -----------------------------------------

// Define la función que se exportará para mensajes unificados
exports.notifyOnNewUnifiedMessage = onDocumentCreated("unified_messages/{messageId}", async (event) => {
  // En v2, el snapshot está en event.data
  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }
  const newMessage = snap.data();
  const messageId = event.params.messageId; // Obtener messageId de event.params

  console.log(`Nuevo mensaje unificado detectado [${messageId}]. Datos:`, JSON.stringify(newMessage));

  // Extraer datos necesarios
  const senderId = newMessage.senderId || null;
  const senderName = newMessage.senderName || "Usuario";
  
  // Dependiendo del tipo, asignaremos los destinatarios
  let participantsIds = [];
  
  // Si es un mensaje personal, solo notificar al receptor
  if (newMessage.receiverId && newMessage.receiverId !== "") {
    participantsIds.push(newMessage.receiverId);
  } 
  // Si es un mensaje grupal, notificar a todos los receptores
  else if (Array.isArray(newMessage.receiversIds) && newMessage.receiversIds.length > 0) {
    participantsIds = newMessage.receiversIds;
  } 
  // Si no hay destinatarios explícitos, es posible que sea una notificación para todos
  else {
    // Aquí podrías implementar lógica para determinar a quién notificar
    console.log("Mensaje sin destinatarios explícitos, no se enviarán notificaciones push");
    return;
  }
  
  const messageType = newMessage.type || "CHAT";
  const messageContent = newMessage.content || "";
  const messageTitle = newMessage.title || `Nuevo mensaje de ${senderName}`;
  const conversationId = newMessage.conversationId || "";
  const priority = newMessage.priority || "NORMAL";

  // Excluir al remitente de las notificaciones
  const recipientsIds = participantsIds.filter(id => id !== senderId);
  
  if (recipientsIds.length === 0) {
    console.log("No hay destinatarios para notificar después de filtrar al remitente");
    return;
  }

  // Obtener tokens FCM de los destinatarios
  let tokensToSend = [];
  try {
    // Consulta para obtener los tokens de los usuarios destinatarios
    const usersSnapshot = await admin.firestore().collection("usuarios")
      .where(admin.firestore.FieldPath.documentId(), "in", recipientsIds)
      .get();
    
    if (usersSnapshot.empty) {
      console.log(`No se encontraron usuarios para los IDs: ${recipientsIds.join(", ")}`);
      return;
    }
    
    usersSnapshot.forEach(doc => {
      const userData = doc.data();
      const fcmTokens = userData.fcmTokens || {};
      
      // Añadir cada token del usuario
      Object.values(fcmTokens).forEach(token => {
        if (token && typeof token === "string") {
          tokensToSend.push(token);
        }
      });
    });
    
    if (tokensToSend.length === 0) {
      console.log("No se encontraron tokens FCM para los destinatarios");
      return;
    }
    
    console.log(`Se encontraron ${tokensToSend.length} tokens para enviar notificaciones`);
    
    // Configurar prioridad según el tipo de mensaje
    const notificationPriority = priority === "HIGH" || priority === "URGENT" ? "high" : "normal";
    
    // Datos para la notificación y deep linking
    const notificationData = {
      messageId: messageId,
      messageType: messageType,
      senderId: senderId,
      senderName: senderName,
      conversationId: conversationId,
      priority: priority
    };
    
    // Enviar mensajes de notificación
    const batchSize = 500; // FCM tiene un límite de tokens por solicitud
    const messagePromises = [];
    
    for (let i = 0; i < tokensToSend.length; i += batchSize) {
      const batch = tokensToSend.slice(i, i + batchSize);
      
      const message = {
        notification: {
          title: messageTitle,
          body: messageContent
        },
        data: notificationData,
        android: {
          priority: notificationPriority,
          notification: {
            channelId: getChannelIdForMessageType(messageType)
          }
        },
        apns: {
          payload: {
            aps: {
              alert: {
                title: messageTitle,
                body: messageContent
              },
              sound: notificationPriority === "high" ? "default" : null
            }
          }
        },
        tokens: batch
      };
      
      messagePromises.push(admin.messaging().sendMulticast(message));
    }
    
    const results = await Promise.all(messagePromises);
    
    let successCount = 0;
    let failureCount = 0;
    
    results.forEach(result => {
      successCount += result.successCount;
      failureCount += result.failureCount;
    });
    
    console.log(`Notificaciones enviadas: ${successCount} éxitos, ${failureCount} fallos`);
    
    return { success: true, successCount, failureCount };
    
  } catch (error) {
    console.error("Error al enviar notificaciones:", error);
    return { success: false, error: error.message };
  }
});

// Define la función para mensajes regulares (compatibilidad con versión anterior)
exports.notifyOnNewMessage = onDocumentCreated("messages/{messageId}", async (event) => {
  // En v2, el snapshot está en event.data
  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }
  const newMessage = snap.data();
  const messageId = event.params.messageId; // Obtener messageId de event.params

  console.log(`Nuevo mensaje detectado [${messageId}]. Datos:`, JSON.stringify(newMessage));

  // Extraer datos necesarios para Apps Script
  // ¡VALIDA que estos campos existen en tu documento newMessage!
  const senderId = newMessage.senderId || null;
  // *** CRUCIAL: Asegúrate de que tus mensajes en Firestore tienen este Array/Lista ***
  const participantsIds = newMessage.participantsIds || [];
  const messageType = newMessage.type || "UNKNOWN";
  const messageContent = newMessage.content || "";
  // Genera un título si no existe en el mensaje
  const messageTitle = newMessage.title || (newMessage.senderName ? `Nuevo mensaje de ${newMessage.senderName}` : "Nuevo mensaje");

  // Construir el payload para Apps Script
  const payload = {
    messageId: messageId,
    senderId: senderId,
    participantsIds: participantsIds,
    messageType: messageType,
    messageContent: messageContent,
    messageTitle: messageTitle,
  };

  // Validar que tenemos IDs de participantes
  if (!participantsIds || participantsIds.length === 0) {
    console.log("No hay participantes para notificar");
    return;
  }

  try {
    console.log("Enviando datos a Apps Script:", JSON.stringify(payload));

    // Hacer la llamada HTTP a Apps Script
    const response = await fetch(APPS_SCRIPT_WEB_APP_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const responseData = await response.json();
    console.log("Respuesta de Apps Script:", JSON.stringify(responseData));

    return responseData;
  } catch (error) {
    console.error("Error al llamar a Apps Script:", error);
    return { success: false, error: error.message };
  }
});

// Nueva función para solicitudes de vinculación
exports.notifyOnNewSolicitudVinculacion = onDocumentCreated("solicitudes_vinculacion/{solicitudId}", async (event) => {
  const snap = event.data;
  if (!snap) {
    console.log("No data associated with the event");
    return;
  }
  
  const solicitudData = snap.data();
  const solicitudId = event.params.solicitudId;
  
  console.log(`Nueva solicitud de vinculación detectada [${solicitudId}]. Datos:`, JSON.stringify(solicitudData));
  
  // Extraer datos de la solicitud
  const centroId = solicitudData.centroId;
  const nombreFamiliar = solicitudData.nombreFamiliar || "Un familiar";
  const alumnoId = solicitudData.alumnoId || "un alumno";
  const tipoRelacion = solicitudData.tipoRelacion || "TUTOR";
  
  if (!centroId) {
    console.log("No se encontró centroId en la solicitud");
    return;
  }
  
  try {
    // Buscar todos los usuarios y filtrar en el código
    const allUsersSnapshot = await admin.firestore().collection("usuarios").get();
    
    console.log(`Se encontraron ${allUsersSnapshot.size} usuarios en total`);
    
    // Filtrar administradores del centro específico
    const adminUsers = [];
    allUsersSnapshot.forEach(doc => {
      const userData = doc.data();
      const perfiles = userData.perfiles || [];
      
      const isAdminOfCenter = perfiles.some(perfil => 
        perfil.tipo === "ADMIN_CENTRO" && 
        perfil.centroId === centroId && 
        perfil.verificado === true
      );
      
      if (isAdminOfCenter) {
        console.log(`Administrador encontrado: ${doc.id}`);
        adminUsers.push({
          id: doc.id,
          data: userData
        });
      }
    });
    
    if (adminUsers.length === 0) {
      console.log(`No se encontraron administradores para el centro ${centroId}`);
      return;
    }
    
    console.log(`Se encontraron ${adminUsers.length} administradores para el centro`);
    
    // Obtener tokens FCM de los administradores
    const tokensToSend = [];
    adminUsers.forEach(admin => {
      // Buscar token en preferencias.notificaciones.fcmToken
      const fcmToken = admin.data.preferencias?.notificaciones?.fcmToken;
      if (fcmToken && typeof fcmToken === "string") {
        console.log(`Token FCM encontrado para admin ${admin.id}: ${fcmToken.substring(0, 20)}...`);
        tokensToSend.push(fcmToken);
      }
      
      // También buscar en fcmTokens como respaldo
      const fcmTokens = admin.data.fcmTokens || {};
      Object.values(fcmTokens).forEach(token => {
        if (token && typeof token === "string" && !tokensToSend.includes(token)) {
          console.log(`Token FCM adicional encontrado para admin ${admin.id}: ${token.substring(0, 20)}...`);
          tokensToSend.push(token);
        }
      });
    });
    
    if (tokensToSend.length === 0) {
      console.log("No se encontraron tokens FCM para los administradores del centro");
      return;
    }
    
    console.log(`Se encontraron ${tokensToSend.length} tokens FCM para enviar notificaciones`);
    
    // Preparar el mensaje de notificación
    const titulo = "Nueva solicitud de vinculación";
    const mensaje = `El familiar ${nombreFamiliar} ha solicitado vincularse como ${tipoRelacion} con el alumno ${alumnoId}`;
    
    // Enviar notificaciones usando HTTP directo en lugar de sendMulticast
    try {
      // Obtener el token de acceso para la API de FCM
      const accessToken = await admin.credential.applicationDefault().getAccessToken();
      
      // Enviar notificación a cada token individualmente
      const notificationPromises = tokensToSend.map(async (token) => {
        const fcmMessage = {
          message: {
            token: token,
            notification: {
              title: titulo,
              body: mensaje
            },
            data: {
              tipo: "solicitud_vinculacion",
              solicitudId: solicitudId,
              centroId: centroId,
              click_action: "SOLICITUD_PENDIENTE"
            },
            android: {
              priority: "high",
              notification: {
                channel_id: "channel_solicitudes_vinculacion"
              }
            },
            apns: {
              payload: {
                aps: {
                  alert: {
                    title: titulo,
                    body: mensaje
                  },
                  sound: "default"
                }
              }
            }
          }
        };
        
        try {
          const response = await fetch(`https://fcm.googleapis.com/v1/projects/umeegunero/messages:send`, {
            method: "POST",
            headers: {
              "Authorization": `Bearer ${accessToken.access_token}`,
              "Content-Type": "application/json"
            },
            body: JSON.stringify(fcmMessage)
          });
          
          if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
          }
          
          const result = await response.json();
          console.log(`Notificación enviada exitosamente a token ${token.substring(0, 20)}...: ${result.name}`);
          return { success: true, token: token, result: result };
          
        } catch (error) {
          console.error(`Error enviando notificación a token ${token.substring(0, 20)}...:`, error.message);
          return { success: false, token: token, error: error.message };
        }
      });
      
      const results = await Promise.all(notificationPromises);
      
      const successCount = results.filter(r => r.success).length;
      const failureCount = results.filter(r => !r.success).length;
      
      console.log(`Notificaciones de solicitud enviadas: ${successCount} éxitos, ${failureCount} fallos`);
      
      return { success: true, successCount, failureCount, results };
      
    } catch (error) {
      console.error("Error al obtener token de acceso o enviar notificaciones:", error);
      return { success: false, error: error.message };
    }
    
  } catch (error) {
    console.error("Error al buscar administradores del centro:", error);
    return { success: false, error: error.message };
  }
});

// Nueva función para manejar actualizaciones de solicitudes de vinculación
exports.notifyOnSolicitudVinculacionUpdated = onDocumentUpdated("solicitudes_vinculacion/{solicitudId}", async (event) => {
  const beforeSnap = event.data.before;
  const afterSnap = event.data.after;
  
  if (!beforeSnap || !afterSnap) {
    console.log("No hay datos before/after en el evento de actualización");
    return;
  }
  
  const beforeData = beforeSnap.data();
  const afterData = afterSnap.data();
  const solicitudId = event.params.solicitudId;
  
  console.log(`Solicitud de vinculación actualizada [${solicitudId}]`);
  console.log(`Estado anterior: ${beforeData.estado}, Estado nuevo: ${afterData.estado}`);
  
  // Solo procesar si el estado cambió de PENDIENTE a APROBADA o RECHAZADA
  if (beforeData.estado === "PENDIENTE" && (afterData.estado === "APROBADA" || afterData.estado === "RECHAZADA")) {
    console.log(`Procesando cambio de estado: ${beforeData.estado} → ${afterData.estado}`);
    
    const familiarId = afterData.familiarId;
    const nombreAlumno = afterData.nombreAlumno || "un alumno";
    const estado = afterData.estado;
    
    if (!familiarId) {
      console.log("No se encontró familiarId en la solicitud");
      return;
    }
    
    try {
      // Buscar el familiar por ID
      const familiarDoc = await admin.firestore().collection("usuarios").doc(familiarId).get();
      
      if (!familiarDoc.exists) {
        console.log(`No se encontró el familiar con ID: ${familiarId}`);
        return;
      }
      
      const familiarData = familiarDoc.data();
      console.log(`Familiar encontrado: ${familiarId}`);
      
      // Obtener tokens FCM del familiar
      const tokensToSend = [];
      
      // Buscar token en preferencias.notificaciones.fcmToken
      const fcmToken = familiarData.preferencias?.notificaciones?.fcmToken;
      if (fcmToken && typeof fcmToken === "string") {
        console.log(`Token FCM encontrado para familiar ${familiarId}: ${fcmToken.substring(0, 20)}...`);
        tokensToSend.push(fcmToken);
      }
      
      // También buscar en fcmTokens como respaldo
      const fcmTokens = familiarData.fcmTokens || {};
      Object.values(fcmTokens).forEach(token => {
        if (token && typeof token === "string" && !tokensToSend.includes(token)) {
          console.log(`Token FCM adicional encontrado para familiar ${familiarId}: ${token.substring(0, 20)}...`);
          tokensToSend.push(token);
        }
      });
      
      if (tokensToSend.length === 0) {
        console.log("No se encontraron tokens FCM para el familiar");
        return;
      }
      
      console.log(`Se encontraron ${tokensToSend.length} tokens FCM para enviar notificaciones`);
      
      // Preparar el mensaje según el estado
      let titulo, mensaje;
      if (estado === "APROBADA") {
        titulo = "Solicitud aprobada";
        mensaje = `Tu solicitud para vincularte con ${nombreAlumno} ha sido aprobada`;
      } else {
        titulo = "Solicitud rechazada";
        mensaje = `Tu solicitud para vincularte con ${nombreAlumno} ha sido rechazada`;
      }
      
      // Enviar notificaciones usando HTTP directo
      try {
        const accessToken = await admin.credential.applicationDefault().getAccessToken();
        
        const notificationPromises = tokensToSend.map(async (token) => {
          const fcmMessage = {
            message: {
              token: token,
              notification: {
                title: titulo,
                body: mensaje
              },
              data: {
                tipo: "solicitud_procesada",
                solicitudId: solicitudId,
                estado: estado,
                click_action: "SOLICITUD_PROCESADA"
              },
              android: {
                priority: "high",
                notification: {
                  channel_id: "channel_solicitudes_vinculacion"
                }
              },
              apns: {
                payload: {
                  aps: {
                    alert: {
                      title: titulo,
                      body: mensaje
                    },
                    sound: "default"
                  }
                }
              }
            }
          };
          
          try {
            const response = await fetch(`https://fcm.googleapis.com/v1/projects/umeegunero/messages:send`, {
              method: "POST",
              headers: {
                "Authorization": `Bearer ${accessToken.access_token}`,
                "Content-Type": "application/json"
              },
              body: JSON.stringify(fcmMessage)
            });
            
            if (!response.ok) {
              const errorText = await response.text();
              throw new Error(`HTTP ${response.status}: ${errorText}`);
            }
            
            const result = await response.json();
            console.log(`Notificación de estado enviada exitosamente a token ${token.substring(0, 20)}...: ${result.name}`);
            return { success: true, token: token, result: result };
            
          } catch (error) {
            console.error(`Error enviando notificación de estado a token ${token.substring(0, 20)}...:`, error.message);
            return { success: false, token: token, error: error.message };
          }
        });
        
        const results = await Promise.all(notificationPromises);
        
        const successCount = results.filter(r => r.success).length;
        const failureCount = results.filter(r => !r.success).length;
        
        console.log(`Notificaciones de estado enviadas: ${successCount} éxitos, ${failureCount} fallos`);
        
        return { success: true, successCount, failureCount, results };
        
      } catch (error) {
        console.error("Error al obtener token de acceso o enviar notificaciones de estado:", error);
        return { success: false, error: error.message };
      }
      
    } catch (error) {
      console.error("Error al buscar familiar:", error);
      return { success: false, error: error.message };
    }
  } else {
    console.log("No se requiere procesamiento - cambio de estado no relevante");
    return;
  }
});

// Función auxiliar para determinar el canal de notificación según el tipo de mensaje
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
```

---

## 📋 Notas del Backup

### Funciones Principales
1. **`notifyOnNewUnifiedMessage`** - Sistema de mensajes unificado
2. **`notifyOnNewMessage`** - Compatibilidad con sistema anterior
3. **`notifyOnNewSolicitudVinculacion`** - Notificaciones de solicitudes de vinculación
4. **`notifyOnSolicitudVinculacionUpdated`** ⭐ **NUEVA** - Notificaciones cuando se procesa una solicitud

### Características Importantes
- ✅ Uso de HTTP directo para FCM API (evita error 404)
- ✅ Canal de notificación correcto: `channel_solicitudes_vinculacion`
- ✅ Búsqueda robusta de administradores de centro
- ✅ Logs detallados para debugging
- ✅ Manejo de errores completo

### Dependencias
- `firebase-functions`: ^4.9.0
- `firebase-admin`: ^12.0.0
- `node-fetch`: ^2.7.0

### Variables de Entorno
- `APPS_SCRIPT_WEB_APP_URL`: URL del Google Apps Script

---

## 🔄 Para Restaurar este Código

1. Copiar el contenido del código JavaScript
2. Pegarlo en `/Users/maitane/UmeEguneroFirebaseFunctions/functions/index.js`
3. Ejecutar `npm run lint -- --fix`
4. Desplegar con `firebase deploy --only functions`

---

**⚠️ RECORDATORIO: Este es solo un backup para documentación. El código activo está en el directorio de Firebase Functions.** 