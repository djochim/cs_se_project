package dev.bitvictory.aeon.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.serialization.removeValue
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import kotlinx.serialization.ExperimentalSerializationApi

interface LocalKeyValueStore {
	var token: TokenDTO?
	var user: UserDTO?
}

class SharedSettingsHelper(
	private val encryptedSettings: Settings
): LocalKeyValueStore {
	@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
	override var token: TokenDTO?
		get() {
			return encryptedSettings.decodeValueOrNull(TokenDTO.serializer(), TOKEN_NAME)
		}
		set(value) {
			if (value == null) {
				encryptedSettings.removeValue<TokenDTO>(TOKEN_NAME)
				return
			}
			encryptedSettings.encodeValue(TokenDTO.serializer(), TOKEN_NAME, value)
		}

	@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
	override var user: UserDTO?
		get() {
			return encryptedSettings.decodeValueOrNull(UserDTO.serializer(), USER_NAME)
		}
		set(value) {
			if (value == null) {
				encryptedSettings.removeValue<UserDTO>(USER_NAME)
				return
			}
			encryptedSettings.encodeValue(UserDTO.serializer(), USER_NAME, value)
		}

	companion object {
		const val DATABASE_NAME = "UNENCRYPTED_SETTINGS"
		const val ENCRYPTED_DATABASE_NAME = "ENCRYPTED_SETTINGS"
		const val encryptedSettingsName = "encryptedSettings"
		const val unencryptedSettingsName = "unencryptedSettings"
		const val TOKEN_NAME = "TOKEN"
		const val USER_NAME = "USER"
	}
}