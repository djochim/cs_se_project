package dev.bitvictory.aeon.infrastructure

import dev.bitvictory.aeon.infrastructure.database.databaseModule
import dev.bitvictory.aeon.infrastructure.network.networkModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun infrastructureModule(): Module = module {
	includes(networkModule())
	includes(databaseModule)
}