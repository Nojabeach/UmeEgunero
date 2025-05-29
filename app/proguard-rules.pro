# Archivo de configuración de ProGuard para la aplicación UmeEgunero
# 
# ProGuard es una herramienta que reduce, optimiza y ofusca el código Java. Es importante
# configurarla correctamente para evitar problemas en la compilación y ejecución de la app.
#
# En este archivo vamos a incluir reglas específicas para las bibliotecas que utilizamos
# y para mantener el funcionamiento correcto de nuestra aplicación en modo release.
#
# Autor: Estudiante 2º DAM
# Fecha: Mayo 2024
# Asignatura: Programación Multimedia y Dispositivos Móviles

# ==== CONFIGURACIÓN GENERAL ====

# Preservar información para depuración en caso de error
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Mantener anotaciones para que funcionen correctamente las inyecciones de dependencias
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# ==== APLICACIÓN PRINCIPAL ====

# Preservar la clase de aplicación principal
-keep class com.tfg.umeegunero.UmeEguneroApp { *; }
-keep public class com.tfg.umeegunero.UmeEguneroApp
-keepnames class com.tfg.umeegunero.UmeEguneroApp

# ==== FIREBASE ====

# Reglas para Firebase y Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.firebase.** { *; }

# Mantener nuestros modelos de datos para Firestore
-keep class com.tfg.umeegunero.data.model.** { *; }

# ==== ROOM DATABASE ====

# Preservar entidades y DAOs de Room
-keep class com.tfg.umeegunero.data.local.entity.** { *; }
-keep class com.tfg.umeegunero.data.local.dao.** { *; }
-keep class com.tfg.umeegunero.data.local.database.** { *; }

# Mantener anotaciones de Room
-keep class androidx.room.** { *; }
-keep class android.arch.persistence.room.** { *; }

# Preservar clases que tienen anotaciones de Room
-keepclasseswithmembers class * {
    @androidx.room.* <fields>;
}
-keepclasseswithmembers class * {
    @androidx.room.* <methods>;
}

# ==== HILT ====

# Reglas específicas para Dagger Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Mantener las clases de generación de Hilt
-keep class com.tfg.umeegunero.UmeEguneroApp_HiltComponents** { *; }
-keep class com.tfg.umeegunero.di.** { *; }

# ==== KOTLIN ====

# Reglas para preservar clases de Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ==== CORRUTINAS ====

# Preservar información de depuración para corrutinas
-keepattributes LineNumberTable,SourceFile
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ==== JETPACK COMPOSE ====

# Reglas para Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# ==== RETROFIT ====

# Preservar clases y métodos para Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes Exceptions

# ==== GSON ====

# Reglas específicas para Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ==== OKHTTP ====

# Preservar OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ==== JAVAMAIL ====

# Reglas para JavaMail
-keep class javax.mail.** { *; }
-keep class com.sun.mail.** { *; }
-dontwarn javax.mail.**
-dontwarn com.sun.mail.**

# ==== OTRAS BIBLIOTECAS ====

# Preservar Timber para logs
-keep class timber.log.** { *; }

# ==== OPTIMIZACIONES AVANZADAS ====

# Eliminar código de logging en versión release para mejorar rendimiento
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimización: si una clase se inicializa pero nunca se accede después, eliminar
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# ==== COMENTARIOS DE APRENDIZAJE PARA ESTUDIANTES ====
# Nota: En un proyecto real de producción, estas reglas deben ajustarse según
# las necesidades específicas de la aplicación. Es importante validar que la app
# funcione correctamente después de aplicar ProGuard realizando pruebas exhaustivas.
#
# No olvidar que después de aplicar ProGuard, el archivo APK resultante debe ser
# considerablemente más pequeño que el original y es más difícil de descompilar,
# lo que proporciona una capa adicional de seguridad para tu aplicación.
#
# Un error común es no incluir reglas específicas para las bibliotecas de terceros,
# lo que puede llevar a errores en tiempo de ejecución difíciles de depurar.