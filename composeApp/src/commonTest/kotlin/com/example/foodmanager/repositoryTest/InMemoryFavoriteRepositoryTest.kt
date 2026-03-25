package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.data.repository.InMemoryFavoriteRepository
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.domain.model.FavoriteFoodItem
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryFavoriteRepositoryTest {
    private val settingsRepository = MockSettingsRepository()
    private val repository = InMemoryFavoriteRepository(settingsRepository)

    @BeforeTest
    fun setUp() {
        MockDb.resetFavoriteState()
        MockDb.storeHousehold(Household(id = "house_1", name = "House 1"))
    }

    @Test
    fun `favorites are filtered by current household`() = runTest {
        val favorites = repository.getFavoriteItems().first()

        assertEquals(2, favorites.size)
        assertEquals(setOf("Milk", "Apples"), favorites.map { it.name }.toSet())
    }

    @Test
    fun `adding a favorite stores it for the active household`() = runTest {
        repository.addFavoriteItem(
            FavoriteFoodItem(
                id = "favorite_3",
                householdId = null,
                name = "Yogurt",
                amount = 2.0,
                unit = "units",
                category = "Dairy"
            )
        )

        val favorites = repository.getFavoriteItems().first()
        assertEquals(3, favorites.size)
        assertEquals("house_1", favorites.first { it.name == "Yogurt" }.householdId)
    }
}
