package com.example.foodmanager.ui.utils

import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
object CategoryConstants {
    val menuCategories = listOf(
        "Vegetables", "Fruits", "Meat", "Dairy", "Bread", "Pasta", "Rice", "Frozen", "Other"
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

    private fun getDaysForCategory(category: String): Int {
        return when (category.lowercase()) {
            "meat" -> 3
            "vegetables", "fruits" -> 7
            "dairy" -> 10
            "bread" -> 5
            "frozen" -> 180
            "pasta", "rice" -> 365
            else -> 14
        }
    }

    fun getDefaultExpiryDate(category: String): String {
        val daysToAdd = getDaysForCategory(category)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")

        return formatter.format(calendar.time)
    }

    fun getDefaultExpiryMillis(category: String): Long {
        val daysToAdd = getDaysForCategory(category)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}