package com.p6ksolutions.mealplanner.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p6ksolutions.mealplanner.data.MealPlannerRepository
import com.p6ksolutions.mealplanner.model.MealPlanEntry
import com.p6ksolutions.mealplanner.model.Recipe
import com.p6ksolutions.mealplanner.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealPlannerUiState(
    val user: User? = null,
    val recipes: List<Recipe> = emptyList(),
    val suggestions: List<Recipe> = emptyList(),
    val mealPlan: List<MealPlanEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggingIn: Boolean = false,
    val isSigningUp: Boolean = false,
    val errorMessage: String? = null,
    val loginErrorMessage: String? = null,
    val signupErrorMessage: String? = null
)

class MealPlannerViewModel(
    private val repository: MealPlannerRepository = MealPlannerRepository()
) : ViewModel() {
    private companion object {
        const val TAG = "MealPlannerViewModel"
    }

    private val _uiState = MutableStateFlow(MealPlannerUiState())
    val uiState: StateFlow<MealPlannerUiState> = _uiState.asStateFlow()
    private var authToken: String? = null

    fun login(username: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Logging in user=$username")
            _uiState.update {
                it.copy(
                    isLoggingIn = true,
                    loginErrorMessage = null,
                    errorMessage = null
                )
            }

            runCatching { repository.login(username, password) }
                .onSuccess { session ->
                    Log.d(TAG, "Logged in user=${session.user.username}")
                    authToken = session.token
                    _uiState.update {
                        it.copy(
                            user = session.user,
                            isLoggingIn = false,
                            loginErrorMessage = null
                        )
                    }
                    loadMealPlannerData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Unable to log in", error)
                    _uiState.update {
                        it.copy(
                            isLoggingIn = false,
                            loginErrorMessage = error.message ?: "Unable to log in"
                        )
                    }
                }
        }
    }

    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Signing up user=$username")
            _uiState.update {
                it.copy(
                    isSigningUp = true,
                    signupErrorMessage = null,
                    errorMessage = null
                )
            }

            runCatching { repository.signup(username, email, password) }
                .onSuccess { session ->
                    Log.d(TAG, "Signed up user=${session.user.username}")
                    authToken = session.token
                    _uiState.update {
                        it.copy(
                            user = session.user,
                            isSigningUp = false,
                            signupErrorMessage = null
                        )
                    }
                    loadMealPlannerData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Unable to sign up", error)
                    _uiState.update {
                        it.copy(
                            isSigningUp = false,
                            signupErrorMessage = error.message ?: "Unable to create account"
                        )
                    }
                }
        }
    }

    fun logout() {
        val token = authToken
        authToken = null
        _uiState.update { MealPlannerUiState() }

        if (token != null) {
            viewModelScope.launch {
                runCatching { repository.logout(token) }
                    .onFailure { error -> Log.e(TAG, "Unable to log out on backend", error) }
            }
        }
    }

    fun refreshCurrentUser() {
        val token = authToken ?: return
        viewModelScope.launch {
            runCatching { repository.getCurrentUser(token) }
                .onSuccess { user -> _uiState.update { it.copy(user = user) } }
                .onFailure { error -> Log.e(TAG, "Unable to refresh current user", error) }
        }
    }

    fun loadMealPlannerData() {
        val token = authToken
        if (token == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Log in before loading meal planner data"
                )
            }
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Loading meal planner data")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getMealPlannerData(token) }
                .onSuccess { data ->
                    Log.d(
                        TAG,
                        "Loaded meal planner data recipes=${data.recipes.size} " +
                            "suggestions=${data.suggestions.size} mealPlan=${data.mealPlan.size}"
                    )
                    _uiState.update {
                        it.copy(
                            recipes = data.recipes,
                            suggestions = data.suggestions,
                            mealPlan = data.mealPlan,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Unable to load meal planner data", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load meal planner data"
                        )
                    }
                }
        }
    }
}
