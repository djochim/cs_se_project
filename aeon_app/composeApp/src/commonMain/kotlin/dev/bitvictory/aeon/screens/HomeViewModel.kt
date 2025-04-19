package dev.bitvictory.aeon.screens

import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(userService: UserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(HomeUIState(isAuthenticated = isAuthenticated()))
	val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()
}