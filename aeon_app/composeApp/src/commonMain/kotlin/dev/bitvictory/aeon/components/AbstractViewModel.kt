package dev.bitvictory.aeon.components

import androidx.lifecycle.ViewModel
import dev.bitvictory.aeon.client.AuthClient

abstract class AbstractViewModel(private val authClient: AuthClient): ViewModel() {

	fun isAuthenticated() = authClient.isLoggedIn()

}