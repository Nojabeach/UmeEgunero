package com.tfg.umeegunero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun NavigationDrawerContent(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    navItems: List<NavigationStructure.NavItem> = emptyList(),
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "UmeEgunero",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        navItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = false,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                    onCloseDrawer()
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            if (item.dividerAfter) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Divider()
        
        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = {
                onLogout()
                onCloseDrawer()
            },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión") },
            modifier = Modifier.padding(vertical = 8.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = Color.Red,
                unselectedTextColor = Color.Red
            )
        )
    }
} 