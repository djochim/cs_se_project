package dev.bitvictory.aeon.model.api.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(val email: String, val name: String, val accountType: String, val sendAnalyticalData: Boolean, val acceptedPrivacyVersion: Int)