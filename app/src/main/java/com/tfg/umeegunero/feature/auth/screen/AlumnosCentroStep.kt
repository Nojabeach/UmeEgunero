/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene las pantallas relacionadas con el proceso de autenticación y registro de usuarios.
 * 
 * @see AlumnosCentroStep
 * @see AlumnoItem
 */
package com.tfg.umeegunero.feature.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import android.content.res.Configuration

/**
 * Componente de paso para la gestión de alumnos y selección de centro educativo.
 * 
 * Este componente implementa la interfaz para:
 * - Añadir y eliminar alumnos mediante su DNI
 * - Seleccionar el centro educativo
 * - Mostrar errores de validación
 * 
 * ## Características
 * - Gestión dinámica de lista de alumnos
 * - Validación en tiempo real de DNIs
 * - Selección de centro educativo con dropdown
 * - Indicador de carga para centros
 * - Manejo de errores por campo
 * 
 * @param alumnos Lista de DNIs de alumnos
 * @param centroId ID del centro seleccionado
 * @param centros Lista de centros disponibles
 * @param isLoadingCentros Estado de carga de centros
 * @param onAddAlumno Callback para añadir alumno
 * @param onRemoveAlumno Callback para eliminar alumno
 * @param onCentroSelect Callback para seleccionar centro
 * @param errors Map de errores por campo
 */
