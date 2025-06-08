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