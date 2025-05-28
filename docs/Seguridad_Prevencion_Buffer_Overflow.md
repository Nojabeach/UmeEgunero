# Seguridad y Prevención de Desbordamiento de Buffer en UmeEgunero

## Introducción

Este documento técnico detalla las medidas de seguridad implementadas en la aplicación UmeEgunero para prevenir desbordamientos de buffer, inyecciones SQL y otros problemas de seguridad relacionados con el manejo de datos y la persistencia local.

## Prevención de Desbordamiento de Buffer

Un desbordamiento de buffer ocurre cuando un programa escribe datos más allá de los límites asignados a un buffer en memoria. En UmeEgunero, hemos implementado las siguientes medidas para prevenir este tipo de vulnerabilidades:

### 1. Restricciones de tamaño en campos de entrada

Para mitigar riesgos de desbordamiento de buffer, implementamos restricciones de tamaño en todos los campos de entrada de datos. Estas restricciones fueron establecidas después de un análisis detallado de los requisitos funcionales de cada tipo de contenido:

* Mensajes de chat: limitados a 500 caracteres
* Comunicados oficiales: máximo 2000 caracteres
* Títulos de comunicados: máximo 100 caracteres
* Registros de actividades: máximo 1000 caracteres

Adicionalmente, implementamos un sistema de compresión automática para los contenidos multimedia antes de su transmisión a Firebase Storage, estableciendo límites máximos de dimensiones y tamaño en kilobytes según el tipo de contenido.

### 2. Uso de Room como capa de abstracción

Room proporciona una capa de abstracción segura sobre SQLite que elimina la necesidad de escribir consultas SQL directamente, reduciendo el riesgo de errores que podrían llevar a desbordamientos de buffer:

```kotlin
@Database(
    entities = [
        RegistroActividadEntity::class,
        ChatMensajeEntity::class,
        ConversacionEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAOs de acceso a datos
}
```

### 3. Parámetros con nombre en consultas SQL

Todas las consultas SQL en UmeEgunero utilizan parámetros con nombre, lo que previene tanto la inyección SQL como posibles desbordamientos relacionados con la construcción dinámica de consultas:

```kotlin
@Query("SELECT * FROM registros_actividad WHERE alumnoId = :alumnoId ORDER BY fechaTimestamp DESC")
fun getRegistrosActividadByAlumno(alumnoId: String): Flow<List<RegistroActividadEntity>>
```

En este ejemplo, `:alumnoId` es un parámetro con nombre que Room procesa de forma segura, asegurando que cualquier caracter especial sea correctamente escapado.

### 4. Validación de entrada

UmeEgunero implementa validación de entrada en múltiples niveles:

#### a) Nivel de UI

```kotlin
// Validación y sanitización de datos de entrada
val sanitizedValue = capacidadMaxima.trim()

if (sanitizedValue.isEmpty()) {
    return ValidationResult(
        isValid = false,
        capacidadMaxima = sanitizedValue,
        errorMessage = "La capacidad máxima no puede estar vacía"
    )
}

if (!sanitizedValue.all { it.isDigit() }) {
    return ValidationResult(
        isValid = false,
        capacidadMaxima = sanitizedValue,
        errorMessage = "La capacidad máxima debe ser un número entero"
    )
}

val capacidadInt = sanitizedValue.toIntOrNull()
if (capacidadInt == null || capacidadInt <= 0) {
    return ValidationResult(
        isValid = false,
        capacidadMaxima = sanitizedValue,
        errorMessage = "La capacidad máxima debe ser mayor que cero"
    )
}
```

#### b) Nivel de Repositorio

Antes de persistir datos, se validan tipos, longitudes y formatos:

```kotlin
fun validarRegistroActividad(registro: RegistroActividad): Boolean {
    // Validación de campos obligatorios
    if (registro.id.isBlank() || registro.alumnoId.isBlank()) {
        return false
    }
    
    // Validación de longitudes máximas para prevenir desbordamientos
    if (registro.observacionesGenerales.length > MAX_OBSERVACIONES_LENGTH) {
        return false
    }
    
    // Otras validaciones específicas...
    return true
}
```

