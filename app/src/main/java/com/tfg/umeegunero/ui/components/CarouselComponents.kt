package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CarouselItem(
    val icon: Any,
    val title: String,
    val description: String,
    val infoDetail: String = ""
)

@Composable
fun CarouselItemContent(
    item: CarouselItem,
    iconScale: Float = 1f,
    textAlpha: Float = 1f
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (item.icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(64.dp * iconScale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                is Painter -> {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(64.dp * iconScale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer(alpha = textAlpha)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer(alpha = textAlpha),
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (item.infoDetail.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.infoDetail,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer(alpha = textAlpha * 0.8f),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 