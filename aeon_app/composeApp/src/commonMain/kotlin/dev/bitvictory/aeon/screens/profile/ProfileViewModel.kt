package dev.bitvictory.aeon.screens.profile

import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.service.UserService

class ProfileViewModel(private val userService: UserService): AbstractViewModel(userService) {

	val userState
		get() = userService.userState

}