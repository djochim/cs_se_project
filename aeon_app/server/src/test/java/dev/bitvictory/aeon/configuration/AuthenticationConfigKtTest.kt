package dev.bitvictory.aeon.configuration

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.ktor.server.auth.jwt.JWTCredential
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AuthenticationConfigKtTest {

	@Test
	fun `Valid JWT with correct type and audience`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns arrayOf("ADMIN", "ACCESS")
			every { payload.subject } returns "1234567890"
			every { payload.audience } returns listOf("other.com", "aeon.api")

			val user = validateCredentials(credentials)

			user shouldNot beNull()
			user?.id shouldBe "1234567890"
		}
	}

	@Test
	fun `JWT with incorrect type`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns arrayOf("GUEST")
			every { payload.subject } returns "1234567890"
			every { payload.audience } returns listOf("other.com", "aeon.api")

			val user = validateCredentials(credentials)

			user shouldBe beNull()
		}
	}

	@Test
	fun `JWT with incorrect audience`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns arrayOf("ACCESS")
			every { payload.subject } returns "1234567890"
			every { payload.audience } returns listOf("other.com")

			val user = validateCredentials(credentials)

			user shouldBe beNull()
		}
	}

	@Test
	fun `JWT with missing type claim`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns null
			every { payload.subject } returns "1234567890"
			every { payload.audience } returns listOf("other.com", "aeon.api")

			val user = validateCredentials(credentials)

			user shouldBe beNull()
		}
	}

	@Test
	fun `JWT with missing audience`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns arrayOf("ACCESS")
			every { payload.subject } returns "1234567890"
			every { payload.audience } returns null

			val user = validateCredentials(credentials)

			user shouldBe beNull()
		}
	}

	@Test
	fun `JWT with missing subject`(@MockK credentials: JWTCredential, @MockK payload: Payload, @MockK claim: Claim) {
		runBlocking {
			every { credentials.payload } returns payload
			every { payload.getClaim("type") } returns claim
			every { claim.asArray(String::class.java) } returns arrayOf("ACCESS")
			every { payload.subject } returns null
			every { payload.audience } returns listOf("other.com", "aeon.api")

			val user = validateCredentials(credentials)

			user shouldBe beNull()
		}
	}

}