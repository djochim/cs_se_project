package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.AuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserService(private val authClient: AuthClient) {

	private val _isLoggedIn = MutableStateFlow(false)
	val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

	fun login() {
		_isLoggedIn.value = true
	}

	fun logout() {
		_isLoggedIn.value = false
	}

}