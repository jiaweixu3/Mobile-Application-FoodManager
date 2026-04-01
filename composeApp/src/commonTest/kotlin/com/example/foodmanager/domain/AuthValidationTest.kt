package com.example.foodmanager

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Assuming you have a helper object or function for this in your main code.
// If not, you can build it based on these tests!
object AuthValidator {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

class AuthValidationTest {

    // --- EMAIL TESTS ---
    @Test
    fun `test valid email returns true`() {
        assertTrue(AuthValidator.isValidEmail("user@example.com"))
        assertTrue(AuthValidator.isValidEmail("firstname.lastname@domain.co"))
    }

    @Test
    fun `test empty email returns false`() {
        assertFalse(AuthValidator.isValidEmail(""))
        assertFalse(AuthValidator.isValidEmail("   "))
    }

    @Test
    fun `test email without @ symbol returns false`() {
        assertFalse(AuthValidator.isValidEmail("userexample.com"))
    }

    @Test
    fun `test email without domain returns false`() {
        assertFalse(AuthValidator.isValidEmail("user@"))
        assertFalse(AuthValidator.isValidEmail("user@domain")) // missing the .com/.co
    }

    // --- PASSWORD TESTS ---
    @Test
    fun `test valid password returns true`() {
        assertTrue(AuthValidator.isValidPassword("securePass123!"))
        assertTrue(AuthValidator.isValidPassword("123456"))
    }

    @Test
    fun `test short password returns false`() {
        assertFalse(AuthValidator.isValidPassword("12345"))
        assertFalse(AuthValidator.isValidPassword("pass"))
    }

    @Test
    fun `test empty password returns false`() {
        assertFalse(AuthValidator.isValidPassword(""))
        assertFalse(AuthValidator.isValidPassword("     "))
    }
}