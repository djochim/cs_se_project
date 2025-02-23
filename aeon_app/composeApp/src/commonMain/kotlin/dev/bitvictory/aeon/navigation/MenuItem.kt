package dev.bitvictory.aeon.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destination: Any
) {
    fun icon(selected: Boolean): ImageVector = if (selected) {
        selectedIcon
    } else {
        unselectedIcon
    }
}
