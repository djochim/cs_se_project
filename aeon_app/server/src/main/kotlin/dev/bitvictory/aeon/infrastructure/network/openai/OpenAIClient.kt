package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.ktor.util.logging.KtorSimpleLogger

class OpenAIClient(private val openAI: OpenAI): SystemComponentHealthProvider {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override fun getName(): String = "OpenAI"

	override suspend fun getHealth(): SystemComponentHealth {
		logger.debug("Checking OpenAI health")
		try {
			val models = openAI.models()
			return if (models.isNotEmpty()) {
				SystemComponentHealth(getName(), UptimeStatus.UP)
			} else {
				SystemComponentHealth(getName(), UptimeStatus.DOWN, "No models found")
			}
		} catch (e: Exception) {
			logger.error("Error when checking OpenAI health.", e)
			return SystemComponentHealth(getName(), UptimeStatus.DOWN, "Error when checking OpenAI health. ${e.message}")
		}
	}
}