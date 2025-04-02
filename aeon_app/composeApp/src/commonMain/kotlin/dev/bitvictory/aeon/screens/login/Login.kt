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
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil3.compose.AsyncImage
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
			value = "",
			onValueChange = { },
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
			)
		)
		OutlinedTextField(
			value = "",
			onValueChange = { },
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
			visualTransformation = PasswordVisualTransformation()
		)
		Button(
			onClick = {
				onLoginSuccess()
			},
			Modifier.fillMaxWidth()
				.focusRequester(buttonFocusRequester)
		) {
			Text("Sign In")
		}
	}
}