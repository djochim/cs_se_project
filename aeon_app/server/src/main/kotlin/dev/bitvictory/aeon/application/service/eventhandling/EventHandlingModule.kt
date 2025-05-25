package dev.bitvictory.aeon.application.service.eventhandling

import org.koin.core.module.Module
import org.koin.dsl.module

fun eventProcessorModule(): Module = module {
	single { ThreadEventProcessor(get()) }
	single { MessageEventProcessor(get()) }
	single { MessageDeltaEventProcessor(get()) }
	single { RunEventProcessor(get()) }
	single { RunStepEventProcessor(get()) }
	single { RunStepDeltaEventProcessor(get()) }
	single { DoneEventProcessor(get()) }
	single { ErrorEventProcessor(get()) }
	single { UnknownEventProcessor(get()) }

	single { AdvisoryEventDispatcher(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}