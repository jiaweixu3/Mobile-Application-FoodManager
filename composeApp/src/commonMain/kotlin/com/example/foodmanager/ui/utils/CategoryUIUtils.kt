package com.example.foodmanager.ui.utils

fun getCategoryIcon(category: String): String {
    return when (category.lowercase()) {
        "vegetables" -> "🥦"
        "fruits" -> "🍎"
        "meat" -> "🥩"
        "dairy" -> "🥛"
        "bread" -> "🍞"
        "fridge" -> "❄️"
        "pantry" -> "🥫"
        "frozen" -> "🧊"
        else -> "📦" // Default
    }
}