package com.p6ksolutions.mealplanner.model

data class AuthSession(
    val token: String,
    val user: User
)
