package dev.bitvictory.aeon.infrastructure.environment

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk

object OtelEnvironment {

	private val appConfig by lazy { HoconApplicationConfig(ConfigFactory.load()) }
	const val serviceName = "otel-aeon-server"
	val openTelemetry: OpenTelemetry = getOpenTelemetry(serviceName)

	val enabled by lazy { appConfig.property("otel.enabled").getString().toBoolean() }

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
