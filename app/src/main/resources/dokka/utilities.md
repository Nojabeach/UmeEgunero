# Utilidades de UmeEgunero

## Paquete com.tfg.umeegunero.util

Este paquete contiene clases de utilidad que proporcionan funcionalidades transversales a toda la aplicación.

### Clases Principales

#### Result<T>
Wrapper genérico para encapsular resultados de operaciones que pueden fallar.

```kotlin
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data class Loading(val message: String = "") : Result<Nothing>()
}
```

**Uso típico:**
```kotlin
suspend fun obtenerDatos(): Result<List<Alumno>> {
    return try {
        val datos = repository.obtenerAlumnos()
        Result.Success(datos)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

#### Constants
Contiene todas las constantes utilizadas en la aplicación.

**Categorías de constantes:**
- **Firebase Collections**: Nombres de colecciones de Firestore
- **Shared Preferences**: Claves para preferencias compartidas
- **Navigation**: Rutas de navegación
- **Validation**: Patrones de validación
- **Notification Channels**: IDs de canales de notificación

#### Extensions
Extensiones de Kotlin para simplificar operaciones comunes.

**Extensiones disponibles:**
- `String.isValidEmail()`: Validación de email
- `String.isValidDNI()`: Validación de DNI español
- `Date.toFormattedString()`: Formateo de fechas
- `Context.showToast()`: Mostrar toast
- `Flow<T>.asResult()`: Convertir Flow a Result

#### Validators
Validadores para diferentes tipos de datos.

**Validadores implementados:**
- **EmailValidator**: Validación de direcciones de email
- **DNIValidator**: Validación de DNI español con dígito de control
- **PhoneValidator**: Validación de números de teléfono
- **PasswordValidator**: Validación de contraseñas seguras

### Utilidades de Seguridad

#### DatabaseEncryptionUtil
Utilidad para cifrado de datos sensibles en la base de datos local.

**Funcionalidades:**
- Cifrado AES-256 para datos sensibles
- Gestión segura de claves de cifrado
- Integración con Android Keystore

#### SecureTokenManager
Gestor seguro de tokens de autenticación.

**Características:**
- Almacenamiento seguro de tokens FCM
- Rotación automática de tokens
- Validación de integridad

### Utilidades de Red y Cache

#### FirestoreCache
Sistema de cache para optimizar consultas a Firestore.

**Beneficios:**
- Reducción de consultas repetidas
- Mejora del rendimiento
- Gestión automática de TTL (Time To Live)

#### ErrorHandler
Manejador centralizado de errores.

**Funcionalidades:**
- Clasificación de tipos de error
- Logging estructurado
- Integración con Crashlytics
- Mensajes de error localizados

### Utilidades de Conversión

#### Converters
Convertidores para Room Database y serialización.

**Convertidores disponibles:**
- `TimestampConverter`: Conversión entre Timestamp y Long
- `ListStringConverter`: Conversión entre List<String> y String
- `DateConverter`: Conversión entre Date y Long
- `MapConverter`: Conversión entre Map y JSON String

### Utilidades de Paginación

#### FirestorePagination<T>
Implementación de paginación eficiente para Firestore.

**Características:**
- Carga bajo demanda
- Cache de páginas
- Soporte para filtros y ordenamiento
- Optimización de costos de Firestore

**Ejemplo de uso:**
```kotlin
val pagination = FirestorePagination<Alumno>(
    db = firestore,
    collectionPath = "alumnos",
    pageSize = 20,
    orderBy = "nombre",
    parser = { data -> Alumno.fromMap(data) }
)

val primerasPagina = pagination.getFirstPage()
val siguientePagina = pagination.getNextPage()
```

### Utilidades de Diagnóstico

#### NotificationDiagnostic
Herramienta de diagnóstico para problemas de notificaciones push.

**Verificaciones realizadas:**
- Estado de permisos de notificación
- Validez del token FCM
- Configuración de canales de notificación
- Conectividad con Firebase
- Estado de autenticación

### Mejores Prácticas

1. **Uso de Result<T>**: Siempre encapsular operaciones que pueden fallar
2. **Validación temprana**: Usar validators antes de procesar datos
3. **Cache inteligente**: Implementar cache para datos frecuentemente accedidos
4. **Manejo de errores**: Usar ErrorHandler para logging consistente
5. **Seguridad**: Cifrar datos sensibles con DatabaseEncryptionUtil

### Dependencias

Las utilidades dependen de:
- **Kotlin Coroutines**: Para operaciones asíncronas
- **Hilt**: Para inyección de dependencias
- **Timber**: Para logging
- **Firebase**: Para integración con servicios
- **Room**: Para persistencia local
- **Android Keystore**: Para seguridad

---
*Documentación de utilidades - UmeEgunero v1.0* 