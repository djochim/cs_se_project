package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.entities.system.SystemHealth
import dev.bitvictory.aeon.model.api.system.SystemComponentHealthDTO
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SystemApiTest {

	@Test
	fun `System Health converts to DTO and UP`() {
		val systemData1 = SystemComponentHealth(
			"api", UptimeStatus.UP
		)
		val systemData2 = SystemComponentHealth(
			"api2", UptimeStatus.UP
		)
		val systemHealth = SystemHealth(listOf(systemData1, systemData2))

		val result = systemHealth.toDTO()

		result shouldBe SystemHealthDTO(
			UptimeStatus.UP,
			listOf(SystemComponentHealthDTO("api", UptimeStatus.UP), SystemComponentHealthDTO("api2", UptimeStatus.UP))
		)
	}

	@Test
	fun `System Health converts to DTO and DOWN`() {
		val systemData1 = SystemComponentHealth(
			"api", UptimeStatus.UP
		)
		val systemData2 = SystemComponentHealth(
			"api2", UptimeStatus.DOWN
		)
		val systemHealth = SystemHealth(listOf(systemData1, systemData2))

		val result = systemHealth.toDTO()

		result shouldBe SystemHealthDTO(
			UptimeStatus.DOWN,
			listOf(SystemComponentHealthDTO("api", UptimeStatus.UP), SystemComponentHealthDTO("api2", UptimeStatus.DOWN))
		)
	}

}