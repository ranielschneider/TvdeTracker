package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

enum class HomeNavigationDestination {
    HOME,
    ROUTES,
    STATISTICS,
    SETTINGS
}

@Composable
fun HomeBottomNavigation(
    selectedDestination: HomeNavigationDestination = HomeNavigationDestination.HOME,
    onHomeClick: () -> Unit = {},
    onRoutesClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedDestination == HomeNavigationDestination.HOME,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Início"
                )
            },
            label = {
                Text("Início")
            },
            colors = homeNavigationItemColors()
        )

        NavigationBarItem(
            selected = selectedDestination == HomeNavigationDestination.ROUTES,
            onClick = onRoutesClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Rotas"
                )
            },
            label = {
                Text("Rotas")
            },
            colors = homeNavigationItemColors()
        )

        NavigationBarItem(
            selected = selectedDestination == HomeNavigationDestination.STATISTICS,
            onClick = onStatisticsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Estatísticas"
                )
            },
            label = {
                Text("Estatísticas")
            },
            colors = homeNavigationItemColors()
        )

        NavigationBarItem(
            selected = selectedDestination == HomeNavigationDestination.SETTINGS,
            onClick = onSettingsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações"
                )
            },
            label = {
                Text("Configurações")
            },
            colors = homeNavigationItemColors()
        )
    }
}

@Composable
private fun homeNavigationItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = VerdeTracking,
        selectedTextColor = VerdeTracking,
        indicatorColor = VerdeTracking.copy(alpha = 0.10f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
    )