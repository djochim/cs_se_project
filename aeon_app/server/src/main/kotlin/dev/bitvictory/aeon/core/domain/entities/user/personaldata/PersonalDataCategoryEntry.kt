package dev.bitvictory.aeon.core.domain.entities.user.personaldata

data class PersonalDataCategoryEntry(
	val name: PersonalDataCategoryEntryName,
	val value: PersonalDataCategoryEntryValue,
	val isDeletable: Boolean = true
)
