package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class OpenAIClientTest {

	@MockK
	lateinit var openAI: OpenAI

	@InjectMockKs
	lateinit var openAIClient: OpenAIClient

	@Nested
	inner class HealthCheckTests {
		@Test
		fun `Successful health check with models`() {
			runBlocking {
				val model = mockk<Model>()
				val models = listOf(model)
				coEvery { openAI.models() } returns models

				val result = openAIClient.getHealth()

				coVerify {
					openAI.models()
				}
				result shouldBe SystemComponentHealth(openAIClient.getName(), UptimeStatus.UP)
			}
		}

		@Test
		fun `Successful health check with no models result in downtime`() {
			runBlocking {
				val models = listOf<Model>()
				coEvery { openAI.models() } returns models

				val result = openAIClient.getHealth()

				coVerify {
					openAI.models()
				}
				result.status shouldBe UptimeStatus.DOWN
			}
		}

		@Test
		fun `API call failure`() {
			runBlocking {
				coEvery { openAI.models() } throws Exception("API call failed")

				val result = openAIClient.getHealth()

				result.status shouldBe UptimeStatus.DOWN
			}
		}
	}

}