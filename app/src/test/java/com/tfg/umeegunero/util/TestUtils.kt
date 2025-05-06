package com.tfg.umeegunero.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import java.util.Calendar
import java.util.Date

/**
 * Clase de utilidades para pruebas.
 * 
 * Esta clase proporciona diversos métodos y funciones para facilitar la creación
 * de datos de prueba y simulación de entornos para testing.
 * 
 * Incluye generadores de datos ficticios para:
 * - Usuarios (diferentes roles)
 * - Alumnos
 * - Centros educativos
 * - Mensajes
 * - Notificaciones
 * - Asistencias
 * - Otros datos de prueba
 */
object TestUtils {

    /**
     * Crea un [Intent] con [Bundle] para pruebas.
     * @param context Contexto Android
     * @param destinationClass Clase de destino para el Intent
     * @param bundle Bundle opcional con datos extras
     * @return Intent configurado
     */
    fun createTestIntent(
        context: Context,
        destinationClass: Class<*>,
        bundle: Bundle? = null
    ): Intent {
        val intent = Intent(context, destinationClass)
        bundle?.let {
            intent.putExtras(it)
        }
        return intent
    }

    /**
     * Genera una lista de centros educativos de prueba.
     * @param count Número de centros a generar
     * @return Lista de centros educativos
     */
    fun generateTestCentros(count: Int): List<Centro> {
        val centros = mutableListOf<Centro>()
        
        for (i in 1..count) {
            centros.add(
                Centro(
                    id = "centro$i",
                    nombre = "Centro Educativo $i",
                    codigo = "CE$i",
                    direccion = mapOf(
                        "calle" to "Calle Principal $i",
                        "ciudad" to "Ciudad $i",
                        "provincia" to "Provincia $i",
                        "codigoPostal" to "0100$i"
                    ),
                    telefono = "94${i}000000",
                    email = "centro$i@example.com",
                    adminId = "admin$i",
                    logo = "https://example.com/logo$i.png",
                    activo = true
                )
            )
        }
        
        return centros
    }

    /**
     * Genera una lista de usuarios de prueba.
     * @param count Número de usuarios a generar
     * @param tipoUsuario Tipo de usuario a generar
     * @return Lista de usuarios
     */
    fun generateTestUsuarios(count: Int, tipoUsuario: TipoUsuario): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        
        for (i in 1..count) {
            usuarios.add(
                Usuario(
                    uid = "user$i",
                    email = "usuario$i@example.com",
                    dni = "0000000${i}T",
                    nombre = "Usuario $i",
                    apellidos = "Apellido Apellido",
                    telefono = "6$i$i000000",
                    fotoPerfil = "https://example.com/avatar$i.png",
                    tipoUsuario = tipoUsuario,
                    centroId = if (tipoUsuario == TipoUsuario.FAMILIAR) null else "centro1",
                    fechaRegistro = Timestamp(Date()),
                    activo = true
                )
            )
        }
        
        return usuarios
    }

    /**
     * Genera una lista de alumnos de prueba.
     * @param count Número de alumnos a generar
     * @return Lista de alumnos
     */
    fun generateTestAlumnos(count: Int): List<Alumno> {
        val alumnos = mutableListOf<Alumno>()
        
        for (i in 1..count) {
            // Asignar edad aleatoria entre 2 y 6 años
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -(2 + i % 5))
            calendar.add(Calendar.DAY_OF_YEAR, -(1 + i % 100))
            
            alumnos.add(
                Alumno(
                    id = "alumno$i",
                    dni = "",
                    nombre = "Alumno $i",
                    apellidos = "Apellido Apellido",
                    fechaNacimiento = Timestamp(calendar.time),
                    foto = "https://example.com/alumno$i.png",
                    claseId = "clase${1 + i % 3}",
                    centroId = "centro1",
                    familiaresIds = listOf("familiar1", "familiar2"),
                    activo = true,
                    curso = "Infantil ${i % 3 + 1}",
                    clase = "Clase ${1 + i % 3}"
                )
            )
        }
        
        return alumnos
    }

    /**
     * Genera una lista de clases de prueba.
     * @param count Número de clases a generar
     * @return Lista de clases
     */
    fun generateTestClases(count: Int): List<Clase> {
        val clases = mutableListOf<Clase>()
        
        for (i in 1..count) {
            clases.add(
                Clase(
                    id = "clase$i",
                    nombre = "Clase $i",
                    cursoId = "curso${1 + i % 3}",
                    tutorId = "profesor$i",
                    profesoresIds = listOf("profesor$i", "profesor${i+1}"),
                    capacidad = 20 + i,
                    activa = true,
                    centroId = "centro1",
                    curso = "Infantil ${i % 3 + 1}"
                )
            )
        }
        
        return clases
    }

    /**
     * Genera una lista de asistencias de prueba.
     * @param count Número de asistencias a generar
     * @param alumnoId ID del alumno al que pertenecen las asistencias
     * @return Lista de asistencias
     */
    fun generateTestAsistencias(count: Int, alumnoId: String): List<Asistencia> {
        val asistencias = mutableListOf<Asistencia>()
        val calendar = Calendar.getInstance()
        
        // Retrocedemos al primer día para ir avanzando
        calendar.add(Calendar.DAY_OF_YEAR, -count)
        
        // Lista de tipos de asistencia para rotar
        val tiposAsistencia = listOf(
            Asistencia.TipoAsistencia.PRESENTE, 
            Asistencia.TipoAsistencia.AUSENTE, 
            Asistencia.TipoAsistencia.RETRASO
        )
        
        for (i in 1..count) {
            asistencias.add(
                Asistencia(
                    id = "asistencia$i",
                    alumnoId = alumnoId,
                    tipoAsistencia = tiposAsistencia[i % tiposAsistencia.size].name,
                    fecha = Timestamp(calendar.time),
                    comentarios = if (i % 5 == 0) "Comentario para día ${calendar.get(Calendar.DAY_OF_MONTH)}" else null,
                    registradoPor = "profesor1"
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return asistencias
    }
} 