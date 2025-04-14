package dev.bitvictory.aeon.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.service.UserService
import kotlinx.coroutines.launch

abstract class AbstractViewModel(private val userService: UserService): ViewModel() {

	fun isAuthenticated() = userService.isAuthenticated()

	fun logout() {
		viewModelScope.launch {
			userService.logout()
		}
	}

}