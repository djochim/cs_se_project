package dev.bitvictory.aeon.model.api.user

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@ExperimentalSerializationApi
@Serializable
data class UpdateUserRequest(
	val email: String? = null,
	val name: String? = null,
	val oldPassword: String? = null,
	val newPassword: String? = null,
	val profileImage: String? = null,
	val sendAnalyticalData: Boolean? = null,
	val acceptedPrivacyVersion: Int? = null,
)
