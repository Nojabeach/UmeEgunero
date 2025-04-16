# UmeEgunero - Tareas Pendientes

Este documento detalla todas las tareas pendientes identificadas tras el análisis del proyecto UmeEgunero para completar su desarrollo como Trabajo de Fin de Grado de 2º de DAM.

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
- **Vinculación de Profesores con Aulas**: Sistema para asignar profesores a aulas específicas.
  - Desarrollar interfaz para selección múltiple de aulas
  - Implementar visualización de horarios y conflictos
  - Añadir funcionalidad para definir profesores principales y de apoyo
  - Prioridad: Alta
  - Tiempo estimado: 3 días

- **Vinculación de Familiares con Alumnos**: Sistema para relacionar cuentas de familiares con alumnos.
  - Desarrollar sistema de invitación por correo electrónico
  - Implementar diferentes niveles de relación/parentesco
  - Añadir verificación de la relación familiar-alumno
  - Prioridad: Alta
  - Tiempo estimado: 3 días

## Pantallas temporales (DummyScreen) pendientes de implementación

1. **Pantalla de Detalle de Clase**
   - Aunque existe una ruta en `AppScreens.DetalleClase` y está configurada en Navigation.kt, el documento Sprint_Consolidado.md menciona que aún se navega hacia una pantalla Dummy en algunos casos
   - Es necesario revisar todas las referencias a DetalleClase y asegurar que se use la implementación real en lugar de Dummy
   - Prioridad: Alta
   - Tiempo estimado: 1 día

2. **Pantalla de Gestión de Centros en el Panel de Administrador**
   - Actualmente al hacer clic en el card de "Centros" en el panel de administrador, la aplicación se cierra
   - Se requiere implementar una redirección a DummyScreen o la pantalla correspondiente
   - Revisar el resto de cards y botones del panel para asegurar que no cierren la aplicación
   - Prioridad: Alta
   - Tiempo estimado: 1 día

## Revisión de Funcionalidades por Perfil

### Perfil Administrador de Aplicación

1. **Dashboard Principal** ⚠️
   - Verificar que todos los botones y tarjetas de navegación funcionan correctamente
   - **URGENTE**: Corregir el card de "Centros" que actualmente causa cierre de la aplicación
   - Comprobar que las estadísticas mostradas se actualizan con datos reales
   - Revisar la visualización de centros educativos registrados
   - Prioridad: Alta
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
   - Verificar envío de mensajes a familias
   - Comprobar creación y difusión de comunicados
   - Implementar visualización de estadísticas de lectura y confirmación de comunicados
   - Validar notificaciones de respuestas
   - **Pendiente**: Implementar panel de administración para monitorizar confirmaciones de lectura
   - Prioridad: Alta
   - Tiempo estimado: 1 día

4. **Actividades Preescolares**
   - Revisar creación y asignación de actividades
   - Validar seguimiento del progreso por alumno
   - Comprobar notificaciones a familias
   - Prioridad: Media
   - Tiempo estimado: 1 día

5. **Funcionalidades de seguridad**
   - Verificar el funcionamiento de la autenticación biométrica
   - Comprobar el flujo de activación y desactivación de la biometría
   - Validar la seguridad en el acceso a datos de alumnos
   - Prioridad: Media
   - Tiempo estimado: 1 día

### Perfil Familiar

1. **Dashboard Principal**
   - Verificar visualización correcta de los hijos registrados
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

3. **Comunicaciones** ⚠️
   - Verificar recepción de mensajes y comunicados ✅
   - Comprobar sistema de chat con profesores
   - Validar notificaciones de nuevos mensajes
   - **Completado parcialmente**: Sistema de confirmación de lectura de comunicados implementado
   - Prioridad: Alta
   - Tiempo estimado: 1 día

4. **Actividades Preescolares**
   - Revisar visualización de actividades asignadas
   - Comprobar funcionalidad para marcar actividades completadas
   - Validar envío de comentarios sobre actividades
   - Prioridad: Media
   - Tiempo estimado: 1 día

5. **Funcionalidades de seguridad**
   - Verificar el funcionamiento de la autenticación biométrica
   - Comprobar el flujo de activación y desactivación de la biometría
   - Validar la seguridad en el acceso a información familiar
   - Prioridad: Media
   - Tiempo estimado: 1 día

## Mejoras en módulos existentes

### Administración

1. **Formularios de alta/modificación**
   - Completar formularios para gestión de usuarios y centros
   - Mejorar validaciones de formularios
   - Prioridad: Media
   - Tiempo estimado: 3 días

2. **Mejoras en gestión de usuarios**
   - Implementar sistema robusto de roles y permisos
   - Completar vinculación entre familiares y alumnos
   - Añadir sistema de invitaciones para nuevos usuarios
   - Prioridad: Alta
   - Tiempo estimado: 4 días

### Comunicación

