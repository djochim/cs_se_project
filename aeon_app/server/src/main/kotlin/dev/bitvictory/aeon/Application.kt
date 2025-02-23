package dev.bitvictory.aeon

import dev.bitvictory.aeon.application.applicationModule
import dev.bitvictory.aeon.presentation.api.system
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
	install(Koin) {
		modules(applicationModule(environment))
	}

	install(ContentNegotiation) {
		protobuf()
	}

	routing {
		system()
	}
}