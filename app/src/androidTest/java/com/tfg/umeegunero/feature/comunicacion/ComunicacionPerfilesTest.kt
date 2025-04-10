package com.tfg.umeegunero.feature.comunicacion

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.common.comunicacion.screen.BandejaEntradaScreen
import com.tfg.umeegunero.feature.common.comunicacion.screen.ComposerMensajeScreen
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.ComunicacionViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Test para verificar la interacción entre diferentes perfiles, como
 * la comunicación entre profesores y familias.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ComunicacionPerfilesTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    /**
     * Test para verificar el envío de mensajes de un profesor a un familiar
     */
    @Test
    fun testProfesorEnviaMensajeAFamiliar() {
        // 1. Configurar ViewModels y datos necesarios
        val comunicacionViewModel = mockk<ComunicacionViewModel>(relaxed = true)
        
        // Usuario profesor actual
        val profesor = Usuario(
            id = "prof123",
            nombre = "Pedro",
            apellidos = "Martínez",
            email = "profesor@eguneroko.com",
            tipoUsuario = TipoUsuario.PROFESOR
        )
        
        // Familiar destinatario
        val familiar = Usuario(
            id = "fam123",
            nombre = "Ana",
            apellidos = "García",
            email = "familiar@eguneroko.com",
            tipoUsuario = TipoUsuario.FAMILIAR
        )
        
        // Alumno (hijo del familiar)
        val alumno = Alumno(
            id = "alumno1",
            nombre = "Juan",
            apellidos = "García",
            fechaNacimiento = Date(),
            claseId = "clase1"
        )
        
        // Configurar los datos del ViewModel
        every { comunicacionViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(profesor)
        every { comunicacionViewModel.contactos } returns androidx.compose.runtime.mutableStateOf(
            listOf(familiar)
        )
        every { comunicacionViewModel.alumnosPorFamiliar } returns mapOf(
            familiar.id to listOf(alumno)
        )
        
        // Capturar el mensaje que se va a enviar
        val mensajeSlot = slot<Mensaje>()
        every { comunicacionViewModel.enviarMensaje(capture(mensajeSlot)) } returns Unit
        
        // 2. Mostrar pantalla de composición de mensaje
        composeTestRule.setContent {
            UmeEguneroTheme {
                ComposerMensajeScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = comunicacionViewModel,
                    destinatarioPreseleccionado = familiar.id
                )
            }
        }
        
        // 3. Interactuar con la interfaz para crear y enviar el mensaje
        // Verificar que el destinatario está preseleccionado
        composeTestRule.onNodeWithText(familiar.nombre + " " + familiar.apellidos, useUnmergedTree = true)
            .assertExists("El destinatario no aparece en la pantalla")
        
        // Ingresar asunto
        composeTestRule.onNodeWithText("Asunto", useUnmergedTree = true)
            .performTextInput("Reunión sobre progreso de Juan")
        
        // Ingresar contenido del mensaje
        composeTestRule.onNodeWithText("Mensaje", useUnmergedTree = true)
            .performTextInput("Estimada Ana, me gustaría concertar una reunión para hablar sobre el progreso de Juan. ¿Le vendría bien el próximo martes?")
        
        // Enviar mensaje
        composeTestRule.onNodeWithText("Enviar", useUnmergedTree = true)
            .performClick()
        
        // 4. Verificar que se llamó al método de envío con los datos correctos
        verify { comunicacionViewModel.enviarMensaje(any()) }
        
        // Verificar los datos del mensaje capturado
        assertEquals("Reunión sobre progreso de Juan", mensajeSlot.captured.asunto)
        assertEquals(profesor.id, mensajeSlot.captured.remiteId)
        assertEquals(familiar.id, mensajeSlot.captured.destinatarioId)
    }
    
    /**
     * Test para verificar la recepción y lectura de mensajes por un familiar
     */
    @Test
    fun testFamiliarRecibeMensajeDeProfesor() {
        // 1. Configurar ViewModels y datos necesarios
        val comunicacionViewModel = mockk<ComunicacionViewModel>(relaxed = true)
        
        // Usuario familiar actual
        val familiar = Usuario(
            id = "fam123",
            nombre = "Ana",
            apellidos = "García",
            email = "familiar@eguneroko.com",
            tipoUsuario = TipoUsuario.FAMILIAR
        )
        
        // Profesor remitente
        val profesor = Usuario(
            id = "prof123",
            nombre = "Pedro",
            apellidos = "Martínez",
            email = "profesor@eguneroko.com",
            tipoUsuario = TipoUsuario.PROFESOR
        )
        
        // Mensaje recibido
        val mensaje = Mensaje(
            id = "msg123",
            asunto = "Reunión sobre progreso de Juan",
            contenido = "Estimada Ana, me gustaría concertar una reunión para hablar sobre el progreso de Juan. ¿Le vendría bien el próximo martes?",
            remiteId = profesor.id,
            destinatarioId = familiar.id,
            fecha = Date(),
            leido = false
        )
        
        // Configurar los datos del ViewModel
        every { comunicacionViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(familiar)
        every { comunicacionViewModel.contactos } returns androidx.compose.runtime.mutableStateOf(
            listOf(profesor)
        )
        every { comunicacionViewModel.mensajesRecibidos } returns androidx.compose.runtime.mutableStateOf(
            listOf(mensaje)
        )
        every { comunicacionViewModel.usuarioPorId(profesor.id) } returns profesor
        
        // 2. Mostrar bandeja de entrada
        composeTestRule.setContent {
            UmeEguneroTheme {
                BandejaEntradaScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = comunicacionViewModel
                )
            }
        }
        
        // 3. Verificar que el mensaje aparece en la bandeja de entrada
        composeTestRule.onNodeWithText(mensaje.asunto, useUnmergedTree = true)
            .assertExists("El mensaje no aparece en la bandeja de entrada")
        
        composeTestRule.onNodeWithText(profesor.nombre + " " + profesor.apellidos, useUnmergedTree = true)
            .assertExists("El remitente no aparece en la bandeja de entrada")
        
        // Verificar que se muestra como no leído
        composeTestRule.onAllNodesWithContentDescription("Mensaje no leído", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .let { nodes ->
                assert(nodes.isNotEmpty()) {
                    "No se encontró el indicador de mensaje no leído"
                }
            }
        
        // 4. Abrir el mensaje
        composeTestRule.onNodeWithText(mensaje.asunto, useUnmergedTree = true)
            .performClick()
        
        // 5. Verificar que se marca como leído
        verify { comunicacionViewModel.marcarComoLeido(mensaje.id) }
    }
    
    /**
     * Test para verificar la comunicación desde un administrador de centro a todos los profesores
     */
    @Test
    fun testAdminCentroEnviaMensajeATodosProfesores() {
        // 1. Configurar ViewModels y datos necesarios
        val comunicacionViewModel = mockk<ComunicacionViewModel>(relaxed = true)
        
        // Usuario administrador de centro actual
        val adminCentro = Usuario(
            id = "adminc123",
            nombre = "Begoña",
            apellidos = "Merana",
            email = "bmerana@eguneroko.com",
            tipoUsuario = TipoUsuario.ADMIN_CENTRO
        )
        
        // Lista de profesores del centro
        val profesores = listOf(
            Usuario(id = "prof1", nombre = "Pedro", apellidos = "Martínez", email = "pmartinez@eguneroko.com", tipoUsuario = TipoUsuario.PROFESOR),
            Usuario(id = "prof2", nombre = "Laura", apellidos = "Sánchez", email = "lsanchez@eguneroko.com", tipoUsuario = TipoUsuario.PROFESOR),
            Usuario(id = "prof3", nombre = "Carlos", apellidos = "López", email = "clopez@eguneroko.com", tipoUsuario = TipoUsuario.PROFESOR)
        )
        
        // Configurar los datos del ViewModel
        every { comunicacionViewModel.currentUser } returns androidx.compose.runtime.mutableStateOf(adminCentro)
        every { comunicacionViewModel.contactos } returns androidx.compose.runtime.mutableStateOf(profesores)
        every { comunicacionViewModel.gruposPredefinidos } returns androidx.compose.runtime.mutableStateOf(
            mapOf("Todos los profesores" to profesores.map { it.id })
        )
        
        // Capturar los mensajes que se van a enviar
        val mensajesSlot = mutableListOf<Mensaje>()
        every { comunicacionViewModel.enviarMensajeAGrupo(any(), any()) } answers {
            val grupos = secondArg<List<String>>()
            grupos.forEach { grupoId ->
                profesores.forEach { profesor ->
                    mensajesSlot.add(
                        Mensaje(
                            id = "msg_${profesor.id}",
                            asunto = firstArg<Mensaje>().asunto,
                            contenido = firstArg<Mensaje>().contenido,
                            remiteId = adminCentro.id,
                            destinatarioId = profesor.id,
                            fecha = Date(),
                            leido = false
                        )
                    )
                }
            }
        }
        
        // 2. Mostrar pantalla de composición de mensaje
        composeTestRule.setContent {
            UmeEguneroTheme {
                ComposerMensajeScreen(
                    navController = androidx.navigation.compose.rememberNavController(),
                    viewModel = comunicacionViewModel
                )
            }
        }
        
        // 3. Interactuar con la interfaz para crear y enviar el mensaje a todos los profesores
        // Seleccionar grupo "Todos los profesores"
        composeTestRule.onNodeWithText("Seleccionar destinatarios", useUnmergedTree = true)
            .performClick()
        
        composeTestRule.onNodeWithText("Todos los profesores", useUnmergedTree = true)
            .performClick()
        
        // Ingresar asunto
        composeTestRule.onNodeWithText("Asunto", useUnmergedTree = true)
            .performTextInput("Reunión general del claustro")
        
        // Ingresar contenido del mensaje
        composeTestRule.onNodeWithText("Mensaje", useUnmergedTree = true)
            .performTextInput("Estimados profesores, les convoco a una reunión general del claustro el próximo viernes a las 16:00h para tratar temas de evaluación.")
        
        // Enviar mensaje
        composeTestRule.onNodeWithText("Enviar", useUnmergedTree = true)
            .performClick()
        
        // 4. Verificar que se llamó al método de envío con los datos correctos
        verify { comunicacionViewModel.enviarMensajeAGrupo(any(), any()) }
        
        // Verificar que se crearon los mensajes para todos los profesores
        assertEquals(profesores.size, mensajesSlot.size)
        mensajesSlot.forEach { mensaje ->
            assertEquals("Reunión general del claustro", mensaje.asunto)
            assertEquals(adminCentro.id, mensaje.remiteId)
            assert(profesores.any { it.id == mensaje.destinatarioId }) {
                "El mensaje se envió a un destinatario que no es profesor"
            }
        }
    }
} 