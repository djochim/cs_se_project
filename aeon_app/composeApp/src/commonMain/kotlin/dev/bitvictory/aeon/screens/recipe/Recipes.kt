package dev.bitvictory.aeon.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object Recipes

fun NavGraphBuilder.recipesDestination(onRecipeClick: (recipeHeader: RecipeHeaderDTO) -> Unit) {
	composable<Recipes> { RecipesScreen(onRecipeClick) }
}

fun NavController.navigateToRecipes() {
	navigate(route = Recipes)
}

/**
 * Composable function that displays the main screen of the Recipes application.
 *
 * This screen includes a search bar to filter recipes and a list of available recipes.
 * It observes the UI state from the [RecipesViewModel] to update the displayed data.
 * It also handles showing snackbar messages for events like errors or information.
 *
 * @param onRecipeClick A lambda function that is invoked when a recipe is clicked.
 *                      It receives the [RecipeHeaderDTO] of the clicked recipe as a parameter.
 * @param recipesViewModel An instance of [RecipesViewModel] that provides the UI state
 *                         and handles business logic for this screen. Defaults to an instance
 *                         injected by Koin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(onRecipeClick: (recipeHeader: RecipeHeaderDTO) -> Unit, recipesViewModel: RecipesViewModel = koinInject()) {
	val uiState = recipesViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	LaunchedEffect(key1 = recipesViewModel.snackbarEvent) {
		recipesViewModel.snackbarEvent.collect { message ->
			snackbarHostState.showSnackbar(message)
		}
	}
	Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
		Column(modifier = Modifier.padding(innerPadding)) {
			Box(Modifier.fillMaxSize().semantics { isTraversalGroup = true }) {
				DockedSearchBar(
					modifier =
						Modifier.align(Alignment.TopCenter).padding(top = 8.dp).semantics {
							traversalIndex = 0f
						},
					inputField = {
						SearchBarDefaults.InputField(
							query = uiState.value.searchQuery,
							onQueryChange = { query ->
								recipesViewModel.onSearchQueryChange(query)
							},
							onSearch = {

							},
							expanded = false,
							onExpandedChange = { },
							placeholder = { Text("Search ..") },
							leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
						)
					},
					expanded = false,
					onExpandedChange = { },
				) {}
				RecipeList(uiState.value.recipes, onRecipeClick)
			}
		}
	}
}

@Composable
fun RecipeList(recipes: List<RecipeHeaderDTO>, onRecipeClick: (recipeHeader: RecipeHeaderDTO) -> Unit) {
	LazyColumn(
		contentPadding = PaddingValues(
			top = 72.dp,
		),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.semantics { traversalIndex = 1f },
	) {
		recipes.forEach {
			item {
				RecipeItem(it) {
					onRecipeClick(it)
				}
			}
		}
	}
}

@Composable
fun RecipeItem(
	recipe: RecipeHeaderDTO,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
			.clickable { onClick() },
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(
				text = recipe.name,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = recipe.description,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}