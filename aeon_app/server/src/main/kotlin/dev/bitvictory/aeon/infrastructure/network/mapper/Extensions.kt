package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.message.MessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.core.exceptions.InvalidMessageException

fun Role.toAuthor() = when (this) {
    Role.User -> Author.USER
    Role.Assistant -> Author.ASSISTANT
    Role.Tool -> Author.TOOL
    Role.Function -> Author.TOOL
    Role.System -> Author.SYSTEM
    else -> throw InvalidMessageException("Unknown role $this")
}

@OptIn(BetaOpenAI::class)
fun MessageContent.toAssistantMessage() = when (this) {
    is MessageContent.Text -> StringMessage(this.text.value)
    is MessageContent.Image -> throw InvalidMessageException("Image content not supported")
}