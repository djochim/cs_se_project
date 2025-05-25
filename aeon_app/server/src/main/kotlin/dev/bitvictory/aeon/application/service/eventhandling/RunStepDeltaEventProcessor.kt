package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunStepDeltaEvent
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import io.ktor.util.logging.KtorSimpleLogger
import org.bson.types.ObjectId

class RunStepDeltaEventProcessor(
	advisoryPersistence: AdvisoryPersistence,
): AdvisoryEventProcessor<AeonRunStepDeltaEvent>(advisoryPersistence) {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun process(advisoryId: ObjectId, event: AeonRunStepDeltaEvent): AeonActionCallWrapper {
		logger.info("Ignored run step delta event. AdvisoryId: {}, Event: {}", advisoryId, event)
		return AeonActionCallWrapper.empty()
	}

}