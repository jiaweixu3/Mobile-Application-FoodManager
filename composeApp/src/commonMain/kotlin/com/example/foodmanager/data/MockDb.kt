package com.example.foodmanager.data

import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


// using an object as this will be better for both Inventory and Shopping List.
object MockDb {
    // Food Items for the inventory, set to private so they will not be overwritten
    private val _fooditems = MutableStateFlow(
        listOf(
            FoodItem(
                id = 1,
                name = "Milk",
                expiryDate = "2026-02-01",
                amount = 1.0,
                unit = "Carton",
                category = "Fridge",
                barcode = "123456789",
                photoUrl = "milk.jpg"
            ),
            FoodItem(
                id = 2,
                name = "Spinach",
                expiryDate = "2026-02-10",
                amount = 200.0,
                unit = "g",
                category = "Fridge"
            ),
            FoodItem(
                id = 3,
                name = "Canned Beans",
                expiryDate = "2026-03-15",
                amount = 2.0,
                unit = "Cans",
                category = "Pantry"
            )
        )
    )
    val fooditems = _fooditems.asStateFlow()

    fun addFoodItem(newItem: FoodItem) {
        // Checking the name string is not Empty
        if (newItem.name.isBlank()) {
            return
        }
        // Checking if an item already exists
        val existingItem = _fooditems.value.find {
            it.name.equals(
                newItem.name,
                ignoreCase = true
            ) && it.expiryDate == newItem.expiryDate
        }

        // If the same item exists, we update the amount of the ingredient
        if (existingItem != null) {
            val newAmount = existingItem.amount + newItem.amount
            // If amount is 0, we delete the item
            if (newAmount <= 0.0) {
                deleteFoodItem(existingItem.id)

            } else { // If not, we update the correct amount
                _fooditems.value = _fooditems.value.map { item -> //Iterating through the list to find the correct item
                    if (item.name == newItem.name && item.expiryDate == newItem.expiryDate) {
                        item.copy(amount = newAmount)
                    } else {
                        item
                    }
                }
            }
        } else { // Item does not exist, we add it to the list
            if (newItem.amount > 0.0) { //Checking there is an acutal quantity
                _fooditems.value = _fooditems.value.plus(newItem)
            }
        }
    }

    fun deleteFoodItem(item_id: Int) {
        _fooditems.value = _fooditems.value.filter { it.id != item_id } // Delete elements with the same id
    }

    fun updateFoodItem(updatedItem: FoodItem) {
        if (updatedItem.amount <= 0.0) {
            deleteFoodItem(item_id = updatedItem.id)
        } else {
            _fooditems.value = _fooditems.value.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }


    // Resetting Inventory State for Unit Tests, to avoid dependencies
    fun resetInventoryState() {
        _fooditems.value = listOf(
            FoodItem(
                id = 1,
                name = "Milk",
                expiryDate = "2026-02-01",
                amount = 1.0,
                unit = "Carton",
                category = "Fridge",
                barcode = "123456789",
                photoUrl = "milk.jpg"
            ),
            FoodItem(
                id = 2,
                name = "Spinach",
                expiryDate = "2026-02-10",
                amount = 200.0,
                unit = "g",
                category = "Fridge"
            ),
            FoodItem(
                id = 3,
                name = "Canned Beans",
                expiryDate = "2026-03-15",
                amount = 2.0,
                unit = "Cans",
                category = "Pantry"
            )
        )
    }


    // Food Items for the shopping list, set to private so they will not be overwritten
    private val _shoppingitems = MutableStateFlow(
        listOf(
            ShoppingItem(1, "Apples", false),
            ShoppingItem(2, "Bananas", true),
            ShoppingItem(3, "Milk", false),
            ShoppingItem(4, "Eggs", true),
            ShoppingItem(5, "Bread", false)
        )
    )
    val shoppingitems = _shoppingitems.asStateFlow()

    fun addShoppingItem(newShoppingItem: ShoppingItem) {
        if (newShoppingItem.name.isBlank()) {
            return
        }
        // Checking if the item already exists
        val itemExists = _shoppingitems.value.any { it.name.equals(newShoppingItem.name, ignoreCase = true) }

        // If item does not exist, we add it
        if (!itemExists) {
            _shoppingitems.value = _shoppingitems.value.plus(newShoppingItem)
        }
    }


    fun deleteShoppingItem(item_id: Int) {
        _shoppingitems.value = _shoppingitems.value.filter { it.id != item_id } // Delete elements with the same id
    }

    fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        _shoppingitems.value =
            _shoppingitems.value.map { if (it.id == updatedShoppingItem.id) updatedShoppingItem else it }
    }

    // Resetting Shopping State for Unit Tests, to avoid dependencies
    fun resetShoppingState() {
        _shoppingitems.value = listOf(
            ShoppingItem(1, "Apples", false),
            ShoppingItem(2, "Bananas", true),
            ShoppingItem(3, "Milk", false),
            ShoppingItem(4, "Eggs", true),
            ShoppingItem(5, "Bread", false)
        )
    }
}

