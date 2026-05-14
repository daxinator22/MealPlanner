package com.p6ksolutions.mealplanner.data

class MealPlannerRepository(
    private val apiClient: MealPlannerApiClient = MealPlannerApiClient()
) {
    suspend fun login(username: String, password: String) = apiClient.login(username, password)

    suspend fun signup(username: String, email: String, password: String) =
        apiClient.signup(username, email, password)

    suspend fun logout(token: String) = apiClient.logout(token)

    suspend fun getCurrentUser(token: String) = apiClient.getCurrentUser(token)

    suspend fun getMealPlannerData(token: String) = MealPlannerData(
        recipes = apiClient.getRecipes(token),
        suggestions = apiClient.getSuggestions(token),
        mealPlan = apiClient.getMealPlan(token)
    )
}
