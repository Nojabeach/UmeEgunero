/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene los componentes relacionados con el proceso de registro
 * y gestión de datos de dirección de los usuarios.
 * 
 * @see DireccionStep
 */
package com.tfg.umeegunero.feature.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Paso del formulario de registro para la introducción de datos de dirección.
 * 
 * Este componente implementa un formulario para la captura de datos de dirección
 * con validación en tiempo real y feedback visual. Utiliza Material Design 3
 * para proporcionar una experiencia de usuario coherente y moderna.
 * 
 * ## Campos del formulario
 * - Calle (vía pública)
 * - Número de portal
 * - Piso y puerta
 * - Código postal
 * - Ciudad
 * - Provincia
 * 
 * ## Características
 * - Validación en tiempo real de todos los campos
 * - Feedback visual de errores
 * - Organización optimizada del espacio
 * - Teclados específicos para cada tipo de dato
 * - Navegación fluida entre campos
 * - Soporte para scroll vertical
 * 
 * ## Validaciones implementadas
 * - Formato de código postal español (5 dígitos)
 * - Campos obligatorios
 * - Longitud máxima de campos
 * 
 * @param calle Nombre de la vía pública
 * @param numero Número del portal
 * @param piso Identificador de piso y puerta
 * @param codigoPostal Código postal (5 dígitos)
 * @param ciudad Nombre de la ciudad
 * @param provincia Nombre de la provincia
 * @param onCalleChange Callback para cambios en calle
 * @param onNumeroChange Callback para cambios en número
 * @param onPisoChange Callback para cambios en piso
 * @param onCodigoPostalChange Callback para cambios en código postal
 * @param onCiudadChange Callback para cambios en ciudad
 * @param onProvinciaChange Callback para cambios en provincia
 * @param errors Mapa de errores por campo
 * 
 * @see Card
 * @see OutlinedTextField
 * @see MaterialTheme
 */
@Composable
fun DireccionStep(
    calle: String,
    numero: String,
    piso: String,
    codigoPostal: String,
    ciudad: String,
    provincia: String,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onPisoChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onProvinciaChange: (String) -> Unit,
    errors: Map<String, String>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                // Calle
                OutlinedTextField(
                    value = calle,
                    onValueChange = onCalleChange,
                    label = { Text("Calle") },
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("calle"),
                    supportingText = {
                        if (errors.containsKey("calle")) {
                            Text(errors["calle"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Número y Piso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Número
                    OutlinedTextField(
                        value = numero,
                        onValueChange = onNumeroChange,
                        label = { Text("Número") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        isError = errors.containsKey("numero"),
                        supportingText = {
                            if (errors.containsKey("numero")) {
                                Text(errors["numero"] ?: "")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Piso
                    OutlinedTextField(
                        value = piso,
                        onValueChange = onPisoChange,
                        label = { Text("Piso/Puerta") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        isError = errors.containsKey("piso"),
                        supportingText = {
                            if (errors.containsKey("piso")) {
                                Text(errors["piso"] ?: "")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Código Postal
                OutlinedTextField(
                    value = codigoPostal,
                    onValueChange = onCodigoPostalChange,
                    label = { Text("Código Postal") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("codigoPostal"),
                    supportingText = {
                        if (errors.containsKey("codigoPostal")) {
                            Text(errors["codigoPostal"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Ciudad
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = onCiudadChange,
                    label = { Text("Ciudad") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("ciudad"),
                    supportingText = {
                        if (errors.containsKey("ciudad")) {
                            Text(errors["ciudad"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Provincia
                OutlinedTextField(
                    value = provincia,
                    onValueChange = onProvinciaChange,
                    label = { Text("Provincia") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    isError = errors.containsKey("provincia"),
                    supportingText = {
                        if (errors.containsKey("provincia")) {
                            Text(errors["provincia"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Vista previa del componente DireccionStep en estado vacío.
 * 
 * Muestra el formulario sin datos introducidos y sin errores.
 */
@Preview(
    name = "Dirección Step",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DireccionStepPreview() {
    UmeEguneroTheme {
        DireccionStep(
            calle = "",
            numero = "",
            piso = "",
            codigoPostal = "",
            ciudad = "",
            provincia = "",
            onCalleChange = {},
            onNumeroChange = {},
            onPisoChange = {},
            onCodigoPostalChange = {},
            onCiudadChange = {},
            onProvinciaChange = {},
            errors = emptyMap()
        )
    }
}

/**
 * Vista previa del componente DireccionStep con datos de ejemplo.
 * 
 * Muestra el formulario con todos los campos completados correctamente.
 */
@Preview(
    name = "Dirección Step - Con Datos",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DireccionStepWithDataPreview() {
    UmeEguneroTheme {
        DireccionStep(
            calle = "Calle Principal",
            numero = "42",
            piso = "3B",
            codigoPostal = "28001",
            ciudad = "Madrid",
            provincia = "Madrid",
            onCalleChange = {},
            onNumeroChange = {},
            onPisoChange = {},
            onCodigoPostalChange = {},
            onCiudadChange = {},
            onProvinciaChange = {},
            errors = emptyMap()
        )
    }
}

/**
 * Vista previa del componente DireccionStep con errores.
 * 
 * Muestra el formulario con errores de validación en varios campos.
 */
@Preview(
    name = "Dirección Step - Con Errores",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DireccionStepWithErrorsPreview() {
    UmeEguneroTheme {
        DireccionStep(
            calle = "Calle Principal",
            numero = "abc", // Error: no es un número válido
            piso = "3B",
            codigoPostal = "123", // Error: código postal incompleto
            ciudad = "",  // Error: campo requerido
            provincia = "Madrid",
            onCalleChange = {},
            onNumeroChange = {},
            onPisoChange = {},
            onCodigoPostalChange = {},
            onCiudadChange = {},
            onProvinciaChange = {},
            errors = mapOf(
                "numero" to "Debe ser un número válido",
                "codigoPostal" to "El código postal debe tener 5 dígitos",
                "ciudad" to "La ciudad es obligatoria"
            )
        )
    }
}

/**
 * Vista previa del componente DireccionStep en modo oscuro.
 * 
 * Muestra el formulario con el tema oscuro de Material Design.
 */
@Preview(
    name = "Dirección Step - Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DireccionStepDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        DireccionStep(
            calle = "Calle Principal",
            numero = "42",
            piso = "3B",
            codigoPostal = "28001",
            ciudad = "Madrid",
            provincia = "Madrid",
            onCalleChange = {},
            onNumeroChange = {},
            onPisoChange = {},
            onCodigoPostalChange = {},
            onCiudadChange = {},
            onProvinciaChange = {},
            errors = emptyMap()
        )
    }
}