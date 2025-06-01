package dev.bitvictory.aeon.core.domain.entities.recipe.fio

import dev.bitvictory.aeon.core.domain.entities.recipe.Quantity
import kotlinx.serialization.Serializable
import java.util.regex.*

@Serializable
data class IngredientFIO(
	val canonicalName: String,
	val localizations: List<LocalizationFIO>,
	val quantity: String,
	val canonicalUnitOfMeasure: String?,
	val unitOfMeasure: String?,
	val note: String? = null
) {
	fun parsedQuantity(): Quantity {
		val trimmedInput = quantity.trim()

		// Regex to find a number (integer or decimal) optionally followed by a unit.
		// This regex tries to capture:
		// 1. An optional leading fraction (e.g., "1/2", "3/4")
		// 2. An optional whole number part (e.g., "1", "1 1/2" where "1" is whole)
		// 3. A number (integer or decimal) - this is the primary numeric capture
		// 4. The rest of the string as the unit
		// Breakdown:
		// - Group 1 (number): (?:(\d+\s*\/\s*\d+)\s*)?   Optional fraction like "1/2 " or "1 / 2 "
		//                  (?:(\d+)\s+)?              Optional whole number for mixed fractions like "1 " in "1 1/2"
		//                  (\d*\.?\d+)                The main number (e.g., "2", "0.5", "100")
		// - Group 2 (unit):   \s*(.*)                     Any characters following the number, trimmed
		// This regex is a bit more complex to handle cases like "1 1/2 cups" or "1/2 tsp"
		val pattern = Pattern.compile(
			"""^\s*(?:(\d+\s*/\s*\d+)\s*)?(?:(\d+)\s+)?(\d*\.?\d+)?\s*(.*)$""",
			Pattern.CASE_INSENSITIVE
		)
		val matcher = pattern.matcher(trimmedInput)

		if (matcher.matches()) {
			val fractionStr = matcher.group(1)?.replace("\\s".toRegex(), "") // e.g., "1/2"
			val mixedWholeStr = matcher.group(2) // e.g., "1" from "1 1/2 cups"
			val numberStr = matcher.group(3) // e.g., "0.5" or "2"
			var totalNumericValue: Double? = null

			try {
				// Handle fraction part
				fractionStr?.let {
					val parts = it.split("/")
					if (parts.size == 2) {
						val numerator = parts[0].toDouble()
						val denominator = parts[1].toDouble()
						if (denominator != 0.0) {
							totalNumericValue = (totalNumericValue ?: 0.0) + (numerator / denominator)
						}
					}
				}

				// Handle whole number part for mixed fractions
				mixedWholeStr?.let {
					totalNumericValue = (totalNumericValue ?: 0.0) + it.toDouble()
				}

				// Handle main number part
				numberStr?.takeIf { it.isNotBlank() }?.let {
					totalNumericValue = (totalNumericValue ?: 0.0) + it.toDouble()
				}

			} catch (e: NumberFormatException) {
				// Could happen if parts of the regex match non-numbers unexpectedly,
				// though the regex tries to prevent this for numeric groups.
				// If totalNumericValue is still null, it means no valid number was parsed.
			}

			// If no numeric value was found at all by the regex, but it matched (e.g. only unit was found)
			if (totalNumericValue == null && numberStr == null && fractionStr == null && mixedWholeStr == null) {
				// This might mean the input was just a unit, or a non-numeric quantity that wasn't in our initial map
				// For example "cups" or "some"
				return Quantity(0.0, canonicalUnitOfMeasure, unitOfMeasure)
			}


			return Quantity(totalNumericValue ?: 0.0, canonicalUnitOfMeasure, unitOfMeasure)
		}

		// If regex doesn't match, it might be just a unit or a phrase we don't understand numerically
		// or the LLM gave something completely unexpected.
		// We can assume the whole string is a non-numeric quantity or unit if it's not empty.
		return Quantity(0.0, canonicalUnitOfMeasure, unitOfMeasure)
	}
}