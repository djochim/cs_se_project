package dev.bitvictory.aeon.components

import androidx.lifecycle.ViewModel
import dev.bitvictory.aeon.service.UserService

abstract class AbstractViewModel(private val userService: UserService): ViewModel() {

	fun isAuthenticated() = userService.isAuthenticated()

}