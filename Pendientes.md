# Tareas Pendientes

Este documento registra las pantallas y funcionalidades pendientes de desarrollo o integración en la aplicación UmeEgunero, especialmente para el dashboard de administración.

## Dashboard de Administración de la Aplicación

### Gestión de Usuarios
- **Listado de Usuarios**: Pantalla para visualizar y gestionar todos los usuarios registrados en la plataforma.
  - Implementar filtros por tipo de usuario (Administrador App, Administrador Centro, Profesor, Familiar)
  - Añadir funcionalidades de búsqueda por nombre, apellido o DNI
  - Integrar opciones de activación/desactivación de cuentas

- **Crear Nuevo Usuario**: Completar la pantalla para la creación de diferentes perfiles de usuario.
  - Integrar validaciones específicas según el tipo de usuario
  - Implementar subida de documentación requerida para ciertos perfiles
  - Añadir opción para asignación directa a centros educativos

### Vinculaciones y Relaciones
- **Vinculación de Profesores con Aulas**: Sistema para asignar profesores a aulas específicas.
  - Desarrollar interfaz para selección múltiple de aulas
  - Implementar visualización de horarios y conflictos
  - Añadir funcionalidad para definir profesores principales y de apoyo

- **Vinculación de Familiares con Alumnos**: Sistema para relacionar cuentas de familiares con alumnos.
  - Desarrollar sistema de invitación por correo electrónico
  - Implementar diferentes niveles de relación/parentesco
  - Añadir verificación de la relación familiar-alumno

## Prioridades para el Próximo Sprint
1. Completar la pantalla de Listado de Usuarios con capacidades básicas de filtrado
2. Finalizar la pantalla de Crear Nuevo Usuario para todos los perfiles
3. Implementar la funcionalidad básica de vinculación de profesores con aulas
4. Desarrollar la funcionalidad de vinculación de familiares con alumnos

## Consideraciones de Diseño
- Mantener consistencia con el sistema de diseño existente
- Priorizar la experiencia móvil, asegurando que todas las pantallas sean totalmente responsivas
- Implementar animaciones sutiles para mejorar la experiencia de usuario

## Consideraciones Técnicas
- Todas las nuevas pantallas deben seguir la arquitectura MVVM existente
- Implementar adecuado manejo de errores y estados de carga 
- Mantener la organización por módulos según el tipo de usuario
- Asegurar tests unitarios para las nuevas funcionalidades 