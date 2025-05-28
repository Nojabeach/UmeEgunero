package com.tfg.umeegunero.data.model

/**
 * Modelo que representa un perfil de usuario en el sistema UmeEgunero, definiendo
 * su rol y contexto de actuación dentro de la plataforma educativa.
 * 
 * Esta clase permite implementar un sistema de múltiples roles para un mismo usuario, 
 * donde cada perfil representa un conjunto específico de capacidades, permisos y 
 * relaciones dentro del ecosistema educativo. Un usuario puede tener varios perfiles,
 * cada uno asociado potencialmente a diferentes centros o alumnos.
 * 
 * El modelo de perfiles múltiples proporciona flexibilidad en escenarios como:
 * - Un profesor que trabaja en varios centros educativos
 * - Un familiar que tiene hijos en diferentes centros
 * - Un administrador que gestiona múltiples centros
 * - Un usuario que es tanto profesor como familiar de alumno
 * 
 * Cada perfil está ligado al tipo de usuario, y opcionalmente a un subtipo específico
 * si se trata de un familiar, así como al centro educativo donde se ejerce ese rol.
 * 
 * @property tipo Rol principal del usuario dentro del sistema, define sus permisos generales
 * @property subtipo Especificación adicional del tipo de relación familiar (solo para FAMILIAR)
 * @property centroId Identificador del centro educativo donde se aplica este perfil
 * @property verificado Indica si el perfil ha sido verificado por un administrador
 * @property alumnos Lista de identificadores (DNI) de alumnos asociados a este perfil
 * @property clasesIds Lista de identificadores de clases asignadas (solo para PROFESOR)
 * 
 * @see TipoUsuario Enumeración que define los posibles roles de usuario
 * @see SubtipoFamiliar Enumeración que detalla los tipos de relaciones familiares
 * @see Usuario Clase principal que contiene la colección de perfiles
 */
data class Perfil(
    val tipo: TipoUsuario = TipoUsuario.FAMILIAR,
    val subtipo: SubtipoFamiliar? = null,
    val centroId: String = "",
    val verificado: Boolean = false,
    val alumnos: List<String> = emptyList(), // Lista de DNIs de alumnos
    val clasesIds: List<String> = emptyList() // Lista de IDs de clases (para profesores)
) 