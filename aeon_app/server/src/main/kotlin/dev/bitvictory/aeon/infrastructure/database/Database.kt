package dev.bitvictory.aeon.infrastructure.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient

class Database(username: String, password: String, url: String, database: String) {
    private val connectionString = ConnectionString("mongodb://$username:$password@$url")
    private val clientSettings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .build()
    private val client = MongoClient.create(clientSettings)
    val value = client.getDatabase(database)
}