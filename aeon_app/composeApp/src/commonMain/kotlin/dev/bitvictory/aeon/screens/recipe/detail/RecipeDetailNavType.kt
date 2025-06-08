package dev.bitvictory.aeon.screens.recipe.detail

// In a suitable file, perhaps near your RecipeHeaderDTO or navigation setup
import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import kotlinx.serialization.json.Json

val NavTypeJson = Json {
	ignoreUnknownKeys = true
	encodeDefaults = true
	explicitNulls = true
}

/**
 * Custom [NavType] for [RecipeHeaderDTO] objects.
 *
 * This NavType allows passing [RecipeHeaderDTO] objects as navigation arguments.
 * It handles the serialization and deserialization of the [RecipeHeaderDTO] to and from JSON strings
 * for storage in [Bundle] objects and for use in deep links.
 *
 * This NavType does not allow null values (`isNullableAllowed = false`).
 */
val RecipeHeaderNavType: NavType<RecipeHeaderDTO> = object: NavType<RecipeHeaderDTO>(
	isNullableAllowed = false
) {
	override val name: String
		get() = "RecipeDetail"

	/**
	 * Retrieve RecipeDetail from a bundle
	 */
	override fun get(bundle: Bundle, key: String): RecipeHeaderDTO? {
		val jsonString = bundle.getString(key)
			?: return null
		println("NavType GET - Input String: $jsonString")
		return NavTypeJson.decodeFromString<RecipeHeaderDTO>(jsonString)
	}

	/**
	 * Put RecipeDetail in a bundle
	 */
	override fun put(bundle: Bundle, key: String, value: RecipeHeaderDTO) {
		val jsonString = value.let { NavTypeJson.encodeToString(it) }
		bundle.putString(key, jsonString)
	}

	/**
	 * Retrieve RecipeDetail from a deep link
	 */
	override fun parseValue(value: String): RecipeHeaderDTO {
		println("NavType GET - Input String: $value")
		return NavTypeJson.decodeFromString<RecipeHeaderDTO>(value)
	}

	/**
	 * Serialize RecipeDetail to a string for deep links
	 */
	override fun serializeAsValue(value: RecipeHeaderDTO): String {
		// Serialize to a string representation for deep links
		return NavTypeJson.encodeToString(value)
	}
}