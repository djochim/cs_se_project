package dev.bitvictory.aeon.application.service.actioncall

import kotlinx.serialization.json.Json

val FunctionInterfaceJson = Json {
	encodeDefaults = true
	ignoreUnknownKeys = true
	isLenient = true
}