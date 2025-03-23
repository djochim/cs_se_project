package dev.bitvictory.aeon.infrastructure.environment

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk

object OtelEnvironment {

	const val serviceName = "otel-aeon-server"
	val openTelemetry: OpenTelemetry = getOpenTelemetry(serviceName)

	private fun getOpenTelemetry(serviceName: String): OpenTelemetry {
		return AutoConfiguredOpenTelemetrySdk.builder().addResourceCustomizer { oldResource, _ ->
			oldResource.toBuilder()
				.putAll(oldResource.attributes)
				.put("service.name", serviceName)
				.put("deployment.environment", "staging")
				.build()
		}.build().openTelemetrySdk
	}

}
