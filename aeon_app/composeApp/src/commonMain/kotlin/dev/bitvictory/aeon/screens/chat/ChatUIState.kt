package dev.bitvictory.aeon.screens.chat

import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.screens.RootUIState

data class ChatUIState(
	val newMessage: String = "",
	val messages: List<MessageDTO> = emptyList(),
	val advisoryId: AdvisoryIdDTO? = null,
	val assistantIsTyping: Boolean = false,
	val error: String = "",
	val isRefreshing: Boolean = false
): RootUIState(false)
