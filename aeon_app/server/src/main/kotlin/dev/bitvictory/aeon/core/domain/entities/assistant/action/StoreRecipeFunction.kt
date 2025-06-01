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
			"language": {
			  "type": "string",
			  "description": "Two-letter language code"
			},
			"description": {
			  "type": "string",
			  "description": "Description of the recipe"
			},
			"ingredients": {
			  "type": "array",
			  "items": {
				"type": "object",
				"properties": {
				  "canonicalName": {
					"type": "string",
					"description": "English name of the ingredient."
				  },
				  "localizations": {
					"type": "array",
					"description": "name of the ingredient in other languages",
					"items": {
					  "type": "object",
					  "properties": {
						"lang": {
						  "type": "string",
						  "description": "Two-letter language code"
						},
						"name": {
						  "type": "string",
						  "description": "translated name"
						}
					  },
					  "required": [
						"lang",
						"name"
					  ],
					  "additionalProperties": false
					}
				  },
				  "quantity": {
					"type": "number",
					"description": "The quantity value (e.g., 200, 0.5)."
				  },
				  "canonicalUnitOfMeasure": {
					"type": "string",
					"description": "The unit for the quantity in english (e.g., grams, ml, pieces)."
				  },
				  "unitOfMeasure": {
					"type": "string",
					"description": "The unit for the quantity in users language."
				  },
				  "note": {
					"type": "string",
					"description": "Additional details (e.g., 'chopped', 'divided').",
					"nullable": true
				  }
				},
				"required": [
				  "canonicalName",
				  "localizations",
				  "quantity",
				  "canonicalUnitOfMeasure",
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
			  "description": "Optional list of tips for preparing or serving the recipe.",
			  "items": {
				"type": "string"
			  }
			}
		  },
		  "required": [
			"name",
			"description",
			"language",
			"ingredients",
			"steps",
			"tips"
		  ],
		  "additionalProperties": false
		}
	"""
)