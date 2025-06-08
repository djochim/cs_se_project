package dev.bitvictory.aeon.configuration

import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Configures content negotiation for the application.
 *
 * This function installs the `ContentNegotiation` plugin, which allows the application
 * to serialize and deserialize data in different formats based on the client's request
 * (e.g., using `Accept` and `Content-Type` headers).
 *
 * In this specific configuration, it enables Protocol Buffers (`protobuf`) serialization.
 * This means the application will be able to handle requests and responses formatted
 * as Protocol Buffers.
 *
 * The `@OptIn(ExperimentalSerializationApi::class)` annotation is used because
 * the `protobuf()` extension is part of an experimental serialization API.
 */
@OptIn(ExperimentalSerializationApi::class) fun Application.configureContentNegotiation() {
	install(ContentNegotiation) {
		protobuf()
	}
}