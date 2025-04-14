# Pruebas de Dashboard - UmeEgunero

Este documento detalla las pruebas a realizar para verificar el correcto funcionamiento de los dashboards para cada perfil de usuario en la aplicación UmeEgunero. Servirá como guía y registro de las verificaciones realizadas para el Trabajo de Fin de Grado.

## Metodología de Pruebas

Para cada dashboard se verificarán los siguientes aspectos:
1. **Navegación**: Funcionamiento de todos los botones y enlaces.
2. **Visualización de datos**: Correcta presentación de datos y estadísticas.
3. **Tiempo real**: Actualización en tiempo real de la información.
4. **Interactividad**: Respuesta a acciones del usuario.
5. **Rendimiento**: Tiempo de carga y fluidez de interacción.

Cada elemento se marcará con uno de los siguientes estados:
- ✅ Funciona correctamente
- ⚠️ Funciona con limitaciones
- ❌ No funciona
- 🔄 Pendiente de verificar

## Dashboard Administrador de Aplicación

### Navegación y Botones

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Botón de Perfil | 🔄 | |
| Menú lateral | 🔄 | |
| Botón Gestión de Centros | 🔄 | |
| Botón Gestión de Usuarios | 🔄 | |
| Botón Configuración | 🔄 | |
| Botón Estadísticas | 🔄 | |
| Botón Notificaciones | 🔄 | |
| Botón Soporte | 🔄 | |
| Botón Cerrar Sesión | 🔄 | |

### Tarjetas de Información

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Tarjeta Total Centros | 🔄 | Verificar datos correctos |
| Tarjeta Usuarios Activos | 🔄 | Verificar actualización en tiempo real |
| Tarjeta Nuevos Registros | 🔄 | Verificar datos del periodo correcto |

### Gráficos y Datos en Tiempo Real

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Gráfico de Usuarios por Tipo | 🔄 | Comprobar distribución correcta |
| Gráfico de Actividad | 🔄 | Verificar actualización en tiempo real |
| Listado de Centros | 🔄 | Comprobar ordenación y filtrado |
| Indicadores de Estado | 🔄 | Verificar código de colores correcto |

## Dashboard Administrador de Centro

### Navegación y Botones

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Botón de Perfil | 🔄 | |
| Menú lateral | 🔄 | |
| Botón Gestión Académica | 🔄 | |
| Botón Profesores | 🔄 | |
| Botón Alumnos | 🔄 | |
| Botón Comunicaciones | 🔄 | |
| Botón Calendario | 🔄 | |
| Botón Configuración | 🔄 | |
| Botón Cerrar Sesión | 🔄 | |

### Tarjetas de Información

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Tarjeta Total Alumnos | 🔄 | |
| Tarjeta Total Profesores | 🔄 | |
| Tarjeta Total Clases | 🔄 | |
| Tarjeta Asistencia | 🔄 | Verificar porcentaje correcto |

### Gráficos y Datos en Tiempo Real

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Gráfico de Distribución por Curso | 🔄 | |
| Gráfico de Asistencia Semanal | 🔄 | Verificar actualización diaria |
| Calendario de Eventos | 🔄 | Comprobar eventos próximos |
| Notificaciones Recientes | 🔄 | Verificar tiempo real |
| Listado de Clases | 🔄 | |

## Dashboard Profesor

### Navegación y Botones

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Botón de Perfil | 🔄 | |
| Menú lateral | 🔄 | |
| Botón Mis Clases | 🔄 | |
| Botón Actividades | 🔄 | |
| Botón Comunicaciones | 🔄 | |
| Botón Asistencia | 🔄 | |
| Botón Evaluaciones | 🔄 | |
| Botón Calendario | 🔄 | |
| Botón Cerrar Sesión | 🔄 | |

### Tarjetas de Información

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Tarjeta Clases Asignadas | 🔄 | |
| Tarjeta Alumnos Totales | 🔄 | |
| Tarjeta Mensajes sin Leer | 🔄 | Verificar contador en tiempo real |
| Tarjeta Próximos Eventos | 🔄 | |

