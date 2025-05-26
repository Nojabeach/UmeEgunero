# Sistema de Notificaciones UmeEgunero - CONFIGURACIÓN FINAL
**Actualizado: 26 Mayo 2025, 18:13** ✅ **SISTEMA COMPLETAMENTE FUNCIONAL**

## URLs Activas
- **Google Apps Script v9**: https://script.google.com/macros/s/AKfycbwf5p65kfWFkAS1EghW9d5s4QfajlHKw1HZuFAnVMIlTzw07nR42U6dK9fXO2OB8X4O3A/exec
- **Firebase Console**: https://console.firebase.google.com/project/umeegunero/overview

## Cloud Functions Desplegadas ✅
1. `notifyOnNewSolicitudVinculacion` - Notifica a administradores cuando se crea solicitud
2. `notifyOnSolicitudVinculacionUpdated` - Notifica a familiares cuando se procesa solicitud  
3. `notifyOnNewUnifiedMessage` - Sistema de mensajes unificado
4. `notifyOnNewMessage` - Compatibilidad con sistema anterior

## Flujo de Solicitudes de Vinculación
```
1. Familiar crea solicitud → Firestore → Push + Email a administradores
2. Admin aprueba/rechaza → Firestore → Push + Email automático al familiar
```

## Arquitectura
```
Android App → Cloud Functions → Google Apps Script
     ↓              ↓                    ↓
Firestore    Push notifications    Envío de emails
```

## Plantillas de Email (Google Apps Script)
- `SOLICITUD_PROCESADA` - Aprobaciones/rechazos automáticos
- `APROBACION`, `RECHAZO`, `BIENVENIDA`, `RECORDATORIO`, `SOPORTE`

## Canales de Notificación Android
```kotlin
CHANNEL_SOLICITUDES_VINCULACION = "channel_solicitudes_vinculacion"
CHANNEL_MENSAJES = "channel_mensajes"  
CHANNEL_GENERAL = "channel_general"
```

## Estado de Pruebas
- ✅ Emails desde Android deshabilitados (evita duplicados)
- 🧪 **LISTO PARA PRUEBAS COMPLETAS**

---
**Sistema listo para producción** 🚀 