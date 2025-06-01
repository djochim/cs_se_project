package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.core.Status
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus

fun Status.toAeonStatus() = when (this) {
	Status.Queued, Status.ValidatingFiles                           -> AeonStatus.PENDING

	Status.Running, Status.InProgress, Status.Cancelling            -> AeonStatus.PROCESSING

	Status.Succeeded, Status.Completed, Status.Processed            -> AeonStatus.FINALIZED

	Status.RequiresAction                                           -> AeonStatus.ACTION_REQUIRED

	Status.Failed, Status.Cancelled, Status.Expired, Status.Deleted -> AeonStatus.FAILED_OR_CANCELLED

	else                                                            -> AeonStatus.UNKNOWN
}