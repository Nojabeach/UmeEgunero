# UmeEgunero - Tareas Pendientes (Actualizado)

Este documento detalla las tareas pendientes actualizadas para completar el desarrollo del proyecto UmeEgunero como Trabajo de Fin de Grado de 2º de DAM.

## Plan de Desarrollo Prioritario

1. **Completar pantallas críticas pendientes** ⚠️
   - Completar la integración de pantallas de gestión administrativa restantes
   - Prioridad: Crítica
   - Tiempo estimado: 3 días

2. **Integración completa entre módulos**
   - Asegurar que todas las características estén correctamente conectadas
   - Validar flujos de trabajo entre diferentes perfiles de usuario
   - Prioridad: Alta
   - Tiempo estimado: 3 días

3. **Mejoras de usabilidad y experiencia**
   - Implementar validaciones completas en todos los formularios
   - Mejorar la navegación y retroalimentación al usuario
   - Optimizar rendimiento en carga de datos
   - Prioridad: Alta
   - Tiempo estimado: 4 días

4. **Pruebas y depuración**
   - Realizar pruebas exhaustivas con diferentes perfiles
   - Identificar y corregir errores
   - Prioridad: Alta
   - Tiempo estimado: 3 días

## Pantallas del Dashboard de Administración pendientes de implementación

### Gestión de Usuarios
- **Listado de Usuarios**: Pantalla para visualizar y gestionar todos los usuarios registrados en la plataforma.
  - Implementar filtros por tipo de usuario (Administrador App, Administrador Centro, Profesor, Familiar)
  - Añadir funcionalidades de búsqueda por nombre, apellido o DNI
  - Integrar opciones de activación/desactivación de cuentas
  - Prioridad: Alta
  - Tiempo estimado: 3 días

- **Crear Nuevo Usuario**: Completar la pantalla para la creación de diferentes perfiles de usuario.
  - Integrar validaciones específicas según el tipo de usuario
  - Implementar subida de documentación requerida para ciertos perfiles
  - Añadir opción para asignación directa a centros educativos
  - Prioridad: Alta
  - Tiempo estimado: 3 días

### Vinculaciones y Relaciones
- **Vinculación de Profesores con Aulas**: ✅ 
  - **COMPLETADO**: Sistema para asignar profesores a aulas específicas.

- **Vinculación de Familiares con Alumnos**: ✅ 
  - **COMPLETADO**: Sistema para relacionar cuentas de familiares con alumnos.

## Pantallas con problemas críticos

1. **Pantalla de Detalle de Clase** ✅ 
   - **COMPLETADO**: Se ha implementado completamente la pantalla DetalleClaseScreen con todas sus funcionalidades.

2. **Pantalla de Gestión de Centros en el Panel de Administrador** ⚠️
   - Corregir la navegación para que dirija a la pantalla ListCentrosScreen
   - Revisar el resto de cards y botones del panel para asegurar que no cierren la aplicación
   - Prioridad: Crítica
   - Tiempo estimado: 1 día

3. **ComunicadosScreen para Profesor** ⚠️
   - El archivo ComunicadosScreen.kt en feature/profesor/screen está vacío (0 bytes)
   - Es necesario implementar esta pantalla crucial ya que la comunicación es una característica principal
   - Prioridad: Crítica
   - Tiempo estimado: 2 días

4. **IncidenciasScreen para Profesor** ⚠️
   - El archivo IncidenciasScreen.kt en feature/profesor/screen está vacío (0 bytes)
   - Es necesario implementar esta pantalla para registrar y gestionar incidencias
   - Prioridad: Alta
   - Tiempo estimado: 2 días

## Revisión de Funcionalidades por Perfil

### Perfil Administrador de Aplicación

1. **Dashboard Principal** ⚠️
   - Verificar que todos los botones y tarjetas de navegación funcionan correctamente
   - **URGENTE**: Corregir el card de "Centros" que actualmente causa cierre de la aplicación
   - Comprobar que las estadísticas mostradas se actualizan con datos reales
   - Revisar la visualización de centros educativos registrados
   - Prioridad: Crítica
   - Tiempo estimado: 1 día

