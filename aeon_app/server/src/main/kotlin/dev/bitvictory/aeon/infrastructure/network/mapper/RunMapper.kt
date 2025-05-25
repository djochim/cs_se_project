package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.run.MessageCreationStepDetails
import com.aallam.openai.api.run.RequiredAction
import com.aallam.openai.api.run.Run
import com.aallam.openai.api.run.RunStep
import com.aallam.openai.api.run.RunStepDetails
import com.aallam.openai.api.run.ToolCallStep
import com.aallam.openai.api.run.ToolCallStepDetails
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCalls
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunctionCall
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonRunStep
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonThreadRun
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonToolCallStep
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.MessageCreationStep
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.StepDetails
import dev.bitvictory.aeon.model.AeonError
import kotlinx.datetime.Instant

@OptIn(BetaOpenAI::class) fun Run.toAeonThreadRun() = AeonThreadRun(
	id.id,
	Instant.fromEpochSeconds(createdAt.toLong()),
	threadId.id,
	assistantId.id,
	status.toAeonStatus(),
	lastError?.let { AeonError(message = it.message, details = mapOf("code" to it.code)) },
	requiredAction?.toAeonActionCalls()
)

@OptIn(BetaOpenAI::class)
private fun RequiredAction.toAeonActionCalls(): AeonActionCalls = when (this) {
	is RequiredAction.SubmitToolOutputs -> AeonActionCalls(toolOutputs.toolCalls.map { it.toAeonAction() })
}

private fun ToolCall.toAeonAction() = when (this) {
	is ToolCall.Function -> AeonFunctionCall(this.id.id, function.name, function.arguments)
}

@OptIn(BetaOpenAI::class) fun RunStep.toAronRunStep() = AeonRunStep(
	id.id,
	Instant.fromEpochSeconds(createdAt.toLong()),
	threadId.id,
	runId.id,
	status.toAeonStatus(),
	lastError?.let { AeonError(message = it.message, details = mapOf("code" to it.code)) },
	stepDetails.toAeonStepDetails()
)

@OptIn(BetaOpenAI::class) fun RunStepDetails.toAeonStepDetails(): List<StepDetails> = when (this) {
	is MessageCreationStepDetails -> listOf(MessageCreationStep(this.messageCreation.messageId.id))
	is ToolCallStepDetails        -> this.toolCalls?.map { it.toAeonToolCallStep() } ?: emptyList()
}

@OptIn(BetaOpenAI::class) fun ToolCallStep.toAeonToolCallStep(): StepDetails = when (this) {
	is ToolCallStep.CodeInterpreter -> throw IllegalStateException("Code interpreter not supported")
	is ToolCallStep.FileSearchTool  -> throw IllegalStateException("File search tool not supported")
	is ToolCallStep.FunctionTool    -> AeonToolCallStep.FunctionCall(this.function.name)
	is ToolCallStep.RetrievalTool   -> throw IllegalStateException("Retrieval tool not supported")
}