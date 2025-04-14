package dev.bitvictory.aeon.state

import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UserStateTest {

	private companion object {
		val VALID_TOKEN = TokenDTO("123", "token", "refresh")
		val VALID_USER = UserDTO("email", "name", "USER", true, 1)
		val VALID_USER_STATE = UserState(VALID_TOKEN, VALID_USER)
		val USER_STATE_NULL_TOKEN = UserState(null, VALID_USER)
		val USER_STATE_NULL_USER = UserState(VALID_TOKEN, null)
	}

	@Test
	fun `getId with valid token`() {
		VALID_USER_STATE.id shouldBe "123"
	}

	@Test
	fun `getId with null token`() {
		USER_STATE_NULL_TOKEN.id shouldBe ""
	}

	@Test
	fun `getRefreshToken with valid token`() {
		VALID_USER_STATE.refreshToken shouldBe "refresh"
	}

	@Test
	fun `getRefreshToken with null token`() {
		USER_STATE_NULL_TOKEN.refreshToken shouldBe ""
	}

	@Test
	fun `getAccessToken with valid token`() {
		VALID_USER_STATE.accessToken shouldBe "token"
	}

	@Test
	fun `getAccessToken with null token`() {
		USER_STATE_NULL_TOKEN.accessToken shouldBe ""
	}

	@Test
	fun `getName with valid user`() {
		VALID_USER_STATE.name shouldBe "name"
	}

	@Test
	fun `getName with null user`() {
		USER_STATE_NULL_USER.name shouldBe ""
	}

	@Test
	fun `getEmail with valid user`() {
		VALID_USER_STATE.email shouldBe "email"
	}

	@Test
	fun `getEmail with null user`() {
		USER_STATE_NULL_USER.email shouldBe ""
	}

	@Test
	fun `isAuthenticated with valid token`() {
		VALID_USER_STATE.isAuthenticated() shouldBe true
	}

	@Test
	fun `isAuthenticated with null token`() {
		USER_STATE_NULL_TOKEN.isAuthenticated() shouldBe false
	}

}