package dev.bitvictory.aeon.application.usecases.system

import dev.bitvictory.aeon.core.domain.entities.system.SystemHealth

interface ProvideSystemInformation {

	suspend fun getHealthStatus(): SystemHealth

}