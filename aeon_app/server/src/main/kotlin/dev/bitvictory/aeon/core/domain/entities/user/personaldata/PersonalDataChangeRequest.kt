package dev.bitvictory.aeon.core.domain.entities.user.personaldata

data class PersonalDataChangeRequest(
	val key: PersonalDataCategoryKey,
	val deletions: List<PersonalDataCategoryEntryName> = emptyList(),
	val changes: List<PersonalDataCategoryEntry> = emptyList()
)
