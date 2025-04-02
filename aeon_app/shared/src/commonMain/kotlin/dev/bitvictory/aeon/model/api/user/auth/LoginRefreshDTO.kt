package dev.bitvictory.aeon.model.api.user.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRefreshDTO(val refreshToken: String)