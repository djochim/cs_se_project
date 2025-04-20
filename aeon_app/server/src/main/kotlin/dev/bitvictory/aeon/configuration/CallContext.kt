package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.exceptions.AuthenticationException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal

data class UserContext(
	val user: User?,
	val token: String?
) {
	fun tokenOrThrow() = token ?: throw AuthenticationException("Auth token is not available in this scope")
}

fun ApplicationCall.userContext(): UserContext {
	val user = this.principal<User>()
	val userContext = UserContext(user, this.request.headers["Authorization"]?.apply { removePrefix("Bearer ") })
	return userContext
}