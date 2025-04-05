package dev.bitvictory.aeon.dependencyinjection

import dev.bitvictory.aeon.client.AeonApiClient
import dev.bitvictory.aeon.client.AuthClient
import dev.bitvictory.aeon.screens.HomeViewModel
import dev.bitvictory.aeon.screens.login.LoginViewModel
import dev.bitvictory.aeon.service.UserService
import dev.bitvictory.aeon.storage.SharedSettingsHelper
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
	appDeclaration()
	modules(
		platformModule,
		coreModule,
		clientModule,
		serviceModel,
		uiModule,
	)
}

private val coreModule = module {
	single {
		SharedSettingsHelper(
			get(named(SharedSettingsHelper.encryptedSettingsName))
		)
	}
}

private val clientModule = module {
	single { AuthClient("http://192.168.2.101:8070/v1") }
	single { AeonApiClient("http://192.168.2.101:8080", get()) }
}

private val serviceModel = module {
	single { UserService(get(), get()) }
}

private val uiModule = module {
	viewModel { HomeViewModel(get()) }
	viewModel { LoginViewModel(get()) }
}

expect val platformModule: Module