package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.run.MessageDeltaContent
import com.aallam.openai.api.run.MessageDeltaData
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiImageIdMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiImageURLMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiMessageDelta
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiTextMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import kotlinx.datetime.Instant

@OptIn(BetaOpenAI::class)
fun Message.toAeonMessage(status: AeonStatus) = AiMessage(
    this.id.id,
    Instant.fromEpochSeconds(this.createdAt.toLong()),
    this.threadId.id,
    this.runId?.id,
    this.assistantId?.id,
    this.role.toAuthor(),
    status,
    this.content.map { it.toAeonMessageContent() }
)

@OptIn(BetaOpenAI::class)
fun MessageDeltaData.toAeonMessageDelta() = AiMessageDelta(
    this.role.toAuthor(),
    this.content.map { it.toAeonMessageContent() }
)

@OptIn(BetaOpenAI::class)
fun MessageContent.toAeonMessageContent() = when (this) {
    is MessageContent.Image -> AiImageIdMessageContent(this.imageFile.fileId.id)
    is MessageContent.Text -> AiTextMessageContent(this.text.value)
}

@OptIn(BetaOpenAI::class)
fun MessageDeltaContent.toAeonMessageContent() = when (this) {
    is MessageDeltaContent.Image -> AiImageIdMessageContent(this.imageFile.fileId.id)
    is MessageDeltaContent.Text -> AiTextMessageContent(this.text.value)
    is MessageDeltaContent.ImageURL -> AiImageURLMessageContent(this.imageURL.url)
    is MessageDeltaContent.Refusal -> AiTextMessageContent(this.refusal)
}