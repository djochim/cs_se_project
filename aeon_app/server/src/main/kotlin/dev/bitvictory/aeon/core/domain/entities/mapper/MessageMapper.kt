package dev.bitvictory.aeon.core.domain.entities.mapper

import dev.bitvictory.aeon.core.domain.entities.advisory.ImageIdMessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.ImageMessage
import dev.bitvictory.aeon.core.domain.entities.advisory.ImageURLMessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.MessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiImageIdMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiImageMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiImageURLMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiTextMessageContent


fun MessageContent.toAeonMessageContent(): AiMessageContent = when (this) {
    is StringMessage -> AiTextMessageContent(this.content)
    is ImageIdMessageContent -> AiImageIdMessageContent(this.fileId)
    is ImageMessage -> AiImageMessageContent(this.content)
    is ImageURLMessageContent -> AiImageURLMessageContent(this.url)
}