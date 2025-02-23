package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.usecases.system.ProvideSystemInformation
import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.entities.system.SystemHealth
import dev.bitvictory.aeon.model.api.system.SystemComponentHealthDTO
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.system() {
	route("/system") {
		val provideSystemInformation: ProvideSystemInformation by inject()
		route("/health") {
			get {
				val healthStatus = provideSystemInformation.getHealthStatus()
				call.respond(HttpStatusCode.OK, healthStatus.toDTO())
			}
		}
	}
}

fun SystemHealth.toDTO() = SystemHealthDTO(this.status, this.components.map { it.toDTO() })

fun SystemComponentHealth.toDTO() = SystemComponentHealthDTO(this.name, this.status, this.message)
