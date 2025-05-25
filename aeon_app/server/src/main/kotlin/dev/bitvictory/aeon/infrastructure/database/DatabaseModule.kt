package dev.bitvictory.aeon.infrastructure.database

import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.core.domain.usecases.food.FoodPersistence
import dev.bitvictory.aeon.core.domain.usecases.recipe.RecipePersistence
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.database.repository.AdvisoryCollection
import dev.bitvictory.aeon.infrastructure.database.repository.DBHealthCollection
import dev.bitvictory.aeon.infrastructure.database.repository.FoodCollection
import dev.bitvictory.aeon.infrastructure.database.repository.RecipeCollection
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
	single { Database() }
	single { DBHealthCollection(get()) }.bind(SystemComponentHealthProvider::class)
	single { AdvisoryCollection(get()) }.bind(AdvisoryPersistence::class)
	single { RecipeCollection(get()) }.bind(RecipePersistence::class)
	single { FoodCollection(get()) }.bind(FoodPersistence::class)

}