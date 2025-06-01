package dev.bitvictory.aeon.core.domain.entities.assistant.event

import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonRunStep
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonRunStepDelta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AeonRunStepEvent(override val eventKey: String, @SerialName("run_step") open val step: AeonRunStep?): AeonAssistantEvent(eventKey) {

	@Serializable
	data class Created(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.created", step)

	@Serializable
	data class InProgress(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.in_progress", step)

	@Serializable
	data class Completed(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.completed", step)

	@Serializable
	data class Failed(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.failed", step)

	@Serializable
	data class Cancelled(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.cancelled", step)

	@Serializable
	data class Expired(override val step: AeonRunStep?): AeonRunStepEvent("thread.run.step.expired", step)
}

@Serializable
data class AeonRunStepDeltaEvent(val step: AeonRunStepDelta?): AeonAssistantEvent("thread.run.step.delta")
