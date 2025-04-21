package dev.bitvictory.aeon.application

import dev.bitvictory.aeon.application.service.SystemInformationService
import dev.bitvictory.aeon.application.service.UserService
import dev.bitvictory.aeon.application.usecases.system.ProvideSystemInformation
import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.infrastructure.infrastructureModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun applicationModule(): Module = module {
	includes(infrastructureModule())
	single { SystemInformationService(getAll()) }.bind(ProvideSystemInformation::class)
	single { UserService(getAll()) }.bind(ManagePersonalData::class)
}