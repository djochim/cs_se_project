package dev.bitvictory.aeon.core.domain.entities.recipe.fio

import dev.bitvictory.aeon.core.domain.entities.recipe.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientFIOTest {

	// Helper to create IngredientFIO with a specific quantity string and a default unit
	private fun createFio(quantityStr: String, defaultUnit: String = "defaultUnit") =
		IngredientFIO("Test Item", listOf(LocalizationFIO("en", "Test Item")), quantityStr, defaultUnit, defaultUnit)

	@Test
	fun `getQuantity with simple number only uses default unit`() {
		val fio = createFio("2", "cups")
		assertEquals(Quantity(2.0, "cups", "cups"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with decimal number only uses default unit`() {
		val fio = createFio("0.5", "ml")
		assertEquals(Quantity(0.5, "ml", "ml"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with decimal number double unit`() {
		val fio = createFio("0.5 ml", "ml")
		assertEquals(Quantity(0.5, "ml", "ml"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with decimal number only in quantity unit`() {
		val fio = createFio("0.5 ml", "")
		assertEquals(Quantity(0.5, "ml", "ml"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with leading and trailing spaces in number only uses default unit`() {
		val fio = createFio("  3  ", "tablespoons")
		assertEquals(Quantity(3.0, "tablespoons", "tablespoons"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with non-numeric known phrase 'a pinch' ignores default unit`() {
		val fio = createFio("a pinch", "defaultUnitShouldBeIgnored")
		assertEquals(Quantity(1.0, "pinch", "pinch"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with non-numeric known phrase 'to taste' ignores default unit`() {
		val fio = createFio("to taste", "defaultUnitShouldBeIgnored")
		assertEquals(Quantity(0.0, "to taste", "to taste"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with non-numeric known phrase 'A Dash' ignores default unit`() {
		val fio = createFio("A Dash", "defaultUnitShouldBeIgnored")
		assertEquals(Quantity(1.0, "dash", "dash"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with simple fraction only uses default unit`() {
		val fio = createFio("1/2", "tsp")
		assertEquals(Quantity(0.5, "tsp", "tsp"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with fraction and spaces only uses default unit`() {
		val fio = createFio("3 / 4", "cup")
		assertEquals(Quantity(0.75, "cup", "cup"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with zero in fraction denominator uses default unit`() {
		val fio = createFio("1/0", "cup")
		// Behavior: fraction parsing results in 0.0 or initial totalNumericValue remains null.
		// Then it defaults to 0.0 and uses the default unit.
		assertEquals(Quantity(0.0, "cup", "cup"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with empty string uses default unit and 0 value`() {
		val fio = createFio("", "kg")
		assertEquals(Quantity(0.0, "kg", "kg"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with only whitespace uses default unit and 0 value`() {
		val fio = createFio("   ", "lbs")
		assertEquals(Quantity(0.0, "lbs", "lbs"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with completely unparseable string uses default unit and 0 value`() {
		// This hits the final return Quantity(0.0, unitOfMeasure)
		val fio = createFio("some random stuff", "pieces")
		assertEquals(Quantity(0.0, "pieces", "pieces"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with unit only in quantity string still uses default unit and 0 value`() {
		// Based on the logic: if (totalNumericValue == null && numberStr == null && fractionStr == null && mixedWholeStr == null)
		// it returns Quantity(0.0, unitOfMeasure)
		val fio = createFio("cups", "defaultUnit") // "cups" is matcher.group(4)
		assertEquals(Quantity(0.0, "defaultUnit", "defaultUnit"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with number like '5' without explicit unit uses default unit`() {
		val fio = createFio("5", "items")
		assertEquals(Quantity(5.0, "items", "items"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with only a decimal point (e_g_ `() {
		val fio = createFio(".5", "units")
		assertEquals(Quantity(0.5, "units", "units"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with leading zero decimal uses default unit`() {
		val fio = createFio("0.75", "liters")
		assertEquals(Quantity(0.75, "liters", "liters"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity with only fraction and unit in string uses default unit`() {
		val fio = createFio("1/4 grams", "defaultUnit")
		// "1/4" is fractionStr, "grams" is unitStr.
		// Returns Quantity(0.25, unitOfMeasure)
		assertEquals(Quantity(0.25, "defaultUnit", "defaultUnit"), fio.parsedQuantity())
	}

	@Test
	fun `getQuantity handles number with comma by splitting number and unit`() {
		// Regex: `(\d*\.?\d+)?` captures "1". `\s*(.*)` captures ",5 kg"
		// This will result in 1.0 and unitOfMeasure.
		val fio = createFio("1,5 kg", "defaultUnit")
		assertEquals(Quantity(1.0, "defaultUnit", "defaultUnit"), fio.parsedQuantity())
	}

}