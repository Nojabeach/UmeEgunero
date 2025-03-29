package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un centro educativo en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para los centros educativos, que son
 * entidades fundamentales en la jerarquía organizativa de la aplicación. Un centro
 * educativo constituye la unidad principal que agrupa profesores, cursos, clases
 * y alumnos, y sirve como base para la organización de toda la actividad educativa.
 * 
 * Los centros educativos se almacenan como documentos en la colección 'centros' 
 * en Firestore, donde el ID del documento coincide con el campo [id] de esta clase.
 * 
 * Relaciones principales:
 * - Un centro puede tener múltiples administradores ([adminIds])
 * - Un centro puede tener múltiples profesores ([profesorIds])
 * - Un centro tiene múltiples cursos (referenciados desde la colección 'cursos')
 * - Un centro tiene múltiples aulas (referenciadas desde la colección 'aulas')
 * - Un centro tiene múltiples alumnos (referenciados desde la colección 'alumnos')
 * 
 * Esta entidad es administrada principalmente por usuarios con rol ADMIN_APP y ADMIN_CENTRO.
 * 
 * @property id Identificador único del centro, anotado con @DocumentId para Firestore
 * @property nombre Nombre completo del centro educativo
 * @property direccion Dirección física del centro, estructurada en un objeto [Direccion]
 * @property contacto Información de contacto del centro, estructurada en un objeto [Contacto]
 * @property adminIds Lista de IDs de usuarios con permisos de administración sobre este centro
 * @property profesorIds Lista de IDs de profesores asignados a este centro
 * @property activo Indica si el centro está actualmente operativo en el sistema
 * @property latitud Coordenada geográfica para ubicación en mapas y cálculos de distancia
 * @property longitud Coordenada geográfica para ubicación en mapas y cálculos de distancia
 * @property descripcion Texto descriptivo sobre el centro, su enfoque educativo, etc.
 *
 * @see Direccion Para la estructura de la dirección física
 * @see Contacto Para la estructura de la información de contacto
 * @see Curso Entidad relacionada que define los cursos ofrecidos por el centro
 * @see Aula Entidad relacionada que define las aulas físicas del centro
 * @see Usuario Entidad relacionada para administradores y profesores
 */
data class Centro(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val direccion: Direccion = Direccion(),
    val contacto: Contacto = Contacto(),
    val adminIds: List<String> = emptyList(),
    val profesorIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val descripcion: String = ""
) 