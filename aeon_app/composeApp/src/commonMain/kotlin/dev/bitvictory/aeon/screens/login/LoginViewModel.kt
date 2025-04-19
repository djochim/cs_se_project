package dev.bitvictory.aeon.screens.login

import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(LoginUIState())
	val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

	fun changeEmail(email: String) {
		_uiState.value = _uiState.value.copy(email = email)
	}

	fun changePassword(password: String) {
		_uiState.value = _uiState.value.copy(password = password)
	}

	/**
	 * Launches a coroutine to login the user. During that time the UI state is updated with loading and afterwards back.
	 */
	fun login() {
		_uiState.value = _uiState.value.copy(isLoading = true, error = "", emailError = "", passwordError = "", success = false)
		viewModelScope.launch {
			// sleep 5 seconds to simulate a network request
			when (val loginResponse = userService.login(LoginDTO(_uiState.value.email, _uiState.value.password))) {
				is AeonSuccessResponse -> _uiState.value = LoginUIState(isLoading = false, success = true)
				is AeonErrorResponse   -> handleError(loginResponse.error)
			}
		}

	}

	private fun handleError(error: AeonError) {
		if (error.details.isEmpty() && !error.details.containsKey("email") && !error.details.containsKey("password")) {
			_uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
			return
		}
		_uiState.value = _uiState.value.copy(isLoading = false, emailError = error.details["email"] ?: "", passwordError = error.details["password"] ?: "")
	}

}