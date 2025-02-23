package dev.bitvictory.aeon.infrastructure.database.repository

import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.ktor.util.logging.KtorSimpleLogger
import org.bson.BsonDocument
import org.bson.BsonInt64

class DBHealthCollection(database: Database): SystemComponentHealthProvider {

	private val database = database.value
	private val logger = KtorSimpleLogger(this.javaClass.name)

	override fun getName(): String = "mongo"

	override suspend fun getHealth(): SystemComponentHealth {
		logger.debug("Checking mongo database health")
		try {
			val command = BsonDocument("dbStats", BsonInt64(1))
			val status = database.runCommand(command).getOrDefault("ok", -1).let { it as Double }
			return if (status == 1.0) {
				SystemComponentHealth("mongo", UptimeStatus.UP)
			} else {
				SystemComponentHealth("mongo", UptimeStatus.DOWN, "Database status: $status")
			}
		} catch (e: Exception) {
			logger.error("Error when checking mongo database health", e)
			return SystemComponentHealth("mongo", UptimeStatus.DOWN, "Error when checking mongo database health. ${e.message}")
		}
	}

}