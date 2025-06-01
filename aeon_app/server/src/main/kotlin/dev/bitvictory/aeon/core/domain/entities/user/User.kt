package dev.bitvictory.aeon.core.domain.entities.user

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: String) {
	companion object {
		const val CURRENT_PLACEHOLDER = "[CURRENT]"
	}

}
