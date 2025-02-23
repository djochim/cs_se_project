package dev.bitvictory.aeon.application

import dev.bitvictory.aeon.application.service.SystemInformationService
import dev.bitvictory.aeon.application.usecases.system.ProvideSystemInformation
import dev.bitvictory.aeon.infrastructure.infrastructureModule
import io.ktor.server.application.ApplicationEnvironment
import org.koin.dsl.bind
import org.koin.dsl.module

fun applicationModule(environment: ApplicationEnvironment) = module {
	includes(infrastructureModule(environment))
	single { SystemInformationService(getAll()) }.bind(ProvideSystemInformation::class)
}