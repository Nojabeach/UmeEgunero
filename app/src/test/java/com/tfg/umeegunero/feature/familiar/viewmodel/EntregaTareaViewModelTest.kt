package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.TareaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class EntregaTareaViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tareaRepository: TareaRepository
    private lateinit var viewModel: EntregaTareaViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tareaRepository = mockk(relaxed = true)
        viewModel = EntregaTareaViewModel(tareaRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `inicializar carga tarea correctamente`() = runTest {
        // Given
        val tareaId = "tarea123"
        val alumnoId = "alumno456"
        val tarea = Tarea(
            id = tareaId,
            titulo = "Tarea de prueba",
            descripcion = "Descripción de la tarea de prueba",
            fechaCreacion = Timestamp.now(),
            fechaEntrega = Timestamp.now()
        )
        
        coEvery { tareaRepository.obtenerTarea(tareaId) } returns Result.Success(tarea)
        
        // When
        viewModel.inicializar(tareaId, alumnoId)
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals(tarea, uiState.tarea)
        assertEquals(alumnoId, uiState.alumnoId)
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `inicializar maneja error correctamente`() = runTest {
        // Given
        val tareaId = "tarea123"
        val alumnoId = "alumno456"
        val errorMessage = "Error al cargar tarea"
        
        coEvery { tareaRepository.obtenerTarea(tareaId) } returns Result.Error(Exception(errorMessage))
        
        // When
        viewModel.inicializar(tareaId, alumnoId)
        val uiState = viewModel.uiState.first()
        
        // Then
        assertNull(uiState.tarea)
        assertEquals(false, uiState.isLoading)
        assertEquals("Error al cargar la tarea: $errorMessage", uiState.error)
    }
    
    @Test
    fun `enviarEntrega guarda entrega correctamente`() = runTest {
        // Given
        val tareaId = "tarea123"
        val alumnoId = "alumno456"
        val tarea = Tarea(
            id = tareaId,
            titulo = "Tarea de prueba",
            descripcion = "Descripción de la tarea de prueba",
            fechaCreacion = Timestamp.now(),
            fechaEntrega = Timestamp.now()
        )
        val comentario = "Comentario de prueba"
        val archivos = listOf("uri1", "uri2")
        val entregaId = "entrega789"
        
        // Configurar el estado inicial del ViewModel
        coEvery { tareaRepository.obtenerTarea(tareaId) } returns Result.Success(tarea)
        viewModel.inicializar(tareaId, alumnoId)
        
        // Mock para la entrega
        coEvery { tareaRepository.guardarEntrega(any()) } returns Result.Success(entregaId)
        coEvery { tareaRepository.actualizarEstadoTarea(tareaId, EstadoTarea.COMPLETADA.name) } returns Result.Success(true)
        
        // When
        viewModel.enviarEntrega(comentario, archivos)
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals("Tarea entregada exitosamente", uiState.mensaje)
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.error)
        
        // Verificar que se llamaron los métodos del repositorio
        coVerify { tareaRepository.guardarEntrega(any()) }
        coVerify { tareaRepository.actualizarEstadoTarea(tareaId, EstadoTarea.COMPLETADA.name) }
    }
    
    @Test
    fun `enviarEntrega maneja error correctamente`() = runTest {
        // Given
        val tareaId = "tarea123"
        val alumnoId = "alumno456"
        val tarea = Tarea(
            id = tareaId,
            titulo = "Tarea de prueba",
            descripcion = "Descripción de la tarea de prueba",
            fechaCreacion = Timestamp.now(),
            fechaEntrega = Timestamp.now()
        )
        val comentario = "Comentario de prueba"
        val archivos = listOf("uri1", "uri2")
        val errorMessage = "Error al guardar entrega"
        
        // Configurar el estado inicial del ViewModel
        coEvery { tareaRepository.obtenerTarea(tareaId) } returns Result.Success(tarea)
        viewModel.inicializar(tareaId, alumnoId)
        
        // Mock para la entrega
        coEvery { tareaRepository.guardarEntrega(any()) } returns Result.Error(Exception(errorMessage))
        
        // When
        viewModel.enviarEntrega(comentario, archivos)
        val uiState = viewModel.uiState.first()
        
        // Then
        assertEquals("Error al enviar la entrega: $errorMessage", uiState.error)
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.mensaje)
        
        // Verificar que solo se llamó el método de guardar (no el de actualizar estado)
        coVerify { tareaRepository.guardarEntrega(any()) }
        coVerify(exactly = 0) { tareaRepository.actualizarEstadoTarea(any(), any()) }
    }
    
    @Test
    fun `limpiarMensajes limpia error y mensaje`() = runTest {
        // Given
        val errorMessage = "Error de prueba"
        val mensaje = "Mensaje de prueba"
        
        // Configurar el estado inicial con error y mensaje
        viewModel = EntregaTareaViewModel(tareaRepository)
        viewModel.inicializar("tarea123", "alumno456")
        
        // Establecer error y mensaje manualmente
        val uiStateField = EntregaTareaViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        val _uiState = uiStateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<EntregaTareaUiState>
        _uiState.value = _uiState.value.copy(error = errorMessage, mensaje = mensaje)
        
        // Verificar que el estado tiene error y mensaje
        assertEquals(errorMessage, viewModel.uiState.first().error)
        assertEquals(mensaje, viewModel.uiState.first().mensaje)
        
        // When
        viewModel.limpiarMensajes()
        
        // Then
        assertNull(viewModel.uiState.first().error)
        assertNull(viewModel.uiState.first().mensaje)
    }
} 