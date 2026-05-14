package com.p6ksolutions.mealplanner.model

data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val prepTime: Int,
    val tags: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>
)
