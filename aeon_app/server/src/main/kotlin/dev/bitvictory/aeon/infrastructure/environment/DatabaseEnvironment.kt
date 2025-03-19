package dev.bitvictory.aeon.infrastructure.environment

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object DatabaseEnvironment {

	private val appConfig by lazy { HoconApplicationConfig(ConfigFactory.load()) }

	val user by lazy { appConfig.property("db.user").getString() }
	val password by lazy { appConfig.property("db.password").getString() }
	val url by lazy { appConfig.property("db.url").getString() }
	val database by lazy { appConfig.property("db.database").getString() }

}
