package dev.bitvictory.aeon.application.service.actioncall

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun actionCallModule(): Module = module {
	single { StoreRecipeProcessor(get(), get()) } bind AeonActionProcessor::class

	single { ActionCallDispatcher(getAll()) }
}