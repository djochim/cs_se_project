package dev.bitvictory.aeon.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object Recipes

fun NavGraphBuilder.recipesDestination() {
	composable<Recipes> { RecipesScreen() }
}

@Composable
fun RecipesScreen() {
	Column {
		Text("Recipes")
	}
}