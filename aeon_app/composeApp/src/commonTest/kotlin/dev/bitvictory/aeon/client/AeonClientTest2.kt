package dev.bitvictory.aeon.client

class AeonClientTest2 {

	companion object {
		const val BASE_URL = "http://localhost:8080"
	}
//8080
//	private fun getAuthClient(mockEngine: MockEngine): AuthClient {
//		val client = HttpClient(mockEngine) {
//			install(ContentNegotiation) {
//				json(json = Json {
//					encodeDefaults = true
//					isLenient = true
//					allowSpecialFloatingPointValues = true
//					allowStructuredMapKeys = true
//					prettyPrint = false
//					useArrayPolymorphism = false
//					ignoreUnknownKeys = true
//				})
//			}
//
//			defaultRequest {
//				contentType(ContentType.Application.Json)
//			}
//		}
//
//		return AuthClient(BASE_URL, client)
//	}
//
//	@Test
//	fun `get user returns user`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "type": "dev.bitvictory.model.user.RegisteredUser",
//			  "_id": "6fdsajnhasdf4576cda",
//			  "accountType": "USER",
//			  "accountStatus": "ACTIVE",
//			  "sendAnalyticalData": false,
//			  "acceptedPrivacyVersion": 0,
//			  "twoFactorToken": "",
//			  "email": "test@jochim.dev",
//			  "name": "tester",
//			  "profileImage": null,
//			  "password": "[MASKED]"
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.OK,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.getUser(userContext)
//
//		receivedToken shouldBe expectedToken
//		result.shouldBeInstanceOf<AeonSuccessResponse<UserDTO>>()
//		result.data shouldBe UserDTO("test@jochim.dev", "tester", "USER", false, 0)
//	}
//
//	@Test
//	fun `get user is failing and returns failed response`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "correlationId": "e2a52070-b79b-496b-87d3-6e294a270272",
//			  "message": "The request has a bad format",
//			  "details": {
//				"email": "Must be a valid"
//			  }
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.BadRequest,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.getUser(userContext)
//
//		receivedToken shouldBe expectedToken
//		result.shouldBeInstanceOf<AeonErrorResponse<UserDTO>>()
//		result.statusCode shouldBe 400
//		result.type shouldBe ErrorType.CLIENT_ERROR
//		result.error shouldBe AeonError("e2a52070-b79b-496b-87d3-6e294a270272", "The request has a bad format", mapOf("email" to "Must be a valid"))
//	}
//
//	@OptIn(ExperimentalSerializationApi::class)
//	@Test
//	fun `patch user`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//
//		var receivedToken: String? = null
//		var receivedBodyString: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			receivedBodyString = (request.body as TextContent).text
//			respond(
//				content = "",
//				status = HttpStatusCode.Accepted
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.patchUser(userContext, UpdateUserRequest("tester", "test@jochim.dev"))
//
//		receivedToken shouldBe expectedToken
//		result.shouldBeInstanceOf<AeonSuccessResponse<Unit>>()
//		receivedBodyString shouldNot beNull()
//		Json.decodeFromString<UpdateUserRequest>(receivedBodyString!!) shouldBe UpdateUserRequest("tester", "test@jochim.dev")
//	}
//
//	@OptIn(ExperimentalSerializationApi::class)
//	@Test
//	fun `patch user is failing and returns failed response`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "correlationId": "e2a52070-b79b-496b-87d3-6e294a270272",
//			  "message": "The request has a bad format",
//			  "details": {
//				"email": "Must be a valid"
//			  }
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.BadRequest,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.patchUser(userContext, UpdateUserRequest("tester", "test@jochim.dev"))
//
//		receivedToken shouldBe expectedToken
//		result.shouldBeInstanceOf<AeonErrorResponse<Unit>>()
//		result.statusCode shouldBe 400
//		result.type shouldBe ErrorType.CLIENT_ERROR
//		result.error shouldBe AeonError("e2a52070-b79b-496b-87d3-6e294a270272", "The request has a bad format", mapOf("email" to "Must be a valid"))
//	}
//
//	@Test
//	fun `get personal data of user`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "type": "dev.bitvictory.model.user.RegisteredUser",
//			  "_id": "6fdsajnhasdf4576cda",
//			  "accountType": "USER",
//			  "accountStatus": "ACTIVE",
//			  "sendAnalyticalData": false,
//			  "acceptedPrivacyVersion": 0,
//			  "twoFactorToken": "",
//			  "email": "test@jochim.dev",
//			  "name": "tester",
//			  "profileImage": null,
//			  "password": "[MASKED]"
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.OK,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.getPersonalData(userContext)
//
//		receivedToken shouldBe expectedToken
//		result shouldBe PersonalData(
//			listOf(
//				PersonalDataCategory(
//					PersonalDataCategoryKey.PROFILE,
//					PersonalDataCategoryName.PROFILE, listOf(
//						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("test@jochim.dev"), false),
//						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("tester")),
//					)
//				)
//			)
//		)
//	}
//
//	@Test
//	fun `get personal data filters empty name`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "type": "dev.bitvictory.model.user.RegisteredUser",
//			  "_id": "6fdsajnhasdf4576cda",
//			  "accountType": "USER",
//			  "accountStatus": "ACTIVE",
//			  "sendAnalyticalData": false,
//			  "acceptedPrivacyVersion": 0,
//			  "twoFactorToken": "",
//			  "email": "test@jochim.dev",
//			  "name": "",
//			  "profileImage": null,
//			  "password": "[MASKED]"
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.OK,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.getPersonalData(userContext)
//
//		receivedToken shouldBe expectedToken
//		result shouldBe PersonalData(
//			listOf(
//				PersonalDataCategory(
//					PersonalDataCategoryKey.PROFILE,
//					PersonalDataCategoryName.PROFILE, listOf(
//						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("test@jochim.dev"), false),
//					)
//				)
//			)
//		)
//	}
//
//	@Test
//	fun `get personal data filters empty email`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "type": "dev.bitvictory.model.user.RegisteredUser",
//			  "_id": "6fdsajnhasdf4576cda",
//			  "accountType": "USER",
//			  "accountStatus": "ACTIVE",
//			  "sendAnalyticalData": false,
//			  "acceptedPrivacyVersion": 0,
//			  "twoFactorToken": "",
//			  "email": "",
//			  "name": "tester",
//			  "profileImage": null,
//			  "password": "[MASKED]"
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.OK,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		val result = authClient.getPersonalData(userContext)
//
//		receivedToken shouldBe expectedToken
//		result shouldBe PersonalData(
//			listOf(
//				PersonalDataCategory(
//					PersonalDataCategoryKey.PROFILE,
//					PersonalDataCategoryName.PROFILE, listOf(
//						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("tester")),
//					)
//				)
//			)
//		)
//	}
//
//	@Test
//	fun `get user is failing and get personal data throws exception`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//		val jsonResponse = """
//			{
//			  "correlationId": "e2a52070-b79b-496b-87d3-6e294a270272",
//			  "message": "The request has a bad format",
//			  "details": {
//				"email": "Must be a valid"
//			  }
//			}
//		""".trimIndent()
//		var receivedToken: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			respond(
//				content = jsonResponse,
//				status = HttpStatusCode.BadRequest,
//				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		assertThrows<Exception> { authClient.getPersonalData(userContext) }
//
//		receivedToken shouldBe expectedToken
//	}
//
//	@OptIn(ExperimentalSerializationApi::class)
//	@Test
//	fun `patch personal data with deletions only`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//
//		var receivedToken: String? = null
//		var receivedBodyString: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			receivedBodyString = (request.body as TextContent).text
//			respond(
//				content = "",
//				status = HttpStatusCode.Accepted
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		authClient.patchPersonalData(
//			userContext,
//			PersonalDataChangeRequest(PersonalDataCategoryKey.PROFILE, listOf(PersonalDataCategoryEntryName("name")), emptyList())
//		)
//
//		receivedToken shouldBe expectedToken
//		receivedBodyString shouldNot beNull()
//		Json.decodeFromString<UpdateUserRequest>(receivedBodyString!!) shouldBe UpdateUserRequest(name = "")
//	}
//
//	@OptIn(ExperimentalSerializationApi::class)
//	@Test
//	fun `patch personal data with changes only`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//
//		var receivedToken: String? = null
//		var receivedBodyString: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			receivedBodyString = (request.body as TextContent).text
//			respond(
//				content = "",
//				status = HttpStatusCode.Accepted
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		authClient.patchPersonalData(
//			userContext, PersonalDataChangeRequest(
//				PersonalDataCategoryKey.PROFILE, emptyList(), listOf(
//					PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("testNew")),
//					PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("emailNew"))
//				)
//			)
//		)
//
//		receivedToken shouldBe expectedToken
//		receivedBodyString shouldNot beNull()
//		Json.decodeFromString<UpdateUserRequest>(receivedBodyString!!) shouldBe UpdateUserRequest("emailNew", "testNew")
//	}
//
//	@OptIn(ExperimentalSerializationApi::class)
//	@Test
//	fun `patch personal data deletion overrides changes`(@MockK userContext: UserContext) = runTest {
//		val expectedToken = "my-very-secret-jwt-token"
//
//		var receivedToken: String? = null
//		var receivedBodyString: String? = null
//
//		val mockEngine = MockEngine { request ->
//			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
//			receivedBodyString = (request.body as TextContent).text
//			respond(
//				content = "",
//				status = HttpStatusCode.Accepted
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		every { userContext.tokenOrThrow() } returns expectedToken
//
//		authClient.patchPersonalData(
//			userContext, PersonalDataChangeRequest(
//				PersonalDataCategoryKey.PROFILE, listOf(PersonalDataCategoryEntryName("name")), listOf(
//					PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("testNew")),
//					PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("emailNew"))
//				)
//			)
//		)
//
//		receivedToken shouldBe expectedToken
//		receivedBodyString shouldNot beNull()
//		Json.decodeFromString<UpdateUserRequest>(receivedBodyString!!) shouldBe UpdateUserRequest("emailNew", "")
//	}
//
//	@Test
//	fun `patch personal change not executed with only different keys`(@MockK userContext: UserContext) = runTest {
//		every { userContext.tokenOrThrow() } returns "my-very-secret-jwt-token"
//		val mockEngine = MockEngine { request ->
//			respond(
//				content = "",
//				status = HttpStatusCode.BadRequest
//			)
//		}
//		val authClient = getAuthClient(mockEngine)
//
//		authClient.patchPersonalData(
//			userContext, PersonalDataChangeRequest(
//				PersonalDataCategoryKey.PROFILE, listOf(PersonalDataCategoryEntryName("data")), listOf(
//					PersonalDataCategoryEntry(PersonalDataCategoryEntryName("test"), PersonalDataCategoryEntryValue("testNew")),
//				)
//			)
//		)
//
//		verify(exactly = 0) { userContext.tokenOrThrow() }
//	}

}