2. **Gestión de Centros Educativos**
   - Revisar flujo completo de creación, edición y eliminación de centros
   - Validar la asignación de administradores a centros
   - Verificar las visualizaciones de datos y estadísticas
   - Prioridad: Alta
   - Tiempo estimado: 1 día

3. **Gestión de Usuarios**
   - Comprobar permisos de creación de usuarios administradores
   - Validar funcionalidad de reseteo de contraseñas
   - Revisar panel de activación/desactivación de usuarios
   - Prioridad: Alta
   - Tiempo estimado: 1 día

4. **Funcionalidades de seguridad**
   - Verificar el funcionamiento de la autenticación biométrica
   - Comprobar el flujo de activación y desactivación de la biometría
   - Validar la seguridad y gestión de credenciales biométricas
   - Prioridad: Media
   - Tiempo estimado: 1 día

### Perfil Administrador de Centro

1. **Dashboard Principal**
   - Verificar que las estadísticas del centro se muestran correctamente
   - Comprobar la visualización de actividad reciente
   - Revisar acceso a las diferentes secciones
   - Prioridad: Alta
   - Tiempo estimado: 1 día

2. **Gestión Académica**
   - Revisar flujo completo de creación y gestión de cursos
   - Validar la gestión de clases y asignación de profesores
   - Comprobar la organización del calendario académico
   - Prioridad: Alta
   - Tiempo estimado: 1 día

3. **Administración de Personal**
   - Comprobar la creación y gestión de profesores
   - Validar la asignación de profesores a clases
   - Revisar gestión de permisos dentro del centro
   - Prioridad: Media
   - Tiempo estimado: 1 día

4. **Funcionalidades de seguridad**
   - Verificar el funcionamiento de la autenticación biométrica
   - Comprobar el flujo de activación y desactivación de la biometría
   - Validar el acceso seguro a datos sensibles del centro
   - Prioridad: Media
   - Tiempo estimado: 1 día

### Perfil Profesor

1. **Dashboard Principal**
   - Verificar que se muestran correctamente las clases asignadas
   - Comprobar que el calendario muestra eventos relevantes
   - Validar acceso a mensajería con familias
   - Prioridad: Alta
   - Tiempo estimado: 1 día

2. **Gestión de Clases**
   - Revisar listado de alumnos por clase
   - Validar registro de asistencia
   - Comprobar acceso a información de cada alumno
   - Prioridad: Alta
   - Tiempo estimado: 1 día

3. **Comunicaciones** ⚠️
   - Implementar la pantalla ComunicadosScreen para profesores
   - Verificar envío de mensajes a familias
   - Comprobar creación y difusión de comunicados
   - Validar notificaciones de respuestas
   - Prioridad: Crítica
   - Tiempo estimado: 2 días

4. **Incidencias** ⚠️
   - Implementar la pantalla IncidenciasScreen para profesores
   - Desarrollar el sistema de registro y seguimiento de incidencias
   - Implementar DetalleIncidenciaScreen para visualizar y gestionar detalles
   - Implementar el flujo de notificación a familias de incidencias
   - Prioridad: Alta
   - Tiempo estimado: 2 días

5. **Actividades Preescolares**
   - Revisar creación y asignación de actividades
   - Validar seguimiento del progreso por alumno
   - Comprobar notificaciones a familias
   - Prioridad: Media
   - Tiempo estimado: 1 día

### Perfil Familiar

1. **Dashboard Principal**
   - Verificar visualización correcta de los hijos registrados
   - ✅ **COMPLETADO**: Implementado el acceso a incidencias desde el dashboard
   - Comprobar notificaciones de actividad reciente
   - Validar acceso a todas las secciones
   - Prioridad: Alta
   - Tiempo estimado: 1 día

2. **Seguimiento de Hijos**
   - Revisar visualización de progreso académico
   - Comprobar historial de asistencia
   - Validar acceso a evaluaciones y comentarios
   - Prioridad: Alta
   - Tiempo estimado: 1 día

