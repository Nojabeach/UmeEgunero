package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

/**
 * Componente que muestra los botones de acceso para los diferentes tipos de usuarios.
 * 
 * @param onCentroLogin Función lambda que se ejecuta al pulsar el botón de acceso de centro
 * @param onProfesorLogin Función lambda que se ejecuta al pulsar el botón de acceso de profesor
 * @param onFamiliarLogin Función lambda que se ejecuta al pulsar el botón de acceso familiar
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun LoginButtons(
    onCentroLogin: () -> Unit,
    onProfesorLogin: () -> Unit,
    onFamiliarLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón de acceso centro con diseño moderno y color institucional
        Button(
            onClick = onCentroLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CentroColor,
                contentColor = Color.White,
                disabledContainerColor = CentroColor.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            ),
            interactionSource = remember { MutableInteractionSource() },
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Centro",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Botón de acceso profesor con color verde educativo
        Button(
            onClick = onProfesorLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfesorColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Profesor",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Botón de acceso familiar con color morado familiar
        Button(
            onClick = onFamiliarLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FamiliarColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Familiar",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Vista previa del componente LoginButtons para comprobar el diseño en el editor.
 */
@Preview(showBackground = true)
@Composable
private fun LoginButtonsPreview() {
    UmeEguneroTheme {
        Surface {
            LoginButtons(
                onCentroLogin = {},
                onProfesorLogin = {},
                onFamiliarLogin = {}
            )
        }
    }
} 