# UmeEgunero - Tareas Pendientes Actualizadas

Lista simplificada de tareas pendientes para completar el desarrollo del proyecto:

## Tareas Críticas

1. **Corregir navegación en Dashboard de Administrador**
   - Arreglar el card de "Centros" que provoca cierre de la aplicación
   - Asegurar que todos los botones funcionen correctamente
   
   ### Plan de mejora para los 4 dashboards (AdminApp, AdminCentro, Profesor, Familiar)
   1. **Unificar componentes visuales**
      - Crear componentes comunes en `ui.components.dashboard`
      - Aplicar a los cuatro dashboards existentes
      
   2. **Estandarizar estructura UI**
      - Implementar tarjeta de bienvenida consistente
      - Añadir panel de estadísticas por rol
      - Reorganizar grid de accesos rápidos
      - Unificar sección de notificaciones
      
   3. **Homogeneizar ViewModels**
      - Refactorizar para seguir patrón común
      - Estandarizar estados UI y métodos
      
   4. **Mejorar sistema de temas**
      - Mantener colores distintivos por rol
      - Implementar paletas complementarias
      
   5. **Unificar animaciones**
      - Aplicar mismas transiciones y tiempos
      - Estandarizar feedback visual
      
   6. **Optimizar carga de datos**
      - Implementar paginación consistente
      - Mejorar consultas a Firestore por rol
      
   7. **Crear sistema de notificaciones común**
      - Componente unificado para alertas
      - Adaptar contenido según rol
      
   8. **Mejorar responsividad**
      - Optimizar layouts para diferentes dispositivos
      - Implementar soporte landscape

2. **Implementar pantallas pendientes críticas**
   - ComunicadosScreen para Profesor
   - IncidenciasScreen para Profesor
   - DetalleIncidenciaScreen para visualizar detalles

3. **Completar funcionalidades de comunicación**
   - Implementar envío de mensajes entre profesores y familias
   - Verificar sistema de notificaciones de nuevos mensajes
   - Implementar notificaciones push para incidencias

## Tareas de Alta Prioridad

4. **Gestión de Usuarios**
   - Completar pantalla de listado de usuarios con filtros y búsqueda
   - Finalizar pantalla de creación de diferentes perfiles de usuario
   - Implementar funcionalidad de reseteo de contraseñas

5. **Implementar autenticación biométrica**
   - Integrar BiometricPrompt de AndroidX
   - Añadir configuración para activar/desactivar biometría
   - Implementar manejo adecuado de errores

6. **Optimizar rendimiento**
   - Implementar paginación para listas largas
   - Optimizar consultas a Firestore
   - Implementar caché local para datos frecuentes

7. **Implementar pruebas**
   - Añadir tests unitarios para ViewModels principales
   - Implementar tests de integración para flujos críticos
   - Configurar pruebas de UI con Compose

8. **Crear menú de configuración administrativa**
   - Desarrollar pantalla de ajustes administrativos
   - Incluir configuración de email de soporte
   - Añadir opciones administrativas globales

9. **Corregir problemas de interfaz**
   - Implementar envío de correos electrónicos a soporte
   - Desarrollar pantalla de Términos y Condiciones
   - Implementar funcionalidad del botón "Más información" en FAQ

## Tareas de Media Prioridad

10. **Mejorar UI/UX**
    - Unificar estética en todas las pantallas (especialmente Listar/Añadir usuarios)
    - Implementar animaciones y transiciones fluidas
    - Mejorar adaptación a tablets y dispositivos de diferentes densidades

11. **Documentación**
    - Mejorar documentación KDoc para clases principales
    - Crear diagramas de entidad-relación para Firestore
    - Documentar estructura de colecciones y documentos

12. **Visualización de datos en tiempo real**
    - Verificar actualización automática de estadísticas en Dashboard
    - Validar actualización en tiempo real de asistencia
    - Comprobar sincronización de datos entre dispositivos

13. **Conectar pantallas no accesibles**
    - Revisar PerfilScreen.kt y otras pantallas sin flujos de navegación
    - Asegurar que todas las pantallas sean accesibles lógicamente

## Tareas de Baja Prioridad

14. **Refactorizar código**
    - Actualizar uso de APIs obsoletas
    - Eliminar código redundante
    - Normalizar estilo de código en todos los archivos

15. **Implementar tema oscuro completo**
    - Ajustar componentes y colores en modo oscuro

## Módulos para Desarrollo Futuro

16. **Sistema de Asistencia para Profesores**
17. **Calendario Compartido**
18. **Sistema de Tareas**
19. **Módulo completo de Actividades Preescolares**

## Implementación de Servicio de Email (Mailjet)

Para completar al 100% la implementación del servicio de email con Mailjet:

1. **Configuración inicial**
   - Obtener API Key de Mailjet registrándose en https://www.mailjet.com/
   - Verificar dominio de email del remitente en el panel de Mailjet
   - Configurar la API Key en el archivo EmailService.kt

2. **Completar funcionalidades de EmailService**
   - Implementar método para adjuntar archivos en los correos
   - Desarrollar plantillas HTML completas para cada tipo de correo
   - Añadir sistema de tracking de emails (abiertos, clics, etc.)

3. **Integración completa**
   - Integrar EmailService con todas las partes de la aplicación que requieran notificaciones por email:
     - Reseteo de contraseñas
     - Notificaciones de tareas
     - Recordatorios de eventos
     - Alertas de incidencias
     - Notificaciones de nuevos comunicados
     - Confirmaciones de registro
     - Resúmenes semanales/mensuales de actividad

4. **Seguridad y monitorización**
   - Implementar sistema de reintento para emails fallidos
   - Crear mecanismo para cambiar de proveedor de email si Mailjet falla
   - Añadir panel de monitorización de emails enviados/fallidos
   - Implementar cifrado adicional para datos sensibles en los emails

5. **Optimización**
   - Implementar envío de emails en lotes para grandes volúmenes
   - Crear sistema de cola para envíos programados
   - Optimizar rendimiento para dispositivos con conexión lenta

6. **Testing completo**
   - Desarrollar tests unitarios para cada tipo de email
   - Implementar tests de integración con Mailjet
   - Crear tests de regresión para asegurar compatibilidad con futuras versiones

7. **Documentación detallada**
   - Crear manual de configuración para Mailjet
   - Documentar API completa del servicio MailjetEmailSender
   - Incluir ejemplos de uso para cada tipo de email