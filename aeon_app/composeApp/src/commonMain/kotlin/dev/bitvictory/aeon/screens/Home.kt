package dev.bitvictory.aeon.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.bitvictory.aeon.aeonApi
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import kotlinx.serialization.Serializable

@Serializable
object Home

fun NavGraphBuilder.homeDestination() {
	composable<Home> { HomeScreen() }
}

@Composable
fun HomeScreen() {
	var status by rememberSaveable { mutableStateOf("Not known") }
	LaunchedEffect(Unit) {
		status = when (val statusResponse = aeonApi.getStatus()) {
			is AeonErrorResponse   -> "DOWN"
			is AeonSuccessResponse -> statusResponse.data.status.name
		}
	}
	Column {
		Text("Home")
		Text("Server status is $status")
	}
}