### 5. Estructuras de datos tipo-seguras

UmeEgunero utiliza exclusivamente estructuras de datos tipo-seguras proporcionadas por Kotlin, eliminando errores comunes relacionados con punteros y gestión manual de memoria:

```kotlin
// Uso de data classes inmutables
data class RegistroActividadEntity(
    @PrimaryKey
    val id: String,
    val alumnoId: String,
    val alumnoNombre: String = "",
    // Otros campos...
)
```

## Prevención de Inyección SQL

### 1. Uso de consultas parametrizadas

Room traduce las consultas con parámetros a sentencias SQL preparadas que separan los datos de la estructura de la consulta, evitando completamente la inyección SQL:

```kotlin
@Query("SELECT * FROM registros_actividad WHERE claseId = :claseId AND fechaTimestamp BETWEEN :startTime AND :endTime ORDER BY fechaTimestamp DESC")
suspend fun getRegistrosActividadByClaseAndFecha(claseId: String, startTime: Long, endTime: Long): List<RegistroActividadEntity>
```

### 2. Separación de datos y código

Las entidades y las consultas están completamente separadas, imposibilitando la mezcla de datos de usuario con el código SQL:

```kotlin
@Entity(tableName = "chat_mensajes")
data class ChatMensajeEntity(
    @PrimaryKey
    val id: String,
    val conversacionId: String,
    val emisorId: String,
    val contenido: String,
    // Otros campos...
)
```

## Protección contra corrupción de datos

### 1. Transacciones atómicas

Las operaciones complejas se realizan dentro de transacciones para garantizar la integridad de los datos:

```kotlin
@Transaction
suspend fun actualizarConversacionYMensajes(conversacion: ConversacionEntity, mensajes: List<ChatMensajeEntity>) {
    insertConversacion(conversacion)
    insertMensajes(mensajes)
}
```

### 2. Verificación de integridad

Se implementan mecanismos para verificar la integridad de los datos antes de su uso:

```kotlin
fun validarIntegridadDatos(entidad: RegistroActividadEntity): Boolean {
    // Verificar campos obligatorios
    if (entidad.id.isBlank() || entidad.alumnoId.isBlank()) {
        return false
    }
    
    // Verificar coherencia temporal
    if (entidad.fechaTimestamp <= 0) {
        return false
    }
    
    // Verificar otros aspectos de integridad
    return true
}
```

## Serialización segura

### 1. Manejo seguro de JSON

UmeEgunero utiliza Gson para la serialización/deserialización de objetos a JSON con manejo de excepciones adecuado:

```kotlin
val comidas = try {
    gson.fromJson(comidasJson, Comidas::class.java) ?: Comidas()
} catch (e: Exception) {
    Comidas() // Valor por defecto seguro
}
```

### 2. Convertidores de tipo con validación

Los convertidores de tipo implementan validación para asegurar que los datos sean consistentes:

```kotlin
@TypeConverter
fun toEstadoComida(value: String?): EstadoComida {
    return try {
        value?.let { EstadoComida.valueOf(it) } ?: EstadoComida.NO_SERVIDO
    } catch (e: IllegalArgumentException) {
        EstadoComida.NO_SERVIDO // Valor por defecto seguro
    }
}
```

## Conclusión

La aplicación UmeEgunero implementa múltiples capas de protección contra desbordamientos de buffer y otros problemas de seguridad:

1. Restricciones de tamaño específicas para cada tipo de contenido
2. Compresión automática de contenido multimedia
3. Uso de Room como ORM seguro
4. Consultas parametrizadas para prevenir inyección SQL
5. Validación rigurosa de datos de entrada
6. Estructuras de datos tipo-seguras
7. Transacciones atómicas para mantener la integridad
8. Manejo seguro de serialización/deserialización

Estas medidas en conjunto proporcionan un alto nivel de seguridad en el manejo de datos, tanto en memoria como en la persistencia local, minimizando los riesgos asociados con desbordamientos de buffer y otras vulnerabilidades comunes en aplicaciones móviles. 