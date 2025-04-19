package dev.bitvictory.aeon.screens.login

import aeon.composeapp.generated.resources.Res
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil3.compose.AsyncImage
import dev.bitvictory.aeon.components.StateButton
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject

@Serializable
object Login

fun NavGraphBuilder.loginDestination(onLoginSuccess: () -> Unit) {
	composable<Login> { LoginScreen(onLoginSuccess) }
}

fun NavController.navigateToLogin() {
	navigate(route = Login)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, loginViewModel: LoginViewModel = koinInject()) {

	val focusManager = LocalFocusManager.current
	val keyboardController = LocalSoftwareKeyboardController.current

	val emailFocusRequester = remember { FocusRequester() }
	val passwordFocusRequester = remember { FocusRequester() }
	val buttonFocusRequester = remember { FocusRequester() }

	val uiState = loginViewModel.uiState.collectAsStateWithLifecycle()

	if (uiState.value.success) {
		onLoginSuccess()
	}

	Column(
		Modifier.fillMaxWidth().fillMaxHeight().padding(8.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Top
	) {
		AsyncImage(
			model = Res.getUri("files/assistant_hello.svg"),
			contentDescription = null,
			modifier = Modifier.size(150.dp).padding(8.dp),
		)
		Text(
			modifier = Modifier.padding(8.dp),
			text = "Hi there! Ready to Feel Your Best?",
			style = MaterialTheme.typography.headlineMedium,
			textAlign = TextAlign.Center
		)
		Text(
			modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 24.dp),
			text = "Let's make healthy living easy—sign in to get started!",
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center
		)

		OutlinedTextField(
			value = uiState.value.email,
			onValueChange = { loginViewModel.changeEmail(it) },
			maxLines = 1,
			label = { Text("Email Address") },
			modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
				.focusRequester(emailFocusRequester),
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Email,
				imeAction = ImeAction.Next
			),
			keyboardActions = KeyboardActions(
				onNext = {
					focusManager.moveFocus(FocusDirection.Down)
				}
			),
			isError = uiState.value.emailError.isNotEmpty()
		)
		if (uiState.value.emailError.isNotEmpty()) {
			Text(uiState.value.emailError, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().align(Alignment.Start))
		}
		OutlinedTextField(
			value = uiState.value.password,
			onValueChange = { loginViewModel.changePassword(it) },
			maxLines = 1,
			label = { Text("Password") },
			modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
				.focusRequester(passwordFocusRequester),
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Password,
				imeAction = ImeAction.Done
			),
			keyboardActions = KeyboardActions(
				onDone = {
					buttonFocusRequester.requestFocus()
					keyboardController?.hide()
				}
			),
			visualTransformation = PasswordVisualTransformation(),
			isError = uiState.value.passwordError.isNotEmpty()
		)
		if (uiState.value.passwordError.isNotEmpty()) {
			Text(uiState.value.passwordError, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().align(Alignment.Start))
		}
		if (uiState.value.error.isNotEmpty()) {
			Text(uiState.value.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().align(Alignment.Start))
		}
		StateButton(
			onClick = {
				loginViewModel.login()
			},
			modifier = Modifier.fillMaxWidth()
				.focusRequester(buttonFocusRequester),
			initialText = "Sign In",
			loadingText = "Signing In...",
			isLoading = uiState.value.isLoading
		)
	}
}