package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.service.AdvisoryService
import dev.bitvictory.aeon.configuration.userPrincipal
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.MessageContent
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.Author
import dev.bitvictory.aeon.core.domain.entities.assistant.message.ImageMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.ImageURLMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.TextMessageContent
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.MessageContentDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
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

/**
 * Defines the routes for handling advisories within the application.
 *
 * This function sets up the following authenticated endpoints:
 *
 * - **POST /advisories**: Creates a new advisory.
 *   - Expects a JSON request body of type `AdvisoryMessageRequest`.
 *   - Initializes a new advisory with the provided message.
 *   - The message author is set to `Author.USER` and the principal is taken from the authenticated user.
 *   - Returns HTTP status `201 Created` with the DTO representation of the newly created advisory.
 *
 * - **GET /advisories/{id}**: Retrieves a specific advisory by its ID.
 *   - `{id}`: The unique identifier of the advisory to retrieve.
 *   - Ensures that the authenticated user has permission to access the advisory.
 *   - Returns HTTP status `200 OK` with the DTO representation of the advisory if found and accessible.
 *   - Throws `IllegalArgumentException` if the `id` parameter is missing.
 *
 * - **POST /advisories/{id}/messages**: Adds a new message to an existing advisory.
 *   - `{id}`: The unique identifier of the advisory to which the message will be added.
 *   - Expects a JSON request body of type `AdvisoryMessageRequest`.
 *   - The message author is set to `Author.USER` and the principal is taken from the authenticated user.
 *   - Validates that the message content is not blank.
 *   - Returns HTTP status `201 Created` with the DTO representation of the newly added message.
 *   - Throws `IllegalArgumentException` if the `id` parameter is missing or if the message content is blank.
 *
 * Authentication is required for all routes defined within this function.
 * It utilizes an `AdvisoryService` (injected via Koin) to handle the business logic
 * for creating, retrieving, and updating advisories and their messages.
 */
fun Route.advisories() {
	authenticate {
		route("/advisories") {
			val advisoryService by inject<AdvisoryService>()
			post {
				val request = call.receive<AdvisoryMessageRequest>()
				val messageContent = request.message.toMessageContent()
				val message = Message(Clock.System.now(), Author.USER, call.userPrincipal(), messageContent)
				val advisory = advisoryService.startNewAdvisory(message)
				call.respond(HttpStatusCode.Created, advisory.toDTO())
			}
			route("/{id}") {
				get {
					val id =
						call.parameters["id"] ?: throw IllegalArgumentException("Missing request id")
					val advisory = advisoryService.retrieveAdvisoryById(ObjectId(id), call.userPrincipal())
					call.respond(HttpStatusCode.OK, advisory.toDTO())
				}
				route("/messages") {
					post {
						val id =
							call.parameters["id"]
								?: throw IllegalArgumentException("Missing request id")
						val request = call.receive<AdvisoryMessageRequest>()
						val messageContent = request.message.toMessageContent()
						if (messageContent.content.isBlank()) {
							throw IllegalArgumentException("Message content cannot be blank")
						}
						val message =
							Message(
								Clock.System.now(),
								Author.USER,
								call.userPrincipal(),
								messageContent
							)
						advisoryService.addMessage(ObjectId(id), message)
						call.respond(HttpStatusCode.Created, message.toDTO())
					}
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
	listOf(this.messageContent.toDTO()),
	this.creationDateTime,
	AuthorDTO.valueOf(this.author.name),
	this.status ?: "",
	this.error ?: ""
)

fun Advisory.toDTO() =
	AdvisoryDTO(this.id.toHexString(), this.threadId, this.messages.map { it.toDTO() })

fun AeonMessage.toDTO() = MessageDTO(
	id = this.id,
	messageContents = this.content.map { it.toDTO() },
	creationDateTime = this.createdAt,
	author = AuthorDTO.valueOf(this.role.name),
	status = this.status.value
)

fun AeonMessageContent.toDTO() = when (this) {
	is TextMessageContent     -> StringMessageDTO(this.value)
	is ImageMessageContent    -> throw UnsupportedOperationException("Image content is not yet supported.")
	is ImageURLMessageContent -> throw UnsupportedOperationException("Image content is not yet supported.")
}