package dev.bitvictory.aeon.screens.home

import dev.bitvictory.aeon.service.IUserService
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
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

class HomeViewModelTest {

	private val userService: IUserService = mock(MockMode.autoUnit)

	private lateinit var homeViewModel: HomeViewModel

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
		every { userService.isAuthenticated() } returns true
		homeViewModel = HomeViewModel(userService)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `getUiState initial state`() {
		homeViewModel.uiState.value shouldBe HomeUIState(isAuthenticated = true)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new query text state changes`() = runTest {
		val states = mutableListOf<HomeUIState>()
		val job = launch {
			homeViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		homeViewModel.changeQuery("first")
		advanceUntilIdle()
		homeViewModel.changeQuery("first.second")
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			HomeUIState(isAuthenticated = true),
			HomeUIState(isAuthenticated = true, query = "first"),
			HomeUIState(isAuthenticated = true, query = "first.second")
		)
		homeViewModel.uiState.value shouldBe HomeUIState(isAuthenticated = true, query = "first.second")
	}

}