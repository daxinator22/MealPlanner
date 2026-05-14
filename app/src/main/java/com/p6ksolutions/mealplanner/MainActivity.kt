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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.p6ksolutions.mealplanner.ui.theme.MealPlannerTheme

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
fun MealPlannerApp() {
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
                                text = "Plan dinners, save recipes, find ideas",
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
                }
            )
        },
        bottomBar = {
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
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            val recipe = selectedRecipe
            if (recipe != null) {
                RecipeDetailScreen(recipe = recipe)
            } else {
                when (selectedTab) {
                    0 -> WeekPlanScreen(
                        mealPlan = sampleMealPlan,
                        onRecipeClick = { selectedRecipe = it }
                    )

                    1 -> RecipesScreen(
                        recipes = sampleRecipes,
                        onRecipeClick = { selectedRecipe = it }
                    )

                    else -> SuggestionsScreen(
                        suggestions = sampleSuggestions,
                        onRecipeClick = { selectedRecipe = it }
                    )
                }
            }
        }
    }
}

@Composable
fun WeekPlanScreen(
    mealPlan: List<MealPlanEntry>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
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
            CalendarWeekStrip(mealPlan = mealPlan)
        }

        item {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(mealPlan) { entry ->
            CalendarAgendaCard(
                entry = entry,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

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
                        text = "Backend integration coming soon. These suggestions are sample data.",
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
            text = entry.day.take(3),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = entry.dateLabel,
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
                    text = entry.day.take(3).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = entry.dateLabel,
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

data class Recipe(
    val name: String,
    val description: String,
    val prepTime: Int,
    val tags: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>
)

data class MealPlanEntry(
    val day: String,
    val dateLabel: String,
    val mealType: String,
    val recipe: Recipe?
)

private val sampleRecipes = listOf(
    Recipe(
        name = "Lemon Herb Chicken Bowls",
        description = "Grilled chicken with rice, cucumber, tomatoes, and a bright yogurt sauce.",
        prepTime = 35,
        tags = listOf("Healthy", "High Protein", "Dinner"),
        ingredients = listOf(
            "2 chicken breasts",
            "2 cups cooked rice",
            "1 cucumber, chopped",
            "1 cup cherry tomatoes, halved",
            "1/2 cup Greek yogurt",
            "1 lemon",
            "Fresh parsley and dill"
        ),
        instructions = listOf(
            "Cook the rice according to package directions.",
            "Season the chicken with salt, pepper, lemon zest, and herbs.",
            "Grill or sear the chicken until cooked through, then slice.",
            "Mix Greek yogurt with lemon juice, herbs, salt, and pepper.",
            "Assemble rice, vegetables, chicken, and sauce in bowls."
        )
    ),
    Recipe(
        name = "Veggie Pesto Pasta",
        description = "Pasta tossed with basil pesto, roasted zucchini, cherry tomatoes, and parmesan.",
        prepTime = 25,
        tags = listOf("Quick", "Vegetarian", "Family"),
        ingredients = listOf(
            "12 oz pasta",
            "1/2 cup basil pesto",
            "2 zucchini, sliced",
            "1 cup cherry tomatoes",
            "1/3 cup grated parmesan",
            "Olive oil",
            "Salt and black pepper"
        ),
        instructions = listOf(
            "Boil pasta in salted water until al dente.",
            "Roast zucchini and tomatoes with olive oil, salt, and pepper.",
            "Reserve a splash of pasta water, then drain the pasta.",
            "Toss pasta with pesto, vegetables, and reserved pasta water.",
            "Finish with parmesan and serve warm."
        )
    ),
    Recipe(
        name = "Turkey Taco Lettuce Wraps",
        description = "Seasoned turkey, black beans, avocado, salsa, and crunchy romaine leaves.",
        prepTime = 30,
        tags = listOf("Low Carb", "Quick", "Meal Prep"),
        ingredients = listOf(
            "1 lb ground turkey",
            "1 tbsp taco seasoning",
            "1 cup black beans",
            "1 avocado, diced",
            "1/2 cup salsa",
            "Romaine lettuce leaves",
            "Lime wedges"
        ),
        instructions = listOf(
            "Brown the turkey in a skillet over medium heat.",
            "Add taco seasoning and a splash of water, then simmer briefly.",
            "Warm the black beans and prep avocado, salsa, and lettuce.",
            "Spoon turkey and beans into lettuce leaves.",
            "Top with avocado, salsa, and lime juice."
        )
    ),
    Recipe(
        name = "Coconut Lentil Curry",
        description = "Red lentils simmered with coconut milk, ginger, garlic, and warm curry spices.",
        prepTime = 40,
        tags = listOf("Vegetarian", "Cozy", "One Pot"),
        ingredients = listOf(
            "1 cup red lentils",
            "1 can coconut milk",
            "2 cups vegetable broth",
            "1 onion, diced",
            "2 garlic cloves, minced",
            "1 tbsp grated ginger",
            "2 tbsp curry powder"
        ),
        instructions = listOf(
            "Saute onion, garlic, and ginger until fragrant.",
            "Stir in curry powder and cook for 30 seconds.",
            "Add lentils, coconut milk, and vegetable broth.",
            "Simmer until lentils are tender and creamy.",
            "Season to taste and serve with rice or naan."
        )
    ),
    Recipe(
        name = "Salmon Rice Plates",
        description = "Roasted salmon served with steamed rice, edamame, carrots, and sesame sauce.",
        prepTime = 30,
        tags = listOf("Omega-3", "Balanced", "Dinner"),
        ingredients = listOf(
            "2 salmon fillets",
            "2 cups cooked rice",
            "1 cup shelled edamame",
            "1 carrot, shredded",
            "2 tbsp soy sauce",
            "1 tbsp sesame oil",
            "Sesame seeds"
        ),
        instructions = listOf(
            "Roast salmon until it flakes easily with a fork.",
            "Steam edamame and prepare the rice.",
            "Whisk soy sauce, sesame oil, and a splash of water.",
            "Build plates with rice, salmon, edamame, and carrot.",
            "Drizzle with sauce and sprinkle with sesame seeds."
        )
    )
)

private val sampleSuggestions = listOf(
    Recipe(
        name = "Sheet Pan Fajita Bowls",
        description = "Peppers, onions, and chicken roasted together for easy rice bowls.",
        prepTime = 35,
        tags = listOf("Suggested", "Sheet Pan", "Easy"),
        ingredients = listOf(
            "2 chicken breasts, sliced",
            "2 bell peppers, sliced",
            "1 onion, sliced",
            "2 tbsp fajita seasoning",
            "2 cups cooked rice",
            "Salsa",
            "Sour cream"
        ),
        instructions = listOf(
            "Toss chicken, peppers, and onion with fajita seasoning.",
            "Spread everything on a sheet pan.",
            "Roast until the chicken is cooked through and vegetables are tender.",
            "Serve over rice with salsa and sour cream."
        )
    ),
    Recipe(
        name = "Miso Mushroom Noodles",
        description = "Savory noodles with mushrooms, bok choy, scallions, and a miso broth base.",
        prepTime = 20,
        tags = listOf("Suggested", "Quick", "Vegetarian"),
        ingredients = listOf(
            "8 oz noodles",
            "2 cups sliced mushrooms",
            "2 cups chopped bok choy",
            "2 tbsp miso paste",
            "3 cups vegetable broth",
            "2 scallions",
            "Sesame oil"
        ),
        instructions = listOf(
            "Cook noodles according to package directions.",
            "Saute mushrooms in sesame oil until browned.",
            "Add broth and bok choy, then simmer until tender.",
            "Whisk in miso paste off the heat.",
            "Serve broth and vegetables over noodles with scallions."
        )
    ),
    Recipe(
        name = "Greek Chickpea Pitas",
        description = "Warm pitas filled with chickpeas, cucumber salad, feta, and tzatziki.",
        prepTime = 18,
        tags = listOf("Suggested", "No Cook", "Lunch"),
        ingredients = listOf(
            "4 pita breads",
            "1 can chickpeas, drained",
            "1 cucumber, chopped",
            "1 cup cherry tomatoes",
            "1/2 cup feta",
            "1/2 cup tzatziki",
            "Fresh dill"
        ),
        instructions = listOf(
            "Warm the pitas in a skillet or oven.",
            "Toss chickpeas, cucumber, tomatoes, feta, and dill together.",
            "Spread tzatziki inside each pita.",
            "Fill pitas with the chickpea mixture and serve."
        )
    )
)

private val sampleMealPlan = listOf(
    MealPlanEntry("Monday", "12", "Dinner", sampleRecipes[0]),
    MealPlanEntry("Tuesday", "13", "Dinner", sampleRecipes[1]),
    MealPlanEntry("Wednesday", "14", "Dinner", null),
    MealPlanEntry("Thursday", "15", "Dinner", sampleRecipes[2]),
    MealPlanEntry("Friday", "16", "Dinner", sampleRecipes[3]),
    MealPlanEntry("Saturday", "17", "Dinner", null),
    MealPlanEntry("Sunday", "18", "Dinner", sampleRecipes[4])
)

@Preview(showBackground = true)
@Composable
fun MealPlannerPreview() {
    MealPlannerTheme {
        MealPlannerApp()
    }
}
