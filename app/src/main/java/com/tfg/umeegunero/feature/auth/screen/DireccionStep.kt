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

// Previews para DireccionStep

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

@Preview(
    name = "Dirección Step - Con Errores",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DireccionStepWithErrorsPreview() {
    UmeEguneroTheme {
        DireccionStep(
            calle = "",
            numero = "abc",
            piso = "",
            codigoPostal = "123",
            ciudad = "",
            provincia = "",
            onCalleChange = {},
            onNumeroChange = {},
            onPisoChange = {},
            onCodigoPostalChange = {},
            onCiudadChange = {},
            onProvinciaChange = {},
            errors = mapOf(
                "calle" to "La calle es obligatoria",
                "numero" to "El número debe ser válido",
                "codigoPostal" to "El código postal debe tener 5 dígitos"
            )
        )
    }
}

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