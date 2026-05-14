package com.p6ksolutions.mealplanner.model

data class MealPlanEntry(
    val id: Int,
    val day: String,
    val dateLabel: String,
    val mealType: String,
    val recipe: Recipe?
)
