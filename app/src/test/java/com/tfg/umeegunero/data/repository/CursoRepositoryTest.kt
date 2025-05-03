package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.util.Result
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para el repositorio de cursos.
 * 
 * Estas pruebas validan el correcto funcionamiento de las operaciones
 * de obtención y manipulación de los cursos académicos.
 * 
 * @author Maitane - TFG UmeEgunero
 */
@ExperimentalCoroutinesApi
class CursoRepositoryTest {

    // Mocks
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cursosCollection: CollectionReference
    private lateinit var clasesCollection: CollectionReference
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var query: Query
    private lateinit var documentReference: DocumentReference
    
    // Repositorio a probar
    private lateinit var cursoRepository: CursoRepository
    
    @Before
    fun setup() {
        // Inicializar mocks
        firestore = mockk(relaxed = true)
        cursosCollection = mockk(relaxed = true)
        clasesCollection = mockk(relaxed = true)
        documentSnapshot = mockk(relaxed = true)
        querySnapshot = mockk(relaxed = true)
        query = mockk(relaxed = true)
        documentReference = mockk(relaxed = true)
        
        // Configurar el comportamiento básico de los mocks
        every { firestore.collection("cursos") } returns cursosCollection
        every { firestore.collection("clases") } returns clasesCollection
        
        // Inicializar repositorio con el mock de firestore
        cursoRepository = CursoRepository()
        // Asignar el mock de firestore al campo privado usando reflection
        val field = CursoRepository::class.java.getDeclaredField("firestore")
        field.isAccessible = true
        field.set(cursoRepository, firestore)
    }
    
    @Test
    fun `obtenerCursosPorCentro retorna lista de cursos cuando hay datos`() = runTest {
        // Given: Un centro con cursos
        val centroId = "centro123"
        val cursos = listOf(
            Curso(id = "curso1", nombre = "1º Infantil", centroId = centroId),
            Curso(id = "curso2", nombre = "2º Infantil", centroId = centroId)
        )
        
        // Configurar el comportamiento de los mocks para simular la consulta
        every { cursosCollection.whereEqualTo("centroId", centroId) } returns query
        every { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns cursos.map { 
            mockk<DocumentSnapshot>().also { 
                every { it.toObject(Curso::class.java) } returns cursos[cursos.indexOfFirst { curso -> curso.id == it.id }]
                every { it.id } returns cursos[cursos.indexOfFirst { curso -> curso.id == it.id }].id
            }
        }
        
        // When: Obtenemos los cursos del centro
        val result = cursoRepository.obtenerCursosPorCentro(centroId)
        
        // Then: Debería retornar una lista con los cursos
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        assertEquals("1º Infantil", result.data[0].nombre)
        assertEquals("2º Infantil", result.data[1].nombre)
        
        // Verificación: Se realizó la consulta con los parámetros correctos
        verify { cursosCollection.whereEqualTo("centroId", centroId) }
        verify { query.get() }
    }
    
    @Test
    fun `obtenerCursosPorCentro retorna lista vacía cuando no hay cursos`() = runTest {
        // Given: Un centro sin cursos
        val centroId = "centro456"
        
        // Configurar el comportamiento de los mocks para simular consulta sin resultados
        every { cursosCollection.whereEqualTo("centroId", centroId) } returns query
        every { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()
        
        // When: Obtenemos los cursos del centro
        val result = cursoRepository.obtenerCursosPorCentro(centroId)
        
        // Then: Debería retornar una lista vacía
        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.size)
        
        // Verificación: Se realizó la consulta con los parámetros correctos
        verify { cursosCollection.whereEqualTo("centroId", centroId) }
        verify { query.get() }
    }
    
    @Test
    fun `obtenerClasesPorCurso retorna lista de clases cuando hay datos`() = runTest {
        // Given: Un curso con clases
        val cursoId = "curso789"
        val clases = listOf(
            Clase(id = "clase1", nombre = "Clase A", cursoId = cursoId),
            Clase(id = "clase2", nombre = "Clase B", cursoId = cursoId)
        )
        
        // Configurar el comportamiento de los mocks para simular la consulta
        every { clasesCollection.whereEqualTo("cursoId", cursoId) } returns query
        every { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns clases.map { 
            mockk<DocumentSnapshot>().also { 
                every { it.toObject(Clase::class.java) } returns clases[clases.indexOfFirst { clase -> clase.id == it.id }]
                every { it.id } returns clases[clases.indexOfFirst { clase -> clase.id == it.id }].id
            }
        }
        
        // When: Obtenemos las clases del curso
        val result = cursoRepository.obtenerClasesPorCurso(cursoId)
        
        // Then: Debería retornar una lista con las clases
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        assertEquals("Clase A", result.data[0].nombre)
        assertEquals("Clase B", result.data[1].nombre)
        
        // Verificación: Se realizó la consulta con los parámetros correctos
        verify { clasesCollection.whereEqualTo("cursoId", cursoId) }
        verify { query.get() }
    }
    
    @Test
    fun `crearCurso retorna éxito cuando se guarda correctamente`() = runTest {
        // Given: Un nuevo curso a crear
        val nuevoCurso = Curso(
            id = UUID.randomUUID().toString(),
            nombre = "3º Infantil",
            centroId = "centro123"
        )
        
        // Configurar el comportamiento de los mocks para simular la operación de guardar
        every { cursosCollection.document(nuevoCurso.id) } returns documentReference
        coEvery { documentReference.set(nuevoCurso).await() } returns mockk()
        
        // When: Creamos el nuevo curso
        val result = cursoRepository.crearCurso(nuevoCurso)
        
        // Then: Debería retornar éxito
        assertTrue(result is Result.Success)
        assertEquals(nuevoCurso.id, (result as Result.Success).data)
        
        // Verificación: Se guardó el curso con los datos correctos
        verify { cursosCollection.document(nuevoCurso.id) }
        coVerify { documentReference.set(nuevoCurso) }
    }
    
    @Test
    fun `obtenerCursoById retorna el curso cuando existe`() = runTest {
        // Given: Un curso existente
        val cursoId = "curso123"
        val curso = Curso(id = cursoId, nombre = "1º Primaria", centroId = "centro123")
        
        // Configurar el comportamiento de los mocks para simular la consulta
        every { cursosCollection.document(cursoId) } returns documentReference
        every { documentReference.get().await() } returns documentSnapshot
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.toObject(Curso::class.java) } returns curso
        
        // When: Obtenemos el curso por su ID
        val result = cursoRepository.obtenerCursoById(cursoId)
        
        // Then: Debería retornar el curso
        assertTrue(result is Result.Success)
        assertEquals(cursoId, (result as Result.Success).data.id)
        assertEquals("1º Primaria", result.data.nombre)
        
        // Verificación: Se realizó la consulta con el ID correcto
        verify { cursosCollection.document(cursoId) }
        verify { documentReference.get() }
    }
} 