package dev.bitvictory.aeon.infrastructure

import dev.bitvictory.aeon.infrastructure.database.databaseModule
import dev.bitvictory.aeon.infrastructure.network.networkModule
import io.ktor.server.application.ApplicationEnvironment
import org.koin.dsl.module

fun infrastructureModule(environment: ApplicationEnvironment) = module {
    includes(networkModule(environment))
    includes(databaseModule)
}