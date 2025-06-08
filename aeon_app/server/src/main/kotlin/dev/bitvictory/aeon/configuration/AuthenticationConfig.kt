package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.infrastructure.environment.AuthenticationEnvironment
import io.klogging.logger
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.jwt

/**
 * Configures JWT authentication for the Ktor application.
 *
 * This function installs the Ktor Authentication feature and configures it to use JWT (JSON Web Token)
 * for authenticating requests. It sets up the realm, verifier, and validation logic.
 *
 * The configuration relies on values provided by `AuthenticationEnvironment` for:
 * - `jwkRealm`: The realm string to be used in WWW-Authenticate headers.
 * - `jwkProvider`: The JWK (JSON Web Key) provider URL used to fetch public keys for signature verification.
 * - `jwkIssuer`: The expected issuer of the JWT.
 *
 * It also allows for a small leeway (3 seconds) in token expiration to account for clock skew.
 *
 * The `validateCredentials` function (not shown in this snippet) is responsible for any additional
 * validation of the decoded JWT payload (e.g., checking user existence, roles, etc.) and
 * returning a `Principal` if the credentials are valid, or `null` otherwise.
 */
fun Application.configureAuthentication() {
	install(Authentication) {
		jwt {
			realm = AuthenticationEnvironment.jwkRealm
			verifier(AuthenticationEnvironment.jwkProvider, AuthenticationEnvironment.jwkIssuer) {
				acceptLeeway(3)
			}
			validate { credentials ->
				validateCredentials(credentials)
			}
		}
	}
}

internal suspend fun validateCredentials(credentials: JWTCredential): User? {
	val logger = logger("Authentication")
	var validToken = true
	val payload = credentials.payload
	val userId = payload.subject
	val types = payload.getClaim("type").asArray(String::class.java)
	if (types == null || types.isEmpty()) {
		validToken = false
		logger.error("Token type is missing")
	} else if (!types.contains("ACCESS")) {
		validToken = false
		logger.error("Token type ${types.joinToString()} is invalid")
	}

	val audience = payload.audience
	if (audience == null || audience.isEmpty()) {
		validToken = false
		logger.error("Token audience missing")
	} else if (!audience.contains("aeon.api")) {
		validToken = false
		logger.error("Token audience ${audience.joinToString()} is invalid")
	}

	if (userId == null) {
		validToken = false
		logger.error("Token subject missing")
	}

	return if (validToken) {
		User(userId)
	} else {
		logger.error("Invalid Token")
		null
	}
}