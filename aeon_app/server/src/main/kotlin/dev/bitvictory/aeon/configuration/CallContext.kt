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

/**
 * Retrieves the authenticated user principal from the ApplicationCall.
 *
 * This extension function attempts to extract the `User` principal from the current `ApplicationCall`.
 * If a `User` principal is found (meaning the user has been successfully authenticated),
 * it is returned. Otherwise, if no authenticated user is found, an `AuthenticationException`
 * is thrown, indicating that the request could not be processed due to missing authentication.
 *
 * @receiver The `ApplicationCall` instance from which to retrieve the user principal.
 * @return The authenticated `User` object.
 * @throws AuthenticationException if no authenticated `User` principal is found in the call.
 *
 * @see io.ktor.server.auth.principal
 */
fun ApplicationCall.userPrincipal(): User {
	val user = this.principal<User>()
	if (user == null) {
		throw AuthenticationException("Authenticated user object could not be found")
	} else {
		return user
	}
}