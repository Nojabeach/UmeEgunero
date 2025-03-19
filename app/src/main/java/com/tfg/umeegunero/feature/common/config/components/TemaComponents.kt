package com.tfg.umeegunero.feature.common.config.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.theme.getNombreTema

/**
 * Componente reutilizable para seleccionar el tema de la aplicación
 * que se utilizará en las pantallas de configuración de todos los tipos de usuarios
 */
@Composable
fun TemaSelector(
    temaSeleccionado: TemaPref,
    onTemaSeleccionado: (TemaPref) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tema de la aplicación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Selecciona el tema visual de la aplicación:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Opción tema claro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemaSeleccionado(TemaPref.LIGHT) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = temaSeleccionado == TemaPref.LIGHT,
                    onClick = { onTemaSeleccionado(TemaPref.LIGHT) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Tema claro",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Modo claro",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Divider()
            
            // Opción tema oscuro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemaSeleccionado(TemaPref.DARK) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = temaSeleccionado == TemaPref.DARK,
                    onClick = { onTemaSeleccionado(TemaPref.DARK) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Tema oscuro",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Modo oscuro",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Divider()
            
            // Opción tema sistema
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemaSeleccionado(TemaPref.SYSTEM) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = temaSeleccionado == TemaPref.SYSTEM,
                    onClick = { onTemaSeleccionado(TemaPref.SYSTEM) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.SettingsSuggest,
                    contentDescription = "Tema sistema",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Usar configuración del sistema",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Componente para mostrar el tema actualmente seleccionado
 */
@Composable
fun TemaActual(
    temaSeleccionado: TemaPref,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tema actual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val temaIcono = when (temaSeleccionado) {
                    TemaPref.LIGHT -> Icons.Default.LightMode
                    TemaPref.DARK -> Icons.Default.DarkMode
                    TemaPref.SYSTEM -> Icons.Default.SettingsSuggest
                }
                
                Icon(
                    imageVector = temaIcono,
                    contentDescription = "Tema actual",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = getNombreTema(temaSeleccionado),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 