package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import org.bson.types.ObjectId

class MessageEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<AeonMessageEvent>(advisoryPersistence) {

	override suspend fun process(advisoryId: ObjectId, event: AeonMessageEvent): AeonActionCallWrapper {
		when (event) {
			is AeonMessageEvent.Created    -> advisoryPersistence.upsertMessage(advisoryId, event.message, event)
			is AeonMessageEvent.InProgress -> advisoryPersistence.upsertMessage(advisoryId, event.message, event)
			is AeonMessageEvent.Completed  -> advisoryPersistence.upsertMessage(advisoryId, event.message, event)
			is AeonMessageEvent.Incomplete -> advisoryPersistence.upsertMessage(advisoryId, event.message, event)
		}
		return AeonActionCallWrapper.empty()
	}

}