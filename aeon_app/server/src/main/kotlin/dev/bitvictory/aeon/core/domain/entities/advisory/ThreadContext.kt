package dev.bitvictory.aeon.core.domain.entities.advisory

import dev.bitvictory.aeon.core.domain.entities.user.User

data class ThreadContext(
	val threadId: String,
	val user: User
)