@Composable
fun AlumnosCentroStep(
    alumnos: List<String>,
    centroId: String,
    centros: List<Centro>,
    isLoadingCentros: Boolean,
    onAddAlumno: (String) -> Unit,
    onRemoveAlumno: (String) -> Unit,
    onCentroSelect: (String) -> Unit,
    errors: Map<String, String>
) {
    val scrollState = rememberScrollState()
    var nuevoDni by remember { mutableStateOf("") }
    var showCentrosDropdown by remember { mutableStateOf(false) }
    var centroSeleccionado by remember(centroId) {
        mutableStateOf(centros.find { it.id == centroId }?.nombre ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de alumnos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Hijos/as a registrar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Añadir nuevo alumno
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoDni,
                        onValueChange = { nuevoDni = it },
                        label = { Text("DNI del alumno") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (nuevoDni.isNotBlank()) {
                                onAddAlumno(nuevoDni)
                                nuevoDni = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir alumno",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Lista de alumnos añadidos
                if (alumnos.isEmpty()) {
                    Text(
                        text = "No has añadido ningún alumno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    if (errors.containsKey("alumnos")) {
                        Text(
                            text = errors["alumnos"] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alumnos.forEach { dni ->
                            AlumnoItem(
                                dni = dni,
                                onRemove = { onRemoveAlumno(dni) },
                                isError = errors.containsKey("alumno_${alumnos.indexOf(dni)}"),
                                errorText = errors["alumno_${alumnos.indexOf(dni)}"]
                            )
                        }
                    }
                }
            }
        }

        // Sección de selección de centro
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Centro Educativo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Selector de centro
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCentrosDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        if (isLoadingCentros) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Cargando centros...")
                        } else {
                            Text(
                                text = centroSeleccionado.ifEmpty { "Seleccionar centro" },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCentrosDropdown,
                        onDismissRequest = { showCentrosDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        centros.forEach { centro ->
                            DropdownMenuItem(
                                text = { Text(centro.nombre) },
                                onClick = {
                                    centroSeleccionado = centro.nombre
                                    onCentroSelect(centro.id)
                                    showCentrosDropdown = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                if (errors.containsKey("centro")) {
                    Text(
                        text = errors["centro"] ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Componente de ítem de alumno en la lista.
 * 
 * Muestra el DNI de un alumno con opción de eliminación y manejo de errores.
 * 
 * @param dni DNI del alumno
 * @param onRemove Callback de eliminación
 * @param isError Estado de error
 * @param errorText Mensaje de error
 */
@Composable
fun AlumnoItem(
    dni: String,
    onRemove: () -> Unit,
    isError: Boolean = false,
    errorText: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = dni,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                    color = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (isError && !errorText.isNullOrEmpty()) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
    }
}

/**
 * Vista previa del componente AlumnosCentroStep en modo claro.
 * 
 * Muestra una versión de prueba con datos de ejemplo.
 */
@Preview(
    name = "AlumnosCentroStep - Vacío",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun AlumnosCentroStepEmptyPreview() {
    UmeEguneroTheme {
        AlumnosCentroStep(
            alumnos = emptyList(),
            centroId = "",
            centros = listOf(
                Centro(id = "1", nombre = "IES San José"),
                Centro(id = "2", nombre = "Colegio Santa María"),
                Centro(id = "3", nombre = "Escuela Montessori")
            ),
            isLoadingCentros = false,
            onAddAlumno = {},
            onRemoveAlumno = {},
            onCentroSelect = {},
            errors = mapOf("alumnos" to "Debes añadir al menos un alumno")
        )
    }
}

/**
 * Vista previa del componente AlumnosCentroStep en modo claro.
 * 
 * Muestra una versión de prueba con datos de ejemplo.
 */
@Preview(
    name = "AlumnosCentroStep - Con Alumnos",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun AlumnosCentroStepWithStudentsPreview() {
    UmeEguneroTheme {
        AlumnosCentroStep(
            alumnos = listOf("12345678A", "87654321B", "11223344C"),
            centroId = "2",
            centros = listOf(
                Centro(id = "1", nombre = "IES San José"),
                Centro(id = "2", nombre = "Colegio Santa María"),
                Centro(id = "3", nombre = "Escuela Montessori")
            ),
            isLoadingCentros = false,
            onAddAlumno = {},
            onRemoveAlumno = {},
            onCentroSelect = {},
            errors = emptyMap()
        )
    }
}

/**
 * Vista previa del componente AlumnosCentroStep en modo claro.
 * 
 * Muestra una versión de prueba con datos de ejemplo.
 */
@Preview(
    name = "AlumnosCentroStep - Cargando Centros",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun AlumnosCentroStepLoadingPreview() {
    UmeEguneroTheme {
        AlumnosCentroStep(
            alumnos = listOf("12345678A"),
            centroId = "",
            centros = emptyList(),
            isLoadingCentros = true,
            onAddAlumno = {},
            onRemoveAlumno = {},
            onCentroSelect = {},
            errors = emptyMap()
        )
    }
}

/**
 * Vista previa del componente AlumnosCentroStep en modo claro.
 * 
 * Muestra una versión de prueba con datos de ejemplo.
 */
@Preview(
    name = "AlumnosCentroStep - Con Errores",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun AlumnosCentroStepWithErrorsPreview() {
    UmeEguneroTheme {
        AlumnosCentroStep(
            alumnos = listOf("1234", "87654321B"),
            centroId = "",
            centros = listOf(
                Centro(id = "1", nombre = "IES San José"),
                Centro(id = "2", nombre = "Colegio Santa María"),
                Centro(id = "3", nombre = "Escuela Montessori")
            ),
            isLoadingCentros = false,
            onAddAlumno = {},
            onRemoveAlumno = {},
            onCentroSelect = {},
            errors = mapOf(
                "alumno_0" to "DNI inválido",
                "centro" to "Debes seleccionar un centro"
            )
        )
    }
}

/**
 * Vista previa del componente AlumnosCentroStep en modo oscuro.
 * 
 * Muestra una versión de prueba con datos de ejemplo en modo oscuro.
 */
@Preview(
    name = "AlumnosCentroStep - Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AlumnosCentroStepDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        AlumnosCentroStep(
            alumnos = listOf("12345678A", "87654321B"),
            centroId = "1",
            centros = listOf(
                Centro(id = "1", nombre = "IES San José"),
                Centro(id = "2", nombre = "Colegio Santa María"),
                Centro(id = "3", nombre = "Escuela Montessori")
            ),
            isLoadingCentros = false,
            onAddAlumno = {},
            onRemoveAlumno = {},
            onCentroSelect = {},
            errors = emptyMap()
        )
    }
}

// Preview para el componente AlumnoItem independiente
@Preview(
    name = "AlumnoItem - Normal",
    showBackground = true
)
@Composable
fun AlumnoItemPreview() {
    UmeEguneroTheme {
        AlumnoItem(
            dni = "12345678A",
            onRemove = {},
            isError = false,
            errorText = null
        )
    }
}

@Preview(
    name = "AlumnoItem - Con Error",
    showBackground = true
)
@Composable
fun AlumnoItemErrorPreview() {
    UmeEguneroTheme {
        AlumnoItem(
            dni = "1234",
            onRemove = {},
            isError = true,
            errorText = "El DNI debe tener 9 caracteres"
        )
    }
}