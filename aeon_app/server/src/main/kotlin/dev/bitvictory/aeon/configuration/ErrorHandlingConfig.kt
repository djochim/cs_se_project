package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.exceptions.authenticationStatusHandling
import dev.bitvictory.aeon.exceptions.conflictStatusHandling
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages

fun Application.configureErrorHandling() {
	install(StatusPages) {
		authenticationStatusHandling()
		conflictStatusHandling()
	}
}