package dev.bitvictory.aeon.screens.privacyinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object PrivacyInformation

fun NavGraphBuilder.privacyInformationDestination(onClose: () -> Unit = {}) {
	composable<PrivacyInformation> { PrivacyInformationScreen(onClose) }
}

fun NavController.navigateToPrivacyInformation() {
	navigate(route = PrivacyInformation)
}

/**
 * Composable function for the Privacy Information screen.
 * This screen displays the user's personal information stored by the application
 * and provides an option to delete it.
 *
 * @param onClose A lambda function to be invoked when the close button is clicked.
 *                Defaults to an empty lambda.
 * @param profileViewModel The view model for managing the privacy information data.
 *                         It is injected using Koin by default.
 */
@Composable
fun PrivacyInformationScreen(onClose: () -> Unit = {}, profileViewModel: PrivacyInformationViewModel = koinInject()) {

	val uiState = profileViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	LaunchedEffect(key1 = profileViewModel.snackbarEvent) {
		profileViewModel.snackbarEvent.collect { message ->
			snackbarHostState.showSnackbar(message)
		}
	}
	Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
		Column(modifier = Modifier.padding(innerPadding).fillMaxWidth()) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				IconButton(
					onClick = { onClose() },
					modifier = Modifier.padding(start = 8.dp)
				) {
					Icon(Icons.Outlined.Close, contentDescription = "Close privacy information")
				}
				Text("Privacy Information", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 10.dp))
			}
			Text("We value your privacy. Here’s the personal information we currently store for your account:", style = MaterialTheme.typography.bodyLarge)
			if (uiState.value.privacyInformation == null && uiState.value.error == null) {
				Text("Loading privacy information...", style = MaterialTheme.typography.bodyMedium)
			} else if (uiState.value.error != null) {
				Text("Error loading privacy information: ${uiState.value.error}", style = MaterialTheme.typography.bodyMedium)
			} else {
				if (uiState.value.groups.isEmpty()) {
					Text("No privacy information found", style = MaterialTheme.typography.bodyMedium)
				} else {
					PrivacyInformationData(uiState.value.groups) { group, entry -> profileViewModel.deletePrivacyInformation(group, entry) }
				}
			}
		}
	}
}

@Composable
fun PrivacyInformationData(privacyInformation: List<PrivacyInformationGroupDTO>, deleteEntry: (group: String, entry: String) -> Unit) {
	LazyColumn(
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.semantics { traversalIndex = 1f }.fillMaxWidth(),
	) {
		items(privacyInformation) {
			PrivacyInformationGroup(it, deleteEntry)
		}
	}
}

@Composable
fun PrivacyInformationGroup(group: PrivacyInformationGroupDTO, deleteEntry: (group: String, entry: String) -> Unit) {
	Text(group.name, style = MaterialTheme.typography.bodyLarge)
	Column(modifier = Modifier.fillMaxWidth()) {
		group.entries.forEach {
			Row(
				modifier = Modifier.padding(start = 8.dp, top = 4.dp).fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text("- ${it.key}: ", style = MaterialTheme.typography.labelMedium)
					Text(it.value, style = MaterialTheme.typography.bodyMedium)
				}
				if (it.isDeletable) {
					IconButton(onClick = {
						deleteEntry(group.key, it.key)
					}) {
						Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete personal data entry")
					}
				}
			}
		}
	}

}