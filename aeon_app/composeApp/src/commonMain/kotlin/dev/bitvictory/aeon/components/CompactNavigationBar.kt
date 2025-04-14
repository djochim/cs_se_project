package dev.bitvictory.aeon.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import dev.bitvictory.aeon.navigation.MenuItem

@Composable
fun NavigationSuiteItems(
	currentDestination: NavDestination?,
	navHost: NavHostController,
	items: List<MenuItem>
): NavigationSuiteScope.() -> Unit = {
	items.forEach {
		val isCurrent =
			currentDestination?.hierarchy?.any { current -> current.route == it.destination } == true
		item(
			icon = {
				Icon(
					it.icon(isCurrent),
					contentDescription = it.name
				)
			},
			label = { Text(it.name) },
			selected = isCurrent,
			onClick = { navigateWithBackStackHandling(it.destination, navHost) }
		)
	}
}

fun navigateWithBackStackHandling(route: Any, navHost: NavHostController) {
	navHost.navigate(route) {
		popUpTo(
			navHost.graph.findStartDestination()
		) {
			saveState = true
		}
		launchSingleTop = true
		restoreState = true
	}
}
