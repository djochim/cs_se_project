package dev.bitvictory.aeon.screens.login

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.service.IUserService
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoginViewModelTest {

	private val userService: IUserService = mock(MockMode.autoUnit)

	private lateinit var loginViewModel: LoginViewModel

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
		loginViewModel = LoginViewModel(userService)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `getUiState initial state`() {
		loginViewModel.uiState.value shouldBe LoginUIState()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `email state changes`() = runTest {
		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changeEmail("first")
		advanceUntilIdle()
		loginViewModel.changeEmail("first.second")
		advanceUntilIdle()
		loginViewModel.changeEmail("first.second@jochim.dev")
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			LoginUIState(),
			LoginUIState(email = "first"),
			LoginUIState(email = "first.second"),
			LoginUIState(email = "first.second@jochim.dev")
		)
		loginViewModel.uiState.value shouldBe LoginUIState(email = "first.second@jochim.dev")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `password state changes`() = runTest {
		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changePassword("one")
		advanceUntilIdle()
		loginViewModel.changePassword("onetwo")
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			LoginUIState(),
			LoginUIState(password = "one"),
			LoginUIState(password = "onetwo")
		)
		loginViewModel.uiState.value shouldBe LoginUIState(password = "onetwo")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `login state changes`() = runTest {
		everySuspend { userService.login(LoginDTO("email", "pw")) } returns AeonSuccessResponse(TokenDTO("", "", ""))

		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changePassword("pw")
		loginViewModel.changeEmail("email")
		advanceUntilIdle()
		loginViewModel.login()
		advanceUntilIdle()
		job.cancel()

		states shouldContain LoginUIState()
		states shouldContain LoginUIState(password = "pw", email = "email")
		states shouldContain LoginUIState(success = true)
		loginViewModel.uiState.value shouldBe LoginUIState(success = true)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `login failure state changes`() = runTest {
		everySuspend { userService.login(LoginDTO("email", "pw")) } returns AeonErrorResponse(500, AeonError(message = "error"), ErrorType.CLIENT_ERROR)

		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changePassword("pw")
		loginViewModel.changeEmail("email")
		advanceUntilIdle()
		loginViewModel.login()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			LoginUIState(),
			LoginUIState(password = "pw", email = "email"),
			LoginUIState(password = "pw", email = "email", isLoading = false, success = false, error = "error"),
		)
		loginViewModel.uiState.value shouldBe LoginUIState(email = "email", password = "pw", isLoading = false, success = false, error = "error")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `email failure state changes`() = runTest {
		everySuspend { userService.login(LoginDTO("email", "pw")) } returns AeonErrorResponse(
			500,
			AeonError(message = "email is wrong", details = mapOf("email" to "email is wrong")),
			ErrorType.CLIENT_ERROR
		)

		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changePassword("pw")
		loginViewModel.changeEmail("email")
		advanceUntilIdle()
		loginViewModel.login()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			LoginUIState(),
			LoginUIState(password = "pw", email = "email"),
			LoginUIState(password = "pw", email = "email", isLoading = false, success = false, emailError = "email is wrong"),
		)
		loginViewModel.uiState.value shouldBe LoginUIState(email = "email", password = "pw", isLoading = false, success = false, emailError = "email is wrong")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `password failure state changes`() = runTest {
		everySuspend { userService.login(LoginDTO("email", "pw")) } returns AeonErrorResponse(
			500,
			AeonError(message = "password is wrong", details = mapOf("password" to "password is wrong")),
			ErrorType.CLIENT_ERROR
		)

		val states = mutableListOf<LoginUIState>()
		val job = launch {
			loginViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		loginViewModel.changePassword("pw")
		loginViewModel.changeEmail("email")
		advanceUntilIdle()
		loginViewModel.login()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			LoginUIState(),
			LoginUIState(password = "pw", email = "email"),
			LoginUIState(password = "pw", email = "email", isLoading = false, success = false, passwordError = "password is wrong"),
		)
		loginViewModel.uiState.value shouldBe LoginUIState(
			email = "email",
			password = "pw",
			isLoading = false,
			success = false,
			passwordError = "password is wrong"
		)
	}

}