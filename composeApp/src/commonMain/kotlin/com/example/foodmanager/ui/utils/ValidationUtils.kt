package com.example.foodmanager.utils

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.isNotBlank() && email.matches(Regex(emailRegex))
}



fun getPasswordError(password: String): String? {
    if (password.length < 8) return "Password must be at least 8 characters."
    if (password.length > 32) return "Password must be less than 32 characters."
    if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter."
    if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter."
    if (!password.any { it.isDigit() }) return "Password must contain a number."
    if (!password.any { !it.isLetterOrDigit() }) return "Password must contain a special character."

    return null
}