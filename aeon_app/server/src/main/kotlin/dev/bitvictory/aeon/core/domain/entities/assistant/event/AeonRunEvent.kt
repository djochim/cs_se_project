package dev.bitvictory.aeon.core.domain.entities.assistant.event

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCalls
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonThreadRun
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AeonRunEvent(override val eventKey: String, @SerialName("thread_run") open val run: AeonThreadRun?): AeonAssistantEvent(eventKey) {
	@Serializable
	data class Created(override val run: AeonThreadRun?): AeonRunEvent("thread.run.created", run)

	@Serializable
	data class Queued(override val run: AeonThreadRun?): AeonRunEvent("thread.run.queued", run)

	@Serializable
	data class InProgress(override val run: AeonThreadRun?): AeonRunEvent("thread.run.in_progress", run)

	@Serializable
	data class Completed(override val run: AeonThreadRun?): AeonRunEvent("thread.run.completed", run)

	@Serializable
	data class RequiresAction(override val run: AeonThreadRun?): AeonRunEvent("thread.run.requires_action", run) {
		val requiredAction: AeonActionCalls
			get() = run?.requiredAction ?: AeonActionCalls.empty()
	}

	@Serializable
	data class Incomplete(override val run: AeonThreadRun?): AeonRunEvent("thread.run.incomplete", run)

	@Serializable
	data class Cancelling(override val run: AeonThreadRun?): AeonRunEvent("thread.run.cancelling", run)

	@Serializable
	data class Cancelled(override val run: AeonThreadRun?): AeonRunEvent("thread.run.cancelled", run)

	@Serializable
	data class Failed(override val run: AeonThreadRun?): AeonRunEvent("thread.run.failed", run)

	@Serializable
	data class Expired(override val run: AeonThreadRun?): AeonRunEvent("thread.run.expired", run)
}