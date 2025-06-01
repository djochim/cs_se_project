package dev.bitvictory.aeon.core.domain.entities.assistant.thread

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCalls
import dev.bitvictory.aeon.model.AeonError
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AeonThreadRun(
	val id: String,
	val createdAt: Instant,
	val threadId: String,
	val assistantId: String,
	val status: AeonStatus,
	val lastError: AeonError?,
	val requiredAction: AeonActionCalls?
)

@Serializable
enum class AeonStatus(val value: String) {
	CREATED("created"),
	PENDING("pending"),       // For states like queued, validating_files
	PROCESSING("processing"), // For states like running, in_progress, cancelling
	FINALIZED("finalized"),   // For states like succeeded, completed
	ACTION_REQUIRED("action_required"), // For requires_action
	FAILED_OR_CANCELLED("failed_or_cancelled"), // For failed, canceled, expired, deleted
	UNKNOWN("unknown");       // For any unrecognized status
}