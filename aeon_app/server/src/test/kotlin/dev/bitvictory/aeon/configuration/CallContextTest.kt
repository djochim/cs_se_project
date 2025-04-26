package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.exceptions.AuthenticationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class CallContextTest {

	@Test
	fun `User context returns token and does not throw`() {
		val userContext = UserContext(null, "token")

		val result = userContext.tokenOrThrow()

		result shouldBe "token"
	}

	@Test
	fun `User context throws`() {
		val userContext = UserContext(null, null)

		shouldThrow<AuthenticationException> { userContext.tokenOrThrow() }
	}

}