package com.example.foodmanager.domain

import com.example.foodmanager.domain.useCase.ValidateFoodItemUseCase
import com.example.foodmanager.domain.useCase.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateFoodItemUseCaseTest {


    private lateinit var validateFoodItem: ValidateFoodItemUseCase

    @Before
    fun setUp() {
        validateFoodItem = ValidateFoodItemUseCase()
    }

    @Test
    fun `valid input returns Success`() {
        val tomorrow = System.currentTimeMillis() + 86400000
        val result = validateFoodItem.execute("Manzana", "5", tomorrow)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `empty name returns Error`() {
        val futureDate = System.currentTimeMillis() + 86400000

        // Empty name
        val result = validateFoodItem.execute("", "5", futureDate)

        // Verify that it returns error
        assertTrue(result is ValidationResult.Error)
        assertEquals("Name cannot be empty.", (result as ValidationResult.Error).message)
    }

    @Test
    fun `invalid quantity returns Error`() {
        val futureDate = System.currentTimeMillis() + 86400000

        // Test case 1: Quantity is 0
        var result = validateFoodItem.execute("Eoeo", "0", futureDate)
        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Quantity must be a number greater than 0.",
            (result as ValidationResult.Error).message
        )

        // Test case 2: Negative quantity
        result = validateFoodItem.execute("Eoeo", "-3", futureDate)
        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Quantity must be a number greater than 0.",
            (result as ValidationResult.Error).message
        )

        // Test case 3: Non-numeric quantity
        result = validateFoodItem.execute("Eoeo", "abc", futureDate)
        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Quantity must be a number greater than 0.",
            (result as ValidationResult.Error).message
        )
    }

    @Test
    fun `null expiry date returns Error`() {
        // Null as the expiry date
        val result = validateFoodItem.execute("Eoeo", "5", null)

        assertTrue(result is ValidationResult.Error)
        assertEquals("An expiry date must be selected.", (result as ValidationResult.Error).message)
    }

    @Test
    fun `past expiry date returns Error`() {
        // Simulate item expired 2 days ago
        val pastDate = System.currentTimeMillis() - (86400000 * 2)

        val result = validateFoodItem.execute("Eoeo", "5", pastDate)

        assertTrue(result is ValidationResult.Error)
        assertEquals("You cannot add already expired items.", (result as ValidationResult.Error).message)
    }
}