1. **Implementación de ComunicadosScreen**
   - Finalizar la implementación para todos los perfiles de usuario
   - Añadir sistema de filtrado y búsqueda
   - Implementar historial de comunicados anteriores
   - Prioridad: Alta
   - Tiempo estimado: 2 días

2. **Confirmación de lectura de comunicados** ✅
   - **COMPLETADO**: Se ha implementado el sistema de confirmación de lectura para comunicados importantes, incluyendo:
     - Modelo de datos actualizado para tracking de lecturas/confirmaciones
     - ViewModel para gestionar la lógica de filtrado y confirmación
     - UI con indicadores visuales de estado (leído/no leído, confirmado/pendiente)
     - Componentes para confirmar lecturas con diálogo informativo
     - Filtros para mostrar comunicados por estado

### Calendario

1. **Sincronización con calendario del dispositivo**
   - Implementar exportación e importación de eventos
   - Prioridad: Baja
   - Tiempo estimado: 3 días

2. **Notificaciones de eventos próximos**
   - Configurar recordatorios automáticos
   - Permitir personalización por usuario
   - Prioridad: Media
   - Tiempo estimado: 2 días

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

## Problemas y optimizaciones pendientes

### Funcionalidades especiales y ocultas


### Problemas específicos de interfaz

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

### Posibles duplicaciones de modelos

Se recomienda revisar y consolidar los siguientes elementos:

1. Posible duplicación en modelos de datos 
   - Revisar particularmente `data/model` para identificar clases con funcionalidades similares
   - Prioridad: Media
   - Tiempo estimado: 1 día

2. ViewModels con funcionalidades similares
   - Analizar especialmente en los módulos de profesor y familiar
   - Considerar refactorizar para reutilizar lógica común
   - Prioridad: Baja
   - Tiempo estimado: 2 días

### Mejoras de rendimiento

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

1. **Pantalla de Edición de Centro Obsoleta** ⚠️
   - La pantalla de edición de centro es rudimentaria y no ofrece la misma funcionalidad que la de añadir centro
   - Se debe actualizar `EditCentroScreen.kt` usando como referencia el diseño y funcionalidades de `AddCentroScreen.kt`
   - Implementar campos de edición completos con validaciones
   - Mostrar información actual del centro para edición
   - Prioridad: Alta
   - Tiempo estimado: 2 días

2. **Mejoras en Gestión de Cursos y Clases** ⚠️
   - Al añadir un curso, debe incluirse un botón para añadir clase si no tiene clase vinculada
   - Implementar opciones para visualizar o eliminar la clase vinculada desde el listado de cursos
   - Mejorar la navegación entre entidades relacionadas (Curso -> Clase -> Alumnos)
   - Prioridad: Alta
   - Tiempo estimado: 2 días

3. **Error en Sección de Seguridad** ⚠️
   - Al hacer clic en la opción de seguridad, la aplicación se cierra
   - Implementar redirección temporal a DummyScreen mientras se desarrolla la funcionalidad completa
   - Analizar y corregir la causa del cierre de la aplicación
   - Prioridad: Crítica
   - Tiempo estimado: 1 día

4. **Falta Menú de Configuración Administrativa** ⚠️
   - No existe menú para modificar configuraciones administrativas como el email de soporte
   - Desarrollar pantalla de ajustes administrativos
   - Incluir configuración de email de soporte y otras opciones administrativas globales
   - Prioridad: Alta
   - Tiempo estimado: 2 días

5. **Unificación de estética en DummyScreens** ⚠️
   - Todas las pantallas DummyScreen deben tener diseño consistente:
     - Gestión de clases
     - Listar usuarios
     - Añadir usuarios
     - Vincular profesores-clases
     - Vincular padres-alumnos
   - Actualizar todas las implementaciones para usar un estilo visual coherente
   - Asegurar que los mensajes informativos sean claros y profesionales
   - Prioridad: Media
   - Tiempo estimado: 1 día

## Conclusiones y prioridades para TFG

El proyecto UmeEgunero, como Trabajo de Fin de Grado para 2º de DAM, se encuentra en una fase avanzada pero requiere completar estas tareas pendientes para ser considerado un proyecto profesional completo que demuestre las competencias adquiridas durante la formación.

Las prioridades recomendadas son:

1. **CRÍTICO**: Corregir los problemas que provocan cierre de la aplicación
   - Pantalla de Seguridad en el Dashboard de Administrador
   - Opción de Centros en el panel de administrador
   - Cualquier otra navegación que provoque cierre inesperado

2. **ALTA**: Actualizar pantallas rudimentarias o incorrectas
   - Actualizar pantalla de edición de centro con las funcionalidades completas
   - Implementar correctamente la gestión de cursos y clases con las opciones requeridas
   - Desarrollar menú de configuración administrativa

