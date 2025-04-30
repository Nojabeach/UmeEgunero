/**
 * Módulo de autenticación y gestión de credenciales de UmeEgunero.
 * 
 * Este módulo implementa toda la funcionalidad relacionada con la autenticación
 * y gestión de credenciales de usuarios en la aplicación.
 * 
 * ## Estructura del módulo
 * 
 * ### Pantallas ({@code screen/})
 * - {@code LoginScreen}: Inicio de sesión adaptativo según tipo de usuario
 * - {@code RegistroScreen}: Registro multi-paso para nuevos usuarios
 * - {@code CambioContrasenaScreen}: Cambio de contraseña
 * - {@code RecuperarPasswordScreen}: Recuperación de contraseña
 * 
 * ### ViewModels ({@code viewmodel/})
 * - {@code LoginViewModel}: Lógica de autenticación
 * - {@code RegistroViewModel}: Gestión del proceso de registro
 * - {@code CambioContrasenaViewModel}: Lógica de cambio de contraseña
 * 
 * ## Características principales
 * - Autenticación segura con Firebase
 * - Validación en tiempo real
 * - Gestión de sesiones
 * - Recuperación de contraseña
 * - Registro multi-paso
 * 
 * @see com.tfg.umeegunero.feature.auth.screen
 * @see com.tfg.umeegunero.feature.auth.viewmodel
 * @version 2.0
 */
package com.tfg.umeegunero.feature.auth; 