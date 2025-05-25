package dev.bitvictory.aeon.core.domain.entities.assistant.thread

import dev.bitvictory.aeon.model.AeonError
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonDiscriminator

@Serializable
data class AeonRunStep(
	val id: String,
	val createdAt: Instant,
	val threadId: String,
	val runId: String,
	val status: AeonStatus,
	val lastError: AeonError?,
	val details: List<StepDetails>
)

@Serializable
data class AeonRunStepDelta(val runStepId: String, val details: List<StepDetails>)

@Serializable
@BsonDiscriminator
sealed interface StepDetails

@Serializable
data class MessageCreationStep(
	val messageId: String
): StepDetails

@Serializable
@BsonDiscriminator
sealed class AeonToolCallStep: StepDetails {

	@Serializable
	data class FunctionCall(
		val name: String,
	): AeonToolCallStep()
}


