package dev.bitvictory.aeon.infrastructure.network.mapper

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.assistant.Function
import com.aallam.openai.api.chat.ToolId
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.run.ToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunction
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonTool
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput

@OptIn(BetaOpenAI::class) fun AeonTool.toOpenAI() = when (this) {
	is AeonFunction -> AssistantTool.FunctionTool(
		Function(
			name = this.name,
			description = this.description,
			parameters = Parameters.fromJsonString(this.parameters)
		)
	)
}

@OptIn(BetaOpenAI::class) fun AeonToolOutput.toOpenAI() = ToolOutput(ToolId(this.toolId), this.output)