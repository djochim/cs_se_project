package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryValue
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryKey
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationKeyDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class UserApiTest {

	@Test
	fun `Personal Data converts to DTO`() {
		val pData1 = PersonalDataCategoryEntry(
			PersonalDataCategoryEntryName("key1"), PersonalDataCategoryEntryValue("value1")
		)
		val pData2 = PersonalDataCategoryEntry(
			PersonalDataCategoryEntryName("key2"), PersonalDataCategoryEntryValue("value2")
		)
		val pData3 = PersonalDataCategoryEntry(
			PersonalDataCategoryEntryName("key3"), PersonalDataCategoryEntryValue("value3")
		)
		val category1 = PersonalDataCategory(PersonalDataCategoryKey("key"), PersonalDataCategoryName("cat1"), listOf(pData1, pData2))
		val category2 = PersonalDataCategory(PersonalDataCategoryKey("key2"), PersonalDataCategoryName("cat2"), listOf(pData3))
		val personalData = PersonalData(listOf(category1, category2))

		val result = personalData.toDTO()

		result shouldBe PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO("key", "cat1", listOf(PrivacyInformationEntryDTO("key1", "value1"), PrivacyInformationEntryDTO("key2", "value2"))),
				PrivacyInformationGroupDTO("key2", "cat2", listOf(PrivacyInformationEntryDTO("key3", "value3")))
			)
		)
	}

	@Test
	fun `Patch DTO converts to Domain Object`() {
		val patchDTO = PrivacyInformationPatchDTO(
			"key",
			listOf(PrivacyInformationKeyDTO("key1"), PrivacyInformationKeyDTO("key2")),
			listOf(PrivacyInformationEntryDTO("key3", "value3"))
		)

		val result = patchDTO.toDomain()

		result shouldBe PersonalDataChangeRequest(
			PersonalDataCategoryKey("key"),
			listOf(PersonalDataCategoryEntryName("key1"), PersonalDataCategoryEntryName("key2")),
			listOf(PersonalDataCategoryEntry(PersonalDataCategoryEntryName("key3"), PersonalDataCategoryEntryValue("value3")))
		)
	}

}