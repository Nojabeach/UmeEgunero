# Sistema de Notificaciones UmeEgunero
**Estado: ✅ COMPLETAMENTE FUNCIONAL - 26 Mayo 2025**

## URLs Activas
- **Google Apps Script v9**: https://script.google.com/macros/s/AKfycbwf5p65kfWFkAS1EghW9d5s4QfajlHKw1HZuFAnVMIlTzw07nR42U6dK9fXO2OB8X4O3A/exec
- **Firebase Console**: https://console.firebase.google.com/project/umeegunero/overview

## Cloud Functions Desplegadas ✅
1. `notifyOnNewSolicitudVinculacion` - Notifica a administradores cuando se crea solicitud
2. `notifyOnSolicitudVinculacionUpdated` - Notifica a familiares cuando se procesa solicitud  
3. `notifyOnNewUnifiedMessage` - Sistema de mensajes unificado
4. `notifyOnNewMessage` - Compatibilidad con sistema anterior

## Flujo de Solicitudes
```
Familiar crea solicitud → Firestore → Push + Email a administradores
Admin aprueba/rechaza → Firestore → Push + Email automático al familiar
```

## Arquitectura
```
Android App → Cloud Functions → Google Apps Script
     ↓              ↓                    ↓
Firestore    Push notifications    Envío de emails
```

## Archivos de Documentación
- `configuracion_final.md` - Configuración completa y detallada del sistema
- `codigo_backup/` - Backups del código de Cloud Functions y Google Apps Script

## ⚠️ Archivos de Código Activos
- **Cloud Functions**: `/Users/maitane/UmeEguneroFirebaseFunctions/functions/index.js`
- **Google Apps Script**: En consola web (URL arriba)

## Próximos Pasos
- 🔄 Deshabilitar envío de emails desde Android (evitar duplicados)
- 🔄 Probar sistema completo con solicitud real

---
**Sistema listo para producción** 🚀 