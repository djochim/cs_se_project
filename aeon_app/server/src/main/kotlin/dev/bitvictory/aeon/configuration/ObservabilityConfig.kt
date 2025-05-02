package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.infrastructure.environment.OtelEnvironment
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

fun Application.configureObservability() {
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

	if (false) {
		install(KtorServerTelemetry) {
			setOpenTelemetry(OtelEnvironment.openTelemetry)
		}
	}

}