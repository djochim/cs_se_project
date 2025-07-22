package dev.bitvictory.aeon.screens.chat

// In a suitable file, perhaps near your RecipeHeaderDTO or navigation setup
import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import dev.bitvictory.aeon.screens.recipe.detail.NavTypeJson
import kotlinx.serialization.json.Json

/**
 * Custom [NavType] for [ChatParameter] objects.
 *
 * This NavType allows passing [ChatParameter] objects as navigation arguments.
 * It handles the serialization and deserialization of the [ChatParameter] to and from JSON strings
 * for storage in [Bundle] objects and for use in deep links.
 *
 * This NavType does not allow null values (`isNullableAllowed = false`).
 */
val ChatParameterNavType: NavType<ChatParameter> = object: NavType<ChatParameter>(
	isNullableAllowed = false
) {
	override val name: String
		get() = "ChatParameter"

	/**
	 * Retrieve RecipeDetail from a bundle
	 */
	override fun get(bundle: Bundle, key: String): ChatParameter? {
		val jsonString = bundle.getString(key)
			?: return null
		println("NavType GET - Input String: $jsonString")
		return NavTypeJson.decodeFromString<ChatParameter>(jsonString)
	}

	/**
	 * Put RecipeDetail in a bundle
	 */
	override fun put(bundle: Bundle, key: String, value: ChatParameter) {
		val jsonString = value.let { NavTypeJson.encodeToString(it) }
		bundle.putString(key, jsonString)
	}

	/**
	 * Retrieve RecipeDetail from a deep link
	 */
	override fun parseValue(value: String): ChatParameter {
		println("NavType GET - Input String: $value")
		return NavTypeJson.decodeFromString<ChatParameter>(value)
	}

	/**
	 * Serialize RecipeDetail to a string for deep links
	 */
	override fun serializeAsValue(value: ChatParameter): String {
		// Serialize to a string representation for deep links
		return NavTypeJson.encodeToString(value)
	}
}