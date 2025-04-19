package dev.bitvictory.aeon.dependencyinjection

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import dev.bitvictory.aeon.storage.SharedSettingsHelper
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun startSdk() {
	initKoin {
		modules()
	}
}

@OptIn(ExperimentalSettingsImplementation::class)
actual val platformModule: Module = module {
	single<Settings>(named(SharedSettingsHelper.unencryptedSettingsName)) {
		NSUserDefaultsSettings.Factory().create(SharedSettingsHelper.DATABASE_NAME)
	}
	single<Settings>(named(SharedSettingsHelper.encryptedSettingsName)) {
		KeychainSettings(service = SharedSettingsHelper.ENCRYPTED_DATABASE_NAME)
	}
}