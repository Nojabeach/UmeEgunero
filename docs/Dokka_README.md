# Documentación con Dokka - UmeEgunero

## Descripción

Este proyecto utiliza [Dokka](https://github.com/Kotlin/dokka) para generar documentación automática del código Kotlin. Dokka es la herramienta oficial de documentación para Kotlin que genera documentación en formato HTML a partir de comentarios KDoc en el código fuente.

## Configuración

### Dependencias

La configuración de Dokka está incluida en `app/build.gradle.kts`:

```kotlin
plugins {
    // ... otros plugins
    id("org.jetbrains.dokka") version "1.8.20"
}
```

### Configuración Personalizada

El proyecto incluye configuración personalizada para Dokka:

- **Archivos de documentación**: Ubicados en `app/src/main/resources/dokka/`
- **Estilos personalizados**: CSS personalizado en `styles/custom-style.css`
- **Plantillas**: Templates personalizados en `custom-dokka-templates/`
- **Enlaces externos**: Enlaces a documentación de Android y Kotlin

## Estructura de Documentación

### Archivos de Documentación

1. **package.md**: Documentación general del paquete principal
2. **modules.md**: Descripción detallada de la estructura de módulos
3. **utilities.md**: Documentación específica de utilidades

### Plantillas Personalizadas

- **head.ftl**: Configuración del `<head>` HTML
- **footer.ftl**: Pie de página personalizado
- **custom-style.css**: Estilos CSS personalizados

## Generación de Documentación

### Comando Gradle

Para generar la documentación, ejecuta:

```bash
./gradlew dokkaHtml
```

### Ubicación de Salida

La documentación generada se encuentra en:
```
app/build/dokka/
```

### Visualización

Para ver la documentación:

1. Navega a `app/build/dokka/`
2. Abre `index.html` en un navegador web

## Configuración Avanzada

### Enlaces Externos

El proyecto está configurado para incluir enlaces a:

- **Android Reference**: https://developer.android.com/reference/
- **Kotlin Stdlib**: https://kotlinlang.org/api/latest/jvm/stdlib/
- **Jetpack Compose**: https://developer.android.com/reference/kotlin/androidx/compose/

### Configuración de Visibilidad

Dokka está configurado para documentar:
- Clases y métodos `public`
- Clases y métodos `protected`
- Excluye elementos `private` e `internal`

### Supresión de Archivos

Se excluyen automáticamente:
- Archivos en paquetes `internal`
- Archivos de test (`test/`, `androidTest/`)
- Archivos temporales y de backup

## Mejores Prácticas para KDoc

### Formato Básico

```kotlin
/**
 * Descripción breve de la clase o función.
 * 
 * Descripción más detallada que puede incluir múltiples párrafos
 * y explicar el propósito, comportamiento y uso de la clase.
 * 
 * @property parametro1 Descripción del primer parámetro
 * @property parametro2 Descripción del segundo parámetro
 * @param entrada Parámetro de entrada del método
 * @return Descripción de lo que retorna el método
 * @throws Exception Descripción de cuándo se lanza esta excepción
 * 
 * @see ClaseRelacionada
 * @see [Enlace externo](https://example.com)
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
class EjemploClase(
    val parametro1: String,
    val parametro2: Int
) {
    /**
     * Método de ejemplo que realiza una operación.
     * 
     * @param entrada Datos de entrada para procesar
     * @return Resultado del procesamiento
     * @throws IllegalArgumentException Si la entrada es inválida
     */
    fun metodoEjemplo(entrada: String): String {
        // implementación
    }
}
```

### Etiquetas Recomendadas

- `@property`: Para propiedades de data classes
- `@param`: Para parámetros de métodos
- `@return`: Para valores de retorno
- `@throws` / `@exception`: Para excepciones
- `@see`: Para referencias cruzadas
- `@since`: Para versión de introducción
- `@author`: Para autoría
- `@sample`: Para ejemplos de código

### Ejemplos de Código

```kotlin
/**
 * Repositorio para gestionar alumnos.
 * 
 * @sample
 * ```kotlin
 * val repository = AlumnoRepository()
 * val alumnos = repository.obtenerTodos()
 * ```
 */
```

## Integración con CI/CD

### GitHub Actions

Para integrar la generación de documentación en CI/CD:

```yaml
- name: Generate Documentation
  run: ./gradlew dokkaHtml

- name: Deploy Documentation
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./app/build/dokka
```

## Personalización de Estilos

### CSS Personalizado

El archivo `custom-style.css` incluye:

- Colores corporativos de UmeEgunero
- Tipografía personalizada
- Responsive design
- Mejoras de accesibilidad

### Modificación de Plantillas

Las plantillas FreeMarker (`.ftl`) permiten:

- Personalizar el header y footer
- Añadir scripts personalizados
- Modificar la estructura HTML

## Troubleshooting

### Problemas Comunes

1. **Error de memoria**: Aumentar heap size de Gradle
   ```bash
   export GRADLE_OPTS="-Xmx4g"
   ```

2. **Enlaces rotos**: Verificar que todas las referencias `@see` sean válidas

3. **Archivos no encontrados**: Verificar rutas en `includes.from()`

### Logs de Depuración

Para obtener más información durante la generación:

```bash
./gradlew dokkaHtml --info --stacktrace
```

## Mantenimiento

### Actualización de Dokka

Para actualizar la versión de Dokka:

1. Modificar la versión en `build.gradle.kts`
2. Verificar compatibilidad con la configuración actual
3. Probar la generación de documentación

### Revisión de Documentación

Se recomienda:

- Revisar la documentación generada regularmente
- Actualizar comentarios KDoc al modificar código
- Mantener ejemplos de código actualizados
- Verificar enlaces externos periódicamente

## Actualizaciones Recientes

Se ha actualizado la documentación para incluir las siguientes nuevas funcionalidades:

1. **Exportación de PDF para Informes de Asistencia**: El sistema ahora permite exportar informes detallados en formato PDF que incluyen datos completos de asistencia, listado de alumnos presentes con DNI, y estadísticas.

2. **Marcado Automático de Mensajes como Leídos**: La aplicación marca automáticamente los mensajes como leídos al visualizarlos, mejorando la experiencia de usuario y manteniendo la sincronización de estados con Firebase.

3. **Sistema de Filtrado por Fecha en DetalleRegistroScreen**: Los usuarios familiares ahora pueden navegar fácilmente entre diferentes registros históricos usando un selector de fecha, sin necesidad de volver al listado principal.

Estas nuevas funcionalidades están completamente documentadas tanto en el código (Dokka) como en la documentación de usuario.

---

**Autor**: Maitane Ibañez Irazabal (2º DAM Online)  
**Proyecto**: UmeEgunero - Sistema de Gestión Escolar  
**Versión**: 1.0  
**Fecha**: 2024 