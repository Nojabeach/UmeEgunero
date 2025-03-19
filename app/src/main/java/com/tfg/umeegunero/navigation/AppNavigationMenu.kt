package com.tfg.umeegunero.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.TipoUsuario

@Composable
fun Badge(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.size(16.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationMenu(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    userTipo: TipoUsuario,
    modifier: Modifier = Modifier
) {
    val navItems = remember(userTipo) {
        NavigationStructure.getNavItemsByTipo(userTipo)
    }
    
    var expandedItems by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = modifier) {
        navItems.forEach { item ->
            if (item.subItems.isNotEmpty()) {
                val isExpanded = expandedItems.contains(item.id)
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "rotation"
                )

                NavigationDrawerItem(
                    icon = {
                        Box {
                            Icon(item.icon, contentDescription = null)
                            if (item.badge != null) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(item.badge.toString())
                                }
                            }
                        }
                    },
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ExpandLess,
                                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        expandedItems = if (isExpanded) {
                            expandedItems - item.id
                        } else {
                            expandedItems + item.id
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (isExpanded) {
                    item.subItems.forEach { subItem ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                        ) {
                            NavigationDrawerItem(
                                icon = {
                                    Box {
                                        Icon(subItem.icon, contentDescription = null)
                                        if (subItem.badge != null) {
                                            Badge(
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            ) {
                                                Text(subItem.badge.toString())
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = subItem.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = currentRoute == subItem.route,
                                onClick = { onNavigate(subItem.route) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                NavigationDrawerItem(
                    icon = {
                        Box {
                            Icon(item.icon, contentDescription = null)
                            if (item.badge != null) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(item.badge.toString())
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (item.dividerAfter) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
} 