package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonThreadEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class ThreadEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<AeonThreadEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: AeonThreadEvent): AeonActionCallWrapper {
		when (event) {
			is AeonThreadEvent.Created -> advisoryPersistence.createThread(advisoryId, event)
		}
		return AeonActionCallWrapper.empty()
	}

}