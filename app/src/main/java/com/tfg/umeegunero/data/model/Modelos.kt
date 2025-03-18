package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Tipos de usuarios y estados
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
    NADA, POCO, BIEN, TODO
}

enum class TipoObservacion {
    COMPORTAMIENTO, ACADEMICO, TOALLITAS, PAÑALES, ROPA, OTRO
}

enum class TemaPref {
    LIGHT, DARK, SYSTEM
}

// Modelo para representación simplificada de centro educativo (usado en UI)
data class CentroEducativo(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val codigoPostal: String = "",
    val telefono: String = "",
    val email: String = "",
    val descripcion: String = "",
    val activo: Boolean = true
)

// Modelo para representar ciudades
data class Ciudad(
    val nombre: String,
    val codigoPostal: String,
    val provincia: String = "",
    val codigoProvincia: String = ""
)

/**
 * Modelo para representar los datos de códigos postales obtenidos del dataset local
 */
data class CodigoPostalData(
    val codigoPostal: String,
    val municipio: String,
    val provincia: String,
    val codigoProvincia: String
)

/**
 * Función de extensión para convertir CodigoPostalData a Ciudad
 */
fun CodigoPostalData.toCiudad(): Ciudad {
    return Ciudad(
        nombre = this.municipio,
        codigoPostal = this.codigoPostal,
        provincia = this.provincia,
        codigoProvincia = this.codigoProvincia
    )
}

// Estado UI para añadir centro
data class AddCentroUiState(
    val nombre: String = "",
    val tipoVia: String = "",
    val nombreVia: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val codigoPostal: String = "",
    val telefono: String = "",
    val email: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val isLoading: Boolean = false,
    val errorMessages: List<String> = emptyList(),
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val descripcion: String = "",
    val nombreDirector: String = "",
    val apellidosDirector: String = "",
    val dniDirector: String = "",
    val telefonoDirector: String = "",
    val emailDirector: String = "",
    val fechaNacimientoDirector: String = "",
    val errorMessageDirector: String? = null,
    val showDirectorDialog: Boolean = false,
) {
    val tieneUbicacionValida: Boolean
        get() = latitud.isNotBlank() && longitud.isNotBlank() && 
                latitud.toDoubleOrNull() != null && longitud.toDoubleOrNull() != null
}

// Modelos principales
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
    val profesorIds: List<String> = emptyList(), // Añadido campo profesorIds
    val activo: Boolean = true,
    val necesidadesEspeciales: String = "",
    val alergias: List<String> = emptyList(),
    val medicacion: String = "", // Añadido campo medicacion
    val observacionesMedicas: String = "", // Añadido campo observacionesMedicas
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
    val alumnoNombre: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val profesorId: String? = null,
    val profesorNombre: String? = null,
    val comida: Comida? = null,
    val siesta: Siesta? = null,
    val cacaControl: CacaControl? = null,
    val actividades: Actividad? = null,
    val comidas: Comidas = Comidas(),
    val necesidadesFisiologicas: NecesidadesFisiologicas = NecesidadesFisiologicas(),
    val observaciones: String? = null,
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null
)

/**
 * Modelo para la información de comidas
 */
data class Comida(
    val consumoPrimero: String? = null,
    val descripcionPrimero: String? = null,
    val consumoSegundo: String? = null,
    val descripcionSegundo: String? = null,
    val consumoPostre: String? = null,
    val descripcionPostre: String? = null,
    val observaciones: String? = null
)

/**
 * Modelo para la información de siesta
 */
data class Siesta(
    val duracion: Int = 0,
    val observaciones: String = "",
    val inicio: Timestamp? = null,
    val fin: Timestamp? = null
)

/**
 * Modelo para la información de necesidades fisiológicas (ahora llamado CacaControl)
 */
data class CacaControl(
    val tipo1: Boolean? = null,
    val tipo2: Boolean? = null,
    val tipo3: Boolean? = null,
    val hora: String? = null,
    val cantidad: String? = null,
    val tipo: String? = null,
    val descripcion: String? = null,
    val caca: Boolean = false,
    val pipi: Boolean = false,
    val observaciones: String = ""
)

/**
 * Alias para compatibilidad con código existente
 */
typealias NecesidadesFisiologicas = CacaControl

/**
 * Modelo para la información de actividades
 */
data class Actividad(
    val titulo: String = "",
    val descripcion: String = "",
    val participacion: String = "",
    val observaciones: String = ""
)

/**
 * Modelo para representar un plato de comida y su nivel de consumo
 */
data class Plato(
    val descripcion: String = "",
    val nivelConsumo: NivelConsumo = NivelConsumo.BIEN,
    val consumo: NivelConsumo = NivelConsumo.BIEN
)

/**
 * Modelo para representar las comidas del día
 */
data class Comidas(
    val primerPlato: Plato = Plato(),
    val segundoPlato: Plato = Plato(),
    val postre: Plato = Plato()
)

/**
 * Modelo para representar una observación
 */
data class Observacion(
    val mensaje: String = "",
    val tipo: TipoObservacion = TipoObservacion.OTRO,
    val timestamp: Timestamp = Timestamp.now()
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