package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageDeltaEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import io.ktor.util.logging.KtorSimpleLogger
import org.bson.types.ObjectId

class MessageDeltaEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<AeonMessageDeltaEvent>(advisoryPersistence) {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun process(advisoryId: ObjectId, event: AeonMessageDeltaEvent): AeonActionCallWrapper {
		logger.info("Ignored message delta event. AdvisoryId: {}, Event: {}", advisoryId, event)
		return AeonActionCallWrapper.empty()
	}

}