3. **ALTA**: Implementar las pantallas que actualmente usan DummyScreen
   - Asegurar que todas las DummyScreen tienen un diseño visual consistente
   - Priorizar: Gestión de usuarios, Vinculación de profesores-clases, Vinculación de familiares-alumnos

4. **ALTA**: Completar la revisión de funcionalidades por perfil de usuario
   - Verificar todas las funcionalidades del administrador de aplicación
   - Completar funcionalidades del administrador de centro
   - Revisar y completar funcionalidades del profesor
   - Validar operaciones del perfil familiar

5. **MEDIA**: Verificar el correcto funcionamiento de los gráficos y datos en tiempo real
   - Asegurar que las visualizaciones de datos muestran información real y actualizada

6. **MEDIA**: Mejorar los sistemas de comunicación y notificaciones
   - **Progreso**: Se ha completado la implementación del sistema de confirmación de lectura de comunicados.
   - Completar sistema de notificaciones push

7. **MEDIA**: Completar la implementación y verificación de la autenticación biométrica
   - Implementar la integración completa con BiometricPrompt de AndroidX
   - Verificar su funcionamiento en todos los tipos de usuario
   - Probar con el Extended Control Finger1 del emulador de Android

8. **MEDIA**: Resolver problemas específicos de interfaz
   - Implementar o eliminar el botón "Más información" en FAQ 
   - Corregir el envío de correos al soporte técnico
   - Desarrollar una pantalla real para los Términos y Condiciones

9. **MEDIA**: Completar la documentación técnica y de usuario
   - Documentar adecuadamente la estructura de la base de datos
   - Completar manuales de usuario para cada perfil

10. **BAJA**: Optimizar rendimiento y experiencia de usuario
    - Mejorar tiempos de carga
    - Optimizar consultas a Firestore
    - Implementar caché eficiente

11. **BAJA**: Refactorizar y limpiar el código
    - Actualizar APIs obsoletas
    - Eliminar código redundante
    - Mejorar la calidad general del código

Es fundamental garantizar una cobertura de pruebas suficiente para demostrar la robustez de la implementación y documentar adecuadamente el código para facilitar su evaluación académica y mostrar las buenas prácticas de desarrollo aprendidas durante el ciclo formativo.

Siguiendo este plan, el proyecto puede completarse en aproximadamente 4-5 sprints, dependiendo del tamaño del equipo de desarrollo.

### Pantallas sin uso (No conectadas al sistema de navegación)

Esta sección lista pantallas implementadas pero que actualmente no están accesibles desde el sistema de navegación.

#### Pantallas Comunes sin vincular
- **PerfilScreen.kt**: Aunque la pantalla de perfil de usuario está definida en la ruta `AppScreens.Perfil.route` en Navigation.kt, no hay ningún enlace ni botón en las vistas principales que redirija a esta pantalla, excepto en el Dashboard de Administrador.

Todas las pantallas del módulo de administración ahora están correctamente vinculadas en el archivo `Navigation.kt`, incluyendo:
- ListCentrosScreen.kt (GestionCentros.route)
- AddCentroScreen.kt (AddCentro.route)
- EditCentroScreen.kt (EditCentro.route)
- DetalleCentroScreen.kt (DetalleCentro.route)
- EmailConfigScreen.kt (EmailConfig.route)
- EstadisticasScreen.kt (Estadisticas.route)
- ReporteUsoScreen.kt (ReporteUso.route)
- NotificacionesScreen.kt (Notificaciones.route)
- ComunicadosScreen.kt (ComunicadosCirculares.route)
- DetalleComunicadoScreen.kt (DetalleComunicado.route)
- NuevoComunicadoScreen.kt (NuevoComunicado.route)
- GestionCentrosScreen.kt (GestionCentros.route)
- DetalleCentroScreen.kt (DetalleCentro.route)
- AddCentroScreen.kt (AddCentro.route)
- EditCentroScreen.kt (EditCentro.route)

Se recomienda revisar los flujos de navegación para asegurar que todas estas pantallas sean accesibles desde los puntos lógicos de la aplicación.

## Prioridades para el Próximo Sprint
1. Completar la pantalla de Listado de Usuarios con capacidades básicas de filtrado
2. Finalizar la pantalla de Crear Nuevo Usuario para todos los perfiles
3. Implementar la funcionalidad básica de vinculación de profesores con aulas
4. Desarrollar la funcionalidad de vinculación de familiares con alumnos

## Consideraciones de Diseño
- Mantener consistencia con el sistema de diseño existente
- Priorizar la experiencia móvil, asegurando que todas las pantallas sean totalmente responsivas
- Implementar animaciones sutiles para mejorar la experiencia de usuario

## Consideraciones Técnicas Generales
- Todas las nuevas pantallas deben seguir la arquitectura MVVM existente
- Implementar adecuado manejo de errores y estados de carga 
- Mantener la organización por módulos según el tipo de usuario
- Asegurar tests unitarios para las nuevas funcionalidades 