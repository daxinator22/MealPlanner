package com.p6ksolutions.mealplanner.model

data class MealPlanEntry(
    val id: Int,
    val date: String,
    val mealType: String,
    val recipe: Recipe?
)
