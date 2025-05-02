package dev.bitvictory.aeon.screens.home

import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(HomeUIState(isAuthenticated = isAuthenticated()))
	val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

	fun changeQuery(newQuery: String) {
		_uiState.value = _uiState.value.copy(query = newQuery)
	}

}