package dev.bitvictory.aeon.core.domain.entities.assistant.action

sealed interface AeonTool

sealed class AeonFunction(
	val name: String,
	val description: String,
	val strictMode: Boolean,
	val parameters: String
): AeonTool

data class AeonToolOutputs(
	val threadId: String,
	val runId: String,
	val outputs: List<AeonToolOutput>
)

data class AeonToolOutput(
	val toolId: String,
	val output: String
)
