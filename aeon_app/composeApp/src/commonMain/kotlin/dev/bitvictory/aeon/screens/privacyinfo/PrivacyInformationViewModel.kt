package dev.bitvictory.aeon.screens.privacyinfo

import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.service.IPrivacyService
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrivacyInformationViewModel(private val privacyService: IPrivacyService, userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(PrivacyInformationUIState())
	val uiState: StateFlow<PrivacyInformationUIState> = _uiState.asStateFlow()

	private val _snackbarEvent = MutableSharedFlow<String>()
	val snackbarEvent = _snackbarEvent.asSharedFlow()

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

	fun deletePrivacyInformation(groupKey: String, entryKey: String) {
		val oldPrivacyInformation = _uiState.value.privacyInformation!!
		val newPrivacyInformation = removePrivacyInformationEntry(oldPrivacyInformation, groupKey, entryKey)
		_uiState.value = _uiState.value.copy(
			privacyInformation = newPrivacyInformation
		)
		viewModelScope.launch {
			val deletionResult = privacyService.deletePrivacyInformation(groupKey, entryKey)
			if (deletionResult is AeonErrorResponse) {
				_uiState.value = _uiState.value.copy(
					privacyInformation = oldPrivacyInformation
				)
				_snackbarEvent.emit("Failed to delete privacy information entry")
			} else {

				_snackbarEvent.emit("Not failed to delete privacy information entry")
			}
		}
	}

	private fun removePrivacyInformationEntry(old: PrivacyInformationDTO, groupKey: String, entryKey: String): PrivacyInformationDTO {
		return PrivacyInformationDTO(old.groups.map {
			if (it.key == groupKey) {
				PrivacyInformationGroupDTO(it.key, it.name, it.entries.filter { entry -> entry.key != entryKey })
			} else it
		})
	}
}