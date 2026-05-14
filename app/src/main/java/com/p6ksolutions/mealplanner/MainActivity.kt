package com.p6ksolutions.mealplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p6ksolutions.mealplanner.model.MealPlanEntry
import com.p6ksolutions.mealplanner.model.Recipe
import com.p6ksolutions.mealplanner.ui.MealPlannerUiState
import com.p6ksolutions.mealplanner.ui.MealPlannerViewModel
import com.p6ksolutions.mealplanner.ui.theme.MealPlannerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealPlannerTheme {
                MealPlannerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerApp(viewModel: MealPlannerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    MealPlannerContent(
        uiState = uiState,
        onLogin = viewModel::login,
        onSignup = viewModel::signup,
        onLogout = viewModel::logout,
        onRetry = viewModel::loadMealPlannerData
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerContent(
    uiState: MealPlannerUiState,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val tabs = listOf("Week", "Recipes", "Suggest")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = {
                    Column {
                        Text(selectedRecipe?.name ?: "Meal Planner")
                        if (selectedRecipe == null) {
                            Text(
                                text = uiState.user?.let { "Signed in as ${it.username}" }
                                    ?: "Sign in to plan dinners, save recipes, find ideas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedRecipe != null) {
                        TextButton(onClick = { selectedRecipe = null }) {
                            Text("Back")
                        }
                    }
                },
                actions = {
                    if (uiState.user != null) {
                        TextButton(onClick = {
                            selectedRecipe = null
                            onLogout()
                        }) {
                            Text("Logout")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.user != null) {
                NavigationBar {
                    tabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                selectedRecipe = null
                            },
                            label = { Text(label) },
                            icon = {},
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            val recipe = selectedRecipe
            if (uiState.user == null) {
                AuthScreen(
                    isLoggingIn = uiState.isLoggingIn,
                    isSigningUp = uiState.isSigningUp,
                    loginErrorMessage = uiState.loginErrorMessage,
                    signupErrorMessage = uiState.signupErrorMessage,
                    onLogin = onLogin,
                    onSignup = onSignup
                )
            } else if (uiState.isLoading) {
                LoadingState()
            } else if (uiState.errorMessage != null) {
                ErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            } else if (recipe != null) {
                RecipeDetailScreen(recipe = recipe)
            } else {
                when (selectedTab) {
                    0 -> WeekPlanScreen(
                        mealPlan = uiState.mealPlan,
                        onRecipeClick = { selectedRecipe = it }
                    )

                    1 -> RecipesScreen(
                        recipes = uiState.recipes,
                        onRecipeClick = { selectedRecipe = it }
                    )

                    else -> SuggestionsScreen(
                        suggestions = uiState.suggestions,
                        onRecipeClick = { selectedRecipe = it }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    isLoggingIn: Boolean,
    isSigningUp: Boolean,
    loginErrorMessage: String?,
    signupErrorMessage: String?,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignup by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isBusy = isLoggingIn || isSigningUp
    val errorMessage = if (isSignup) signupErrorMessage else loginErrorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignup) "Create Account" else "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isSignup) {
                "Create your meal planner account."
            } else {
                "Sign in with your meal planner account."
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            singleLine = true,
            label = { Text("Username") }
        )
        if (isSignup) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                enabled = !isBusy,
                singleLine = true,
                label = { Text("Email") }
            )
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = !isBusy,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("Password") }
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = {
                if (isSignup) {
                    onSignup(username.trim(), email.trim(), password)
                } else {
                    onLogin(username.trim(), password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            enabled = !isBusy && username.isNotBlank() && password.isNotBlank() &&
                (!isSignup || email.isNotBlank())
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isSignup) "Create Account" else "Login")
            }
        }
        TextButton(
            onClick = { isSignup = !isSignup },
            enabled = !isBusy,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (isSignup) "Already have an account? Sign in" else "Create an account")
        }
    }
}

@Composable
fun LoginScreen(
    isLoggingIn: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScreen(
        isLoggingIn = isLoggingIn,
        isSigningUp = false,
        loginErrorMessage = errorMessage,
        signupErrorMessage = null,
        onLogin = onLogin,
        onSignup = { _, _, _ -> },
        modifier = modifier
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Could not load meal planner data",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun WeekPlanScreen(
    mealPlan: List<MealPlanEntry>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentWeekMealPlan = mealPlan.fitToCurrentWeek()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "This Week",
                subtitle = "Calendar view for dinners planned across the week."
            )
        }

        item {
            CalendarWeekStrip(mealPlan = currentWeekMealPlan)
        }

        item {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(currentWeekMealPlan) { entry ->
            CalendarAgendaCard(
                entry = entry,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

private data class WeekDay(
    val date: String
)

private fun currentWeekDays(): List<WeekDay> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    val firstDayOfWeek = calendar.firstDayOfWeek

    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }

    return List(7) {
        val weekDay = WeekDay(
            date = dateFormat.format(calendar.time)
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        weekDay
    }
}

private fun List<MealPlanEntry>.fitToCurrentWeek(): List<MealPlanEntry> {
    val entriesByDate = associateBy { it.date }

    return currentWeekDays().mapIndexed { index, weekDay ->
        val backendEntry = entriesByDate[weekDay.date]

        backendEntry?.copy(
            date = weekDay.date
        ) ?: MealPlanEntry(
            id = -index - 1,
            date = weekDay.date,
            mealType = "Dinner",
            recipe = null
        )
    }
}

private fun MealPlanEntry.dateLabel(): String = date.substringAfterLast('-')

private fun MealPlanEntry.dayName(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val parsedDate = dateFormat.parse(date)
    return if (parsedDate == null) "" else dayNameFormat.format(parsedDate)
}

private fun MealPlanEntry.shortDayName(): String = dayName().take(3)

@Composable
fun RecipesScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Saved Recipes",
                subtitle = "Tap a recipe to view ingredients and instructions."
            )
        }

        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

@Composable
fun SuggestionsScreen(
    suggestions: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Recipe Suggestions",
                subtitle = "A preview of backend-powered meal ideas."
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = "Suggest quick, healthy dinners for this week",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        label = { Text("Prompt") }
                    )
                    Text(
                        text = "Suggestions are loaded from the meal planner backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        items(suggestions) { recipe ->
            RecipeCard(
                recipe = recipe,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

@Composable
fun RecipeDetailScreen(recipe: Recipe, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${recipe.prepTime} min",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                RecipeTags(tags = recipe.tags)
            }
        }

        item {
            DetailSection(title = "Ingredients") {
                recipe.ingredients.forEach { ingredient ->
                    IngredientRow(ingredient = ingredient)
                }
            }
        }

        item {
            DetailSection(title = "Instructions") {
                recipe.instructions.forEachIndexed { index, instruction ->
                    InstructionStep(
                        stepNumber = index + 1,
                        instruction = instruction
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarWeekStrip(mealPlan: List<MealPlanEntry>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        LazyRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mealPlan) { entry ->
                CalendarDayTile(entry = entry)
            }
        }
    }
}

@Composable
fun CalendarDayTile(entry: MealPlanEntry, modifier: Modifier = Modifier) {
    val hasMeal = entry.recipe != null

    Column(
        modifier = modifier
            .width(78.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                color = if (hasMeal) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = entry.shortDayName(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = entry.dateLabel(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (hasMeal) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (hasMeal) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
        )
    }
}

@Composable
fun CalendarAgendaCard(
    entry: MealPlanEntry,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.recipe == null) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .width(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = entry.shortDayName().uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = entry.dateLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            val recipe = entry.recipe
            if (recipe == null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.mealType,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    EmptyMealState()
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onRecipeClick(recipe) },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.mealType,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap for ingredients and instructions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyMealState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "No meal planned yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun RecipeCard(
    recipe: Recipe,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRecipeClick(recipe) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${recipe.prepTime} min",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            RecipeTags(tags = recipe.tags)
            Text(
                text = "Tap to view recipe",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun RecipeTags(tags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = {},
                label = { Text(tag) }
            )
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun IngredientRow(ingredient: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun InstructionStep(
    stepNumber: Int,
    instruction: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MealPlannerPreview() {
    MealPlannerTheme {
        MealPlannerContent(
            uiState = MealPlannerUiState(),
            onLogin = { _, _ -> },
            onSignup = { _, _, _ -> },
            onLogout = {},
            onRetry = {}
        )
    }
}
