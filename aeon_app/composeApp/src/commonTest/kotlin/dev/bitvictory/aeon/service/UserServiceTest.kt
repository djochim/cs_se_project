package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.IAMApi
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.Error
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.state.UserState
import dev.bitvictory.aeon.storage.LocalKeyValueStore
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserServiceTest {
	private val iamApi: IAMApi = mock(MockMode.autoUnit)
	private val localKeyValueStore: LocalKeyValueStore = mock(MockMode.autoUnit)

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `getUserState initial state`() {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null
		val userService = UserService(iamApi, localKeyValueStore)

		userService.userState.value shouldBe UserState()
	}

	@Test
	fun `getUserState with stored token`() {
		// Verify that `userState` emits a `UserState` containing the token and user from
		// `localKeyValueStore` when a token is present.
		val userDTO = UserDTO("email", "name", "admin", true, 1)
		val tokenDTO = TokenDTO("userId", "access", "refresh")
		every { localKeyValueStore.user } returns userDTO
		every { localKeyValueStore.token } returns tokenDTO
		val userService = UserService(iamApi, localKeyValueStore)

		userService.userState.value shouldBe UserState(tokenDTO, userDTO)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `getUserState state changes`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null

		val userDTO = UserDTO("email", "name", "admin", true, 1)
		val tokenDTO = TokenDTO("userId", "access", "refresh")
		everySuspend { iamApi.login(any()) } returns AeonSuccessResponse(tokenDTO)
		everySuspend { iamApi.getUser() } returns AeonSuccessResponse(userDTO)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.login(LoginDTO("email", "password"))
		advanceUntilIdle()
		userService.logout()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(UserState(), UserState(tokenDTO, userDTO), UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `getUserState state changes with login refresh`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null

		val userDTO = UserDTO("email", "name", "admin", true, 1)
		val tokenDTO = TokenDTO("userId", "access", "refresh")
		val tokenDTONew = TokenDTO("userId", "access2", "refresh2")
		everySuspend { iamApi.login(any()) } returns AeonSuccessResponse(tokenDTO)
		everySuspend { iamApi.refreshLogin(any()) } returns AeonSuccessResponse(tokenDTONew)
		everySuspend { iamApi.getUser() } returns AeonSuccessResponse(userDTO)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.isAuthenticated() shouldBe false
		userService.login(LoginDTO("email", "password"))
		advanceUntilIdle()
		userService.isAuthenticated() shouldBe true
		userService.refreshLogin()
		advanceUntilIdle()
		userService.isAuthenticated() shouldBe true
		userService.logout()
		advanceUntilIdle()
		job.cancel()

		userService.isAuthenticated() shouldBe false
		states shouldContainInOrder listOf(UserState(), UserState(tokenDTO, userDTO), UserState(tokenDTONew, userDTO), UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `login failure`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null

		everySuspend { iamApi.login(any()) } returns AeonErrorResponse(400, Error(Uuid.NIL.toHexString(), "error"), ErrorType.SERVER_ERROR)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.isAuthenticated() shouldBe false
		userService.login(LoginDTO("email", "password")).shouldBeInstanceOf<AeonErrorResponse<TokenDTO>>()
		advanceUntilIdle()
		job.cancel()

		userService.isAuthenticated() shouldBe false
		states shouldContainInOrder listOf(UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `getUser fails with server error`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null

		val tokenDTO = TokenDTO("userId", "access", "refresh")
		everySuspend { iamApi.login(any()) } returns AeonSuccessResponse(tokenDTO)
		everySuspend { iamApi.getUser() } returns AeonErrorResponse(500, Error(Uuid.NIL.toHexString(), "error"), ErrorType.SERVER_ERROR)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.login(LoginDTO("email", "password"))
		advanceUntilIdle()
		userService.isAuthenticated() shouldBe true
		userService.logout()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(UserState(), UserState(tokenDTO, null), UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `getUser fails with authentication error`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		every { localKeyValueStore.user } returns null
		every { localKeyValueStore.token } returns null

		val tokenDTO = TokenDTO("userId", "access", "refresh")
		everySuspend { iamApi.login(any()) } returns AeonSuccessResponse(tokenDTO)
		everySuspend { iamApi.getUser() } returns AeonErrorResponse(401, Error(Uuid.NIL.toHexString(), "error"), ErrorType.AUTHENTICATION_ERROR)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.login(LoginDTO("email", "password"))
		advanceUntilIdle()
		userService.isAuthenticated() shouldBe false
		userService.logout()
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `refreshLogin fails`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		val tokenDTO = TokenDTO("userId", "access", "refresh")
		val userDTO = UserDTO("email", "name", "admin", true, 1)

		every { localKeyValueStore.user } returns userDTO
		every { localKeyValueStore.token } returns tokenDTO

		everySuspend { iamApi.refreshLogin(any()) } returns AeonErrorResponse(500, Error(Uuid.NIL.toHexString(), "error"), ErrorType.UNKNOWN_ERROR)

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.isAuthenticated() shouldBe true
		userService.refreshLogin()
		advanceUntilIdle()
		job.cancel()
		userService.isAuthenticated() shouldBe false
		userService.userState.value shouldBe UserState()

		states shouldContainInOrder listOf(UserState(tokenDTO, userDTO), UserState())
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `logout cleans state`() = runTest {
		// Verify that `userState` emits the correct initial `UserState` when no token is present in
		// `localKeyValueStore`.
		val tokenDTO = TokenDTO("userId", "access", "refresh")
		val userDTO = UserDTO("email", "name", "admin", true, 1)

		every { localKeyValueStore.user } returns userDTO
		every { localKeyValueStore.token } returns tokenDTO

		val userService = UserService(iamApi, localKeyValueStore)

		val states = mutableListOf<UserState>()
		val job = launch {
			userService.userState.collect { userState ->
				states.add(userState)
			}
		}
		advanceUntilIdle()

		userService.isAuthenticated() shouldBe true
		userService.logout()
		advanceUntilIdle()
		job.cancel()
		userService.isAuthenticated() shouldBe false
		userService.userState.value shouldBe UserState()

		states shouldContainInOrder listOf(UserState(tokenDTO, userDTO), UserState())
	}

}