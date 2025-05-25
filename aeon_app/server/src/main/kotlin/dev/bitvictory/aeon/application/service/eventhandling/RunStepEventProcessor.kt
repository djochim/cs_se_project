package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunStepEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class RunStepEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<AeonRunStepEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: AeonRunStepEvent): AeonActionCallWrapper {
		advisoryPersistence.addEvent(advisoryId, event)
		return AeonActionCallWrapper.empty()
	}

}