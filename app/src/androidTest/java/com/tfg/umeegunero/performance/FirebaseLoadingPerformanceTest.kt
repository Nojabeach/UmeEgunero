package com.tfg.umeegunero.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.time.measureTimedValue

/**
 * Pruebas de rendimiento para la carga de datos desde Firebase.
 * 
 * Estas pruebas verifican que los tiempos de carga de datos desde Firebase
 * se encuentran dentro de umbrales aceptables para garantizar una buena
 * experiencia de usuario. Se establecen tiempos máximos de respuesta para
 * diversas operaciones críticas como la carga de listas y detalles.
 * 
 * @author Maitane - TFG UmeEgunero
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FirebaseLoadingPerformanceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var centroRepository: CentroRepository
    
    @Inject
    lateinit var cursoRepository: CursoRepository
    
    @Inject
    lateinit var claseRepository: ClaseRepository
    
    @Inject
    lateinit var usuarioRepository: UsuarioRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    /**
     * Verifica que la carga de todos los centros se realiza en menos de 3 segundos.
     * 
     * Esta prueba mide el tiempo que tarda en obtenerse la lista completa de centros
     * educativos desde Firebase, para garantizar que la operación es lo suficientemente
     * rápida para mantener una buena experiencia de usuario.
     */
    @Test
    fun testTiempoCargaCentros() {
        val startTime = Instant.now()
        
        // Ejecutar la operación de carga
        val result = centroRepository.getAllCentros()
        
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        
        // Verificar que el resultado es exitoso
        assertTrue(result is Result.Success)
        
        // Verificar que la operación se completó en menos de 3 segundos
        assertTrue("La carga de centros tardó demasiado: ${duration.toMillis()} ms", 
                   duration.toMillis() < 3000)
        
        // Verificar que se obtuvieron datos
        assertTrue("No se obtuvieron centros", (result as Result.Success).data.isNotEmpty())
        
        // Registrar métricas para análisis
        println("Tiempo de carga de centros: ${duration.toMillis()} ms para ${result.data.size} centros")
        println("Tiempo medio por centro: ${duration.toMillis().toFloat() / result.data.size} ms")
    }
    
    /**
     * Verifica que la carga de cursos de un centro se realiza en menos de 1 segundo.
     * 
     * Esta prueba mide el tiempo que tarda en obtenerse la lista de cursos asociados
     * a un centro educativo específico, operación importante para la navegación en la app.
     */
    @Test
    fun testTiempoCargaCursos() {
        // Primero obtenemos un ID de centro válido
        val centrosResult = centroRepository.getAllCentros()
        assertTrue(centrosResult is Result.Success)
        assertTrue((centrosResult as Result.Success).data.isNotEmpty())
        
        val centroId = centrosResult.data.first().id
        
        // Medimos el tiempo de carga de cursos
        val startTime = Instant.now()
        val result = cursoRepository.obtenerCursosPorCentro(centroId)
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        
        // Verificaciones
        assertTrue(result is Result.Success)
        assertTrue("La carga de cursos tardó demasiado: ${duration.toMillis()} ms", 
                   duration.toMillis() < 1000)
        
        // Registrar métricas
        println("Tiempo de carga de cursos para centro $centroId: ${duration.toMillis()} ms para ${(result as Result.Success).data.size} cursos")
    }
    
    /**
     * Verifica que la carga de clases de un curso se realiza en menos de 1 segundo.
     * 
     * Esta prueba mide el tiempo que tarda en obtenerse la lista de clases asociadas
     * a un curso específico, operación frecuente en la gestión académica.
     */
    @Test
    fun testTiempoCargaClases() {
        // Primero obtenemos un ID de centro válido
        val centrosResult = centroRepository.getAllCentros()
        assertTrue(centrosResult is Result.Success)
        assertTrue((centrosResult as Result.Success).data.isNotEmpty())
        
        val centroId = centrosResult.data.first().id
        
        // Luego obtenemos un ID de curso válido
        val cursosResult = cursoRepository.obtenerCursosPorCentro(centroId)
        assertTrue(cursosResult is Result.Success)
        
        // Si no hay cursos, omitimos la prueba
        if ((cursosResult as Result.Success).data.isEmpty()) {
            println("No hay cursos para probar la carga de clases, prueba omitida")
            return
        }
        
        val cursoId = cursosResult.data.first().id
        
        // Medimos el tiempo de carga de clases
        val startTime = Instant.now()
        val result = cursoRepository.obtenerClasesPorCurso(cursoId)
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        
        // Verificaciones
        assertTrue(result is Result.Success)
        assertTrue("La carga de clases tardó demasiado: ${duration.toMillis()} ms", 
                   duration.toMillis() < 1000)
        
        // Registrar métricas
        println("Tiempo de carga de clases para curso $cursoId: ${duration.toMillis()} ms para ${(result as Result.Success).data.size} clases")
    }
    
    /**
     * Verifica que la carga de un usuario por DNI se realiza en menos de 500 ms.
     * 
     * Esta prueba mide el tiempo que tarda en obtenerse los datos de un usuario específico,
     * operación crítica para la autenticación y personalización.
     */
    @Test
    fun testTiempoCargaUsuario() {
        // Primero obtenemos la lista de usuarios para encontrar un DNI válido
        val usuariosResult = usuarioRepository.getAllUsuarios()
        assertTrue(usuariosResult is Result.Success)
        assertTrue((usuariosResult as Result.Success).data.isNotEmpty())
        
        val dniUsuario = usuariosResult.data.first().dni
        
        // Medimos el tiempo de carga del usuario
        val startTime = Instant.now()
        val result = usuarioRepository.getUsuarioByDni(dniUsuario)
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        
        // Verificaciones
        assertTrue(result is Result.Success)
        assertTrue("La carga de usuario tardó demasiado: ${duration.toMillis()} ms", 
                   duration.toMillis() < 500)
        
        // Registrar métricas
        println("Tiempo de carga de usuario con DNI $dniUsuario: ${duration.toMillis()} ms")
    }
} 