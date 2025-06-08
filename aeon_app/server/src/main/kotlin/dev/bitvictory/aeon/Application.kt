package dev.bitvictory.aeon

import com.typesafe.config.ConfigFactory
import dev.bitvictory.aeon.application.applicationModule
import dev.bitvictory.aeon.configuration.LoggingConfig
import dev.bitvictory.aeon.configuration.configureAuthentication
import dev.bitvictory.aeon.configuration.configureContentNegotiation
import dev.bitvictory.aeon.configuration.configureErrorHandling
import dev.bitvictory.aeon.configuration.configureObservability
import dev.bitvictory.aeon.presentation.api.advisories
import dev.bitvictory.aeon.presentation.api.recipes
import dev.bitvictory.aeon.presentation.api.system
import dev.bitvictory.aeon.presentation.api.user
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
	val serverConfig = HoconApplicationConfig(ConfigFactory.load())
	applicationEnvironment {
		log = LoggerFactory.getLogger("ktor.application")
		config = serverConfig
	}

	LoggingConfig.configure()

	configureObservability()

	install(Koin) {
		modules(applicationModule())
	}

	configureErrorHandling()
	configureContentNegotiation()
	configureAuthentication()

	routing {
		system()
		user()
		advisories()
		recipes()
	}
}
