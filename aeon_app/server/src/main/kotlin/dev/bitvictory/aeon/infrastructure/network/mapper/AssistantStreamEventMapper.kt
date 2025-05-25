package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.run.AssistantStreamEvent
import com.aallam.openai.api.run.AssistantStreamEventType
import com.aallam.openai.api.run.MessageDelta
import com.aallam.openai.api.run.Run
import com.aallam.openai.api.run.RunStep
import com.aallam.openai.api.run.RunStepDelta
import com.aallam.openai.api.thread.Thread
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageDeltaEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonRunStepEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonThreadEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.DoneEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.ErrorEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.UnknownEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonRunStep
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonRunStepDelta
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonThread
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonThreadRun
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

private val jsonMapper = Json {
	ignoreUnknownKeys = true
}

@OptIn(BetaOpenAI::class) fun AssistantStreamEvent.toAeonAssistantEvent() = when (this.type) {
	AssistantStreamEventType.THREAD_CREATED              -> AeonThreadEvent.Created(this.toAeonThread())
	AssistantStreamEventType.THREAD_RUN_CREATED          -> AeonRunEvent.Created(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_QUEUED           -> AeonRunEvent.Queued(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_IN_PROGRESS      -> AeonRunEvent.InProgress(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_REQUIRES_ACTION  -> AeonRunEvent.RequiresAction(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_COMPLETED        -> AeonRunEvent.Completed(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_INCOMPLETE       -> AeonRunEvent.Incomplete(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_FAILED           -> AeonRunEvent.Failed(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_CANCELLING       -> AeonRunEvent.Cancelling(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_CANCELLED        -> AeonRunEvent.Cancelled(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_EXPIRED          -> AeonRunEvent.Expired(this.toAeonThreadRun())
	AssistantStreamEventType.THREAD_RUN_STEP_CREATED     -> AeonRunStepEvent.Created(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_RUN_STEP_IN_PROGRESS -> AeonRunStepEvent.InProgress(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_RUN_STEP_DELTA       -> UnknownEvent(this.data) // AeonRunStepDeltaEvent(this.toAeonAeonRunStepDelta()) TODO serialise issues
	AssistantStreamEventType.THREAD_RUN_STEP_COMPLETED   -> AeonRunStepEvent.Completed(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_RUN_STEP_FAILED      -> AeonRunStepEvent.Failed(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_RUN_STEP_CANCELLED   -> AeonRunStepEvent.Cancelled(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_RUN_STEP_EXPIRED     -> AeonRunStepEvent.Expired(this.toAeonRunStep())
	AssistantStreamEventType.THREAD_MESSAGE_CREATED      -> AeonMessageEvent.Created(this.toAeonMessage(AeonStatus.CREATED))
	AssistantStreamEventType.THREAD_MESSAGE_IN_PROGRESS  -> AeonMessageEvent.InProgress(this.toAeonMessage(AeonStatus.PROCESSING))
	AssistantStreamEventType.THREAD_MESSAGE_DELTA        -> UnknownEvent(this.data) // this.toAeonMessageDelta() TODO serialise issues
	AssistantStreamEventType.THREAD_MESSAGE_COMPLETED    -> AeonMessageEvent.Completed(this.toAeonMessage(AeonStatus.FINALIZED))
	AssistantStreamEventType.THREAD_MESSAGE_INCOMPLETE   -> AeonMessageEvent.Incomplete(this.toAeonMessage(AeonStatus.FAILED_OR_CANCELLED))
	AssistantStreamEventType.ERROR                       -> ErrorEvent(this.data)
	AssistantStreamEventType.DONE                        -> DoneEvent(this.data)
	AssistantStreamEventType.UNKNOWN                     -> UnknownEvent(this.data)
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonThread(): AeonThread? {
	require(this.type.dataType == Thread::class) { "Only events with data type Thread can be mapped to an AeonThread" }
	val thread: Thread? = this.data?.let { jsonMapper.decodeFromString(it) }
	return thread?.let { AeonThread(thread.id.id, Instant.fromEpochSeconds(thread.createdAt.toLong())) }
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonThreadRun(): AeonThreadRun? {
	require(this.type.dataType == Run::class) { "Only events with data type Run can be mapped to an AeonThreadRun" }
	val run: Run? = this.data?.let { jsonMapper.decodeFromString(it) }
	return run?.toAeonThreadRun()
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonRunStep(): AeonRunStep? {
	require(this.type.dataType == RunStep::class) { "Only events with data type RunStep can be mapped to an AeonRunStep" }
	val runStep: RunStep? = this.data?.let { jsonMapper.decodeFromString(it) }
	return runStep?.toAronRunStep()
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonAeonRunStepDelta(): AeonRunStepDelta? {
	require(this.type.dataType == RunStepDelta::class) { "Only events with data type RunStepDelta can be mapped to an AeonRunStepDelta" }
	val runStepDelta: RunStepDelta? = this.data?.let { jsonMapper.decodeFromString(it) }
	return runStepDelta?.let { AeonRunStepDelta(it.id.id, it.delta.stepDetails.toAeonStepDetails()) }
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonMessage(status: AeonStatus): AeonMessage? {
	require(this.type.dataType == Message::class) { "Only events with data type Message can be mapped to an AeonMessage" }
	val message: Message? = this.data?.let { jsonMapper.decodeFromString(it) }
	return message?.toAeonMessage(status)
}

@OptIn(BetaOpenAI::class)
private fun AssistantStreamEvent.toAeonMessageDelta(): AeonMessageDeltaEvent {
	require(this.type.dataType == MessageDelta::class) { "Only events with data type MessageDelta can be mapped to an AeonMessageDelta" }
	val messageDeltaData: MessageDelta? = this.data?.let { jsonMapper.decodeFromString(it) }
	return AeonMessageDeltaEvent(messageDeltaData?.id?.id, messageDeltaData?.delta?.toAeonMessageDelta())
}

