package com.tfg.umeegunero.feature.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
 * Componente generador de contraseñas seguras
 * Permite personalizar la longitud y tipos de caracteres a incluir
 */
@Composable
fun PasswordGenerator(
    onPasswordGenerated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var passwordLength by remember { mutableStateOf(12) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSpecialChars by remember { mutableStateOf(true) }
    
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(Unit) {
        password = generatePassword(
            length = passwordLength,
            includeUppercase = includeUppercase,
            includeLowercase = includeLowercase,
            includeNumbers = includeNumbers,
            includeSpecialChars = includeSpecialChars
        )
        onPasswordGenerated(password)
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
                    text = password,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    // Botón copiar
                    IconButton(
                        onClick = { 
                            clipboardManager.setText(AnnotatedString(password))
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
                            password = generatePassword(
                                length = passwordLength,
                                includeUppercase = includeUppercase,
                                includeLowercase = includeLowercase,
                                includeNumbers = includeNumbers,
                                includeSpecialChars = includeSpecialChars
                            )
                            onPasswordGenerated(password)
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
            
            // Opciones de configuración
            Text(
                text = "Longitud: $passwordLength",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Slider(
                value = passwordLength.toFloat(),
                onValueChange = { 
                    passwordLength = it.toInt()
                    password = generatePassword(
                        length = passwordLength,
                        includeUppercase = includeUppercase,
                        includeLowercase = includeLowercase,
                        includeNumbers = includeNumbers,
                        includeSpecialChars = includeSpecialChars
                    )
                    onPasswordGenerated(password)
                },
                valueRange = 8f..24f,
                steps = 15,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Checkboxes para tipos de caracteres
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeLowercase,
                            onCheckedChange = {
                                includeLowercase = it
                                if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSpecialChars) {
                                    includeLowercase = true
                                }
                                password = generatePassword(
                                    length = passwordLength,
                                    includeUppercase = includeUppercase,
                                    includeLowercase = includeLowercase,
                                    includeNumbers = includeNumbers,
                                    includeSpecialChars = includeSpecialChars
                                )
                                onPasswordGenerated(password)
                            }
                        )
                        Text("a-z", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeUppercase,
                            onCheckedChange = {
                                includeUppercase = it
                                if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSpecialChars) {
                                    includeUppercase = true
                                }
                                password = generatePassword(
                                    length = passwordLength,
                                    includeUppercase = includeUppercase,
                                    includeLowercase = includeLowercase,
                                    includeNumbers = includeNumbers,
                                    includeSpecialChars = includeSpecialChars
                                )
                                onPasswordGenerated(password)
                            }
                        )
                        Text("A-Z", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeNumbers,
                            onCheckedChange = {
                                includeNumbers = it
                                if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSpecialChars) {
                                    includeNumbers = true
                                }
                                password = generatePassword(
                                    length = passwordLength,
                                    includeUppercase = includeUppercase,
                                    includeLowercase = includeLowercase,
                                    includeNumbers = includeNumbers,
                                    includeSpecialChars = includeSpecialChars
                                )
                                onPasswordGenerated(password)
                            }
                        )
                        Text("0-9", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeSpecialChars,
                            onCheckedChange = {
                                includeSpecialChars = it
                                if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSpecialChars) {
                                    includeSpecialChars = true
                                }
                                password = generatePassword(
                                    length = passwordLength,
                                    includeUppercase = includeUppercase,
                                    includeLowercase = includeLowercase,
                                    includeNumbers = includeNumbers,
                                    includeSpecialChars = includeSpecialChars
                                )
                                onPasswordGenerated(password)
                            }
                        )
                        Text("!@#$", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicador de seguridad
            Text(
                text = "Seguridad: ${evaluatePasswordStrength(password)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getPasswordStrengthColor(password)
            )
        }
    }
}

/**
 * Genera una contraseña aleatoria con los parámetros especificados
 */
fun generatePassword(
    length: Int = 12,
    includeUppercase: Boolean = true,
    includeLowercase: Boolean = true,
    includeNumbers: Boolean = true,
    includeSpecialChars: Boolean = true
): String {
    val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
    val numberChars = "0123456789"
    val specialChars = "!@#$%^&*()_-+=<>?/"
    
    var validChars = ""
    
    if (includeLowercase) validChars += lowercaseChars
    if (includeUppercase) validChars += uppercaseChars
    if (includeNumbers) validChars += numberChars
    if (includeSpecialChars) validChars += specialChars
    
    // Si no se seleccionó ninguna opción, usar por defecto minúsculas
    if (validChars.isEmpty()) validChars = lowercaseChars
    
    val password = StringBuilder(length)
    val random = Random.Default
    
    // Asegurar que al menos hay un carácter de cada tipo seleccionado
    if (includeUppercase && length > 0) {
        password.append(uppercaseChars[random.nextInt(uppercaseChars.length)])
    }
    
    if (includeLowercase && length > password.length) {
        password.append(lowercaseChars[random.nextInt(lowercaseChars.length)])
    }
    
    if (includeNumbers && length > password.length) {
        password.append(numberChars[random.nextInt(numberChars.length)])
    }
    
    if (includeSpecialChars && length > password.length) {
        password.append(specialChars[random.nextInt(specialChars.length)])
    }
    
    // Rellenar el resto de la contraseña con caracteres aleatorios
    while (password.length < length) {
        password.append(validChars[random.nextInt(validChars.length)])
    }
    
    // Mezclar todos los caracteres para que no siempre sigan el mismo patrón
    return password.toString().toCharArray().apply { shuffle() }.joinToString("")
}

/**
 * Evalúa la fortaleza de una contraseña
 */
fun evaluatePasswordStrength(password: String): String {
    val length = password.length
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    var score = 0
    
    // Evaluar longitud
    score += when {
        length >= 16 -> 4
        length >= 12 -> 3
        length >= 8 -> 2
        else -> 1
    }
    
    // Evaluar complejidad
    if (hasUppercase) score += 1
    if (hasLowercase) score += 1
    if (hasDigit) score += 1
    if (hasSpecial) score += 1
    
    return when {
        score >= 8 -> "Muy fuerte"
        score >= 6 -> "Fuerte"
        score >= 4 -> "Media"
        else -> "Débil"
    }
}

/**
 * Devuelve un color según la fortaleza de la contraseña
 */
@Composable
fun getPasswordStrengthColor(password: String): androidx.compose.ui.graphics.Color {
    return when (evaluatePasswordStrength(password)) {
        "Muy fuerte" -> MaterialTheme.colorScheme.primary
        "Fuerte" -> MaterialTheme.colorScheme.secondary
        "Media" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordGeneratorPreview() {
    UmeEguneroTheme {
        PasswordGenerator(
            onPasswordGenerated = {},
            modifier = Modifier.padding(16.dp)
        )
    }
} 