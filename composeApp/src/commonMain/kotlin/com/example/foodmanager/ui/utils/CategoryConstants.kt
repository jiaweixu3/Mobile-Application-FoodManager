package com.example.foodmanager.ui.utils

object CategoryConstants {
    val menuCategories = listOf(
        "Vegetables",
        "Fruits",
        "Meat",
        "Dairy",
        "Bread",
        "Pasta",
        "Rice",
        "Frozen",
        "Other"
    )

    fun getIcon(category: String): String {
        return when (category.lowercase()) {
            "vegetables" -> "🥦"
            "fruits" -> "🍎"
            "meat" -> "🥩"
            "dairy" -> "🥛"
            "bread" -> "🍞"
            "pasta" -> "🍝"
            "rice" -> "🍚"
            "frozen" -> "🧊"
            else -> "📦"
        }
    }
}