package com.p6ksolutions.mealplanner.data

import com.p6ksolutions.mealplanner.model.MealPlanEntry
import com.p6ksolutions.mealplanner.model.Recipe

data class MealPlannerData(
    val recipes: List<Recipe>,
    val suggestions: List<Recipe>,
    val mealPlan: List<MealPlanEntry>
)
