package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.usecases.system.ProvideSystemInformation
import dev.bitvictory.aeon.core.domain.entities.system.SystemHealth
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider

class SystemInformationService(private val systemComponentHealthProviders: List<SystemComponentHealthProvider>): ProvideSystemInformation {

	override suspend fun getHealthStatus(): SystemHealth = SystemHealth(systemComponentHealthProviders.map { it.getHealth() })

}