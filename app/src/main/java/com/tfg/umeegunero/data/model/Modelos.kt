package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Enums para los diferentes tipos
enum class TipoUsuario {
    ADMIN_APP, ADMIN_CENTRO, PROFESOR, FAMILIAR, ALUMNO
}

enum class SubtipoFamiliar {
    PADRE, MADRE, TUTOR, OTRO
}

enum class EstadoSolicitud {
    PENDIENTE, APROBADA, RECHAZADA
}

enum class NivelConsumo {
    BIEN, POCO, NADA
}

enum class TipoObservacion {
    TOALLITAS, PAÑALES, ROPA, OTRO
}

enum class TemaPref {
    LIGHT, DARK, SYSTEM
}

// Modelos de datos principales
data class Usuario(
    val dni: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val fechaRegistro: Timestamp = Timestamp.now(),
    val ultimoAcceso: Timestamp? = null,
    val activo: Boolean = true,
    val perfiles: List<Perfil> = emptyList(),
    val direccion: Direccion? = null,
    val preferencias: Preferencias = Preferencias()
) {
    @field:DocumentId
    var documentId: String = dni
}

data class Perfil(
    val tipo: TipoUsuario = TipoUsuario.FAMILIAR,
    val subtipo: SubtipoFamiliar? = null,
    val centroId: String = "",
    val verificado: Boolean = false,
    val alumnos: List<String> = emptyList() // Lista de DNIs de alumnos
)

data class Direccion(
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = ""
)

data class Preferencias(
    val idiomaApp: String = "es",
    val notificaciones: Notificaciones = Notificaciones(),
    val tema: TemaPref = TemaPref.SYSTEM
)

data class Notificaciones(
    val push: Boolean = true,
    val email: Boolean = true
)

data class Centro(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val direccion: Direccion = Direccion(),
    val contacto: Contacto = Contacto(),
    val adminIds: List<String> = emptyList(),
    val profesorIds: List<String> = emptyList(),
    val activo: Boolean = true
)

data class Contacto(
    val telefono: String = "",
    val email: String = ""
)

data class Aula(
    @DocumentId val id: String = "",
    val centroId: String = "",
    val nombre: String = "",
    val curso: String = "",
    val profesorIds: List<String> = emptyList(),
    val alumnoIds: List<String> = emptyList()
)

data class Alumno(
    @DocumentId val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fechaNacimiento: Timestamp = Timestamp.now(),
    val centroId: String = "",
    val aulaId: String = "",
    val familiarIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val necesidadesEspeciales: String = "",
    val alergias: List<String> = emptyList(),
    val observaciones: String = ""
)

data class SolicitudRegistro(
    @DocumentId val id: String = "",
    val usuarioId: String = "",
    val centroId: String = "",
    val tipoSolicitud: TipoUsuario = TipoUsuario.FAMILIAR,
    val alumnoIds: List<String> = emptyList(),
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val fechaSolicitud: Timestamp = Timestamp.now(),
    val fechaResolucion: Timestamp? = null,
    val resolutorId: String = "",
    val motivoRechazo: String = ""
)

data class RegistroActividad(
    @DocumentId val id: String = "",
    val alumnoId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val profesorId: String = "",
    val comidas: Comidas = Comidas(),
    val siesta: Siesta? = null,
    val necesidadesFisiologicas: NecesidadesFisiologicas = NecesidadesFisiologicas(),
    val observaciones: List<Observacion> = emptyList(),
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null
)

data class Comidas(
    val primerPlato: Plato = Plato(),
    val segundoPlato: Plato = Plato(),
    val postre: Plato = Plato()
)

data class Plato(
    val descripcion: String = "",
    val consumo: NivelConsumo = NivelConsumo.BIEN
)

data class Siesta(
    val inicio: Timestamp? = null,
    val fin: Timestamp? = null,
    val duracion: Int = 0  // En minutos
)

data class NecesidadesFisiologicas(
    val caca: Boolean = false,
    val pipi: Boolean = false
)

data class Observacion(
    val tipo: TipoObservacion = TipoObservacion.OTRO,
    val mensaje: String = "",
    val hora: Timestamp = Timestamp.now()
)

data class Mensaje(
    @DocumentId val id: String = "",
    val emisorId: String = "",
    val receptorId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val texto: String = "",
    val leido: Boolean = false,
    val fechaLeido: Timestamp? = null,
    val alumnoId: String = "",
    val adjuntos: List<String> = emptyList()
)

// Modelos para la gestión académica
data class Curso(
    @DocumentId val id: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "Infantil 3 años", "Primaria 1º"
    val descripcion: String = "",
    val edadMinima: Int = 0,
    val edadMaxima: Int = 0,
    val aniosNacimiento: List<Int> = emptyList(), // Lista de años de nacimiento de los alumnos que pertenecen a este curso
    val activo: Boolean = true,
    val anioAcademico: String = "" // Por ejemplo: "2023-2024"
)

data class Clase(
    @DocumentId val id: String = "",
    val cursoId: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "A", "B", "Mañana", "Tarde"
    val profesorTitularId: String = "",
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val alumnosIds: List<String> = emptyList(),
    val capacidadMaxima: Int = 25,
    val activo: Boolean = true,
    val horario: String = "", // Descripción del horario o referencia a un objeto Horario
    val aula: String = "" // Ubicación física dentro del centro
)

// Modelo para registro de nuevo usuario (usado en UI)
data class RegistroUsuarioForm(
    val dni: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val subtipo: SubtipoFamiliar = SubtipoFamiliar.PADRE,
    val direccion: Direccion = Direccion(),
    val alumnosDni: List<String> = emptyList(),
    // Datos del centro a seleccionar
    val centroId: String = ""
)