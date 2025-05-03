package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.datetime.Clock
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AdvisoryApiTest {

	@Test
	fun `test toMessageContent`() {
		val stringMessageDTO = StringMessageDTO("Hello")
		val stringMessage = stringMessageDTO.toMessageContent()

		stringMessage.shouldBeInstanceOf<StringMessage>()
		stringMessage.content shouldBe "Hello"
	}

	@Test
	fun `test toDTO`() {
		val stringMessage = StringMessage("Hello")
		val stringMessageDTO = stringMessage.toDTO()
		stringMessageDTO.shouldBeInstanceOf<StringMessageDTO>()
		stringMessageDTO.content shouldBe "Hello"
	}

	@Test
	fun `test Message toDTO`(@MockK user: User) {
		val message = Message(
			messageId = "mid",
			messageContent = StringMessage("Hello"),
			user = user,
			creationDateTime = Clock.System.now(),
			author = Author.USER,
			status = "status",
			error = "error"
		)

		val messageDTO = message.toDTO()
		messageDTO.id shouldBe "mid"
		messageDTO.messageContent.shouldBeInstanceOf<StringMessageDTO>()
		(messageDTO.messageContent as StringMessageDTO).content shouldBe "Hello"
		messageDTO.creationDateTime shouldBe message.creationDateTime
		messageDTO.author shouldBe AuthorDTO.USER
		messageDTO.status shouldBe "status"
		messageDTO.error shouldBe "error"
	}

	@Test
	fun `Advisory toDTO`(@MockK user: User) {
		val message = Message(
			messageId = "mid",
			messageContent = StringMessage("Hello"),
			user = user,
			creationDateTime = Clock.System.now(),
			author = Author.USER,
			status = "status",
			error = "error"
		)
		val advisory = Advisory(id = ObjectId.get(), threadId = "tid", messages = listOf(message), user = user)

		val advisoryDTO = advisory.toDTO()

		advisoryDTO.id shouldBe advisory.id.toHexString()
		advisoryDTO.threadId shouldBe advisory.threadId
		advisoryDTO.messages.size shouldBe advisory.messages.size
	}

}