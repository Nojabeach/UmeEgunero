package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.feature.admin.viewmodel.ReporteRendimientoViewModel
import com.tfg.umeegunero.ui.components.charts.LineChart
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.Assignment

/**
 * Pantalla de Reporte de Rendimiento Académico.
 * 
 * Muestra estadísticas y métricas de rendimiento de los centros educativos, clases y alumnos,
 * incluyendo calificaciones medias, tasas de aprobados, progreso educativo y áreas de mejora.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteRendimientoScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    var periodoSeleccionado by remember { mutableStateOf("Último trimestre") }
    var centroSeleccionado by remember { mutableStateOf("Todos los centros") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métricas de Rendimiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección de filtros
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Periodo:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(90.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            TextField(
                                value = periodoSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Centro:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(90.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            TextField(
                                value = centroSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aplicar filtros")
                    }
                }
            }
            
            // Sección de resumen general
            Text(
                text = "Resumen de Rendimiento Académico",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IndicadorCard(
                    titulo = "Nota media",
                    valor = "7,6",
                    comparacion = "+0,3",
                    positivo = true,
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                IndicadorCard(
                    titulo = "Tasa aprobados",
                    valor = "84%",
                    comparacion = "+2%",
                    positivo = true,
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IndicadorCard(
                    titulo = "Asistencia",
                    valor = "92%",
                    comparacion = "-1%",
                    positivo = false,
                    icon = Icons.Default.PeopleAlt,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                
                IndicadorCard(
                    titulo = "Tareas completadas",
                    valor = "78%",
                    comparacion = "+5%",
                    positivo = true,
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Sección de rendimiento por áreas
            Text(
                text = "Rendimiento por Áreas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    AreaRendimientoItem(
                        area = "Matemáticas",
                        notaMedia = 7.2f,
                        porcentajeAprobados = 82f,
                        tendencia = "+0.3"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    AreaRendimientoItem(
                        area = "Lenguaje",
                        notaMedia = 7.8f,
                        porcentajeAprobados = 88f,
                        tendencia = "+0.5"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    AreaRendimientoItem(
                        area = "Ciencias",
                        notaMedia = 7.5f,
                        porcentajeAprobados = 85f,
                        tendencia = "+0.1"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    AreaRendimientoItem(
                        area = "Idiomas",
                        notaMedia = 8.1f,
                        porcentajeAprobados = 90f,
                        tendencia = "+0.7"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    AreaRendimientoItem(
                        area = "Arte y Música",
                        notaMedia = 8.3f,
                        porcentajeAprobados = 93f,
                        tendencia = "+0.2"
                    )
                }
            }
            
            // Sección de mejores y peores resultados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mejores resultados
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Mejores Resultados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            RankingItem(
                                posicion = 1,
                                nombre = "6º A - CEIP San José",
                                valor = "9,1"
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            RankingItem(
                                posicion = 2,
                                nombre = "5º B - Colegio Santa Ana",
                                valor = "8,8"
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            RankingItem(
                                posicion = 3,
                                nombre = "4º A - CEIP Cervantes",
                                valor = "8,7"
                            )
                        }
                    }
                }
                
                // Peores resultados
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Áreas de Mejora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            RankingItem(
                                posicion = 1,
                                nombre = "2º C - IES Alameda",
                                valor = "5,8",
                                esMejora = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            RankingItem(
                                posicion = 2,
                                nombre = "1º B - CEIP García Lorca",
                                valor = "6,1",
                                esMejora = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            RankingItem(
                                posicion = 3,
                                nombre = "3º A - IES Velázquez",
                                valor = "6,3",
                                esMejora = true
                            )
                        }
                    }
                }
            }
            
            // Sección de tendencias temporales
            Text(
                text = "Tendencias Temporales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Evolución trimestral de notas medias",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Aquí iría el componente de gráfico de líneas
                    // Para la demostración, usamos un placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Gráfico de evolución trimestral\n(Simulación de componente)",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LeyendaItem(
                            color = MaterialTheme.colorScheme.primary,
                            texto = "1er Trimestre"
                        )
                        
                        LeyendaItem(
                            color = MaterialTheme.colorScheme.secondary,
                            texto = "2do Trimestre"
                        )
                        
                        LeyendaItem(
                            color = MaterialTheme.colorScheme.tertiary,
                            texto = "3er Trimestre"
                        )
                    }
                }
            }
            
            // Sección de recomendaciones
            Text(
                text = "Recomendaciones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    RecomendacionItem(
                        titulo = "Refuerzo en Matemáticas",
                        descripcion = "Las aulas con rendimiento bajo en matemáticas se beneficiarían de sesiones adicionales de refuerzo y material complementario.",
                        icono = Icons.Default.Calculate
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    RecomendacionItem(
                        titulo = "Programa de asistencia",
                        descripcion = "Implementar un sistema de seguimiento para mejorar la asistencia en los centros donde ha disminuido.",
                        icono = Icons.Default.EventAvailable
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    RecomendacionItem(
                        titulo = "Metodologías actualizadas",
                        descripcion = "Incorporar nuevas metodologías didácticas en las áreas con menor progreso respecto al trimestre anterior.",
                        icono = Icons.Default.Lightbulb
                    )
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar informe")
                }
                
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir datos")
                }
            }
            
            // Espacio adicional al final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Tarjeta para mostrar un indicador clave de rendimiento.
 * 
 * @param titulo Título del indicador
 * @param valor Valor principal del indicador
 * @param comparacion Cambio respecto al período anterior
 * @param positivo Indica si el cambio es positivo (true) o negativo (false)
 * @param icon Icono que representa el indicador
 * @param color Color principal para el indicador
 * @param modifier Modificador para personalizar el diseño
 */
@Composable
fun IndicadorCard(
    titulo: String,
    valor: String,
    comparacion: String,
    positivo: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (positivo) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (positivo) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = comparacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (positivo) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * Elemento que muestra el rendimiento de un área académica.
 * 
 * @param area Nombre del área académica
 * @param notaMedia Nota media en el área
 * @param porcentajeAprobados Porcentaje de aprobados en el área
 * @param tendencia Tendencia respecto al período anterior
 */
@Composable
fun AreaRendimientoItem(
    area: String,
    notaMedia: Float,
    porcentajeAprobados: Float,
    tendencia: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = area,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Media: ${String.format("%.1f", notaMedia)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Aprobados: ${String.format("%.0f", porcentajeAprobados)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = tendencia,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

/**
 * Elemento que muestra un ítem en un ranking.
 * 
 * @param posicion Posición en el ranking
 * @param nombre Nombre del elemento
 * @param valor Valor asociado
 * @param esMejora Indica si es un ítem de mejora (true) o de éxito (false)
 */
@Composable
fun RankingItem(
    posicion: Int,
    nombre: String,
    valor: String,
    esMejora: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (!esMejora) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        ) {
            Text(
                text = posicion.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (!esMejora) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Elemento para la leyenda de un gráfico.
 * 
 * @param color Color asociado al elemento de la leyenda
 * @param texto Texto descriptivo
 */
@Composable
fun LeyendaItem(
    color: Color,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Elemento que muestra una recomendación basada en el análisis de datos.
 * 
 * @param titulo Título de la recomendación
 * @param descripcion Descripción detallada
 * @param icono Icono representativo
 */
@Composable
fun RecomendacionItem(
    titulo: String,
    descripcion: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Vista previa de la pantalla de reporte de rendimiento.
 */
@Preview(showBackground = true)
@Composable
fun ReporteRendimientoScreenPreview() {
    MaterialTheme {
        ReporteRendimientoScreen(
            navController = rememberNavController()
        )
    }
} 