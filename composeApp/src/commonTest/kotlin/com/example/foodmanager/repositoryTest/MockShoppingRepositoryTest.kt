package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.data.repository.MockShoppingRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlin.test.assertEquals
import kotlin.test.assertNull

//This file defines the unit tests for the Shopping Repository created in issues 5 and 6 sprint 2
// The current tests work with an initial hardcoded list of 5 items, if this changes, all tests should be changed accordingly
internal class MockShoppingRepositoryTest {
    private val repository = MockShoppingRepository()
    @BeforeTest
    fun setUp() {
        MockDb.resetShoppingState() // Resetting list back to original size
    }

    // Shopping List is correctly obtained
    @Test
    fun testInitialShopping() = runTest {
        val current_Inventory = repository.getShoppingList().first()


        assertEquals(5, current_Inventory.size, "Initial shopping list has 5 items")

    }

    // Adding a shop item
    @Test
    fun testAddShoppingItem() = runTest {
        val new_item = ShoppingItem(id = 6, name = "sausage",amount = 2.0, unit = "pieces", category = "Meat")

        repository.addShoppingItem(new_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(6, current_Inventory.size, "Our new shopping list has 6 items")
    }

    // Adding a shop item, different cases
    @Test
    fun testAddDuplicatesShoppingItem() = runTest {
        val new_item = ShoppingItem(id = 1, name = "aPPleS", amount = 6.0, unit = "pieces", category = "Fruits")
        repository.addShoppingItem(new_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list has 5 items")
    }

    // Adding an empty element should not increase the size
    @Test
    fun testAddEmptyShoppingItem() = runTest {
        val new_item = ShoppingItem(id = 7, name = "",amount = 0.0, unit = "", category = "")
        repository.addShoppingItem(new_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list has 5 items")
    }

    // Deleting a Shop Item
    @Test
    fun testDeleteShoppingItem() = runTest {
        val food_id = 1
        repository.deleteShoppingItem(food_id)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(4, current_Inventory.size, "Our new shopping list has 4 items")
    }

    // Deleting an item which does not exist will not yield problems
    @Test
    fun testDeleteNonExistentShoppingItem() = runTest {
        val food_id = 100
        repository.deleteShoppingItem(food_id)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list should not change in size")
    }

    // Empty Shopping List will not yield problems
    @Test
    fun testEmptyShoppingList() = runTest {
        repository.deleteShoppingItem(1)
        repository.deleteShoppingItem(2)
        repository.deleteShoppingItem(3)
        repository.deleteShoppingItem(4)
        repository.deleteShoppingItem(5)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(0, current_Inventory.size, "Our shopping list is now empty")
    }


    @Test
    fun testUpdateShoppingItem() = runTest {
        val updated_item = ShoppingItem(
            id = 1,
            name = "Apples",
            amount = 6.0,
            unit = "pieces",
            category = "Fruits",
            isChecked = true
        )
        repository.updateShoppingItem(updated_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list should have 5 items")

        val updatedItem = current_Inventory.first { it.id == updated_item.id }
        assertEquals(true, updatedItem.isChecked, "Apples now appear on the shopping list")
    }

    // Updating an item which does not exist will not affect it
    @Test
    fun testUpdateNonExistentFoodItem() = runTest {
        val updated_item = ShoppingItem(id = 100, name = "Orange Juice", amount = 1.0, unit = "liter", category = "other")
        repository.updateShoppingItem(updated_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our inventory should have 5 items")

        val updatedItem = current_Inventory.find { it.id == updated_item.id }
        assertNull(updatedItem, "Orange Juice is not in the inventory")
    }
}