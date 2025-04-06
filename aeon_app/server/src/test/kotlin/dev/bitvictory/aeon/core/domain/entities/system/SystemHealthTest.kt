package dev.bitvictory.aeon.core.domain.entities.system

import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SystemHealthTest {

	@Test
	fun `Overall health is UP no components are there`() {
		val systemHealth = SystemHealth(listOf())
		systemHealth.status shouldBe UptimeStatus.UP
	}

	@Test
	fun `Overall health is UP when all components are UP`() {
		val systemHealth = SystemHealth(listOf(SystemComponentHealth("A", UptimeStatus.UP), SystemComponentHealth("B", UptimeStatus.UP)))
		systemHealth.status shouldBe UptimeStatus.UP
	}

	@Test
	fun `Overall health is DOWN when one component is DOWN`() {
		val systemHealth = SystemHealth(listOf(SystemComponentHealth("A", UptimeStatus.UP), SystemComponentHealth("B", UptimeStatus.DOWN)))
		systemHealth.status shouldBe UptimeStatus.DOWN
	}

}