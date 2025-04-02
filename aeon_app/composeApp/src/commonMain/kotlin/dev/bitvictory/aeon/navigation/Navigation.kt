package dev.bitvictory.aeon.navigation

import androidx.compose.foundation.layout.fillMaxSize
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

@Composable
fun MainNavigation() {
	val mainNavController = rememberNavController()
	val userService = koinInject<UserService>()

	val isLoggedIn by userService.isLoggedIn.collectAsState()

	NavHost(
		navController = mainNavController,
		startDestination = if (isLoggedIn) Main else Login,
		modifier = Modifier.fillMaxSize()
	) {
		mainDestination(navHost = mainNavController)
		loginDestination(onLoginSuccess = {
			mainNavController.navigateToMain()
		})
	}
}
