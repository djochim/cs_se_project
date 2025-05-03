package dev.bitvictory.aeon.screens.home

import dev.bitvictory.aeon.screens.RootUIState

data class HomeUIState(
	val query: String = "",
	override val isAuthenticated: Boolean = false
): RootUIState(isAuthenticated)
