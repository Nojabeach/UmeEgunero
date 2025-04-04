package com.tfg.umeegunero.util

import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import java.util.Date
import java.util.UUID
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.PrioridadTarea

/**
 * Clase de utilidades para crear objetos mock en pruebas y desarrollo.
 * Proporciona funciones para generar objetos modelo con datos de ejemplo.
 */
object TestUtils {
    /**
     * Crea un objeto Usuario de prueba
     * 
     * @param dni Identificador opcional del usuario
     * @return Usuario con datos de prueba
     */
    fun createMockUsuario(dni: String = UUID.randomUUID().toString()): Usuario {
        return Usuario(
            dni = dni,
            nombre = "Usuario Prueba",
            email = "usuario.prueba@umeegunero.com",
            telefono = "600123456",
            fechaRegistro = Timestamp.now(),
            activo = true
        )
    }
    
    /**
     * Crea un objeto Alumno de prueba
     * 
     * @param id Identificador opcional del alumno
     * @return Alumno con datos de prueba
     */
    fun createMockAlumno(id: String = UUID.randomUUID().toString()): Alumno {
        return Alumno(
            id = id,
            dni = "${id}_dni",
            nombre = "Alumno Prueba",
            apellidos = "Apellidos Prueba",
            fechaNacimiento = "2020-01-01",
            centroId = "centro1",
            curso = "Infantil 3 años",
            clase = "A",
            alergias = listOf("Lactosa"),
            observaciones = "Alumno de prueba para desarrollo",
            activo = true
        )
    }
    
    /**
     * Crea un objeto Clase de prueba
     * 
     * @param id Identificador opcional de la clase
     * @return Clase con datos de prueba
     */
    fun createMockClase(id: String = UUID.randomUUID().toString()): Clase {
        return Clase(
            id = id,
            nombre = "Clase Prueba",
            profesorTitularId = "profesor1",
            centroId = "centro1",
            cursoId = "infantil3",
            alumnosIds = listOf("alumno1", "alumno2", "alumno3"),
            capacidadMaxima = 25,
            activo = true
        )
    }
    
    /**
     * Crea un objeto Evento de prueba
     * 
     * @param id Identificador opcional del evento
     * @return Evento con datos de prueba
     */
    fun createMockEvento(id: String = UUID.randomUUID().toString()): Evento {
        return Evento(
            id = id,
            titulo = "Evento de Prueba",
            descripcion = "Descripción detallada del evento de prueba",
            fecha = Timestamp.now(),
            tipo = TipoEvento.OTRO,
            creadorId = "profesor1",
            centroId = "centro1",
            recordatorio = true,
            publico = true,
            destinatarios = listOf("todos")
        )
    }
    
    /**
     * Crea un objeto Tarea de prueba
     * 
     * @param id Identificador opcional de la tarea
     * @return Tarea con datos de prueba
     */
    fun createMockTarea(id: String = UUID.randomUUID().toString()): Tarea {
        return Tarea(
            id = id,
            titulo = "Tarea de Prueba",
            descripcion = "Descripción detallada de la tarea de prueba",
            profesorId = "profesor1",
            profesorNombre = "Profesor Prueba",
            claseId = "clase1",
            nombreClase = "Clase A",
            fechaCreacion = Timestamp.now(),
            fechaEntrega = Timestamp(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)),
            asignatura = "Matemáticas",
            prioridad = PrioridadTarea.MEDIA,
            estado = EstadoTarea.PENDIENTE
        )
    }
    
    /**
     * Crea un objeto RegistroActividad de prueba
     * 
     * @param id Identificador opcional del registro
     * @return RegistroActividad con datos de prueba
     */
    fun createMockRegistroActividad(id: String = UUID.randomUUID().toString()): RegistroActividad {
        return RegistroActividad(
            id = id,
            alumnoId = "alumno1",
            alumnoNombre = "Alumno Prueba",
            claseId = "clase1",
            fecha = Timestamp.now(),
            observaciones = "Ha participado activamente"
        )
    }
} 