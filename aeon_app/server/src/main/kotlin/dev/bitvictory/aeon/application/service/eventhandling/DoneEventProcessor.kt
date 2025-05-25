package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.DoneEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class DoneEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<DoneEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: DoneEvent): AeonActionCallWrapper {
		advisoryPersistence.updateStatus(advisoryId, AeonStatus.FINALIZED, event)
		return AeonActionCallWrapper.empty()
	}

}