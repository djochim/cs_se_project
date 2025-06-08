package dev.bitvictory.aeon.screens.recipe

import androidx.lifecycle.viewModelScope
import dev.bitvictory.aeon.components.AbstractViewModel
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.primitive.Page
import dev.bitvictory.aeon.service.IRecipeService
import dev.bitvictory.aeon.service.IUserService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(private val recipeService: IRecipeService, userService: IUserService): AbstractViewModel(userService) {
	private val _uiState = MutableStateFlow(RecipesUIState())
	val uiState: StateFlow<RecipesUIState> = _uiState.asStateFlow()

	private val _snackbarEvent = MutableSharedFlow<String>()
	val snackbarEvent = _snackbarEvent.asSharedFlow()

	init {
		loadRecipes(Page(), null)
	}

	fun onSearchQueryChange(query: String) {
		_uiState.value = _uiState.value.copy(searchQuery = query)
		if (query.length > 3) {
			loadRecipes(Page(), query)
		}
		if (query.isBlank()) {
			loadRecipes(Page(), null)
		}
	}

	private fun loadRecipes(page: Page, searchQuery: String?) {
		viewModelScope.launch {
			when (val recipesResponse = recipeService.getRecipes(page, searchQuery)) {
				is AeonSuccessResponse -> {
					_uiState.value = _uiState.value.copy(
						recipes = recipesResponse.data.items
					)
				}

				is AeonErrorResponse   -> {
					_snackbarEvent.emit(recipesResponse.error.message)
				}
			}
		}
	}

}