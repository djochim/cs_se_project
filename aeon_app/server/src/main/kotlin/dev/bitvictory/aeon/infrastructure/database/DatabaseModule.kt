package dev.bitvictory.aeon.infrastructure.database

import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.database.repository.DBHealthCollection
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
	single { Database() }
	single { DBHealthCollection(get()) }.bind(SystemComponentHealthProvider::class)
}