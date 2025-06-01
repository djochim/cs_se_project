package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.UnknownEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import io.ktor.util.logging.KtorSimpleLogger
import org.bson.types.ObjectId

class UnknownEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<UnknownEvent>(advisoryPersistence) {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun process(advisoryId: ObjectId, event: UnknownEvent): AeonActionCallWrapper {
		logger.info("Ignored unknown event. AdvisoryId: {}, Event: {}", advisoryId, event)
		return AeonActionCallWrapper.empty()
	}

}