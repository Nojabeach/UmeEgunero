package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los diferentes tipos de relaciones familiares o de tutela
 * que un usuario puede tener con un alumno en el sistema UmeEgunero.
 * 
 * Esta clasificación permite especificar con precisión el vínculo entre cada familiar
 * y el estudiante, lo que es relevante para:
 * - Establecer los niveles de autorización y responsabilidad legal
 * - Personalizar las comunicaciones según el tipo de relación
 * - Organizar adecuadamente la estructura familiar en la ficha del alumno
 * - Cumplir con requisitos legales de registro de tutores y responsables
 * 
 * Se utiliza principalmente en los perfiles de tipo [TipoUsuario.FAMILIAR] para
 * detallar la naturaleza específica de la relación con el alumno.
 * 
 * @property PADRE Padre biológico o legal del alumno
 * @property MADRE Madre biológica o legal del alumno
 * @property TUTOR Tutor legal que no es padre ni madre biológico (como un familiar con custodia legal)
 * @property OTRO Otra relación familiar o de tutela no especificada en las categorías anteriores
 * 
 * @see TipoUsuario Enumeración que define los roles generales de usuario
 * @see Perfil Modelo que utiliza esta enumeración para detallar roles familiares
 * @see Usuario Entidad principal que contiene perfiles con estas relaciones
 */
enum class SubtipoFamiliar {
    /** Padre biológico o legal del alumno */
    PADRE, 
    
    /** Madre biológica o legal del alumno */
    MADRE, 
    
    /** Tutor legal que no es padre ni madre biológico */
    TUTOR, 
    
    /** Otra relación familiar no especificada en las categorías anteriores */
    OTRO
} 