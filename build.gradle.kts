// Top-level build file where you can add configuration options common to all sub-projects/modules
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    }
}

plugins {
    id("com.android.application") version "8.9.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

// Configuración para mejorar la caché de configuración
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Configuración de la caché de configuración a nivel de proyecto
allprojects {
    tasks.withType<Test>().configureEach {
        maxParallelForks = Runtime.getRuntime().availableProcessors()
    }
}