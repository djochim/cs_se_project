package dev.bitvictory.aeon.screens.home

import androidx.compose.ui.graphics.ImageBitmap
import dev.bitvictory.aeon.screens.RootUIState

data class HomeUIState(
	val query: String = "",
	val images: List<ByteArray> = emptyList(),
	override val isAuthenticated: Boolean = false
): RootUIState(isAuthenticated)
