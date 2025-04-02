package dev.bitvictory.aeon.model.api.user.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenDTO(val userId: String, val accessToken: String, val refreshToken: String)