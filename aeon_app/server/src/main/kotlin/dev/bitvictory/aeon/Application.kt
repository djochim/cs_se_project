package dev.bitvictory.aeon

import com.typesafe.config.ConfigFactory
import dev.bitvictory.aeon.application.applicationModule
import dev.bitvictory.aeon.configuration.LoggingConfig
import dev.bitvictory.aeon.infrastructure.environment.OtelEnvironment
import dev.bitvictory.aeon.presentation.api.system
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
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
	LoggingConfig.configure()
	install(CallLogging) {
		format { call ->
			val status = call.response.status()
			val httpMethod = call.request.httpMethod.value
			"$httpMethod ${call.request.path()} -> $status"
		}
		mdc("method") { call ->
			call.request.httpMethod.value
		}
		mdc("path") { call ->
			call.request.path()
		}
		mdc("traceId") {
			Span.current().spanContext.traceId
		}
		mdc("spanId") {
			Span.current().spanContext.spanId
		}
	}

	install(Koin) {
		modules(applicationModule())
	}
	install(KtorServerTelemetry) {
		setOpenTelemetry(OtelEnvironment.openTelemetry)
	}

	install(ContentNegotiation) {
		protobuf()
	}

	routing {
		system()
	}
}
