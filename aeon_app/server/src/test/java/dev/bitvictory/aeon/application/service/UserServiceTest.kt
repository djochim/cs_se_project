package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryValue
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryKey
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest
import dev.bitvictory.aeon.core.domain.usecases.user.PersonaDataProvider
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class UserServiceTest {

	@Test
	fun `Successful retrieval with data`(@MockK personalDataProvider: PersonaDataProvider, @MockK userContext: UserContext) = runTest {
		val personalData = PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryKey("key"),
					PersonalDataCategoryName("cat"),
					listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("entr"), PersonalDataCategoryEntryValue("value")))
				)
			)
		)
		val userService = UserService(listOf(personalDataProvider))
		coEvery { personalDataProvider.getPersonalData(any()) } returns personalData

		val result = userService.getPersonalData(userContext)

		result shouldBe personalData
	}

	@Test
	fun `Empty data providers`(@MockK userContext: UserContext) = runTest {
		val userService = UserService(emptyList())

		val result = userService.getPersonalData(userContext)

		result shouldBe PersonalData(emptyList())
	}

	@Test
	fun `Empty categories`(@MockK personalDataProvider: PersonaDataProvider, @MockK userContext: UserContext) = runTest {
		val personalData = PersonalData(
			listOf()
		)
		val userService = UserService(listOf(personalDataProvider))
		coEvery { personalDataProvider.getPersonalData(any()) } returns personalData

		val result = userService.getPersonalData(userContext)

		result shouldBe personalData
	}

	@Test
	fun `GET Multiple data providers`(
		@MockK personalDataProvider1: PersonaDataProvider,
		@MockK personalDataProvider2: PersonaDataProvider,
		@MockK userContext: UserContext
	) = runTest {
		val personalData1 = PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryKey("key"),
					PersonalDataCategoryName("cat"),
					listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("entr"), PersonalDataCategoryEntryValue("value")))
				)
			)
		)
		val personalData2 = PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryKey("key"),
					PersonalDataCategoryName("cat2"),
					listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("entr2"), PersonalDataCategoryEntryValue("value2")))
				)
			)
		)
		val userService = UserService(listOf(personalDataProvider1, personalDataProvider2))
		coEvery { personalDataProvider1.getPersonalData(any()) } returns personalData1
		coEvery { personalDataProvider2.getPersonalData(any()) } returns personalData2

		val result = userService.getPersonalData(userContext)

		result shouldBe PersonalData(personalData1.categories + personalData2.categories)
	}

	@Test
	fun `Change is only done to the key provider`(
		@MockK personalDataProvider1: PersonaDataProvider,
		@MockK personalDataProvider2: PersonaDataProvider,
		@MockK userContext: UserContext
	) = runTest {
		val userService = UserService(listOf(personalDataProvider1, personalDataProvider2))
		coEvery { personalDataProvider1.getCategories() } returns listOf(PersonalDataCategoryKey("key1"))
		coEvery { personalDataProvider2.getCategories() } returns listOf(PersonalDataCategoryKey("key2"))
		coEvery { personalDataProvider2.patchPersonalData(any(), any()) } returns Unit

		val changeRequest = PersonalDataChangeRequest(
			key = PersonalDataCategoryKey("key2"),
			changes = listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("entr"), PersonalDataCategoryEntryValue("newvalue")))
		)
		userService.patchPersonalData(userContext, changeRequest)

		coVerify {
			personalDataProvider2.patchPersonalData(userContext, changeRequest)
		}
		coVerify(exactly = 0) { personalDataProvider1.patchPersonalData(any(), any()) }
	}

	@Test
	fun `Exception is thrown on missing provider`(
		@MockK personalDataProvider1: PersonaDataProvider,
		@MockK personalDataProvider2: PersonaDataProvider,
		@MockK userContext: UserContext
	) = runTest {
		val userService = UserService(listOf(personalDataProvider1, personalDataProvider2))
		coEvery { personalDataProvider1.getCategories() } returns listOf(PersonalDataCategoryKey("key1"))
		coEvery { personalDataProvider2.getCategories() } returns listOf(PersonalDataCategoryKey("key2"))
		coEvery { personalDataProvider2.patchPersonalData(any(), any()) } returns Unit

		val changeRequest = PersonalDataChangeRequest(
			key = PersonalDataCategoryKey("key3"),
			changes = listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("entr"), PersonalDataCategoryEntryValue("newvalue")))
		)

		assertThrows<Exception> { userService.patchPersonalData(userContext, changeRequest) }

		coVerify(exactly = 0) {
			personalDataProvider1.patchPersonalData(any(), any())
			personalDataProvider2.patchPersonalData(userContext, changeRequest)
		}
	}

}