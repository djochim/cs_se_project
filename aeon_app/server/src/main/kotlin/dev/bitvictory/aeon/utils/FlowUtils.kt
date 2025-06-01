package dev.bitvictory.aeon.utils

import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

val logger = logger("FlowCollection")

fun <T> Flow<T>.collectIn(
	context: CoroutineContext = Dispatchers.IO,
	collectAction: suspend (T) -> Unit
): Job {
	return CoroutineScope(context).launch {
		collect { value ->
			try {
				collectAction(value)
			} catch (e: Exception) {
				logger.error(e)
			}
		}
	}
}