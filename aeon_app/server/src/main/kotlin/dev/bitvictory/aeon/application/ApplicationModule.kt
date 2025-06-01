package dev.bitvictory.aeon.application

import dev.bitvictory.aeon.application.service.AdvisoryService
import dev.bitvictory.aeon.application.service.SystemInformationService
import dev.bitvictory.aeon.application.service.UserService
import dev.bitvictory.aeon.application.service.actioncall.actionCallModule
import dev.bitvictory.aeon.application.service.eventhandling.eventProcessorModule
import dev.bitvictory.aeon.application.usecases.advise.AdviseUser
import dev.bitvictory.aeon.application.usecases.system.ProvideSystemInformation
import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.infrastructure.infrastructureModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun applicationModule(): Module = module {
	includes(infrastructureModule(), actionCallModule(), eventProcessorModule())
	single { SystemInformationService(getAll()) }.bind(ProvideSystemInformation::class)
	single { UserService(getAll()) }.bind(ManagePersonalData::class)
	single { AdvisoryService(get(), get(), get(), get()) }.bind(AdviseUser::class)
}