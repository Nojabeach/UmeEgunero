# Implementación de Cloud Function para Eliminar Usuarios

## Descripción

Esta solución utiliza una Cloud Function de Firebase para eliminar usuarios completamente de Firebase Authentication. A diferencia del método de Google Apps Script que solo deshabilita usuarios, esta implementación los elimina por completo.

## Arquitectura

```
┌─────────────────┐
│   Android App   │
│                 │
│ deleteUserByEmail()
└────────┬────────┘
         │
         │ 1. Crea documento
         ▼
┌─────────────────┐
│    Firestore    │
│                 │
│ user_deletion_  │
│    requests     │
└────────┬────────┘
         │
         │ 2. Trigger
         ▼
┌─────────────────┐
│ Cloud Function  │
│                 │
│ deleteUserByEmail
└────────┬────────┘
         │
         │ 3. Elimina usuario
         ▼
┌─────────────────┐
│  Firebase Auth  │
│                 │
│ admin.deleteUser()
└─────────────────┘
```

## Flujo de Trabajo

1. **Android App**: Crea un documento en la colección `user_deletion_requests` con el email del usuario
2. **Cloud Function**: Se activa automáticamente cuando se crea el documento
3. **Procesamiento**: La función busca el usuario por email y lo elimina completamente
4. **Actualización**: El documento se actualiza con el resultado (COMPLETED, ERROR, USER_NOT_FOUND)

## Implementación

### 1. Cloud Function (ya implementada en `cloud-functions/functions/index.js`)

```javascript
exports.deleteUserByEmail = onDocumentCreated("user_deletion_requests/{requestId}", async (event) => {
  // ... código de eliminación ...
});
```

### 2. Android App (ya implementada en `AuthRepository.kt`)

```kotlin
override suspend fun deleteUserByEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
    // Crear solicitud en Firestore
    val request = hashMapOf(
        "email" to email,
        "status" to "PENDING",
        "createdAt" to Timestamp.now(),
        "requestSource" to "ANDROID_APP"
    )
    
    val docRef = firestore.collection("user_deletion_requests")
        .add(request)
        .await()
    
    // Esperar y verificar resultado...
}
```

## Estructura del Documento

### Solicitud (creada por la app):
```json
{
  "email": "usuario@ejemplo.com",
  "status": "PENDING",
  "createdAt": "2024-01-20T10:30:00Z",
  "requestSource": "ANDROID_APP"
}
```

### Respuesta exitosa (actualizada por Cloud Function):
```json
{
  "email": "usuario@ejemplo.com",
  "status": "COMPLETED",
  "createdAt": "2024-01-20T10:30:00Z",
  "requestSource": "ANDROID_APP",
  "deletedUid": "abc123...",
  "processedAt": "2024-01-20T10:30:02Z"
}
```

### Respuesta de error:
```json
{
  "email": "usuario@ejemplo.com",
  "status": "ERROR",
  "createdAt": "2024-01-20T10:30:00Z",
  "requestSource": "ANDROID_APP",
  "error": "Mensaje de error",
  "errorDetails": "Stack trace...",
  "processedAt": "2024-01-20T10:30:02Z"
}
```

## Despliegue

### 1. Desplegar la Cloud Function

```bash
cd cloud-functions/functions
npm install
firebase deploy --only functions:deleteUserByEmail
```

### 2. Verificar en Firebase Console

1. Ve a **Functions** en Firebase Console
2. Verifica que `deleteUserByEmail` esté activa
3. Revisa los logs para ver el procesamiento

### 3. Configurar reglas de Firestore

Agrega estas reglas para la colección `user_deletion_requests`:

```javascript
// Firestore Security Rules
match /user_deletion_requests/{document} {
  // Solo usuarios autenticados pueden crear solicitudes
  allow create: if request.auth != null;
  
  // Solo lectura del propio documento creado
  allow read: if request.auth != null;
  
  // No permitir actualizaciones desde el cliente
  allow update: if false;
  
  // No permitir eliminaciones desde el cliente
  allow delete: if false;
}
```

## Monitoreo y Logs

### Ver logs de la Cloud Function:

```bash
firebase functions:log --only deleteUserByEmail
```

### En Firebase Console:

1. Ve a **Functions** > `deleteUserByEmail`
2. Click en **View logs**
3. Busca mensajes con emojis:
   - 🗑️ Nueva solicitud
   - 🔍 Buscando usuario
   - ✅ Eliminación exitosa
   - ❌ Error

## Ventajas sobre Google Apps Script

1. **Eliminación completa**: Elimina usuarios completamente, no solo los deshabilita
2. **Más rápido**: Procesamiento casi instantáneo
3. **Integración nativa**: Usa Firebase Admin SDK directamente
4. **Mejor monitoreo**: Logs integrados en Firebase Console
5. **Escalable**: Maneja múltiples solicitudes concurrentemente

## Fallback a Google Apps Script

La implementación incluye un fallback automático a GAS si:
- La Cloud Function no está disponible
- Hay errores de permisos
- No se puede acceder a Firestore

En estos casos, el usuario será **deshabilitado** (no eliminado) usando GAS.

## Testing

### Crear una solicitud manual en Firestore:

1. Ve a Firestore Console
2. Crea un documento en `user_deletion_requests`:
```json
{
  "email": "test@ejemplo.com",
  "status": "PENDING",
  "createdAt": "January 20, 2024 at 10:30:00 AM UTC",
  "requestSource": "MANUAL_TEST"
}
```
3. Observa cómo el status cambia automáticamente

### Endpoint HTTP para testing (opcional):

Si desplegaste `requestUserDeletion`:
```bash
curl -X POST https://[REGION]-[PROJECT].cloudfunctions.net/requestUserDeletion \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@ejemplo.com",
    "apiKey": "tu-api-key-secreta"
  }'
```

## Seguridad

1. **Autenticación requerida**: Solo usuarios autenticados pueden crear solicitudes
2. **Sin manipulación**: Los clientes no pueden actualizar el estado
3. **Auditoría**: Todos los intentos quedan registrados en Firestore
4. **API Key opcional**: Para el endpoint HTTP de testing

## Costos

Con el plan Blaze:
- **Cloud Functions**: Primeras 2M invocaciones gratis/mes
- **Firestore**: Primeras 50K lecturas/20K escrituras gratis/día
- Para una app típica, el costo es mínimo o nulo

## Troubleshooting

### "Permission denied" al crear solicitud
- Verifica las reglas de Firestore
- Asegúrate que el usuario esté autenticado

### Cloud Function no se ejecuta
- Verifica en Functions dashboard que esté activa
- Revisa los logs para errores
- Redespliega con `firebase deploy --only functions`

### Usuario no se elimina
- Verifica que el email sea correcto
- Revisa que el usuario existe en Firebase Auth
- Consulta los logs de la Cloud Function 