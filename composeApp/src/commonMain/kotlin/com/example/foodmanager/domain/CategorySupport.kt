package com.example.foodmanager.domain

private val categoryAliases = mapOf(
    "vegetable" to "Vegetables",
    "vegetables" to "Vegetables",
    "fruit" to "Fruits",
    "fruits" to "Fruits",
    "meat" to "Meat",
    "dairy" to "Dairy",
    "bread" to "Bread",
    "pasta" to "Pasta",
    "rice" to "Rice",
    "frozen" to "Frozen",
    "other" to "Other"
)

fun normalizeCategory(category: String?): String {
    val key = category
        ?.trim()
        ?.lowercase()
        ?.replace('_', ' ')
        ?.replace(Regex("\\s+"), " ")
        .orEmpty()

    return categoryAliases[key] ?: "Other"
}

fun favoriteKey(name: String, unit: String?, category: String?): String {
    val normalizedName = name.trim().lowercase()
    val normalizedUnit = unit?.trim()?.lowercase().orEmpty()
    val normalizedCategory = normalizeCategory(category).lowercase()
    return "$normalizedName|$normalizedUnit|$normalizedCategory"
}
