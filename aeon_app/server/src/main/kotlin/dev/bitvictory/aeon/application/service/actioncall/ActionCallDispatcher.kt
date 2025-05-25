package dev.bitvictory.aeon.application.service.actioncall

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonAction
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunctionCall
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutputs
import dev.bitvictory.aeon.core.domain.entities.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.bson.types.ObjectId

class ActionCallDispatcher(private val actionProcessors: List<AeonActionProcessor>) {

	suspend fun dispatch(advisoryId: ObjectId, user: User, actionWrapper: AeonActionCallWrapper): AeonToolOutputs {
		requireNotNull(actionWrapper.runId)
		requireNotNull(actionWrapper.threadId)
		val output = actionWrapper.calls.actions.map { dispatch(advisoryId, user, actionWrapper.threadId, actionWrapper.runId, it) }.map { it.await() }
		return AeonToolOutputs(actionWrapper.threadId, actionWrapper.runId, output)
	}

	private fun dispatch(advisoryId: ObjectId, user: User, threadId: String, runId: String, action: AeonAction): Deferred<AeonToolOutput> = when (action) {
		is AeonFunctionCall -> dispatch(advisoryId, user, threadId, runId, action)
	}

	private fun dispatch(advisoryId: ObjectId, user: User, threadId: String, runId: String, functionCall: AeonFunctionCall): Deferred<AeonToolOutput> =
		CoroutineScope(Dispatchers.IO).async {
			val actionProcessor =
				actionProcessors.find { it.actionName() == functionCall.name } ?: return@async AeonToolOutput(
					functionCall.callId,
					"tool with name ${functionCall.name} not found"
				)
			actionProcessor.process(advisoryId, user, threadId, runId, functionCall)
		}

}