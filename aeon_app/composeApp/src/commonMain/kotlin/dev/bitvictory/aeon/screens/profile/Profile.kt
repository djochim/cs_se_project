package dev.bitvictory.aeon.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object Profile

fun NavGraphBuilder.profileDestination(onLogout: () -> Unit) {
	composable<Profile> { ProfileScreen(onLogout) }
}

@Composable
fun ProfileSetting(label: String, value: String) {
	Column(modifier = Modifier.padding(8.dp)) {
		Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
		if (value.isEmpty()) {
			Text("Not set", style = MaterialTheme.typography.bodyMedium)
		} else {
			Text(value, style = MaterialTheme.typography.bodyMedium)
		}
	}
}

@Composable
fun ProfileScreen(onLogout: () -> Unit, profileViewModel: ProfileViewModel = koinInject()) {

	val userState = profileViewModel.userState.collectAsState()

	Column(modifier = Modifier.padding(8.dp)) {
		Text("Profile", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))
		ProfileSetting("Name", userState.value.name)
		ProfileSetting("Email", userState.value.email)
		OutlinedButton(
			onClick = {
				profileViewModel.logout()
				onLogout()
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Sign Out")
		}
	}
}