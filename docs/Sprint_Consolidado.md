# UmeEgunero - Estado del Proyecto y Plan de Sprint

## Estado actual del proyecto

El proyecto UmeEgunero es una aplicación móvil Android para la comunicación entre centros educativos, profesores y familias que utiliza una arquitectura MVVM moderna con Jetpack Compose. Se han implementado la mayoría de las funcionalidades, pero aún quedan algunos elementos por completar.

### Módulos implementados

✅ **Sistema de navegación**: Estructura completa con `NavAnimations`, `AppScreens` y `Navigation`

✅ **Autenticación**: 
- Login para todos los perfiles de usuario
- Gestión de sesiones

✅ **Dashboards**: 
- AdminDashboard
- CentroDashboard
- ProfesorDashboard
- FamiliarDashboard

✅ **Comunicación**: 
- Sistema completo de mensajería
- Bandeja de entrada
- Compose de mensajes

✅ **Calendario académico**:
- Visualización de calendario
- Gestión de eventos

✅ **Gestión académica**:
- Cursos y clases
- Gestión de alumnos
- Asistencia

✅ **Gestión para profesores**:
- Registro de actividades preescolares
- Sistema de evaluación
- Gestión de tareas
- Chat con familias

✅ **Funcionalidades para familias**:
- Visualización de registros diarios
- Seguimiento de actividades
- Entrega de tareas
- Chat con profesores

### Correcciones recientes

Se han solucionado los siguientes errores:

1. Referencias incorrectas en `AdminDashboardScreen.kt` a `Comunicados` (ahora usa `ComunicadosCirculares`)
2. Importación de `Result` y corrección de método `obtenerUsuarioPorId` en `ComponerMensajeViewModel.kt`
3. Parámetros para `DocumentoScreen` en `Navigation.kt`
4. Conversión correcta del tipo de datos para `fecha` en `DetalleDiaEventoScreen`

### DummyScreens Pendientes

Tras un análisis exhaustivo, se han identificado las siguientes pantallas que aún utilizan implementaciones temporales (DummyScreen):

1. **Detalle de Clase**: 
   - Actualmente se navega hacia `AppScreens.Dummy.createRoute("Detalle de Clase: ${clase.nombre}")`
   - Falta implementar la pantalla real con detalles específicos de las clases

2. **Editar Familiar**:
   - Actualmente se navega hacia `AppScreens.Dummy.createRoute("Editar Familiar")`
   - Falta implementar el formulario para editar datos de familiares

3. La ruta `Dummy` está definida en `AppScreens.kt` pero no está implementada en `Navigation.kt`, lo que puede causar errores de navegación.

### Otros elementos pendientes por implementar

1. **Pendientes para la administración**:
   - ❌ Implementación completa de formularios de alta/modificación
   - ❌ Mejoras en la gestión de usuarios

2. **Elementos pendientes para comunicación**:
   - ❌ Implementación de lectura de comunicados y circulares
   - ❌ Confirmación de lectura de comunicados

3. **Mejoras para el calendario**:
   - ❌ Sincronización con calendario del dispositivo
   - ❌ Notificaciones de eventos próximos

## Plan de Sprints

### Sprint 1: Completar Formularios y Pantallas de Administración (2 semanas)

#### Objetivos:
1. **Implementación de formularios de alta/modificación**:
   - Crear `EditClaseScreen.kt` con formulario completo para edición de clases
   - Completar `EditFamiliarScreen.kt` para la edición de datos de familiares
   - Implementar `DetalleFamiliarScreen.kt` con vista completa de información familiar

2. **Mejoras en gestión de usuarios**:
   - Implementar mejoras en asignación de roles y permisos
   - Completar vinculación entre familiares y alumnos
   - Añadir sistema de invitaciones para nuevos usuarios

#### Tareas específicas:
| ID | Tarea | Descripción | Prioridad | Estimación |
|----|-------|-------------|-----------|------------|
| 1.1 | Implementar EditClaseScreen | Crear formulario con validación de campos y subida de datos a Firestore | Alta | 2 días |
| 1.2 | Implementar EditFamiliarScreen | Desarrollar formulario para edición de datos de familiares | Alta | 2 días |
| 1.3 | Crear DetalleFamiliarScreen | Diseñar vista de información detallada del familiar | Media | 1 día |
| 1.4 | Añadir sistema de roles | Implementar lógica para gestión de roles y permisos | Alta | 3 días |
| 1.5 | Desarrollar vinculación familiar-alumno | Mejorar el proceso de vinculación entre familiares y alumnos | Alta | 2 días |

### Sprint 2: Módulo de Comunicación y Notificaciones (2 semanas)

#### Objetivos:
1. **Implementación de lectura de comunicados y circulares**:
   - Completar `ComunicadosScreen.kt` para todos los perfiles de usuario
   - Implementar sistema de filtrado y búsqueda de comunicados
   - Añadir historial de comunicados anteriores

2. **Confirmación de lectura de comunicados**:
   - Implementar sistema de confirmación para mensajes importantes
   - Añadir panel de administración para rastrear confirmaciones
   - Crear notificaciones para comunicados sin leer

