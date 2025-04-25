package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.service.AdvisoryService
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.MessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.MessageContentDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.Clock
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun Route.advisories() {
    route("/advisories") {
        val advisoryService by inject<AdvisoryService>()
        post {
            val request = call.receive<AdvisoryMessageRequest>()
            val messageContent = request.message.toMessageContent()
            val message = Message(Clock.System.now(), Author.USER, messageContent)
            val advisory = advisoryService.startNewAdvisory(message)
            call.respond(HttpStatusCode.Created, advisory.toDTO())
        }
        route("/{id}") {
            get {
                val id =
                    call.parameters["id"] ?: throw IllegalArgumentException("Missing request id")
                val advisory = advisoryService.retrieveAdvisoryById(ObjectId(id))
                call.respond(HttpStatusCode.OK, advisory.toDTO())
            }
            route("/messages") {
                post {
                    val id =
                        call.parameters["id"]
                            ?: throw IllegalArgumentException("Missing request id")
                    val request = call.receive<AdvisoryMessageRequest>()
                    val messageContent = request.message.toMessageContent()
                    val message =
                        Message(
                            Clock.System.now(),
                            Author.USER,
                            messageContent
                        )
                    advisoryService.addMessage(ObjectId(id), message)
                    call.respond(HttpStatusCode.Created, message.toDTO())
                }
            }
        }
    }
}

fun MessageContentDTO.toMessageContent() = when (this) {
    is StringMessageDTO -> StringMessage(this.content)
}

fun MessageContent.toDTO() = when (this) {
    is StringMessage -> StringMessageDTO(this.content)
}

@OptIn(ExperimentalUuidApi::class)
fun Message.toDTO() = MessageDTO(
    this.messageId ?: Uuid.NIL.toHexString(),
    this.messageContent.toDTO(),
    this.creationDateTime,
    AuthorDTO.valueOf(this.author.name),
    this.status ?: "",
    this.error ?: ""
)

fun Advisory.toDTO() =
    AdvisoryDTO(this.id.toHexString(), this.threadId, this.messages.map { it.toDTO() })