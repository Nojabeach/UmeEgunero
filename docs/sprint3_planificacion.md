# Planificación del Sprint 3 - UmeEgunero

## Objetivos
El Sprint 3 se enfocará en la implementación de funcionalidades avanzadas para la gestión académica, incorporando herramientas para profesores y familiares que mejoren el seguimiento educativo y la comunicación entre ambos.

## Duración
- **Inicio**: Por definir
- **Finalización**: Por definir
- **Duración total**: 3 semanas

## Funcionalidades a Implementar

### 1. Sistema de Calendario Académico
- **Objetivo**: Proporcionar una visualización completa del calendario escolar con eventos, fechas importantes y recordatorios.
- **Tareas**:
  - Desarrollar la interfaz del calendario con vista mensual, semanal y diaria
  - Implementar la creación, edición y eliminación de eventos
  - Añadir recordatorios y notificaciones para eventos
  - Sincronizar eventos entre usuarios (profesores → familias)
  - Añadir categorización y filtrado de eventos

### 2. Sistema de Evaluación para Profesores
- **Objetivo**: Permitir a los profesores gestionar evaluaciones, notas y comentarios sobre el desempeño de los alumnos.
- **Tareas**:
  - Crear pantalla de gestión de evaluaciones
  - Implementar sistema de rúbricas y criterios de evaluación
  - Desarrollar visualización de progreso académico
  - Añadir sistema de comentarios cualitativos
  - Implementar notificaciones a familiares sobre nuevas evaluaciones

### 3. Panel de Seguimiento Académico para Familiares
- **Objetivo**: Proporcionar a las familias una visión completa del progreso académico de sus hijos.
- **Tareas**:
  - Crear dashboard de seguimiento académico
  - Implementar gráficas de evolución por áreas
  - Mostrar listado de evaluaciones recientes
  - Añadir sistema de comentarios y feedback a profesores
  - Desarrollar resumen de asistencia y participación

### 4. Mejoras en el Sistema de Tareas
- **Objetivo**: Ampliar las funcionalidades del sistema de tareas implementado en sprints anteriores.
- **Tareas**:
  - Añadir sistema de calificación de tareas entregadas
  - Implementar comentarios y retroalimentación en las entregas
  - Desarrollar un sistema de recordatorios personalizables
  - Añadir estadísticas de entrega por grupo y alumno
  - Mejorar la interfaz de gestión de archivos adjuntos

### 5. Registro de Actividades para Preescolar
- **Objetivo**: Completar el sistema de registro de actividades diarias para alumnos de preescolar.
- **Tareas**:
  - Finalizar la interfaz de registro de actividades para profesores
  - Implementar categorías adicionales (alimentación, descanso, actividades)
  - Añadir la posibilidad de incluir fotos en las actividades
  - Desarrollar informes semanales automáticos
  - Implementar notificaciones en tiempo real para familiares

## Requisitos Técnicos
- Optimización del rendimiento en la sincronización de datos
- Mejora en el sistema de caché para funcionamiento offline
- Implementación de pruebas unitarias para las nuevas funcionalidades
- Refactorización del código existente para mejorar mantenibilidad
- Documentación técnica de las API y componentes desarrollados

## Riesgos Identificados
1. **Complejidad en la sincronización de eventos del calendario**: Mitigar mediante desarrollo incremental y pruebas exhaustivas
2. **Posible sobrecarga de notificaciones**: Implementar sistema de configuración avanzada de notificaciones
3. **Problemas de rendimiento en dispositivos antiguos**: Realizar pruebas en múltiples dispositivos y optimizar componentes críticos
4. **Curva de aprendizaje para usuarios**: Desarrollar tutoriales in-app y documentación de uso

## Métricas de Éxito
- Completar al menos 85% de las historias de usuario planificadas
- Mantener una cobertura de pruebas superior al 75%
- Lograr un tiempo de respuesta promedio de la app inferior a 1.5 segundos
- Resolver el 95% de los defectos críticos identificados durante el sprint 