package dev.bitvictory.aeon.screens.login

import dev.bitvictory.aeon.client.AuthClient
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.screens.HomeUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(authClient: AuthClient): AbstractViewModel(authClient) {
	private val _uiState = MutableStateFlow(HomeUIState(isAuthenticated = false))
	val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()
}