package dev.bitvictory.aeon.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.bitvictory.aeon.screens.Main
import dev.bitvictory.aeon.screens.login.Login
import dev.bitvictory.aeon.screens.login.loginDestination
import dev.bitvictory.aeon.screens.mainDestination
import dev.bitvictory.aeon.screens.navigateToMain
import dev.bitvictory.aeon.service.UserService
import org.koin.compose.koinInject

/**
 * Composable function that defines the main navigation structure of the application.
 *
 * This function sets up a `Scaffold` and a `NavHost` to manage navigation between
 * different screens (destinations). It observes the user's authentication state
 * and directs the user to the appropriate starting screen (either `Main` or `Login`).
 *
 * Key functionalities:
 * - **Navigation Controller:** Uses `rememberNavController()` to create and remember the
 *   `NavController` responsible for navigating between composables.
 * - **User Service Injection:** Injects an instance of `UserService` using Koin's `koinInject()`
 *   to access user authentication status.
 * - **User State Observation:** Collects the `userState` from the `UserService` as a Compose `State`
 *   to reactively update the UI based on authentication changes.
 * - **Conditional Start Destination:** Sets the `startDestination` of the `NavHost` to:
 *     - `Main`: If the user is authenticated (`userState.isAuthenticated()` is true).
 *     - `Login`: If the user is not authenticated.
 * - **Navigation Graph Definition:** Defines the navigation graph within the `NavHost` using:
 *     - `mainDestination()`:  Represents the main part of the application, accessible after login.
 *                          It takes the `mainNavController` to enable further navigation within
 *                          this destination.
 *     - `loginDestination()`: Represents the login screen. It includes a callback `onLoginSuccess`
 *                           which is triggered upon successful login, navigating the user to the
 *                           `Main` destination.
 * - **Scaffold Integration:** Wraps the `NavHost` in a `Scaffold` to provide a basic Material
 *   Design visual structure. The `padding` from the `Scaffold` is applied to the `NavHost`.
 */
@Composable
fun MainNavigation() {
	val mainNavController = rememberNavController()
	val userService = koinInject<UserService>()

	val userState by userService.userState.collectAsState()

	Scaffold { padding ->
		NavHost(
			navController = mainNavController,
			startDestination = if (userState.isAuthenticated()) Main else Login,
			modifier = Modifier.fillMaxSize().padding(padding)
		) {
			mainDestination(navHost = mainNavController)
			loginDestination(onLoginSuccess = {
				mainNavController.navigateToMain()
			})
		}
	}
}
