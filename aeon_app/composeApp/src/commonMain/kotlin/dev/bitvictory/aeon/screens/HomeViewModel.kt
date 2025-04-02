package dev.bitvictory.aeon.screens

import dev.bitvictory.aeon.client.AuthClient
import dev.bitvictory.aeon.components.AbstractViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(authClient: AuthClient): AbstractViewModel(authClient) {
	private val _uiState = MutableStateFlow(HomeUIState(isAuthenticated = isAuthenticated()))
	val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()
}