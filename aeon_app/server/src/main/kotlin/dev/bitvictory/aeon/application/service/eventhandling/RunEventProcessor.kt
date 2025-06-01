package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class RunEventProcessor(
	advisoryPersistence: AdvisoryPersistence
): AdvisoryEventProcessor<AeonRunEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: AeonRunEvent): AeonActionCallWrapper {
		when (event) {
			is AeonRunEvent.Cancelled      -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Cancelling     -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Completed      -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Created        -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Expired        -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Failed         -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.InProgress     -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Incomplete     -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.Queued         -> advisoryPersistence.addEvent(advisoryId, event)
			is AeonRunEvent.RequiresAction -> return processRequiredActionEvent(advisoryId, event)
		}
		return AeonActionCallWrapper.empty()
	}

	private suspend fun processRequiredActionEvent(advisoryId: ObjectId, event: AeonRunEvent.RequiresAction): AeonActionCallWrapper {
		advisoryPersistence.addEvent(advisoryId, event)
		return AeonActionCallWrapper(event.run?.threadId, event.run?.id, event.requiredAction)
	}

}