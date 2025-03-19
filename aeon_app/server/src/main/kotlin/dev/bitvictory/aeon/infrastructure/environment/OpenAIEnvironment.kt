package dev.bitvictory.aeon.infrastructure.environment

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object OpenAIEnvironment {

	private val appConfig by lazy { HoconApplicationConfig(ConfigFactory.load()) }

	val token by lazy { appConfig.property("openai.token").getString() }

}
