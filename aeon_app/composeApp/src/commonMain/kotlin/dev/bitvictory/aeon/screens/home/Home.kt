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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Home

fun NavGraphBuilder.homeDestination(onQuerySubmit: (query: String) -> Unit) {
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
fun HomeScreen(onQuerySubmit: (query: String) -> Unit, viewModel: HomeViewModel = koinViewModel()) {
	val homeUIState by viewModel.uiState.collectAsState()

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
			Row(
				verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
					.widthIn(max = 400.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				OutlinedTextField(
					value = homeUIState.query,
					onValueChange = viewModel::changeQuery,
					maxLines = 500,
					placeholder = { Text("Ask Aeon...") },
					modifier = Modifier.weight(1f)
				)
				FilledIconButton(
					onClick = {
						onQuerySubmit(viewModel.uiState.value.query)
						viewModel.changeQuery("")
					},
					modifier = Modifier.padding(start = 8.dp)
				) {
					Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send query")
				}
			}
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