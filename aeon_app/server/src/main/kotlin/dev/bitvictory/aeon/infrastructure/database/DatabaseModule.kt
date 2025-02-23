package dev.bitvictory.aeon.infrastructure.database

import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.database.repository.DBHealthCollection
import org.koin.dsl.bind
import org.koin.dsl.module

val username = "user"
val password = "pass"
val url = "localhost:27017"
val databaseName = "surlive"

val databaseModule = module {
	single { Database(username, password, url, databaseName) }
	single { DBHealthCollection(get()) }.bind(SystemComponentHealthProvider::class)
}