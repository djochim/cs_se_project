package dev.bitvictory.aeon

import com.typesafe.config.ConfigFactory
import dev.bitvictory.aeon.application.applicationModule
import dev.bitvictory.aeon.presentation.api.system
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
	val serverConfig = HoconApplicationConfig(ConfigFactory.load())
	applicationEnvironment {
		log = LoggerFactory.getLogger("ktor.application")
		config = serverConfig
	}
	install(Koin) {
		modules(applicationModule())
	}

	install(ContentNegotiation) {
		protobuf()
	}

	routing {
		system()
	}
}