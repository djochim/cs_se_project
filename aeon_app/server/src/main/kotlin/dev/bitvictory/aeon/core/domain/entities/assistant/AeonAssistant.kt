package dev.bitvictory.aeon.core.domain.entities.assistant

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonTool

sealed class AeonAssistant(
	val name: String,
	val model: String,
	val instructions: String,
	val tools: List<AeonTool>
)