3. **Comunicaciones** 
   - Verificar recepción de mensajes y comunicados
   - Comprobar sistema de chat con profesores
   - Validar notificaciones de nuevos mensajes
   - **Completado parcialmente**: Sistema de confirmación de lectura de comunicados implementado
   - Verificar sistema de notificaciones push para nuevas incidencias
   - Prioridad: Alta
   - Tiempo estimado: 1 día

4. **Actividades Preescolares**
   - Revisar visualización de actividades asignadas
   - Comprobar funcionalidad para marcar actividades completadas
   - Validar envío de comentarios sobre actividades
   - Prioridad: Media
   - Tiempo estimado: 1 día

## Mejoras técnicas necesarias

1. **Optimización de rendimiento**
   - Implementar paginación para listas largas de datos
   - Optimizar consultas a Firestore
   - Implementar caché local para datos frecuentes
   - Prioridad: Alta
   - Tiempo estimado: 2 días

2. **Mejoras de UI/UX**
   - Revisar y estandarizar diseños entre diferentes pantallas
   - Implementar animaciones y transiciones fluidas
   - Mejorar la experiencia en dispositivos pequeños
   - Prioridad: Media
   - Tiempo estimado: 2 días

3. **Documentación técnica del código**
   - Mejorar la documentación KDoc para clases principales
   - Preparar el código para generación de documentación con Dokka
   - Añadir comentarios explicativos en secciones complejas
   - Prioridad: Media
   - Tiempo estimado: 3 días

4. **Testing**
   - Implementar pruebas unitarias para ViewModels principales
   - Añadir pruebas de integración para flujos críticos
   - Configurar pruebas de UI con Compose Testing
   - Prioridad: Alta
   - Tiempo estimado: 3 días

## Resumen de Prioridades Actualizadas

### Tareas Críticas (Inmediatas)
1. Corregir navegación en Dashboard de Administrador (card de Centros)
2. Implementar ComunicadosScreen para Profesor
3. Implementar IncidenciasScreen para Profesor

### Tareas de Alta Prioridad
1. Completar gestión de usuarios y vinculaciones
2. Optimizar rendimiento de consultas y visualización
3. Implementar pruebas para flujos críticos
4. Finalizar funcionalidades de comunicación entre perfiles

### Tareas de Media Prioridad
1. Mejorar experiencia de usuario y diseño consistente
2. Ampliar funcionalidades de seguridad
3. Mejorar documentación técnica
4. Desarrollar características adicionales de cada perfil

## Plan de Implementación Recomendado

**Semana 1: Corrección de problemas críticos**
- Corregir navegación en Dashboard de Administración
- Implementar pantallas pendientes críticas (Comunicados, Incidencias)
- Resolver problemas de cierre inesperado de la aplicación

**Semana 2: Completar funcionalidades principales**
- Completar gestión de usuarios
- Implementar pruebas para flujos críticos

**Semana 3: Optimización y mejoras**
- Optimizar rendimiento
- Mejorar UI/UX
- Completar documentación técnica

**Semana 4: Testing y ajustes finales**
- Probar la aplicación con usuarios reales
- Corregir errores y realizar ajustes
- Preparar documentación final del proyecto

## Revisión de Visualización de Datos en Tiempo Real

1. **Gráficos de Dashboard**
   - Verificar que todos los gráficos muestran datos reales y actualizados
   - Comprobar actualización automática de estadísticas
   - Revisar rendimiento con conjuntos grandes de datos
   - Prioridad: Alta
   - Tiempo estimado: 2 días

2. **Notificaciones en Tiempo Real**
   - Validar recepción de notificaciones push
   - Comprobar actualización de contador de mensajes no leídos
   - Verificar alertas de nuevos eventos o comunicados
   - Prioridad: Alta
   - Tiempo estimado: 2 días

3. **Visualización de Estado de Alumnos**
   - Verificar actualización en tiempo real de asistencia
   - Comprobar sincronización de datos entre dispositivos
   - Validar consistencia de datos entre perfiles
   - Prioridad: Media
   - Tiempo estimado: 2 días

## Problemas específicos de interfaz

