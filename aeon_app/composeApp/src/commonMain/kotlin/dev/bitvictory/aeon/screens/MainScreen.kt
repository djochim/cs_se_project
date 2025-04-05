package dev.bitvictory.aeon.screens

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
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bitvictory.aeon.components.NavigationSuiteItems
import dev.bitvictory.aeon.navigation.MenuItem
import dev.bitvictory.aeon.screens.login.navigateToLogin
import dev.bitvictory.aeon.screens.profile.Profile
import dev.bitvictory.aeon.screens.profile.profileDestination
import kotlinx.serialization.Serializable

@Serializable
object Main

fun NavGraphBuilder.mainDestination(navHost: NavHostController) {
	composable<Main> { MainScreen(navHost) }
}

fun NavHostController.navigateToMain() {
	navigate(Main) {
		launchSingleTop = true
		restoreState = true
	}
}

@Composable
fun MainScreen(
	rootNavHost: NavHostController,
) {
	val mainNavController = rememberNavController()
	val menuItems = listOf(
		MenuItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Home),
		MenuItem(
			"Recipes",
			Icons.AutoMirrored.Filled.List,
			Icons.AutoMirrored.Outlined.List, Recipes
		),
		MenuItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, Profile)
	)
	val currentDestination = mainNavController.currentBackStackEntryAsState().value?.destination

	NavigationSuiteScaffold(
		modifier = Modifier.fillMaxSize(),
		navigationSuiteItems = NavigationSuiteItems(currentDestination, mainNavController, menuItems)
	) { //innerPadding ->
		NavHost(
			navController = mainNavController,
			startDestination = Home,
		) {
			homeDestination()
			recipesDestination()
			profileDestination {
				rootNavHost.navigateToLogin()
			}
		}
	}
}