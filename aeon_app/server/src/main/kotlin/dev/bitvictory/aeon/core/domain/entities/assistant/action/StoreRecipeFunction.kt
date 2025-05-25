package dev.bitvictory.aeon.core.domain.entities.assistant.action

data object StoreRecipeFunction: AeonFunction(
	name = "storeRecipe",
	description = "Stores a recipe into the database if is is commanded.",
	strictMode = true,
	parameters = """
		{
			"type": "object",
			"properties": {
			  "name": {
				"type": "string",
				"description": "The name of the recipe."
			  },
			  "ingredients": {
				"type": "array",
				"items": {
				  "type": "object",
				  "properties": {
					"name": {
					  "type": "string",
					  "description": "Name of the ingredient."
					},
					"quantity": {
					  "type": "number",
					  "description": "The quantity value (e.g., 200, 0.5)."
					},
					"unitOfMeasure": {
					  "type": "string",
					  "description": "The unit for the quantity (e.g., grams, ml, pieces)."
					},
					"note": {
					  "type": "string",
					  "description": "Additional details (e.g., 'chopped', 'divided').",
					  "nullable": true
					}
				  },
				  "required": [
					"name",
					"quantity",
					"unitOfMeasure",
					"note"
				  ],
				  "additionalProperties": false
				},
				"description": "List of ingredients required for the recipe."
			  },
			  "steps": {
				"type": "array",
				"items": {
				  "type": "object",
				  "properties": {
					"stepNumber": {
					  "type": "integer",
					  "description": "The order number of the step."
					},
					"description": {
					  "type": "string",
					  "description": "Description of what to do in this step."
					}
				  },
				  "required": [
					"stepNumber",
					"description"
				  ],
				  "additionalProperties": false
				},
				"description": "List of steps to prepare the recipe."
			  },
			  "tips": {
				"type": "array",
				"items": {
				  "type": "string"
				},
				"description": "Optional list of tips for preparing or serving the recipe."
			  }
			},
			"required": [
			  "name",
			  "ingredients",
			  "steps",
			  "tips"
			],
			"additionalProperties": false
	  	}
	"""
)