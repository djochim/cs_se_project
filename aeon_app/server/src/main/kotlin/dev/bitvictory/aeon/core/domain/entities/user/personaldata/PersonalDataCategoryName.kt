package dev.bitvictory.aeon.core.domain.entities.user.personaldata

@JvmInline
value class PersonalDataCategoryName(val s: String) {
	companion object {
		val PROFILE = PersonalDataCategoryName("Profile Data")
	}
}