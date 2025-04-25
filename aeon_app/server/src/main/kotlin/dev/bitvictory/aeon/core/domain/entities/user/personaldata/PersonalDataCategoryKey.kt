package dev.bitvictory.aeon.core.domain.entities.user.personaldata

@JvmInline
value class PersonalDataCategoryKey(val s: String) {
	companion object {
		val PROFILE = PersonalDataCategoryKey("PROFILE")
	}
}