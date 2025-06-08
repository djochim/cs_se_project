package dev.bitvictory.aeon.application.service.eventhandling

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageDeltaEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunStepDeltaEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunStepEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonThreadEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.DoneEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.ErrorEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.UnknownEvent
import io.ktor.util.logging.KtorSimpleLogger
import org.bson.types.ObjectId

/**
 * AdvisoryEventDispatcher is responsible for dispatching AeonAssistantEvents to their respective processors.
 * It takes an advisoryId and an AeonAssistantEvent as input, and returns an AeonActionCallWrapper.
 *
 * @property threadEventProcessor Processor for AeonThreadEvents.
 * @property messageEventProcessor Processor for AeonMessageEvents.
 * @property messageDeltaEventProcessor Processor for AeonMessageDeltaEvents.
 * @property runEventProcessor Processor for AeonRunEvents.
 * @property runStepEventProcessor Processor for AeonRunStepEvents.
 * @property runStepDeltaEventProcessor Processor for AeonRunStepDeltaEvents.
 * @property doneEventProcessor Processor for DoneEvents.
 * @property errorEventProcessor Processor for ErrorEvents.
 * @property unknownEventProcessor Processor for UnknownEvents.
 */
class AdvisoryEventDispatcher(
	private val threadEventProcessor: ThreadEventProcessor,
	private val messageEventProcessor: MessageEventProcessor,
	private val messageDeltaEventProcessor: MessageDeltaEventProcessor,
	private val runEventProcessor: RunEventProcessor,
	private val runStepEventProcessor: RunStepEventProcessor,
	private val runStepDeltaEventProcessor: RunStepDeltaEventProcessor,
	private val doneEventProcessor: DoneEventProcessor,
	private val errorEventProcessor: ErrorEventProcessor,
	private val unknownEventProcessor: UnknownEventProcessor,
) {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	suspend fun dispatch(advisoryId: ObjectId, event: AeonAssistantEvent): AeonActionCallWrapper {
		logger.info("Dispatching event. AdvisoryId: {}, Event: {}", advisoryId, event.eventKey)
		return when (event) {
			is AeonThreadEvent       -> threadEventProcessor.process(advisoryId, event)
			is AeonMessageEvent      -> messageEventProcessor.process(advisoryId, event)
			is AeonMessageDeltaEvent -> messageDeltaEventProcessor.process(advisoryId, event)
			is AeonRunEvent          -> runEventProcessor.process(advisoryId, event)
			is AeonRunStepEvent      -> runStepEventProcessor.process(advisoryId, event)
			is AeonRunStepDeltaEvent -> runStepDeltaEventProcessor.process(advisoryId, event)
			is DoneEvent             -> doneEventProcessor.process(advisoryId, event)
			is ErrorEvent            -> errorEventProcessor.process(advisoryId, event)
			is UnknownEvent          -> unknownEventProcessor.process(advisoryId, event)
		}
	}

}