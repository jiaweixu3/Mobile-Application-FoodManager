package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.repository.MockShoppingRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlin.test.assertEquals

//This file defines the unit tests for the Shopping Repository created in issues 5 and 6 sprint 2
// The current tests work with an initial hardcoded list of 5 items, if this changes, all tests should be changed accordingly
internal class MockShoppingRepositoryTest {
    private val repository = MockShoppingRepository()
    @BeforeTest
    fun setUp() {
        MockDb.resetShoppingState() // Resetting list back to original size
    }

    @Test
    fun testInitialShopping() = runTest {
        val current_Inventory = repository.getShoppingList().first()


        assertEquals(5, current_Inventory.size, "Initial shopping list has 5 items")

    }

    @Test
    fun testAddShoppingItem() = runTest {
        val new_item = ShoppingItem(id = 6, name = "sausage")

        repository.addShoppingItem(new_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(6, current_Inventory.size, "Our new shopping list has 6 items")
    }

    @Test
    fun testDeleteShoppingItem() = runTest {
        val food_id = 1
        repository.deleteShoppingItem(food_id)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(4, current_Inventory.size, "Our new shopping list has 4 items")
    }

    @Test
    fun testDeleteNonExistentShoppingItem() = runTest {
        val food_id = 100
        repository.deleteShoppingItem(food_id)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list should not change in size")
    }

    @Test
    fun testUpdateShoppingItem() = runTest {
        val updated_item = ShoppingItem(id = 1, name = "Apples", true)
        repository.updateShoppingItem(updated_item)
        val current_Inventory = repository.getShoppingList().first()

        assertEquals(5, current_Inventory.size, "Our new shopping list should have 5 items")

        val updatedItem = current_Inventory.first { it.id == updated_item.id }
        assertEquals(true, updatedItem.isChecked, "Apples now appear on the shopping list")
    }


}