package dev.bitvictory.aeon.infrastructure.network.dto

import com.aallam.openai.api.core.Role
import dev.bitvictory.aeon.core.domain.entities.assistant.message.Author

data class AssistantMessageDTO(
	val role: Author,
	val content: String
) {

	fun externalRole() = when (role) {
		Author.USER      -> Role.User
		Author.TOOL      -> Role.Tool
		Author.ASSISTANT -> Role.Assistant
		Author.SYSTEM    -> Role.System
	}

}
