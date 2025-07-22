package dev.bitvictory.aeon.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.bitvictory.aeon.components.ChatBar
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Home

fun NavGraphBuilder.homeDestination(onQuerySubmit: (query: String, images: List<ByteArray>) -> Unit) {
	composable<Home> { HomeScreen(onQuerySubmit) }
}

fun NavController.navigateToHome() {
	navigate(route = Home)
}

/**
 * Composable function for the home screen of the application.
 *
 * This screen displays a text field for users to input queries and a button to submit them.
 * It also shows a title and a disclaimer.
 *
 * @param onQuerySubmit A lambda function that is invoked when the user submits a query.
 *                      It takes the query string as a parameter.
 * @param viewModel An instance of [HomeViewModel] used to manage the UI state of this screen.
 *                  It is injected by default using Koin.
 */
@Composable
fun HomeScreen(onQuerySubmit: (query: String, images: List<ByteArray>) -> Unit, viewModel: HomeViewModel = koinViewModel()) {
	val homeUIState by viewModel.uiState.collectAsStateWithLifecycle()

	Column(
		Modifier.fillMaxWidth().fillMaxHeight().padding(8.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 24.dp),
			text = "Your health coach is here — what’s on your mind?",
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Center
		)
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			ChatBar(
				homeUIState.query,
				homeUIState.images,
				viewModel::changeQuery,
				{
					onQuerySubmit(homeUIState.query, homeUIState.images)
					viewModel.changeQuery("")
					viewModel.clearSelectedImage()
				},
				viewModel::addImage,
				viewModel::clearSelectedImage
			)
			Row(
				modifier = Modifier.fillMaxWidth()
					.widthIn(max = 400.dp)
			) {
				Text(
					modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 24.dp),
					text = "Powered by Aeon AI • Responses are for informational use only",
					style = MaterialTheme.typography.labelSmall,
					textAlign = TextAlign.Center
				)
			}
		}
	}
}