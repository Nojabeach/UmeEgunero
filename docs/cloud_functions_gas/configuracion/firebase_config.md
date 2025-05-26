# Configuración de Firebase - UmeEgunero

## 📋 Información General

Este documento contiene la configuración completa de Firebase para el proyecto UmeEgunero, incluyendo Cloud Functions, Firestore y Firebase Cloud Messaging.

## 🔧 Configuración del Proyecto Firebase

### Información Básica
- **Proyecto ID:** `umeegunero`
- **Región Principal:** `us-central1`
- **Región de Triggers:** `eur3` (Europa)
- **Cuenta de Servicio:** `1045944201521-compute@developer.gserviceaccount.com`

### URLs de Servicios
- **Cloud Functions:** `https://us-central1-umeegunero.cloudfunctions.net/`
- **Firestore:** `https://firestore.googleapis.com/v1/projects/umeegunero/databases/(default)/documents/`
- **FCM API:** `https://fcm.googleapis.com/v1/projects/umeegunero/messages:send`

## 🗄️ Configuración de Firestore

### Colecciones Principales
```
umeegunero (database)
├── usuarios/                    # Datos de usuarios y tokens FCM
├── solicitudes_vinculacion/     # Solicitudes de vinculación familiar
├── unified_messages/            # Sistema de mensajes unificado
├── messages/                    # Mensajes legacy (compatibilidad)
├── centros/                     # Información de centros educativos
├── alumnos/                     # Datos de alumnos
├── clases/                      # Información de clases
└── cursos/                      # Datos de cursos académicos
```

### Estructura de Documentos

#### Colección: `usuarios`
```javascript
{
  "dni": "86584661B",
  "perfiles": [
    {
      "tipo": "ADMIN_CENTRO",
      "centroId": "d8bc206e-6143-4026-8695-7ad49de27ab7",
      "verificado": true
    }
  ],
  "preferencias": {
    "notificaciones": {
      "fcmToken": "f5f1QUfJQfmDAp27PF5a...",
      "pushEnabled": true
    }
  },
  "fcmTokens": {
    "device1": "token1...",
    "device2": "token2..."
  }
}
```

#### Colección: `solicitudes_vinculacion`
```javascript
{
  "familiarId": "13546991Z",
  "tipoRelacion": "TUTOR",
  "estado": "PENDIENTE",
  "nombreFamiliar": "María Rodríguez Fernández",
  "alumnoId": "38944509N",
  "alumnoDni": "38944509N",
  "centroId": "d8bc206e-6143-4026-8695-7ad49de27ab7",
  "fechaSolicitud": "2025-05-26T15:31:37.000Z",
  "fechaProcesamiento": null,
  "adminId": "",
  "nombreAdmin": "",
  "observaciones": ""
}
```

#### Colección: `unified_messages`
```javascript
{
  "senderId": "system",
  "senderName": "Sistema UmeEgunero",
  "receiverId": "86584661B",
  "receiversIds": [],
  "type": "NOTIFICATION",
  "priority": "HIGH",
  "title": "Nueva solicitud de vinculación pendiente",
  "content": "El familiar María ha solicitado vincularse...",
  "timestamp": "2025-05-26T15:31:37.000Z",
  "status": "UNREAD",
  "relatedEntityType": "SOLICITUD_VINCULACION",
  "relatedEntityId": "RWq6UsQ1tyhRTkT3XICE",
  "metadata": {
    "solicitudId": "RWq6UsQ1tyhRTkT3XICE",
    "alumnoDni": "38944509N",
    "tipoNotificacion": "SOLICITUD_VINCULACION"
  }
}
```

## ⚡ Configuración de Cloud Functions

### Funciones Desplegadas
1. **`notifyOnNewSolicitudVinculacion`**
2. **`notifyOnNewUnifiedMessage`**
3. **`notifyOnNewMessage`**

### Configuración de Runtime
```json
{
  "runtime": "nodejs20",
  "memory": "256Mi",
  "cpu": "1",
  "timeout": "60s",
  "maxInstances": 40,
  "concurrency": 80,
  "ingressSettings": "ALLOW_ALL"
}
```

### Variables de Entorno
```bash
APPS_SCRIPT_WEB_APP_URL="https://script.google.com/macros/s/AKfycbz-icUrMUrWAmvf8iuc6B8qd_WB5x0OORsnt3wfQ3XdzPl0nCml_L3MS3Lr6rLnQuxAdA/exec"
FIREBASE_PROJECT_ID="umeegunero"
```

### Triggers de Firestore
```javascript
// Trigger para solicitudes de vinculación
onDocumentCreated("solicitudes_vinculacion/{solicitudId}")

// Trigger para mensajes unificados
onDocumentCreated("unified_messages/{messageId}")

// Trigger para mensajes legacy
onDocumentCreated("messages/{messageId}")
```

