package dev.bitvictory.aeon.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import kotlinx.serialization.ExperimentalSerializationApi

class SharedSettingsHelper(
	private val encryptedSettings: Settings
) {
	@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
	var token: TokenDTO?
		get() {
			return encryptedSettings.decodeValueOrNull(TokenDTO.serializer(), TOKEN_NAME)
		}
		set(value) {
			if (value == null) {
				encryptedSettings.remove(TOKEN_NAME)
				return
			}
			encryptedSettings.encodeValue(TokenDTO.serializer(), TOKEN_NAME, value)
		}

	companion object {
		const val DATABASE_NAME = "UNENCRYPTED_SETTINGS"
		const val ENCRYPTED_DATABASE_NAME = "ENCRYPTED_SETTINGS"
		const val encryptedSettingsName = "encryptedSettings"
		const val unencryptedSettingsName = "unencryptedSettings"
		const val TOKEN_NAME = "TOKEN"
	}
}