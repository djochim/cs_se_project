package dev.bitvictory.aeon.model.api.user.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginDTO(val email: String, val password: String)