3. **Notificaciones push**:
   - Configurar Firebase Cloud Messaging para notificaciones en tiempo real
   - Implementar preferencias de notificaciones por usuario
   - Crear sistema de notificaciones para eventos, tareas y mensajes

#### Tareas específicas:
| ID | Tarea | Descripción | Prioridad | Estimación |
|----|-------|-------------|-----------|------------|
| 2.1 | Completar ComunicadosScreen | Finalizar la pantalla para todos los tipos de usuario | Alta | 2 días |
| 2.2 | Implementar filtrado de comunicados | Añadir opciones de filtrado y búsqueda | Media | 1 día |
| 2.3 | Desarrollar sistema de confirmación | Implementar confirmación de lectura para comunicados | Alta | 2 días |
| 2.4 | Configurar FCM | Integrar Firebase Cloud Messaging | Alta | 3 días |
| 2.5 | Crear sistema de preferencias de notificaciones | Permitir personalización de notificaciones por usuario | Media | 2 días |

### Sprint 3: Mejoras de Calendario y Experiencia de Usuario (2 semanas)

#### Objetivos:
1. **Mejoras para el calendario**:
   - Implementar sincronización con calendario del dispositivo
   - Añadir notificaciones de eventos próximos
   - Mejorar visualización de eventos recurrentes

2. **Experiencia de usuario**:
   - Optimizar animaciones en transiciones
   - Implementar tema oscuro completo
   - Mejorar adaptación a diferentes tamaños de pantalla

#### Tareas específicas:
| ID | Tarea | Descripción | Prioridad | Estimación |
|----|-------|-------------|-----------|------------|
| 3.1 | Sincronización con calendario del dispositivo | Implementar exportación/importación de eventos | Media | 3 días |
| 3.2 | Notificaciones de eventos | Añadir recordatorios para eventos próximos | Alta | 2 días |
| 3.3 | Mejorar visualización de eventos recurrentes | Optimizar la representación de eventos periódicos | Media | 2 días |
| 3.4 | Perfeccionar tema oscuro | Ajustar componentes y colores en modo oscuro | Media | 2 días |
| 3.5 | Optimizar adaptabilidad | Mejorar adaptación a diferentes tamaños de pantalla | Alta | 1 día |

### Sprint 4: Optimización y Testing (2 semanas)

#### Objetivos:
1. **Optimización de rendimiento**:
   - Implementar técnicas de carga diferida para componentes pesados
   - Mejorar gestión de caché para reducir llamadas al backend
   - Optimizar consumo de batería y rendimiento general

2. **Testing**:
   - Ampliar tests para navegación y pantallas principales
   - Implementar tests de integración para flujos críticos
   - Realizar pruebas de usuario y corregir problemas detectados

3. **Documentación y preparación para lanzamiento**:
   - Completar documentación técnica y de usuario
   - Preparar materiales para publicación en tienda
   - Realizar optimizaciones finales de SEO y metadatos

#### Tareas específicas:
| ID | Tarea | Descripción | Prioridad | Estimación |
|----|-------|-------------|-----------|------------|
| 4.1 | Implementar carga diferida | Optimizar carga de componentes pesados | Alta | 2 días |
| 4.2 | Mejorar gestión de caché | Reducir llamadas a Firestore | Alta | 2 días |
| 4.3 | Ampliar tests unitarios | Aumentar cobertura de pruebas | Alta | 3 días |
| 4.4 | Implementar tests de integración | Probar flujos críticos de la aplicación | Alta | 3 días |
| 4.5 | Completar documentación | Finalizar documentación técnica y de usuario con Dokka | Media | 2 días |

## Recomendaciones técnicas

1. **Eliminar código duplicado**: Se han detectado posibles modelos duplicados y ViewModels con funcionalidades similares que deben consolidarse.

2. **Prioridad en la navegación**: Corregir todos los destinos `Dummy` para que tengan una implementación real o redireccionar a otra pantalla existente.

3. **Enfoque incremental**: Desarrollar primero las funcionalidades críticas para cada perfil de usuario antes de añadir funcionalidades secundarias.

4. **Reaprovechamiento de código**: Reutilizar componentes ya desarrollados para acelerar la implementación de pantallas pendientes.

5. **Testing continuo**: Implementar pruebas unitarias y de integración a medida que se desarrollan las nuevas pantallas.

6. **Optimización de Firestore**: Revisar las consultas a Firestore para minimizar lecturas/escrituras y mejorar el rendimiento.

7. **Documentación con Dokka**: Asegurar que todo el código nuevo o actualizado incluya documentación compatible con Dokka.

## Conclusión

El proyecto UmeEgunero se encuentra en una fase avanzada de desarrollo con la mayoría de las pantallas implementadas. Con este plan de Sprints estructurado, se podrá completar el desarrollo de la aplicación en aproximadamente 8 semanas, enfocándose primero en las funcionalidades críticas pendientes y luego en las optimizaciones y mejoras de experiencia de usuario.

La aplicación ya compila correctamente y es funcional, pero requiere estas mejoras para alcanzar un nivel óptimo de calidad y completar todas las funcionalidades planificadas. Siguiendo este plan, se logrará una aplicación educativa robusta, con buen rendimiento y una experiencia de usuario excelente.
