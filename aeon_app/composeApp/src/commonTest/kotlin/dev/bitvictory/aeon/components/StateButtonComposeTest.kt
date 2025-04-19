package dev.bitvictory.aeon.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StateButtonComposeTest {

	@Test
	fun activeStateButton() = runComposeUiTest {
		setContent {
			StateButton(
				initialText = "Initial Text",
				loadingText = "Loading Text",
				isLoading = false,
				onClick = {}
			)
		}
		onNodeWithTag("stateButton.button").assertIsEnabled()
		onNodeWithTag("stateButton.text", useUnmergedTree = true).assertTextEquals("Initial Text")
		onNodeWithTag("stateButton.icon", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun inactiveStateButton() = runComposeUiTest {
		setContent {
			StateButton(
				initialText = "Initial Text",
				loadingText = "Loading Text",
				isLoading = true,
				onClick = {}
			)
		}

		onNodeWithTag("stateButton.button").assertIsNotEnabled()
		onNodeWithTag("stateButton.text", useUnmergedTree = true).assertTextEquals("Loading Text")
		onNodeWithTag("stateButton.icon", useUnmergedTree = true).assertExists()
	}

	@Test
	fun buttonClickCallbackIsCalled() = runComposeUiTest {
		// Create a mutable state to track whether the callback is called
		val callbackCalled = mutableStateOf(false)

		setContent {
			StateButton(
				initialText = "Initial Text",
				loadingText = "Loading Text",
				isLoading = false,
				onClick = {
					// Update the state to indicate that the callback was called
					callbackCalled.value = true
				}
			)
		}
		// Perform a click on the button
		onNodeWithTag("stateButton.button").performClick()

		// Assert that the callback was called
		callbackCalled.value shouldBe true
	}
}