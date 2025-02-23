package dev.bitvictory.aeon.infrastructure.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.network.openai.OpenAIClient
import io.ktor.server.application.ApplicationEnvironment
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

const val OPEN_AI_TOKEN_PROPERTY = "ktor.openai.token"
fun networkModule(environment: ApplicationEnvironment) = module {
	single {
		OpenAIConfig(
			token = environment.config.property(OPEN_AI_TOKEN_PROPERTY).getString(),
			timeout = Timeout(socket = 60.seconds)
		)
	}
	single {
		OpenAI(config = get())
	}
	single { OpenAIClient(get()) }.bind(SystemComponentHealthProvider::class)
}