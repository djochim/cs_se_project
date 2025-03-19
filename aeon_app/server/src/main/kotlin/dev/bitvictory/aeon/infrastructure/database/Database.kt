package dev.bitvictory.aeon.infrastructure.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.bitvictory.aeon.infrastructure.environment.DatabaseEnvironment

class Database {
	private val connectionString =
		ConnectionString("mongodb://${DatabaseEnvironment.user}:${DatabaseEnvironment.password}@${DatabaseEnvironment.url}")
	private val clientSettings = MongoClientSettings.builder()
		.applyConnectionString(connectionString)
		.build()
	private val client = MongoClient.create(clientSettings)
	val value = client.getDatabase(DatabaseEnvironment.database)
}