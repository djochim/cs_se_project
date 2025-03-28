package dev.bitvictory.aeon.configuration

import io.klogging.context.Context

object LoggingConfig {

	const val APP_NAME_KEY = "appName"
	const val APP_NAME = "aeon-service"

	fun configure() {
		Context.addBaseContext(
			APP_NAME_KEY to APP_NAME
		)
	}

}
