package dev.bitvictory.aeon.application.service.actioncall

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonAction
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.user.User
import org.bson.types.ObjectId

interface AeonActionProcessor {

	fun actionName(): String

	suspend fun process(advisoryId: ObjectId, user: User, threadId: String, runId: String, aeonAction: AeonAction): AeonToolOutput

}