1. **Botón "Más información" en FAQ sin funcionalidad**
   - Al desplegar opciones en la pantalla de FAQ, el botón de "Más información" no realiza ninguna acción
   - Se debe implementar la funcionalidad para mostrar información detallada o eliminar el botón
   - Verificar en todas las secciones de FAQ si este botón tiene el mismo comportamiento
   - Prioridad: Media
   - Tiempo estimado: 1 día

2. **Envío de correos electrónicos a soporte**
   - El formulario de contacto con soporte técnico no completa correctamente el envío de correos
   - Verificar la configuración del servicio de correo electrónico
   - Implementar un sistema alternativo de notificación si el envío de correos no es viable
   - Considerar almacenar las solicitudes de soporte en Firestore como respaldo
   - Prioridad: Alta
   - Tiempo estimado: 2 días

3. **Enlace a Términos y Condiciones en Registro**
   - El enlace para ver los Términos y Condiciones en la pantalla de Registro redirige a una pantalla provisional
   - Desarrollar una pantalla adecuada para mostrar los términos y condiciones reales de la aplicación
   - Implementar la opción de descargar el documento como PDF
   - Prioridad: Alta
   - Tiempo estimado: 2 días

## Mejoras de rendimiento

1. **Optimización de consultas a Firestore**
   - Revisar y mejorar las consultas para minimizar lecturas/escrituras
   - Implementar estrategias de caché más eficientes
   - Prioridad: Media
   - Tiempo estimado: 2 días

2. **Mejoras de rendimiento general**
   - Implementar técnicas de carga diferida para componentes pesados
   - Optimizar consumo de batería y rendimiento
   - Prioridad: Baja
   - Tiempo estimado: 3 días

## Documentación pendiente

1. **Documentación con Dokka**
   - Asegurar que todo el código nuevo o actualizado incluya documentación compatible con Dokka
   - Prioridad: Media
   - Tiempo estimado: 3 días

2. **Documentación técnica completa**
   - Completar documentación técnica y de usuario
   - Prioridad: Media
   - Tiempo estimado: 2 días

3. **Documentación de Base de Datos**
   - Crear diagramas de entidad-relación adaptados para Firestore
   - Documentar estructura de colecciones y documentos
   - Describir relaciones y consultas principales
   - Prioridad: Alta
   - Tiempo estimado: 2 días

## Pruebas pendientes

1. **Ampliar tests unitarios**
   - Aumentar cobertura de pruebas, especialmente en ViewModels clave
   - Desarrollar casos de prueba para funcionalidades críticas del sistema
   - Implementar pruebas para validación de formularios y lógica de negocio
   - Prioridad: Alta
   - Tiempo estimado: 3 días

2. **Implementar tests de integración**
   - Probar flujos críticos de la aplicación
   - Verificar la correcta comunicación entre componentes
   - Validar el ciclo completo de operaciones CRUD en entidades principales
   - Prioridad: Alta
   - Tiempo estimado: 3 días

3. **Pruebas de UI con Compose**
   - Implementar tests para verificar el comportamiento correcto de componentes UI
   - Comprobar flujos de navegación y estados de UI
   - Prioridad: Media
   - Tiempo estimado: 2 días

4. **Pruebas completas de navegación**
   - Verificar todas las rutas de navegación entre pantallas
   - Comprobar comportamiento de backstack en flujos complejos
   - Validar preservación de estado durante la navegación
   - Prioridad: Alta
   - Tiempo estimado: 2 días

## Experiencia de usuario

1. **Tema oscuro completo**
   - Ajustar componentes y colores en modo oscuro
   - Prioridad: Baja
   - Tiempo estimado: 2 días

2. **Adaptabilidad a diferentes tamaños de pantalla**
   - Mejorar adaptación a tablets y dispositivos de diferentes densidades
   - Prioridad: Media
   - Tiempo estimado: 2 días

3. **Implementación de autenticación biométrica**
   - Verificar la implementación actual de autenticación biométrica en la aplicación
   - Comprobar la funcionalidad con diferentes tipos de sensores biométricos (huella, facial)
   - Implementar una integración completa con BiometricPrompt de AndroidX
   - Añadir flujo de configuración para activar/desactivar la biometría
   - Probar específicamente con el Extended Control Finger1 del emulador de Android
   - Implementar manejo adecuado de errores y casos fallidos
   - Prioridad: Alta
   - Tiempo estimado: 3 días

