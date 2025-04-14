# UmeEgunero - Tareas Pendientes

Este documento detalla todas las tareas pendientes identificadas tras el análisis del proyecto UmeEgunero para completar su desarrollo como Trabajo de Fin de Grado de 2º de DAM.

## Pantallas temporales (DummyScreen) pendientes de implementación

1. **Pantalla de Detalle de Clase**
   - Aunque existe una ruta en `AppScreens.DetalleClase` y está configurada en Navigation.kt, el documento Sprint_Consolidado.md menciona que aún se navega hacia una pantalla Dummy en algunos casos
   - Es necesario revisar todas las referencias a DetalleClase y asegurar que se use la implementación real en lugar de Dummy
   - Prioridad: Alta
   - Tiempo estimado: 1 día

## Revisión de Funcionalidades por Perfil

### Perfil Administrador de Aplicación

1. **Dashboard Principal**
   - Verificar que todos los botones y tarjetas de navegación funcionan correctamente
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

3. **Comunicaciones**
   - Verificar envío de mensajes a familias
   - Comprobar creación y difusión de comunicados
   - Validar notificaciones de respuestas
   - Prioridad: Alta
   - Tiempo estimado: 1 día

4. **Actividades Preescolares**
   - Revisar creación y asignación de actividades
   - Validar seguimiento del progreso por alumno
   - Comprobar notificaciones a familias
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

## Conclusiones y prioridades para TFG

El proyecto UmeEgunero, como Trabajo de Fin de Grado para 2º de DAM, se encuentra en una fase avanzada pero requiere completar estas tareas pendientes para ser considerado un proyecto profesional completo que demuestre las competencias adquiridas durante la formación.

Las prioridades recomendadas son:

1. Implementar las pantallas que actualmente usan DummyScreen
2. Completar la revisión de funcionalidades por perfil de usuario
3. Verificar el correcto funcionamiento de los gráficos y datos en tiempo real
4. Mejorar los sistemas de comunicación y notificaciones
5. Completar la implementación de formularios de alta/modificación
6. Ampliar la suite de pruebas (unitarias, integración y UI)
7. Documentar adecuadamente la estructura de la base de datos
8. Optimizar rendimiento y experiencia de usuario

Es fundamental garantizar una cobertura de pruebas suficiente para demostrar la robustez de la implementación y documentar adecuadamente el código para facilitar su evaluación académica y mostrar las buenas prácticas de desarrollo aprendidas durante el ciclo formativo.

Siguiendo este plan, el proyecto puede completarse en aproximadamente 4-5 sprints, dependiendo del tamaño del equipo de desarrollo. 