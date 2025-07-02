package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.infrastructure.environment.OtelEnvironment
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

/**
 * Configures observability for the application.
 *
 * This function installs two plugins:
 *
 * 1.  **CallLogging**: This plugin logs incoming requests.
 *     *   It formats log messages to include the HTTP method, request path, and response status.
 *         Example: `GET /example/path -> 200 OK`
 *     *   It adds the following information to the MDC (Mapped Diagnostic Context) for structured logging:
 *         *   `method`: The HTTP method of the request (e.g., GET, POST).
 *         *   `path`: The path of the request (e.g., /users/123).
 *         *   `traceId`: The OpenTelemetry trace ID for correlating logs with traces.
 *         *   `spanId`: The OpenTelemetry span ID for correlating logs with specific spans in a trace.
 *
 * 2.  **KtorServerTelemetry**: This plugin integrates OpenTelemetry for distributed tracing and metrics.
 *     *   It sets the OpenTelemetry instance to be used, which is obtained from `OtelEnvironment.openTelemetry`.
 *       This allows Ktor to automatically create spans for incoming requests and propagate tracing context.
 */
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