## Refactorización y limpieza del código

1. **Corregir advertencias de compilación**
   - Actualizar uso de APIs obsoletas (migrate from deprecated APIs)
     - Migrar de IconVectors obsoletos a sus versiones AutoMirrored
     - Actualizar `Divider` a `HorizontalDivider`
     - Utilizar la API KTX de Firebase en lugar de los métodos obsoletos como `toObject()`
   - Eliminar llamadas seguras innecesarias a receptores no nulos
   - Eliminar operadores Elvis innecesarios en tipos no nulos
   - Renombrar o utilizar los parámetros no utilizados
   - Eliminar variables no utilizadas
   - Prioridad: Media
   - Tiempo estimado: 3 días

2. **Mejorar calidad del código**
   - Refactorizar código duplicado
   - Simplificar expresiones condicionales innecesarias
   - Mejorar el manejo de tipos nulos para evitar conversiones inseguras
   - Normalizar el estilo de código en todos los archivos
   - Prioridad: Baja
   - Tiempo estimado: 2 días

## Problemas identificados en pruebas del Dashboard de Administrador

1. **Pantalla de Edición de Centro Obsoleta** ✅
   - **COMPLETADO**: La pantalla de edición de centro ha sido actualizada

2. **Mejoras en Gestión de Cursos y Clases** ✅
   - **COMPLETADO**: Se ha implementado la navegación correcta a las pantallas de gestión

3. **Error en Sección de Seguridad** ✅
   - **COMPLETADO**: Se ha corregido el error que provocaba el cierre de la aplicación

4. **Falta Menú de Configuración Administrativa** ⚠️
   - No existe menú para modificar configuraciones administrativas como el email de soporte
   - Desarrollar pantalla de ajustes administrativos
   - Incluir configuración de email de soporte y otras opciones administrativas globales
   - Prioridad: Alta
   - Tiempo estimado: 2 días

5. **Unificación de estética en DummyScreens** ⚠️
   - Estas pantallas aún requieren unificación estética:
     - Listar usuarios
     - Añadir usuarios
   - Actualizar todas las implementaciones para usar un estilo visual coherente
   - Asegurar que los mensajes informativos sean claros y profesionales
   - Prioridad: Media
   - Tiempo estimado: 1 día

6. **Implementación de Mantenimiento de Soporte** ✅
   - **COMPLETADO**: Se ha añadido una opción de "Mantenimiento de Soporte" en el menú de Comunicación

## Prioridades para el Próximo Sprint

1. **Sistema de Asistencia para Profesores**
   - Implementar registro de asistencia diaria de alumnos
   - Desarrollar notificaciones automáticas a familias por ausencias
   - Crear histórico y visualización de patrones de asistencia

2. **Calendario Compartido**
   - Desarrollar la funcionalidad completa del calendario escolar
   - Implementar eventos recurrentes y notificaciones de eventos próximos
   - Permitir a profesores añadir eventos de clase

3. **Sistema de Tareas**
   - Crear sistema de asignación de tareas para profesores
   - Desarrollar pantalla de seguimiento de tareas para familias
   - Implementar recordatorios y sistema de entrega

4. **Actividades Preescolares**
   - Finalizar el desarrollo del módulo de actividades para educación infantil
   - Implementar seguimiento detallado del desarrollo del alumno
   - Crear informes visuales de progreso para familias

## Pantallas sin uso (No conectadas al sistema de navegación)

- **PerfilScreen.kt**: Aunque la pantalla de perfil de usuario está definida en la ruta `AppScreens.Perfil.route` en Navigation.kt, no hay ningún enlace ni botón en las vistas principales que redirija a esta pantalla, excepto en el Dashboard de Administrador.

Se recomienda revisar los flujos de navegación para asegurar que todas las pantallas implementadas sean accesibles desde los puntos lógicos de la aplicación.