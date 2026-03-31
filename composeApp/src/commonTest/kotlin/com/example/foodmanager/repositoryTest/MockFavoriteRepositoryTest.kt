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

class MockFavoriteRepositoryTest {
    private val settingsRepository = MockSettingsRepository()
    private val repository = InMemoryFavoriteRepository(settingsRepository)

    @BeforeTest
    fun setUp() {
        MockDb.resetFavoriteState()
        MockDb.storeHousehold(Household(id = "house_1", name = "House 1"))
    }

    // Favorites are filtered by current household
    @Test
    fun testFilteredFavorites() = runTest {
        val favorites = repository.getFavoriteItems().first()

        assertEquals(2, favorites.size)
        assertEquals(setOf("Milk", "Apples"), favorites.map { it.name }.toSet())
    }

    // Adding a favorite item stores it for the active household
    @Test
    fun testAddFavoriteItem() = runTest {
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

    // Deleting a favorite item removes it
    @Test
    fun testDeleteFavoriteItem() = runTest {
        val favorites = repository.getFavoriteItems().first()
        val itemToDelete = favorites.first()

        repository.deleteFavoriteItem(itemToDelete.id ?: "")

        val updatedFavorites = repository.getFavoriteItems().first()

        assertEquals(1, updatedFavorites.size,  "Size should decrease by 1")
        assertEquals(
            false,
            updatedFavorites.any {it.id == itemToDelete.id},
            "Deleted item will not appear in the list"
        )
    }


}
