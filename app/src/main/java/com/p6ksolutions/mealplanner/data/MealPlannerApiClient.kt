package com.p6ksolutions.mealplanner.data

import android.util.Log
import com.p6ksolutions.mealplanner.model.AuthSession
import com.p6ksolutions.mealplanner.model.MealPlanEntry
import com.p6ksolutions.mealplanner.model.Recipe
import com.p6ksolutions.mealplanner.model.User
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MealPlannerApiClient(
    private val baseUrl: String = "http://10.0.2.2:8000/api"
) {
    private companion object {
        const val TAG = "MealPlannerApi"
        const val ERROR_BODY_PREVIEW_LENGTH = 500
    }

    suspend fun login(username: String, password: String): AuthSession {
        val response = postJson(
            path = "/auth/login/",
            body = JSONObject()
                .put("username", username)
                .put("password", password)
        )
        return AuthSession(
            token = response.getString("token"),
            user = response.getJSONObject("user").toUser()
        )
    }

    suspend fun signup(username: String, email: String, password: String): AuthSession {
        val response = postJson(
            path = "/auth/signup/",
            body = JSONObject()
                .put("username", username)
                .put("email", email)
                .put("password", password)
        )
        return AuthSession(
            token = response.getString("token"),
            user = response.getJSONObject("user").toUser()
        )
    }

    suspend fun logout(token: String) {
        postJson(
            path = "/auth/logout/",
            body = JSONObject(),
            token = token,
            allowEmptyResponse = true
        )
    }

    suspend fun getCurrentUser(token: String): User {
        val response = getJson("/auth/me/", token)
        return response.getJSONObject("user").toUser()
    }

    suspend fun getRecipes(token: String): List<Recipe> {
        val response = getJson("/recipes/", token)
        return response.getJSONArray("recipes").toRecipes()
    }

    suspend fun getSuggestions(token: String): List<Recipe> {
        val response = getJson("/suggestions/", token)
        return response.getJSONArray("suggestions").toRecipes()
    }

    suspend fun getMealPlan(token: String): List<MealPlanEntry> {
        val response = getJson("/meal-plan/", token)
        val entries = response.getJSONArray("mealPlan")
        return List(entries.length()) { index ->
            entries.getJSONObject(index).toMealPlanEntry()
        }
    }

    private suspend fun getJson(path: String, token: String? = null): JSONObject = withContext(Dispatchers.IO) {
        val url = "$baseUrl$path"
        val startedAt = System.currentTimeMillis()
        Log.d(TAG, "GET $url started")

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setAuthHeader(token)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val statusCode = connection.responseCode
            val body = connection.readBody(statusCode)
            val durationMs = System.currentTimeMillis() - startedAt

            if (statusCode !in 200..299) {
                Log.e(
                    TAG,
                    "GET $path failed status=$statusCode durationMs=$durationMs " +
                        "body=${body.take(ERROR_BODY_PREVIEW_LENGTH)}"
                )
                error("Request failed with HTTP $statusCode: $body")
            }

            Log.d(
                TAG,
                "GET $path completed status=$statusCode durationMs=$durationMs bodyBytes=${body.length}"
            )
            JSONObject(body)
        } catch (error: Exception) {
            val durationMs = System.currentTimeMillis() - startedAt
            Log.e(TAG, "GET $path failed exception=${error.message} durationMs=$durationMs", error)
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun postJson(
        path: String,
        body: JSONObject,
        token: String? = null,
        allowEmptyResponse: Boolean = false
    ): JSONObject = withContext(Dispatchers.IO) {
        val url = "$baseUrl$path"
        val startedAt = System.currentTimeMillis()
        Log.d(TAG, "POST $url started")

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setAuthHeader(token)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            connection.outputStream.bufferedWriter().use { it.write(body.toString()) }
            val statusCode = connection.responseCode
            val responseBody = connection.readBody(statusCode)
            val durationMs = System.currentTimeMillis() - startedAt

            if (statusCode !in 200..299) {
                Log.e(
                    TAG,
                    "POST $path failed status=$statusCode durationMs=$durationMs " +
                        "body=${responseBody.take(ERROR_BODY_PREVIEW_LENGTH)}"
                )
                error("Request failed with HTTP $statusCode: $responseBody")
            }

            Log.d(
                TAG,
                "POST $path completed status=$statusCode durationMs=$durationMs " +
                    "bodyBytes=${responseBody.length}"
            )

            if (allowEmptyResponse && responseBody.isBlank()) {
                JSONObject()
            } else {
                JSONObject(responseBody)
            }
        } catch (error: Exception) {
            val durationMs = System.currentTimeMillis() - startedAt
            Log.e(TAG, "POST $path failed exception=${error.message} durationMs=$durationMs", error)
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun HttpURLConnection.setAuthHeader(token: String?) {
        if (!token.isNullOrBlank()) {
            setRequestProperty("Authorization", "Token $token")
        }
    }

    private fun HttpURLConnection.readBody(statusCode: Int): String {
        val stream = if (statusCode in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun JSONArray.toRecipes(): List<Recipe> = List(length()) { index ->
        getJSONObject(index).toRecipe()
    }

    private fun JSONObject.toMealPlanEntry(): MealPlanEntry = MealPlanEntry(
        id = getInt("id"),
        date = getString("date"),
        mealType = getString("mealType"),
        recipe = if (isNull("recipe")) null else getJSONObject("recipe").toRecipe()
    )

    private fun JSONObject.toRecipe(): Recipe = Recipe(
        id = getInt("id"),
        name = getString("name"),
        description = getString("description"),
        prepTime = getInt("prepTime"),
        tags = getJSONArray("tags").toStringList(),
        ingredients = getJSONArray("ingredients").toStringList(),
        instructions = getJSONArray("instructions").toStringList()
    )

    private fun JSONArray.toStringList(): List<String> = List(length()) { index ->
        getString(index)
    }

    private fun JSONObject.toUser(): User = User(
        id = getInt("id"),
        username = getString("username"),
        email = getString("email")
    )
}
