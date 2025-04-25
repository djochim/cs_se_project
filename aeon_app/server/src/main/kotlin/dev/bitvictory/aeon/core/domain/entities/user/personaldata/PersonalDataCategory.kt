package dev.bitvictory.aeon.core.domain.entities.user.personaldata

data class PersonalDataCategory(
	val key: PersonalDataCategoryKey,
	val name: PersonalDataCategoryName,
	val entries: List<PersonalDataCategoryEntry>
)