## 📱 Configuración de Firebase Cloud Messaging

### Configuración de Proyecto
- **Sender ID:** `1045944201521`
- **API Key:** (Configurado en google-services.json)
- **Project Number:** `1045944201521`

### Canales de Notificación Android
```javascript
const channels = {
  "channel_solicitudes_vinculacion": "Solicitudes de Vinculación",
  "channel_chat": "Mensajes de Chat",
  "channel_announcements": "Comunicados",
  "channel_incidents": "Incidencias",
  "channel_attendance": "Asistencia",
  "channel_daily_record": "Registro Diario",
  "channel_notifications": "Notificaciones",
  "channel_system": "Sistema",
  "channel_default": "General"
};
```

### Estructura de Notificación FCM
```javascript
{
  "message": {
    "token": "f5f1QUfJQfmDAp27PF5a...",
    "notification": {
      "title": "Nueva solicitud de vinculación",
      "body": "El familiar María ha solicitado vincularse..."
    },
    "data": {
      "tipo": "solicitud_vinculacion",
      "solicitudId": "RWq6UsQ1tyhRTkT3XICE",
      "centroId": "d8bc206e-6143-4026-8695-7ad49de27ab7",
      "click_action": "SOLICITUD_PENDIENTE"
    },
    "android": {
      "priority": "high",
      "notification": {
        "channel_id": "channel_solicitudes_vinculacion"
      }
    },
    "apns": {
      "payload": {
        "aps": {
          "alert": {
            "title": "Nueva solicitud de vinculación",
            "body": "El familiar María ha solicitado vincularse..."
          },
          "sound": "default"
        }
      }
    }
  }
}
```

## 🔐 Configuración de Seguridad

### Reglas de Firestore
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
    match /usuarios/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Reglas para solicitudes de vinculación
    match /solicitudes_vinculacion/{solicitudId} {
      allow read, write: if request.auth != null;
    }
    
    // Reglas para mensajes unificados
    match /unified_messages/{messageId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Permisos de Cloud Functions
```json
{
  "bindings": [
    {
      "role": "roles/cloudfunctions.invoker",
      "members": ["allUsers"]
    },
    {
      "role": "roles/datastore.user",
      "members": ["serviceAccount:1045944201521-compute@developer.gserviceaccount.com"]
    },
    {
      "role": "roles/firebase.messaging.admin",
      "members": ["serviceAccount:1045944201521-compute@developer.gserviceaccount.com"]
    }
  ]
}
```

## 📊 Configuración de Monitoreo

### Logs de Cloud Functions
```bash
# Ver logs en tiempo real
firebase functions:log

# Ver logs de función específica
firebase functions:log --only notifyOnNewSolicitudVinculacion

# Ver logs con filtro
firebase functions:log --filter="severity>=ERROR"
```

### Métricas de Rendimiento
- **Invocaciones por minuto:** Máximo 1000
- **Duración promedio:** < 2 segundos
- **Tasa de error:** < 1%
- **Memoria utilizada:** < 128Mi promedio

## 🚀 Comandos de Despliegue

### Inicialización del Proyecto
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Inicializar proyecto
firebase init

# Configurar proyecto
firebase use umeegunero
```

### Despliegue de Funciones
```bash
# Desplegar todas las funciones
firebase deploy --only functions

# Desplegar función específica
firebase deploy --only functions:notifyOnNewSolicitudVinculacion

# Desplegar con verificación
firebase deploy --only functions --debug
```

### Configuración de Regiones
```bash
# Configurar región por defecto
firebase functions:config:set region.default=us-central1

# Configurar región de triggers
firebase functions:config:set region.triggers=eur3
```

## 🔧 Configuración de Desarrollo

### Archivo firebase.json
```json
{
  "functions": {
    "source": "functions",
    "runtime": "nodejs20",
    "region": "us-central1"
  },
  "firestore": {
    "rules": "firestore.rules",
    "indexes": "firestore.indexes.json"
  }
}
```

### Archivo .firebaserc
```json
{
  "projects": {
    "default": "umeegunero"
  }
}
```

## 📅 Información de Versiones

- **Firebase CLI:** 14.3.1
- **Node.js:** 20.x
- **Firebase Admin SDK:** 12.0.0
- **Firebase Functions:** 4.9.0
- **Última actualización:** 26 de Mayo de 2025

## 🆘 Troubleshooting

### Problemas Comunes
1. **Error de permisos:** Verificar cuenta de servicio
2. **Timeout de funciones:** Aumentar límite de tiempo
3. **Cuota excedida:** Revisar límites de proyecto
4. **Tokens FCM inválidos:** Limpiar tokens expirados

### Contactos de Soporte
- **Firebase Support:** https://firebase.google.com/support
- **Documentación:** https://firebase.google.com/docs
- **Stack Overflow:** Tag `firebase` 