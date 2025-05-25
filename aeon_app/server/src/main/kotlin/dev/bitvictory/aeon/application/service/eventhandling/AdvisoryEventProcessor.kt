package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

abstract class AdvisoryEventProcessor<T: AeonAssistantEvent>(
	protected val advisoryPersistence: AdvisoryPersistence,
) {

	abstract suspend fun process(advisoryId: ObjectId, event: T): AeonActionCallWrapper

	protected suspend fun addEvent(advisoryId: ObjectId, event: AeonAssistantEvent) {
		advisoryPersistence.addEvent(advisoryId, event)
	}

}