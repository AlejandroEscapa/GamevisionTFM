package es.androidtfm.gamevision.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppScaffold(
    navController: NavController,
    userViewModel: UserViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val isGuest by userViewModel.isGuest.collectAsState()

    val items = if (isGuest) {
        listOf(
            BottomNavItem("news", Icons.Default.Home, "Home"),
            BottomNavItem("gamesearch", Icons.Filled.Search, "Search")
        )
    } else {
        listOf(
            BottomNavItem("gamesearch", Icons.Filled.Search, "Search"),
            BottomNavItem("gamelist", Icons.AutoMirrored.Filled.List, "Game List"),
            BottomNavItem("news", Icons.Default.Home, "Home"),
            BottomNavItem("profile", Icons.Default.Person, "Profile"),
            BottomNavItem("social", Icons.Default.Face, "Social")
        )
    }

    Scaffold(
        bottomBar = { ModernStyledBottomNavigation(navController, items) }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun ModernStyledBottomNavigation(navController: NavController, items: List<BottomNavItem>) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
    ) {
        items.forEach { item ->
            val isSelected = navController.currentDestination?.route == item.route

            BottomNavigationItem(
                selected = isSelected,
                onClick = { navController.navigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                alwaysShowLabel = false // Esto asegura que no se muestren etiquetas
            )
        }
    }
}

@Composable
fun GuestBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("news", Icons.Default.Home, "Home"),
        BottomNavItem("gamesearch", Icons.Filled.Search, "Search")
    )

    ModernStyledBottomNavigation(navController, items)
}