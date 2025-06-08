package dev.bitvictory.aeon.dependencyinjection

import dev.bitvictory.aeon.client.AuthClient
import dev.bitvictory.aeon.client.IAMApi
import dev.bitvictory.aeon.client.aeon.AeonApi
import dev.bitvictory.aeon.client.aeon.AeonApiClient
import dev.bitvictory.aeon.client.aeon.AeonHttpClientFactory
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import dev.bitvictory.aeon.screens.chat.ChatViewModel
import dev.bitvictory.aeon.screens.home.HomeViewModel
import dev.bitvictory.aeon.screens.login.LoginViewModel
import dev.bitvictory.aeon.screens.privacyinfo.PrivacyInformationViewModel
import dev.bitvictory.aeon.screens.profile.ProfileViewModel
import dev.bitvictory.aeon.screens.recipe.RecipesViewModel
import dev.bitvictory.aeon.screens.recipe.detail.RecipeDetailViewModel
import dev.bitvictory.aeon.service.AdvisorService
import dev.bitvictory.aeon.service.IAdvisorService
import dev.bitvictory.aeon.service.IPrivacyService
import dev.bitvictory.aeon.service.IRecipeService
import dev.bitvictory.aeon.service.IUserService
import dev.bitvictory.aeon.service.PrivacyService
import dev.bitvictory.aeon.service.RecipeService
import dev.bitvictory.aeon.service.UserService
import dev.bitvictory.aeon.storage.LocalKeyValueStore
import dev.bitvictory.aeon.storage.SharedSettingsHelper
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Initializes Koin for dependency injection.
 *
 * This function sets up the Koin application by loading necessary modules.
 * It allows for custom Koin application configuration via the `appDeclaration` parameter.
 *
 * The following modules are loaded by default:
 * - `platformModule`: Contains platform-specific dependencies (e.g., Android, iOS).
 * - `coreModule`: Provides core application logic and utilities.
 * - `clientModule`: Handles network communication and API client setup.
 * - `serviceModel`: Defines data models and services for interacting with the backend.
 * - `uiModule`: Contains UI-related components and view models.
 *
 * @param appDeclaration A lambda function to further configure the Koin application.
 *                       This allows for adding custom modules, properties, or other
 *                       Koin-specific configurations. Defaults to an empty lambda.
 * @return The started Koin application instance.
 */
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
	} bind LocalKeyValueStore::class
}

private val clientModule = module {
	single { AuthClient("http://192.168.178.75:8070/v1", get()) } bind IAMApi::class
	single { AeonApiClient("http://192.168.178.75:8080", AeonHttpClientFactory.create(get(IUserService::class))) } bind AeonApi::class
}

private val serviceModel = module {
	single { UserService(get(), get()) } bind IUserService::class
	single { PrivacyService(get()) } bind IPrivacyService::class
	single { AdvisorService(get()) } bind IAdvisorService::class
	single { RecipeService(get()) } bind IRecipeService::class
}

private val uiModule = module {
	viewModel { HomeViewModel(get()) }
	viewModel { LoginViewModel(get()) }
	viewModel { ProfileViewModel(get()) }
	viewModel { PrivacyInformationViewModel(get(), get()) }
	viewModel { ChatViewModel(get(), get()) }
	viewModel { RecipesViewModel(get(), get()) }
	viewModel { (recipeDetail: RecipeHeaderDTO) -> RecipeDetailViewModel(recipeDetail, get(), get()) }
}

expect val platformModule: Module