package com.example.foodmanager.ui.utils

import com.example.foodmanager.domain.normalizeCategory
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
object CategoryConstants {
    val menuCategories = listOf(
        "Vegetables", "Fruits", "Meat", "Dairy", "Bread", "Pasta", "Rice", "Frozen", "Other"
    )

    fun getIcon(category: String): String {
        return when (normalizeCategory(category)) {
            "Vegetables" -> "🥦"
            "Fruits" -> "🍎"
            "Meat" -> "🥩"
            "Dairy" -> "🥛"
            "Bread" -> "🍞"
            "Pasta" -> "🍝"
            "Rice" -> "🍚"
            "Frozen" -> "🧊"
            "Other" -> "📦"
            else -> "📦"
        }
    }

    private fun getDaysForCategory(category: String): Int {
        return when (normalizeCategory(category)) {
            "Meat" -> 3
            "Vegetables", "Fruits" -> 7
            "Dairy" -> 10
            "Bread" -> 5
            "Frozen" -> 180
            "Pasta", "Rice" -> 365
            "Other" -> 10
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
