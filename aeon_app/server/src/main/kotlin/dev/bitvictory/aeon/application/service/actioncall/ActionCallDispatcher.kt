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

/**
 * Dispatches action calls to the appropriate [AeonActionProcessor].
 *
 * This class is responsible for receiving a wrapper containing multiple action calls,
 * finding the correct processor for each action based on its name, and then executing
 * the action using that processor. It aggregates the results of all processed actions.
 *
 * @property actionProcessors A list of [AeonActionProcessor] instances that this dispatcher can use.
 */
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