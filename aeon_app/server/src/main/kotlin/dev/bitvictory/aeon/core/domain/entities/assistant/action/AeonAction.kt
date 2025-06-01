package dev.bitvictory.aeon.core.domain.entities.assistant.action

import kotlinx.serialization.Serializable

data class AeonActionCallWrapper(
	val threadId: String? = null,
	val runId: String? = null,
	val calls: AeonActionCalls
) {
	fun requiresDispatching(): Boolean {
		return threadId != null && runId != null && calls.actions.isNotEmpty()
	}

	companion object {
		fun empty() = AeonActionCallWrapper(calls = AeonActionCalls.empty())
	}
}

@Serializable
data class AeonActionCalls(
	val actions: List<AeonAction>
) {

	companion object {
		fun empty() = AeonActionCalls(emptyList())
	}
}

@Serializable
sealed interface AeonAction

@Serializable
data class AeonFunctionCall(
	val callId: String,
	val name: String,
	val arguments: String
): AeonAction



