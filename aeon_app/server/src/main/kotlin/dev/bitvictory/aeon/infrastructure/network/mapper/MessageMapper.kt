package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.run.MessageDeltaContent
import com.aallam.openai.api.run.MessageDeltaData
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessageDelta
import dev.bitvictory.aeon.core.domain.entities.assistant.message.ImageMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.ImageURLMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.TextMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import kotlinx.datetime.Instant

@OptIn(BetaOpenAI::class) fun Message.toAeonMessage(status: AeonStatus) = AeonMessage(
	this.id.id,
	Instant.fromEpochSeconds(this.createdAt.toLong()),
	this.threadId.id,
	this.runId?.id,
	this.assistantId?.id,
	this.role.toAuthor(),
	status,
	this.content.map { it.toAeonMessageContent() }
)

@OptIn(BetaOpenAI::class) fun MessageDeltaData.toAeonMessageDelta() = AeonMessageDelta(
	this.role.toAuthor(),
	this.content.map { it.toAeonMessageContent() }
)

@OptIn(BetaOpenAI::class) fun MessageContent.toAeonMessageContent() = when (this) {
	is MessageContent.Image -> ImageMessageContent(this.imageFile.fileId.id)
	is MessageContent.Text  -> TextMessageContent(this.text.value)
}

@OptIn(BetaOpenAI::class) fun MessageDeltaContent.toAeonMessageContent() = when (this) {
	is MessageDeltaContent.Image -> ImageMessageContent(this.imageFile.fileId.id)
	is MessageDeltaContent.Text -> TextMessageContent(this.text.value)
	is MessageDeltaContent.ImageURL -> ImageURLMessageContent(this.imageURL.url)
	is MessageDeltaContent.Refusal -> TextMessageContent(this.refusal)
}