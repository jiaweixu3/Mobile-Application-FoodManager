package com.example.foodmanager.ui.utils

import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

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

    fun getDefaultExpiryDate(category: String): String {
        val daysToAdd = when (category.lowercase()) {
            "meat" -> 3
            "vegetables", "fruits" -> 7
            "dairy" -> 10
            "bread" -> 5
            "frozen" -> 180
            "pasta", "rice" -> 365
            else -> 14
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun getDefaultExpiryMillis(category: String): Long {
        val daysToAdd = when (category.lowercase()) {
            "meat" -> 3
            "vegetables", "fruits" -> 7
            "dairy" -> 10
            "bread" -> 5
            "frozen" -> 180
            "pasta", "rice" -> 365
            else -> 14
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar.timeInMillis
    }
}

