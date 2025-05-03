package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.entities.system.SystemHealth
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SystemInformationServiceTest {

	@Test
	fun `Successful retrieval with data`(@MockK systemDataProvider: SystemComponentHealthProvider) = runTest {
		val systemData = SystemComponentHealth(
			"api", UptimeStatus.UP
		)
		val systemInformationService = SystemInformationService(listOf(systemDataProvider))
		coEvery { systemDataProvider.getHealth() } returns systemData

		val result = systemInformationService.getHealthStatus()

		result shouldBe SystemHealth(listOf(systemData))
	}

	@Test
	fun `Empty data providers`() = runTest {
		val systemInformationService = SystemInformationService(emptyList())

		val result = systemInformationService.getHealthStatus()

		result shouldBe SystemHealth(emptyList())
	}

	@Test
	fun `Multiple data providers`(
		@MockK systemDataProvider1: SystemComponentHealthProvider,
		@MockK systemDataProvider2: SystemComponentHealthProvider,
	) = runTest {
		val systemData1 = SystemComponentHealth(
			"api", UptimeStatus.UP
		)
		val systemData2 = SystemComponentHealth(
			"api", UptimeStatus.UP
		)
		val systemInformationService = SystemInformationService(listOf(systemDataProvider1, systemDataProvider2))
		coEvery { systemDataProvider1.getHealth() } returns systemData1
		coEvery { systemDataProvider2.getHealth() } returns systemData2

		val result = systemInformationService.getHealthStatus()

		result shouldBe SystemHealth(listOf(systemData1, systemData2))
	}

}