package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlin.random.Random

/**
 * Componente para generar contraseñas aleatorias con opciones configurables.
 * 
 * @param onPasswordGenerated Callback que se llamará cuando se genere una nueva contraseña
 * @param modifier Modificador opcional para el componente
 */
@Composable
fun PasswordGenerator(
    onPasswordGenerated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordLength by remember { mutableStateOf(12f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSpecialChars by remember { mutableStateOf(true) }
    var passwordValue by remember { mutableStateOf("") }
    
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(Unit) {
        passwordValue = generatePassword(
            length = passwordLength.toInt(),
            includeUppercase = includeUppercase,
            includeNumbers = includeNumbers,
            includeSpecialChars = includeSpecialChars
        )
        onPasswordGenerated(passwordValue)
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Generador de contraseñas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display de contraseña
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = passwordValue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    // Botón copiar
                    IconButton(
                        onClick = { 
                            clipboardManager.setText(AnnotatedString(passwordValue))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy, 
                            contentDescription = "Copiar contraseña",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Botón regenerar
                    IconButton(
                        onClick = { 
                            passwordValue = generatePassword(
                                length = passwordLength.toInt(),
                                includeUppercase = includeUppercase,
                                includeNumbers = includeNumbers,
                                includeSpecialChars = includeSpecialChars
                            )
                            onPasswordGenerated(passwordValue)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = "Regenerar contraseña",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Opciones de generación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Longitud: ${passwordLength.toInt()} caracteres",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = passwordLength,
                    onValueChange = { passwordLength = it },
                    valueRange = 8f..24f,
                    steps = 15,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }
            
            // Opciones de caracteres
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mayúsculas (A-Z)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Switch(
                            checked = includeUppercase,
                            onCheckedChange = { includeUppercase = it }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Números (0-9)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Switch(
                            checked = includeNumbers,
                            onCheckedChange = { includeNumbers = it }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Especiales (!@#$%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Switch(
                            checked = includeSpecialChars,
                            onCheckedChange = { includeSpecialChars = it }
                        )
                    }
                }
            }
            
            // Indicador de fortaleza
            Spacer(modifier = Modifier.height(16.dp))
            
            PasswordStrengthIndicator(
                password = passwordValue,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Botón de generación
            Button(
                onClick = {
                    passwordValue = generatePassword(
                        length = passwordLength.toInt(),
                        includeUppercase = includeUppercase,
                        includeNumbers = includeNumbers,
                        includeSpecialChars = includeSpecialChars
                    )
                    onPasswordGenerated(passwordValue)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar contraseña")
            }
        }
    }
}

/**
 * Genera una contraseña aleatoria con las opciones especificadas.
 * 
 * @param length Longitud de la contraseña a generar
 * @param includeUppercase Si se deben incluir letras mayúsculas
 * @param includeNumbers Si se deben incluir números
 * @param includeSpecialChars Si se deben incluir caracteres especiales
 * @return Una contraseña aleatoria generada
 */
private fun generatePassword(
    length: Int,
    includeUppercase: Boolean,
    includeNumbers: Boolean,
    includeSpecialChars: Boolean
): String {
    val lowercase = "abcdefghijklmnopqrstuvwxyz"
    val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val numbers = "0123456789"
    val special = "!@#$%^&*()_+-=[]{}|;:,.<>?/"
    
    var chars = lowercase
    if (includeUppercase) chars += uppercase
    if (includeNumbers) chars += numbers
    if (includeSpecialChars) chars += special
    
    val random = Random.Default
    
    // Nos aseguramos de que la contraseña incluye al menos un caracter de cada tipo seleccionado
    val password = StringBuilder()
    
    // Siempre incluimos una minúscula
    password.append(lowercase[random.nextInt(lowercase.length)])
    
    // Si se seleccionó incluir mayúsculas, añadimos una
    if (includeUppercase) {
        password.append(uppercase[random.nextInt(uppercase.length)])
    }
    
    // Si se seleccionó incluir números, añadimos uno
    if (includeNumbers) {
        password.append(numbers[random.nextInt(numbers.length)])
    }
    
    // Si se seleccionó incluir caracteres especiales, añadimos uno
    if (includeSpecialChars) {
        password.append(special[random.nextInt(special.length)])
    }
    
    // Completamos el resto de la contraseña con caracteres aleatorios
    while (password.length < length) {
        password.append(chars[random.nextInt(chars.length)])
    }
    
    // Mezclamos los caracteres para evitar un patrón predecible
    return password.toString().toList().shuffled().joinToString("")
}

/**
 * Indicador visual de la fortaleza de la contraseña
 */
@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculatePasswordStrength(password)
    val (color, label) = when {
        strength > 80 -> Pair(MaterialTheme.colorScheme.primary, "Excelente")
        strength > 60 -> Pair(MaterialTheme.colorScheme.tertiary, "Fuerte")
        strength > 40 -> Pair(MaterialTheme.colorScheme.secondary, "Media")
        strength > 20 -> Pair(MaterialTheme.colorScheme.error, "Débil")
        else -> Pair(MaterialTheme.colorScheme.error, "Muy débil")
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Fortaleza:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { strength / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Calcula la fortaleza de la contraseña en una escala de 0 a 100
 */
fun calculatePasswordStrength(password: String): Float {
    if (password.isEmpty()) return 0f
    
    var strength = 0f
    
    // Longitud (hasta 40 puntos)
    strength += minOf(password.length * 3, 40).toFloat()
    
    // Variedad de caracteres (hasta 60 puntos)
    val hasLowercase = password.any { it.isLowerCase() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    if (hasLowercase) strength += 15
    if (hasUppercase) strength += 15
    if (hasDigit) strength += 15
    if (hasSpecial) strength += 15
    
    return strength
}

@Preview(showBackground = true)
@Composable
fun PasswordGeneratorPreview() {
    UmeEguneroTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PasswordGenerator(
                onPasswordGenerated = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 