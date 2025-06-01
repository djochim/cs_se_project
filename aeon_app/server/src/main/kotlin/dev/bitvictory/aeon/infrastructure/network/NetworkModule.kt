package dev.bitvictory.aeon.infrastructure.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import dev.bitvictory.aeon.core.domain.entities.assistant.HealthAssistant
import dev.bitvictory.aeon.core.domain.usecases.assistant.AsyncAssistantExecution
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.core.domain.usecases.user.PersonaDataProvider
import dev.bitvictory.aeon.infrastructure.environment.AuthenticationEnvironment
import dev.bitvictory.aeon.infrastructure.environment.OpenAIEnvironment
import dev.bitvictory.aeon.infrastructure.network.bitauth.AuthClient
import dev.bitvictory.aeon.infrastructure.network.openai.AssistantService
import dev.bitvictory.aeon.infrastructure.network.openai.OpenAIClient
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

fun networkModule(): Module = module {
	single {
		OpenAIConfig(
			token = OpenAIEnvironment.token,
			timeout = Timeout(socket = 60.seconds),
			logging = LoggingConfig(
				logger = Logger.Default,
				logLevel = LogLevel.All
			)
		)
	}
	single {
		OpenAI(config = get())
	}
	single { OpenAIClient(get()) }.bind(SystemComponentHealthProvider::class)
	single { AuthClient(AuthenticationEnvironment.iamUrl) }.bind(PersonaDataProvider::class)
	single { AssistantService(get(), listOf(HealthAssistant)) }.bind(AsyncAssistantExecution::class)

}