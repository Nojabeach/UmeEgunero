package com.tfg.umeegunero.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Test instrumentado para la base de datos Room.
 * Verifica las operaciones CRUD sobre la tabla de registros de actividad.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var registroDao: RegistroActividadDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Crear la base de datos en memoria
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        registroDao = db.registroActividadDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertarYObtenerRegistroActividad() = runBlocking {
        // Crear un registro de prueba
        val registro = RegistroActividadEntity(
            id = "test1",
            alumnoId = "alumno1",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = Date().time,
            haDesayunado = true,
            comidaPrincipal = "Arroz con pollo",
            haHechoCaca = true,
            vistoPorFamiliar = false,
            isSynced = true
        )

        // Insertar en la base de datos
        registroDao.insertRegistro(registro)

        // Obtener el registro por ID
        val registroObtenido = registroDao.getRegistroById("test1")

        // Verificar que se ha recuperado correctamente
        assertNotNull(registroObtenido)
        registroObtenido?.let {
            assertEquals("test1", it.id)
            assertEquals("alumno1", it.alumnoId)
            assertEquals(true, it.haDesayunado)
            assertEquals("Arroz con pollo", it.comidaPrincipal)
            assertEquals(true, it.haHechoCaca)
            assertEquals(false, it.vistoPorFamiliar)
        }
    }

    @Test
    fun actualizarRegistroActividad() = runBlocking {
        // Insertar un registro
        val registro = RegistroActividadEntity(
            id = "test2",
            alumnoId = "alumno1",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = Date().time,
            haDesayunado = true,
            comidaPrincipal = "Pasta",
            haHechoCaca = false,
            vistoPorFamiliar = false,
            isSynced = true
        )
        registroDao.insertRegistro(registro)

        // Actualizar el registro
        val registroActualizado = registro.copy(
            comidaPrincipal = "Lentejas",
            haHechoCaca = true,
            isSynced = false
        )
        registroDao.updateRegistro(registroActualizado)

        // Obtener el registro actualizado
        val registroObtenido = registroDao.getRegistroById("test2")

        // Verificar que se ha actualizado correctamente
        assertNotNull(registroObtenido)
        registroObtenido?.let {
            assertEquals("test2", it.id)
            assertEquals("Lentejas", it.comidaPrincipal)
            assertEquals(true, it.haHechoCaca)
            assertEquals(false, it.isSynced)
        }
    }

    @Test
    fun eliminarRegistroActividad() = runBlocking {
        // Insertar un registro
        val registro = RegistroActividadEntity(
            id = "test3",
            alumnoId = "alumno1",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = Date().time,
            haDesayunado = true,
            comidaPrincipal = "Pescado",
            haHechoCaca = false,
            vistoPorFamiliar = false,
            isSynced = true
        )
        registroDao.insertRegistro(registro)

        // Verificar que se ha insertado
        assertNotNull(registroDao.getRegistroById("test3"))

        // Eliminar el registro
        registroDao.deleteRegistro(registro)

        // Verificar que se ha eliminado
        assertNull(registroDao.getRegistroById("test3"))
    }

    @Test
    fun obtenerRegistrosPorAlumno() = runBlocking {
        // Eliminar registros previos
        registroDao.deleteAllRegistros()

        // Insertar varios registros para un alumno
        val fecha1 = Date().time
        val fecha2 = Date().time - 86400000 // Ayer
        val fecha3 = Date().time - 172800000 // Anteayer

        val registro1 = RegistroActividadEntity(
            id = "test4",
            alumnoId = "alumno2",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = fecha1,
            haDesayunado = true,
            isSynced = true
        )

        val registro2 = RegistroActividadEntity(
            id = "test5",
            alumnoId = "alumno2",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = fecha2,
            haDesayunado = false,
            isSynced = true
        )

        val registro3 = RegistroActividadEntity(
            id = "test6",
            alumnoId = "alumno3", // Otro alumno
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = fecha3,
            haDesayunado = true,
            isSynced = true
        )

        registroDao.insertRegistros(listOf(registro1, registro2, registro3))

        // Obtener registros por alumno
        val registrosAlumno2 = registroDao.getRegistrosByAlumnoId("alumno2").first()
        val registrosAlumno3 = registroDao.getRegistrosByAlumnoId("alumno3").first()

        // Verificar que se obtienen los registros correctos
        assertEquals(2, registrosAlumno2.size)
        assertEquals(1, registrosAlumno3.size)
        assertEquals("test4", registrosAlumno2[0].id) // El más reciente primero
        assertEquals("test5", registrosAlumno2[1].id)
        assertEquals("test6", registrosAlumno3[0].id)
    }

    @Test
    fun marcarComoSincronizado() = runBlocking {
        // Insertar un registro no sincronizado
        val registro = RegistroActividadEntity(
            id = "test7",
            alumnoId = "alumno1",
            claseId = "clase1",
            profesorId = "profesor1",
            fechaTimestamp = Date().time,
            haDesayunado = true,
            isSynced = false
        )
        registroDao.insertRegistro(registro)

        // Verificar que no está sincronizado
        val registrosNoSincronizados = registroDao.getUnsyncedRegistros()
        assertEquals(1, registrosNoSincronizados.size)
        assertEquals("test7", registrosNoSincronizados[0].id)

        // Marcar como sincronizado
        registroDao.markAsSynced("test7")

        // Verificar que ahora está sincronizado
        val registrosNoSincronizadosActualizados = registroDao.getUnsyncedRegistros()
        assertEquals(0, registrosNoSincronizadosActualizados.size)
    }
} 