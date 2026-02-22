package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.repository.MockInventoryRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlin.test.assertEquals
import kotlin.test.assertNull


//This file defines the unit tests for the Inventory Repository created in issues 5 and 6 sprint 2
internal class MockInventoryRepositoryTest { // Internal will make this not visible outside the file

    // Instance of Repository to Test
    // All tests depend on the hardcoded list being of size 3, if this changes, all tests should be changed accordingly
    private val repository = MockInventoryRepository()
    @BeforeTest
    fun setUp() {
        MockDb.resetInventoryState() // Resetting list back to original size
    }

    // Obtaining the initial elements in the Inventory
    @Test
    fun testInitialInventory() = runTest {
        val current_Inventory = repository.getInventory().first()


        assertEquals(3, current_Inventory.size, "Initial inventory has 3 items")

    }

    // Adding the Food Item
    @Test
    fun testAddFoodItem() = runTest {
        val new_item = FoodItem(id = 4, name = "Sausage", expiryDate = "2026-02-27", amount = 5.0, unit = "Links",
            category = "Fridge")

        repository.addFoodItem(new_item)
        val current_Inventory = repository.getInventory().first()

        assertEquals(4, current_Inventory.size, "Our new inventory has 4 items")
    }

    // Same item and expiry date will update the amount of the item
    @Test
    fun testAddDuplicateFoodItem() = runTest {
        val new_item = FoodItem(id = 2, name = "Spinach", expiryDate = "2026-02-10", amount = 200.0, unit = "g", category = "Fridge")
        repository.addFoodItem(new_item)
        val current_Inventory = repository.getInventory().first()
        assertEquals(3, current_Inventory.size, "Our new inventory has 3 items")

        val updatedItem = current_Inventory.first { it.id == new_item.id }
        assertEquals(400.0, updatedItem.amount, "The new spinach amount should be 400")
    }

    // Adding same item, different expiry date will be two separate items
    @Test
    fun testAddAlmostDuplicateFoodItem() = runTest {
        val new_item = FoodItem(id = 2, name = "Spinach", expiryDate = "2026-02-27", amount = 300.0, unit = "g", category = "Fridge")
        repository.addFoodItem(new_item)
        val current_Inventory = repository.getInventory().first()
        assertEquals(4, current_Inventory.size, "Our new inventory has 4 items")

        }


    // Adding an empty name food item should not yield problems
    @Test
    fun testAddEmptyFoodItem() = runTest {
        val new_item = FoodItem(id = 2, name = "", expiryDate = "2026-02-27", amount = 300.0, unit = "g", category = "Fridge")
        repository.addFoodItem(new_item)
        val current_Inventory = repository.getInventory().first()
        assertEquals(3, current_Inventory.size, "Our new inventory has 3 items")

    }

    // If an item with amount 0 is added, it should be discarded
    @Test
    fun testAddNoAmountFoodItem() = runTest {
        val new_item = FoodItem(id = 2, name = "Spinach", expiryDate = "2026-02-27", amount = 0.0, unit = "g", category = "Fridge")
        repository.addFoodItem(new_item)
        val current_Inventory = repository.getInventory().first()
        assertEquals(3, current_Inventory.size, "Our new inventory has 3 items")

    }

    // Deleting a Food Item
    @Test
    fun testDeleteFoodItem() = runTest {
        val food_id = 1
        repository.deleteFoodItem(food_id)
        val current_Inventory = repository.getInventory().first()

        assertEquals(2, current_Inventory.size, "Our new inventory has 2 items")
    }

    // Cannot delete an item which does not exist
    @Test
    fun testDeleteNonExistentFoodItem() = runTest {
        val food_id = 100
        repository.deleteFoodItem(food_id)
        val current_Inventory = repository.getInventory().first()

        assertEquals(3, current_Inventory.size, "Our new inventory should not change in size")
    }

    // An empty list should not yield problems
    @Test
    fun testEmptyList() = runTest {
        repository.deleteFoodItem(1)
        repository.deleteFoodItem(2)
        repository.deleteFoodItem(3)
        val current_Inventory = repository.getInventory().first()

        assertEquals(0, current_Inventory.size, "Our inventory is now empty")

    }

    // Updating a food item
    @Test
    fun testUpdateFoodItem() = runTest {
        val updated_item = FoodItem(id = 1, name = "Milk", expiryDate = "2026-02-01", amount = 2.0, unit = "Carton", category = "Fridge")
        repository.updateFoodItem(updated_item)
        val current_Inventory = repository.getInventory().first()

        assertEquals(3, current_Inventory.size, "Our inventory should have 3 items")

        val updatedItem = current_Inventory.first { it.id == updated_item.id }
        assertEquals(2.0, updatedItem.amount, "The new amount of milk is 2")
    }

    // Updating an item which does not exist
    @Test
    fun testUpdateNonExistentFoodItem() = runTest {
        val updated_item = FoodItem(id = 100, name = "Orange Juice",expiryDate = "2026-02-28", amount = 6.0, unit = "Carton", category = "Fridge" )
        repository.updateFoodItem(updated_item)
        val current_Inventory = repository.getInventory().first()

        assertEquals(3, current_Inventory.size, "Our inventory should have 3 items")

        val updatedItem = current_Inventory.find { it.id == updated_item.id }
        assertNull(updatedItem, "Orange Juice is not in the inventory")
    }

    // If new amount of updated item is 0, app should delete it
    @Test
    fun testUpdateZeroAmountFoodItem() = runTest{
        val updated_item = FoodItem(id = 1, name = "Milk", expiryDate = "2026-02-01", amount = 0.0, unit = "Carton", category = "Fridge")
        repository.updateFoodItem(updated_item)
        val current_Inventory = repository.getInventory().first()

        assertEquals(2, current_Inventory.size, "Our inventory should have 2 items")
    }


}