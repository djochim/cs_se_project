package dev.bitvictory.aeon.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bitvictory.aeon.components.NavigationSuiteItems
import dev.bitvictory.aeon.screens.Home
import dev.bitvictory.aeon.screens.Profile
import dev.bitvictory.aeon.screens.Recipes
import dev.bitvictory.aeon.screens.homeDestination
import dev.bitvictory.aeon.screens.profileDestination
import dev.bitvictory.aeon.screens.recipesDestination

@Composable
fun Navigation() {
	val navHost = rememberNavController()
	val navBackStackEntry by navHost.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination
	val menuItems = listOf(
		MenuItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Home),
		MenuItem(
			"Recipes",
			Icons.AutoMirrored.Filled.List,
			Icons.AutoMirrored.Outlined.List, Recipes
		),
		MenuItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, Profile)
	)
	NavigationSuiteScaffold(
		modifier = Modifier.fillMaxSize(),
		navigationSuiteItems = NavigationSuiteItems(currentDestination, navHost, menuItems)
	) {
		NavHost(
			navController = navHost,
			startDestination = Home
		) {
			homeDestination()
			recipesDestination()
			profileDestination()
		}
	}
}