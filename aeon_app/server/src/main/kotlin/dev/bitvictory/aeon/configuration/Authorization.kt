package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.exceptions.AuthenticationException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal

fun ApplicationCall.userPrincipal(): User {
	val user = this.principal<User>()
	if (user == null) {
		throw AuthenticationException("Authenticated user object could not be found")
	} else {
		return user
	}
}