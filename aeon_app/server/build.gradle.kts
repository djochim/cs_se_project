import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
	alias(libs.plugins.kotlinJvm)
	alias(libs.plugins.ktor)
	alias(libs.plugins.kover)
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

tasks.withType<Test> {
	useJUnitPlatform()
}

kover {
	reports {
		total {
			verify {
				rule("Minimal line coverage in percent") {
					minBound(47, aggregationForGroup = AggregationType.COVERED_PERCENTAGE, coverageUnits = CoverageUnit.LINE)
				}
				rule("Minimal banch coverage in percent") {
					minBound(45, aggregationForGroup = AggregationType.COVERED_PERCENTAGE, coverageUnits = CoverageUnit.BRANCH)
				}
			}
		}
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
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.ktor.server.core)
	implementation(libs.ktor.server.netty)
	implementation(libs.ktor.server.content)
	implementation(libs.ktor.server.websocket)
	implementation(libs.ktor.server.logging)
	implementation(libs.ktor.server.logging.calls)
	implementation(libs.ktor.server.auth.core)
	implementation(libs.ktor.server.auth.jwt)
	implementation(libs.ktor.server.statuspages)
	implementation(libs.ktor.otel)
	implementation(libs.mongo.kotlin.coroutine)
	implementation(libs.mongo.bson)
	implementation(libs.koin.core)
	implementation(libs.koin.ktor)
	implementation(libs.openai.client)
	implementation(libs.otel.sdk.configure)
	implementation(libs.otel.exporter.otlp)
	implementation(libs.otel.semconv)
	testImplementation(libs.ktor.server.tests)
	testImplementation(libs.kotlin.test)
	testImplementation(libs.junit.api)
	testImplementation(libs.junit.engine)
	testImplementation(libs.junit.params)
	testImplementation(libs.kotlin.test.junit)
	testImplementation(libs.kotest.assert)
	testImplementation(libs.mockk)
	testImplementation(libs.ktor.client.tests)
}
