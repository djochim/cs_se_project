package dev.bitvictory.aeon.screens.recipe.detail

import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import dev.bitvictory.aeon.service.IRecipeService
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Recipe Detail screen.
 *
 * This ViewModel is responsible for fetching and managing the data for a specific recipe,
 * including its header information and full details. It communicates with the `IRecipeService`
 * to retrieve recipe data and updates the UI state accordingly. It also handles potential
 * errors during data fetching and exposes a snackbar event for displaying error messages.
 *
 * @param recipeHeader The [RecipeHeaderDTO] containing basic information about the recipe
 *                     to be displayed. This is used for initial display while the full recipe
 *                     details are being loaded.
 * @param recipeService An instance of [IRecipeService] used to fetch recipe details.
 * @param userService An instance of [IUserService] used by the [AbstractViewModel] for
 *                    user-related operations (though not directly used in this specific ViewModel).
 */
class RecipeDetailViewModel(recipeHeader: RecipeHeaderDTO, private val recipeService: IRecipeService, userService: IUserService):
	AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(RecipeDetailUIState(recipeHeader))
	val uiState: StateFlow<RecipeDetailUIState> = _uiState.asStateFlow()

	private val _snackbarEvent = MutableSharedFlow<String>()
	val snackbarEvent = _snackbarEvent.asSharedFlow()

	init {
		loadRecipe()
	}

	private fun loadRecipe() {
		viewModelScope.launch {
			when (val recipesResponse = recipeService.getRecipe(uiState.value.recipeHeader.id)) {
				is AeonSuccessResponse -> {
					_uiState.value = _uiState.value.copy(
						recipe = recipesResponse.data
					)
				}

				is AeonErrorResponse   -> {
					_snackbarEvent.emit(recipesResponse.error.message)
				}
			}
		}
	}

}