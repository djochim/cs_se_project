package dev.bitvictory.aeon.screens

data class HomeUIState(
	val chatTerms: String = "",
	override val isAuthenticated: Boolean = false
): RootUIState(isAuthenticated)
