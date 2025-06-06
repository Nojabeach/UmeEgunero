package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

/**
 * Componente para exportar datos de usuarios en diferentes formatos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataComponent(
    usuarios: List<Usuario>,
    onExportCSV: (List<Usuario>) -> Unit,
    onExportPDF: (List<Usuario>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExportOptions by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf(usuarios) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exportar datos (${usuarios.size} usuarios)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row {
                // Botón para exportar a CSV
                OutlinedButton(
                    onClick = { onExportCSV(usuarios) },
                    enabled = usuarios.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón para exportar a PDF
                OutlinedButton(
                    onClick = { onExportPDF(usuarios) },
                    enabled = usuarios.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }
            }
        }
    }
}

/**
 * Botón de formato de exportación
 */
@Composable
fun ExportFormatButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

/**
 * Clase utilitaria para exportar datos a CSV
 */
object ExportUtils {
    /**
     * Genera el contenido CSV para una lista de usuarios
     */
    fun generateCSVContent(usuarios: List<Usuario>): String {
        val header = "DNI,Nombre,Apellidos,Email,Teléfono,Fecha Registro,Activo\n"
        val rows = usuarios.joinToString("\n") { usuario ->
            "${usuario.dni}," +
            "${usuario.nombre}," +
            "${usuario.apellidos}," +
            "${usuario.email}," +
            "${usuario.telefono}," +
            "${usuario.fechaRegistro?.seconds ?: ""}," +
            "${usuario.activo}"
        }
        return header + rows
    }
    
    /**
     * Genera el contenido para un PDF con los datos de los usuarios
     * Nota: Esta es una implementación simplificada. En un caso real,
     * se utilizaría una biblioteca como iText para generar PDFs.
     */
    fun generatePDFContent(usuarios: List<Usuario>): String {
        // Aquí iría la lógica para generar un PDF
        // En una implementación real, se construiría un documento PDF
        return "PDF content with ${usuarios.size} users"
    }
}

@Preview(showBackground = true)
@Composable
fun ExportDataComponentPreview() {
    UmeEguneroTheme {
        val mockUsuarios = List(5) { index ->
            Usuario(
                dni = "1234567${index}A",
                nombre = "Usuario $index",
                apellidos = "Apellido",
                email = "usuario$index@example.com",
                telefono = "60000000$index",
                activo = index % 2 == 0
            )
        }
        
        ExportDataComponent(
            usuarios = mockUsuarios,
            onExportCSV = { },
            onExportPDF = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 