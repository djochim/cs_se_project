package dev.bitvictory.aeon.core.domain.entities.user.personaldata

data class PersonalDataCategory(
	val name: PersonalDataCategoryName,
	val entries: List<PersonalDataCategoryEntry>
)