package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.ErrorEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class ErrorEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<ErrorEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: ErrorEvent): AeonActionCallWrapper {
		advisoryPersistence.logErrorEvent(advisoryId, event)
		return AeonActionCallWrapper.empty()
	}

}