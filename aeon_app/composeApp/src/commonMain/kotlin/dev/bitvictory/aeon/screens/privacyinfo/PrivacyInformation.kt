package dev.bitvictory.aeon.screens.privacyinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun PrivacyInformationScreen(onClose: () -> Unit = {}, profileViewModel: PrivacyInformationViewModel = koinInject()) {

	val uiState = profileViewModel.uiState.collectAsStateWithLifecycle()

	Column(modifier = Modifier.padding(8.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(
				onClick = { onClose() },
				modifier = Modifier.padding(start = 8.dp)
			) {
				Icon(Icons.Outlined.Close, contentDescription = "Close privacy information")
			}
			Text("Privacy Information", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 10.dp))
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
				PrivacyInformationData(uiState.value.groups)
			}
		}
	}
}

@Composable
fun PrivacyInformationData(privacyInformation: List<PrivacyInformationGroupDTO>) {
	LazyColumn(
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.semantics { traversalIndex = 1f },
	) {
		items(privacyInformation) {
			PrivacyInformationGroup(it)
		}
	}
}

@Composable
fun PrivacyInformationGroup(group: PrivacyInformationGroupDTO) {
	Text(group.name, style = MaterialTheme.typography.bodyLarge)
	Column {
		group.entries.forEach {
			Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
				Text("- ${it.key}: ", style = MaterialTheme.typography.labelMedium)
				Text(it.value, style = MaterialTheme.typography.bodyMedium)
			}
		}
	}

}