package dev.bitvictory.aeon.screens.privacyinfo

import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.service.IPrivacyService
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrivacyInformationViewModel(private val privacyService: IPrivacyService, userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(PrivacyInformationUIState())
	val uiState: StateFlow<PrivacyInformationUIState> = _uiState.asStateFlow()

	init {
		loadPrivacyInformation()
	}

	private fun loadPrivacyInformation() {
		viewModelScope.launch {
			when (val privacyInformation = privacyService.getPrivacyInformation()) {
				is AeonSuccessResponse -> {
					_uiState.value = _uiState.value.copy(
						privacyInformation = privacyInformation.data
					)
				}

				is AeonErrorResponse   -> {
					_uiState.value = _uiState.value.copy(
						error = privacyInformation.error
					)
				}
			}
		}
	}
}