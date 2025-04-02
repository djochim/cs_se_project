package dev.bitvictory.aeon.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Home

fun NavGraphBuilder.homeDestination() {
	composable<Home> { HomeScreen() }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
	val homeUIState by viewModel.uiState.collectAsState()
	Column {
		Text("Home")
	}
}