package dev.bitvictory.aeon.core.domain.usecases.system

import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth

interface SystemComponentHealthProvider {

	fun getName(): String

	suspend fun getHealth(): SystemComponentHealth

}