### Gráficos y Datos en Tiempo Real

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Gráfico de Asistencia | 🔄 | |
| Gráfico de Progreso en Actividades | 🔄 | |
| Listado de Clases | 🔄 | Comprobar acceso directo a cada clase |
| Timeline de Comunicaciones | 🔄 | Verificar ordenación cronológica |
| Estado de Notificaciones | 🔄 | Comprobar actualización en tiempo real |

## Dashboard Familiar

### Navegación y Botones

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Botón de Perfil | 🔄 | |
| Menú lateral | 🔄 | |
| Botón Mis Hijos | 🔄 | |
| Botón Comunicaciones | 🔄 | |
| Botón Actividades | 🔄 | |
| Botón Calendario | 🔄 | |
| Botón Chat | 🔄 | |
| Botón Cerrar Sesión | 🔄 | |

### Tarjetas de Información

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Tarjetas de Hijos | 🔄 | Verificar información correcta |
| Tarjeta Mensajes sin Leer | 🔄 | |
| Tarjeta Actividades Pendientes | 🔄 | |
| Tarjeta Próximos Eventos | 🔄 | |

### Gráficos y Datos en Tiempo Real

| Elemento | Estado | Observaciones |
|----------|--------|---------------|
| Gráfico de Asistencia | 🔄 | |
| Visualización de Progreso | 🔄 | |
| Timeline de Actividad | 🔄 | |
| Notificaciones | 🔄 | Verificar recepción en tiempo real |
| Acceso a Chat | 🔄 | Comprobar indicador de mensajes nuevos |

## Pruebas de Navegación entre Dashboards

### Flujos Críticos

| Flujo | Estado | Observaciones |
|-------|--------|---------------|
| Login → Dashboard correcto según perfil | 🔄 | |
| Dashboard Admin App → Gestión Centros → Detalle Centro | 🔄 | |
| Dashboard Centro → Gestión Académica → Clases → Alumnos | 🔄 | |
| Dashboard Profesor → Clase → Lista Alumnos → Detalle Alumno | 🔄 | |
| Dashboard Familiar → Hijo → Actividades | 🔄 | |
| Cualquier Dashboard → Perfil → Editar Perfil → Guardar | 🔄 | |
| Cualquier Dashboard → Cerrar Sesión → Login | 🔄 | |

## Pruebas de Tiempo Real

### Verificación de Actualizaciones en Tiempo Real

| Escenario | Estado | Observaciones |
|-----------|--------|---------------|
| Nuevo comunicado → Actualización en dashboard receptor | 🔄 | |
| Cambio en asistencia → Actualización de gráficos | 🔄 | |
| Nuevo mensaje → Indicador de notificación | 🔄 | |
| Nueva actividad → Aparece en dashboard familiar | 🔄 | |
| Cambio de estado en actividad → Actualización en gráficos | 🔄 | |

## Pruebas de Rendimiento

### Tiempos de Carga

| Dashboard | Tiempo Promedio | Estado | Observaciones |
|-----------|-----------------|--------|---------------|
| Admin App | ? segundos | 🔄 | |
| Centro | ? segundos | 🔄 | |
| Profesor | ? segundos | 🔄 | |
| Familiar | ? segundos | 🔄 | |

### Consumo de Recursos

| Dashboard | CPU | Memoria | Red | Estado | Observaciones |
|-----------|-----|---------|-----|--------|---------------|
| Admin App | ? | ? | ? | 🔄 | |
| Centro | ? | ? | ? | 🔄 | |
| Profesor | ? | ? | ? | 🔄 | |
| Familiar | ? | ? | ? | 🔄 | |

## Resultados y Conclusiones

[Esta sección se completará una vez realizadas las pruebas con los hallazgos principales, problemas identificados y recomendaciones]

## Registro de Ejecución de Pruebas

| Fecha | Versión App | Perfil Probado | Tester | Resultado General |
|-------|-------------|----------------|--------|-------------------|
| | | | | |
| | | | | |
| | | | | |
| | | | | | 