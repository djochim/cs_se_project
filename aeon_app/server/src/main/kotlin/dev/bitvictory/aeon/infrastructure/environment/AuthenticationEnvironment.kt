package dev.bitvictory.aeon.infrastructure.environment

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.typesafe.config.ConfigFactory
import io.ktor.http.URLBuilder
import io.ktor.server.config.HoconApplicationConfig
import java.net.*
import java.util.concurrent.*

object AuthenticationEnvironment {

	private val appConfig by lazy { HoconApplicationConfig(ConfigFactory.load()) }
	private val jwkUrl by lazy {
		appConfig.property("auth.jwkUrl").getString()
	}
	const val jwkIssuer = "aeon.app"
	val jwkRealm by lazy { "realm" }
	val jwkProvider by lazy { createJwkProvider() }

	private const val CACHE_SIZE = 10L
	private const val EXPIRES_IN = 24L
	private const val BUCKET_SIZE = 10L
	private const val REFILL_RATE = 1L

	private fun createJwkProvider(): JwkProvider {
		val jwkIssuer = URLBuilder(jwkUrl).buildString()
		return JwkProviderBuilder(URI(jwkIssuer).toURL())
			.cached(CACHE_SIZE, EXPIRES_IN, TimeUnit.HOURS)
			.rateLimited(BUCKET_SIZE, REFILL_RATE, TimeUnit.MINUTES)
			.build()
	}

}
