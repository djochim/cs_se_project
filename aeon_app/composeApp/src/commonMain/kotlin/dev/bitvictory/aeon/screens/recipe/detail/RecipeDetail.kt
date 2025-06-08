package dev.bitvictory.aeon.screens.recipe.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.bitvictory.aeon.model.api.recipes.IngredientDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import dev.bitvictory.aeon.model.common.dto.StepDTO
import dev.bitvictory.aeon.model.common.dto.TipDTO
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

@Serializable
data class RecipeDetail(val recipeHeader: RecipeHeaderDTO)

fun NavGraphBuilder.recipeDetailDestination(onClose: () -> Unit = {}) {
	composable<RecipeDetail>(typeMap = mapOf(typeOf<RecipeHeaderDTO>() to RecipeHeaderNavType)) { backStackEntry ->
		val recipeDetailArgs: RecipeDetail = backStackEntry.toRoute()
		val recipeDetailViewModel: RecipeDetailViewModel = koinInject(parameters = {
			parametersOf(recipeDetailArgs.recipeHeader)
		})
		RecipeDetailScreen(onClose, recipeDetailViewModel)
	}
}

fun NavController.navigateToRecipeDetail(recipeHeaderDTO: RecipeHeaderDTO) {
	navigate(route = RecipeDetail(recipeHeaderDTO))
}

@Composable
fun RecipeDetailScreen(onClose: () -> Unit = {}, recipesViewModel: RecipeDetailViewModel) {
	val uiState = recipesViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val scrollState = rememberScrollState()


	LaunchedEffect(key1 = recipesViewModel.snackbarEvent) {
		recipesViewModel.snackbarEvent.collect { message ->
			snackbarHostState.showSnackbar(message)
		}
	}
	Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
		Column(modifier = Modifier.padding(innerPadding).fillMaxWidth().verticalScroll(scrollState)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				IconButton(
					onClick = { onClose() },
					modifier = Modifier.padding(start = 6.dp)
				) {
					Icon(Icons.Outlined.Close, contentDescription = "Close privacy information")
				}
				Text(uiState.value.recipeHeader.name, style = MaterialTheme.typography.headlineMedium)
			}
			Text(uiState.value.recipeHeader.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
			val recipe = uiState.value.recipe
			if (recipe == null) {
				Text("Loading privacy information...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
			} else {
				RecipeDetailBody(recipe)
			}
		}
	}
}

@Composable
fun RecipeDetailBody(recipe: RecipeDTO) {
	Column(modifier = Modifier.padding(horizontal = 16.dp)) {
		Column(modifier = Modifier.padding(top = 16.dp)) {
			Text("Ingredients", style = MaterialTheme.typography.headlineSmall)
			Card(modifier = Modifier.padding(top = 8.dp)) {
				Column(modifier = Modifier.padding(8.dp)) {
					recipe.ingredients.forEach {
						IngredientItem(it)
					}
				}
			}
		}
		Column(modifier = Modifier.padding(top = 16.dp)) {
			Text("Steps", style = MaterialTheme.typography.headlineSmall)
			recipe.steps.forEach {
				Step(it)
			}
		}
		Column(modifier = Modifier.padding(top = 16.dp)) {
			Text("Tips", style = MaterialTheme.typography.headlineSmall)
			recipe.tips.forEach {
				Tip(it)
			}
		}
	}
}

@Composable
fun IngredientItem(
	ingredient: IngredientDTO
) {
	Column(modifier = Modifier.padding(vertical = 8.dp)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = ingredient.name.replaceFirstChar { it.uppercase() },
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.weight(1f)
			)

			Text(
				text = "${ingredient.quantity.value} ${ingredient.quantity.unitOfMeasure.orEmpty()}".trim(),
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Medium,
				textAlign = TextAlign.End,
				modifier = Modifier.weight(1f)
			)
		}
		val note = ingredient.note
		if (!note.isNullOrBlank()) {
			Text(
				text = note,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(top = 2.dp, start = 8.dp)
			)
		}
	}
}

@Composable
fun Step(step: StepDTO) {
	Row(modifier = Modifier.padding(top = 4.dp)) {
		Text(step.number.toString(), modifier = Modifier.padding(horizontal = 4.dp))
		Text(step.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp))
	}
}

@Composable
fun Tip(tip: TipDTO) {
	Row(modifier = Modifier.padding(top = 4.dp)) {
		Text("-", modifier = Modifier.padding(horizontal = 4.dp))
		Text(tip.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp))
	}
}