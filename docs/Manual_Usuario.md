# Manual de Usuario - UmeEgunero

## Índice
1. [Introducción](#introducción)
2. [Requisitos del Sistema](#requisitos-del-sistema)
3. [Instalación](#instalación)
4. [Registro e Inicio de Sesión](#registro-e-inicio-de-sesión)
5. [Perfiles de Usuario](#perfiles-de-usuario)
   - [Administrador de Aplicación](#administrador-de-aplicación)
   - [Administrador de Centro](#administrador-de-centro)
   - [Profesor](#profesor)
   - [Familiar](#familiar)
6. [Módulos Principales](#módulos-principales)
   - [Comunicaciones](#comunicaciones)
   - [Calendario y Eventos](#calendario-y-eventos)
   - [Gestión de Usuarios](#gestión-de-usuarios)
   - [Gestión Académica](#gestión-académica)
   - [Chat](#chat)
7. [Solución de Problemas](#solución-de-problemas)
8. [Preguntas Frecuentes](#preguntas-frecuentes)

## Introducción

Bienvenido a UmeEgunero, la aplicación integral para la gestión educativa que conecta centros escolares, profesores y familias. Este manual le guiará a través de todas las funcionalidades disponibles según su perfil de usuario.

UmeEgunero está diseñada para facilitar la comunicación entre todos los miembros de la comunidad educativa y ofrecer herramientas específicas para cada rol, mejorando la experiencia educativa y administrativa de los centros.

## Requisitos del Sistema

- Dispositivo Android versión 8.0 (API 26) o superior.
- Recomendado: Android 14 (API 34) para todas las funcionalidades.
- Conexión a Internet (algunas funciones están disponibles en modo offline).
- Espacio de almacenamiento mínimo: 100 MB.
- Se recomienda 2GB de memoria RAM o superior.

## Instalación

1. Descargue la aplicación desde Google Play Store.
2. Abra la aplicación una vez instalada.
3. Permita los permisos solicitados durante la instalación:
   - Almacenamiento: para guardar documentos y archivos.
   - Cámara: para tomar fotos de perfil o adjuntar a mensajes.
   - Notificaciones: para recibir alertas de comunicados y mensajes.
4. La aplicación realizará una verificación inicial y descargará recursos adicionales si es necesario.

## Registro e Inicio de Sesión

### Registro de Usuario Nuevo

El registro en UmeEgunero requiere un código de acceso proporcionado por el centro educativo o el administrador del sistema, dependiendo del tipo de perfil:

1. En la pantalla de bienvenida, pulse el botón "Registrarse".
2. Complete el formulario con:
   - Nombre completo
   - Correo electrónico
   - Contraseña (mínimo 8 caracteres, debe incluir mayúsculas, minúsculas y números)
   - Seleccione el tipo de usuario (Familiar, Profesor, Administrador de Centro)

3. **Para usuarios tipo Familiar**:
   - Introduzca el código de centro proporcionado por su centro educativo.
   - Introduzca el código de identificación familiar.
   - Seleccione el alumno o alumnos asociados a su cuenta.

4. **Para usuarios tipo Profesor**:
   - Introduzca el código de centro y el código de identificación de profesor.
   - El administrador del centro deberá validar su cuenta antes de tener acceso completo.

5. **Para administradores de centro**:
   - Necesitará el código especial proporcionado por el administrador de la aplicación.
   - Deberá completar la información del centro si es un centro nuevo.

6. **Para administradores de la aplicación**:
   - Este perfil solo puede ser creado por otro administrador existente.
   - Contacte con soporte técnico para solicitar este perfil.

7. Pulse "Completar Registro".
8. Verifique su correo electrónico siguiendo el enlace enviado.

### Inicio de Sesión

1. Introduzca su correo electrónico registrado.
2. Introduzca su contraseña.
3. Opcionalmente, active "Recordar usuario" para facilitar futuros accesos.
4. Pulse "Iniciar Sesión".
5. Si ha olvidado su contraseña, pulse "¿Olvidó su contraseña?" y siga las instrucciones para restablecerla a través de su correo electrónico.

### Autenticación de Dos Factores (Opcional)

Para mayor seguridad, puede activar la autenticación de dos factores:

1. Vaya a su perfil de usuario.
2. Seleccione "Configuración de seguridad".
3. Active "Autenticación de dos factores".
4. Siga las instrucciones para configurar la autenticación por SMS o aplicación autenticadora.

## Perfiles de Usuario (actualizado)

UmeEgunero distingue entre diferentes perfiles, cada uno con funcionalidades y flujos adaptados a sus necesidades:

### Administrador de Aplicación
- Acceso completo a la gestión global de la plataforma.
- Alta/baja de centros y usuarios administradores de centro.
- Auditoría y seguridad.

### Administrador de Centro
- Gestión de profesores, familias y alumnos.
- Creación y gestión de cursos, clases y eventos del centro.
- Visualización de estadísticas globales y panel de bienestar.
- Envío de comunicados y gestión de incidencias.

### Profesor
- Registro y seguimiento emocional/social de alumnos (pantalla nueva).
- Resumen diario/semanal de rutinas (pantalla nueva).
- Comunicación directa con familias.
- Gestión de asistencia, incidencias y actividades.

### Familiar
- Visualización del bienestar y evolución del niño/a (pantalla nueva).
- Solicitud de reuniones/tutorías (pantalla nueva).
- Acceso a recursos familiares (pantalla nueva).
- Consulta de asistencia, comunicados y chat con profesorado.

## Nuevas Pantallas y Mejoras UX

Se han añadido nuevas pantallas orientadas a la etapa infantil (2-4 años):
- Seguimiento emocional y social (profesor)
- Resumen de rutinas (profesor)
- Bienestar del niño/a (familia)
- Solicitud de reunión (familia)
- Centro de recursos familiares

Todas las pantallas siguen las directrices de Material 3 y buenas prácticas de accesibilidad.

## Accesibilidad y Usabilidad

- Contraste de colores y fuentes adaptadas a infantil.
- Botones grandes y navegación sencilla.
- Compatibilidad con lectores de pantalla.

## Módulos Principales

### Comunicaciones

El sistema de comunicaciones de UmeEgunero permite el intercambio de información entre todos los usuarios de la plataforma.

#### Para Administradores y Profesores

1. **Crear Comunicado**:
   - Pulse el botón "+" en la pantalla de comunicados.
   - Seleccione el tipo: informativo, circular, aviso importante.
   - Elija destinatarios: todo el centro, cursos, clases o usuarios específicos.
   - Redacte el título y contenido.
   - Añada adjuntos si es necesario (hasta 5MB por archivo).
   - Active "Requiere confirmación" si necesita que los destinatarios confirmen la lectura.
   - Pulse "Enviar".

2. **Gestionar Comunicados**:
   - Vea estadísticas de lectura y confirmación.
   - Filtre por estado, fecha o destinatarios.
   - Envíe recordatorios a quienes no han leído/confirmado.
   - Archive comunicados antiguos.
   - Edite o cancele comunicados pendientes.

#### Para Familias

1. **Ver Comunicados**:
   - Acceda desde la tarjeta "Comunicados" en el dashboard.
   - Los comunicados no leídos aparecen destacados.
   - Utilice los filtros: Todos, No leídos, Importantes, Archivados.

2. **Acciones sobre Comunicados**:
   - Pulse sobre un comunicado para leerlo completo.
   - En la vista detallada puede:
     - Marcar como leído
     - Descargar adjuntos
     - Confirmar lectura (si se requiere)
     - Responder (si está habilitado)
     - Archivar para consulta futura

### Calendario y Eventos

Gestione eventos, reuniones, exámenes y otras actividades importantes.

1. **Ver Calendario**:
   - Acceda desde "Calendario" en el menú.
   - Alterne entre vistas: Día, Semana, Mes.
   - Filtre por categorías de eventos (académicos, reuniones, extraescolares).
   - Pulse sobre un evento para ver detalles.

2. **Crear Evento** (Administradores y Profesores):
   - Pulse "+" en la esquina inferior de la vista de calendario.
   - Complete los campos: título, fecha, hora, lugar, descripción.
   - Seleccione la categoría y color del evento.
   - Elija destinatarios: centro, curso, clase o usuarios específicos.
   - Configure recordatorios (15min, 1h, 1 día antes).
   - Pulse "Guardar".

3. **Sincronizar con Calendario Personal**:
   - En la configuración del calendario, active "Sincronizar con mi calendario".
   - Seleccione la cuenta de Google o calendario del dispositivo.
   - Elija qué tipos de eventos desea sincronizar.

### Gestión de Usuarios

#### Para Administradores

1. **Añadir Usuario**:
   - Acceda a "Gestión de Usuarios" en el menú.
   - Pulse "+" para añadir nuevo usuario.
   - Seleccione el tipo de usuario y complete la información requerida.
   - Para profesores y administradores, asigne el centro correspondiente.
   - Pulse "Guardar" para crear el usuario y enviar las credenciales.

2. **Gestionar Permisos**:
   - Seleccione un usuario de la lista.
   - Pulse "Permisos" en su ficha.
   - Active o desactive funcionalidades específicas.
   - Para administradores de centro, configure el alcance de sus permisos.

3. **Gestión Masiva**:
   - Utilice "Importar Usuarios" para crear múltiples usuarios desde un archivo CSV.
   - Descargue la plantilla, complete los datos y súbala al sistema.
   - Revise los resultados y corrija errores si es necesario.

### Gestión Académica

#### Para Administradores de Centro

1. **Configurar Estructura Académica**:
   - Acceda a "Gestión Académica" en el menú.
   - Cree cursos con "Añadir Curso".
   - Dentro de cada curso, cree clases con "Añadir Clase".
   - Asigne tutores y capacidades a cada clase.

2. **Gestionar Horarios**:
   - Seleccione una clase y pulse "Horario".
   - Utilice la matriz de días/horas para asignar materias y profesores.
   - Guarde los cambios y publique el horario cuando esté completo.

3. **Gestionar Evaluaciones**:
   - Configure periodos de evaluación en "Configuración Académica".
   - Establezca criterios y plantillas de evaluación.
   - Defina fechas límite para la entrega de calificaciones.

### Chat

El sistema de chat permite la comunicación directa entre profesores y familias.

1. **Iniciar Conversación**:
   - Acceda a "Chat" en el menú principal.
   - Para profesores: seleccione una familia de la lista o busque por nombre.
   - Para familias: seleccione un profesor de la lista asociada a su hijo.
   - Pulse "Nueva conversación" e introduzca el primer mensaje.

2. **Gestionar Conversaciones**:
   - Las conversaciones activas aparecen ordenadas por fecha de último mensaje.
   - Los mensajes no leídos se destacan y muestran un contador.
   - Puede archivar conversaciones antiguas mediante "Archivar".
   - Recupere conversaciones archivadas en "Archivados".

3. **Adjuntar Archivos**:
   - Dentro de una conversación, pulse el clip para adjuntar.
   - Seleccione el tipo: documento, imagen, audio.
   - Elija el archivo de su dispositivo o tome una foto/grabación.
   - Añada un comentario opcional y envíe.

4. **Configurar Notificaciones**:
   - En "Configuración" > "Notificaciones" personalice:
     - Sonidos para nuevos mensajes
     - Visualización en pantalla de bloqueo
     - Horarios de silencio
     - Prioridad de contactos

## Solución de Problemas

### Problemas de Conexión

1. **La aplicación muestra "Sin conexión"**:
   - Verifique su conexión a Internet.
   - Intente cambiar entre WiFi y datos móviles.
   - Pulse "Reintentar" o cierre y vuelva a abrir la aplicación.
   - La mayoría de las funciones seguirán disponibles en modo offline y se sincronizarán cuando vuelva la conexión.

2. **Datos desactualizados**:
   - Pulse el icono de actualizar en la esquina superior.
   - Vaya a "Configuración" > "Sincronización" y pulse "Sincronizar ahora".
   - Si el problema persiste, cierre sesión y vuelva a iniciarla.

3. **Error al realizar una acción que envía correo electrónico** (Ej. Test de Email, aprobación de usuario, etc.):
   - La aplicación ahora utiliza un servicio externo para garantizar el envío correcto de correos HTML.
   - Si ve errores como "Error de red al enviar" o "Error del script", primero verifique su conexión a Internet.
   - Si el problema persiste con buena conexión, podría haber un problema temporal con el servicio de envío. Inténtelo de nuevo más tarde o contacte con soporte si el error es recurrente.

### Problemas de Cuenta

1. **No puedo iniciar sesión**:
   - Verifique que su correo y contraseña sean correctos.
   - Compruebe si su cuenta ha sido desactivada contactando con su administrador.
   - Utilice "¿Olvidó su contraseña?" para restablecerla.
   - Si el problema persiste, contacte con soporte.

2. **No veo a todos mis hijos/alumnos**:
   - Para familias: contacte con el administrador del centro para verificar la vinculación.
   - Para profesores: compruebe sus asignaciones de clase con el administrador.

### Problemas con Notificaciones (Push)

1. **No recibo notificaciones push**:
   - Vaya a Configuración del dispositivo > Aplicaciones > UmeEgunero > Notificaciones y verifique que estén habilitadas.
   - En la aplicación, vaya a "Configuración" > "Notificaciones" y compruebe la configuración.
   - Verifique que no tenga activo el modo "No molestar" en su dispositivo.

2. **Recibo notificaciones push duplicadas**:
   - Vaya a "Configuración" > "Notificaciones" > "Restablecer preferencias".
   - Cierre sesión y vuelva a iniciarla.

### Problemas con Archivos

1. **No puedo subir archivos**:
   - Verifique que el archivo no supere los 5MB.
   - Compruebe los formatos permitidos (.pdf, .doc, .docx, .jpg, .png).
   - Asegúrese de tener espacio suficiente en su dispositivo.
   - Verifique que tiene una conexión estable a Internet.

2. **No puedo abrir un archivo adjunto**:
   - Asegúrese de tener una aplicación compatible para el tipo de archivo.
   - Intente descargar el archivo en lugar de abrirlo directamente.
   - Si el problema persiste, contacte con el emisor del archivo.

## Preguntas Frecuentes

### Sobre Cuentas y Accesos

**P: ¿Puedo usar la misma cuenta en múltiples dispositivos?**
R: Sí, puede iniciar sesión con sus credenciales en varios dispositivos simultáneamente.

**P: ¿Cómo puedo cambiar mi contraseña?**
R: Vaya a su perfil de usuario, pulse "Seguridad" y seleccione "Cambiar contraseña".

**P: ¿Puedo tener diferentes perfiles en la misma cuenta?**
R: No, cada usuario tiene un tipo de perfil específico. Si necesita roles diferentes, deberá usar cuentas separadas.

### Sobre Comunicaciones

**P: ¿Los destinatarios ven quién más ha recibido un comunicado?**
R: No por defecto. Los destinatarios solo ven que han recibido el comunicado, no la lista completa de receptores, a menos que el emisor active la opción "Mostrar destinatarios".

**P: ¿Puedo cancelar un comunicado ya enviado?**
R: Puede cancelarlo si aún no ha sido leído por ningún destinatario. Una vez leído, solo puede enviar una rectificación.

**P: ¿Hay límite de almacenamiento para comunicados?**
R: Los comunicados se archivan automáticamente después de 6 meses, pero siguen siendo accesibles a través de la sección "Archivo".

### Sobre Uso General

**P: ¿La aplicación consume muchos datos móviles?**
R: El consumo es moderado. Puede reducirlo activando "Ahorro de datos" en la configuración, que limitará la descarga de imágenes y archivos grandes a conexiones WiFi.

**P: ¿Cómo cambio el idioma de la aplicación?**
R: La aplicación usa por defecto el idioma del sistema. Para cambiarlo, modifique el idioma en la configuración de su dispositivo.

**P: ¿Se guardan copias de seguridad de mis datos?**
R: Sí, toda la información se sincroniza con los servidores y se realizan copias de seguridad diarias.

**P: ¿Puedo usar la aplicación sin conexión?**
R: Sí, muchas funciones están disponibles offline. Los cambios se sincronizarán cuando recupere la conexión.

---

## Ejemplo de Caso de Uso

**Escenario:** Un familiar consulta el estado emocional y rutinas de su hijo/a y solicita una reunión con el tutor.

1. Accede al perfil del niño/a y visualiza su evolución reciente.
2. Consulta las rutinas diarias y mensajes del profesorado.
3. Solicita una reunión usando el formulario integrado.

---

## Ejemplo de Uso para Reflejar Mejoras y Enfoque Infantil

**Escenario:** Un profesor utiliza la aplicación para registrar el seguimiento emocional y social de sus alumnos y compartirlo con las familias.

1. Accede a la pantalla de seguimiento emocional y social.
2. Registra las observaciones y comentarios sobre el desarrollo de cada alumno.
3. Comparte el informe con las familias a través de la aplicación.

---

## Accesibilidad y Usabilidad

- Contraste de colores y fuentes adaptadas a infantil.
- Botones grandes y navegación sencilla.
- Compatibilidad con lectores de pantalla.

---

## Nuevas Pantallas y Mejoras UX

Se han añadido nuevas pantallas orientadas a la etapa infantil (2-4 años):
- Seguimiento emocional y social (profesor)
- Resumen de rutinas (profesor)
- Bienestar del niño/a (familia)
- Solicitud de reunión (familia)
- Centro de recursos familiares

Todas las pantallas siguen las directrices de Material 3 y buenas prácticas de accesibilidad.

---

## Ejemplo de Caso de Uso

**Escenario:** Un familiar consulta el bienestar y evolución de su hijo/a y solicita una reunión con el tutor.

1. Accede al perfil del niño/a y visualiza su evolución reciente.
2. Consulta las rutinas diarias y mensajes del profesorado.
3. Solicita una reunión usando el formulario integrado.

---

---

## Versión del manual: 2.3.1 (actualizado a la versión 4.2.0 de la aplicación) 