package dev.bitvictory.aeon.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.launch

abstract class AbstractViewModel(private val userService: IUserService): ViewModel() {

	fun isAuthenticated() = userService.isAuthenticated()

	fun logout() {
		viewModelScope.launch {
			userService.logout()
		}
	}

}