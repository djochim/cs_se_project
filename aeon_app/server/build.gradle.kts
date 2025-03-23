plugins {
	alias(libs.plugins.kotlinJvm)
	alias(libs.plugins.ktor)
	application
	kotlin("plugin.serialization") version "2.0.0"
}

group = "dev.bitvictory.aeon"
version = "1.0.0"
application {
	mainClass.set("io.ktor.server.netty.EngineMain")

	applicationDefaultJvmArgs =
		listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

ktor {
	fatJar {
		archiveFileName.set("aeon.jar")
	}
}

dependencies {
	implementation(platform(libs.otel.bom))
	api(projects.shared)
	api(libs.kotlinx.datetime)
	api(libs.kotlinx.serialization)
	api(libs.ktor.client.core)
	api(libs.ktor.client.content)
	api(libs.ktor.client.okhttp)
	implementation(libs.logback)
	implementation(libs.ktor.server.core)
	implementation(libs.ktor.server.netty)
	implementation(libs.ktor.server.content)
	implementation(libs.ktor.server.websocket)
	implementation(libs.ktor.otel)
	implementation(libs.mongo.kotlin.coroutine)
	implementation(libs.mongo.bson)
	implementation(libs.koin.ktor)
	implementation(libs.logging.core)
	implementation(libs.logging.impl)
	implementation(libs.openai.client)
	implementation(libs.otel.sdk.configure)
	implementation(libs.otel.exporter.otlp)
	implementation(libs.otel.semconv)
	testImplementation(libs.ktor.server.tests)
	testImplementation(libs.kotlin.test)
}