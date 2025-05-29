# Verificar Custom Claims de Usuario

## Método 1: Desde el Cliente (Android)

```kotlin
// En cualquier parte donde tengas acceso al usuario autenticado
FirebaseAuth.getInstance().currentUser?.let { user ->
    user.getIdToken(true).addOnSuccessListener { result ->
        val claims = result.claims
        Log.d("CustomClaims", "DNI: ${claims["dni"]}")
        Log.d("CustomClaims", "isProfesor: ${claims["isProfesor"]}")
        Log.d("CustomClaims", "isAdmin: ${claims["isAdmin"]}")
        Log.d("CustomClaims", "isAdminApp: ${claims["isAdminApp"]}")
        Log.d("CustomClaims", "Todos los claims: $claims")
    }
}
```

## Método 2: Desde Cloud Functions

```javascript
// Función para verificar claims de un usuario por email
exports.getUserClaims = functions.https.onCall(async (data, context) => {
    // Verificar que quien llama es admin
    if (!context.auth || !context.auth.token.isAdmin) {
        throw new functions.https.HttpsError('permission-denied', 'Solo administradores');
    }
    
    const email = data.email;
    try {
        const user = await admin.auth().getUserByEmail(email);
        const customClaims = user.customClaims || {};
        
        return {
            uid: user.uid,
            email: user.email,
            customClaims: customClaims
        };
    } catch (error) {
        throw new functions.https.HttpsError('not-found', 'Usuario no encontrado');
    }
});
```

## Método 3: Script Node.js local

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./path-to-service-account-key.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

async function checkUserClaims(email) {
    try {
        const user = await admin.auth().getUserByEmail(email);
        console.log('UID:', user.uid);
        console.log('Email:', user.email);
        console.log('Custom Claims:', user.customClaims);
    } catch (error) {
        console.error('Error:', error);
    }
}

// Uso
checkUserClaims('usuario@ejemplo.com');
```

## Solución de problemas comunes

### Si un usuario no tiene claims:
```javascript
// Cloud Function para establecer claims
exports.setUserClaims = functions.https.onCall(async (data, context) => {
    // Verificar permisos...
    
    const { uid, dni, isProfesor, isAdmin } = data;
    
    await admin.auth().setCustomUserClaims(uid, {
        dni: dni,
        isProfesor: isProfesor || false,
        isAdmin: isAdmin || false,
        isAdminApp: false
    });
    
    return { success: true };
});
``` 