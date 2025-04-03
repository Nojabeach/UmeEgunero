# Avance del Sprint 2 - UmeEgunero

## Funcionalidades implementadas

### Sistema de Notificaciones
- Integración de Firebase Cloud Messaging (FCM) para envío y recepción de notificaciones
- Servicio `UmeEguneroMessagingService` para manejo de mensajes FCM
- Creación de canales de notificación para diferentes tipos de alertas:
  - Canal de notificaciones de tareas
  - Canal de notificaciones generales
- Pantalla de configuración de notificaciones que permite:
  - Habilitar/deshabilitar notificaciones por tipo
  - Gestionar token FCM del dispositivo
  - Ver información sobre preferencias de notificación

### Gestión de Archivos
- Implementación del repositorio `StorageRepository` para:
  - Subir archivos a Firebase Storage
  - Descargar archivos para visualización local
  - Obtener información detallada de archivos
  - Eliminar archivos
- Modelo de datos `InfoArchivo` para representar metadatos de archivos
- Visor de archivos completo con soporte para:
  - Imágenes (visualización directa)
  - Documentos PDF (visualización a través de visor)
  - Archivos de video (reproducción con ExoPlayer)
  - Archivos de audio (reproducción con ExoPlayer)
  - Otros tipos de archivo (ofreciendo descarga)
- Pantalla `DocumentoScreen` para visualizar archivos a pantalla completa con:
  - Información detallada del archivo
  - Opciones de descarga
  - Visualización adaptada según tipo de archivo

### Infraestructura
- Configuración de `FileProvider` para compartir archivos entre aplicaciones
- Actualización del archivo de manifiesto para:
  - Registrar el servicio de notificaciones
  - Añadir permisos de notificaciones
  - Configurar íconos y colores por defecto para notificaciones
- Módulo Hilt para proveer dependencias de Firebase
- Actualización de la aplicación principal para inicializar canales de notificación

## Próximos pasos
1. Implementar el panel de calendario con eventos académicos
2. Mejorar la interfaz de usuario para visualización de archivos adjuntos en tareas
3. Añadir opciones avanzadas de notificación (programación, silencio temporal)
4. Implementar sistema de recordatorios para eventos y tareas
5. Desarrollar funcionalidades para profesores relacionadas con la compartición de archivos 