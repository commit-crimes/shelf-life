package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.navigation.*
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
    val selectedRecipe = listRecipesViewModel.selectedRecipe.collectAsState().value
        ?: return Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recipe selected. Should not happen",
                modifier = Modifier.testTag("noRecipeSelectedMessage"),
                color = Color.Red
            )
        }

    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    HouseHoldSelectionDrawer(
        scope = scope,
        drawerState = drawerState,
        householdViewModel = householdViewModel,
        navigationActions = navigationActions
    ) {
        if (selectedHousehold == null) {
            FirstTimeWelcomeScreen(navigationActions, householdViewModel)
        } else {
            Scaffold(
                modifier = Modifier.testTag("individualRecipesScreen"),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("topBar"),
                        navigationIcon = {
                            IconButton(
                                onClick = { navigationActions.goBack() },
                                modifier = Modifier.testTag("goBackArrow")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Go back Icon"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = selectedRecipe.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    )
                },
                bottomBar = {
                    BottomNavigationMenu(
                        onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                        tabList = LIST_TOP_LEVEL_DESTINATION,
                        selectedItem = Route.RECIPES
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navigationActions.navigateTo(Screen.SERVINGS_SCREEN)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.testTag("startButton")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow, // Replace with a suitable icon
                            contentDescription = "Start Recipe"
                        )
                    }
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .testTag("recipe")
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google_logo),
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .width(537.dp)
                                    .height(159.dp)
                                    .testTag("recipeImage"),
                                contentScale = ContentScale.FillWidth
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Servings: ${selectedRecipe.servings}",
                                    modifier = Modifier.testTag("recipeServings")
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Time: ${selectedRecipe.time.inWholeMinutes} min",
                                    modifier = Modifier.testTag("recipeTime")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(modifier = Modifier.testTag("recipeIngredients")) {
                                selectedRecipe.ingredients.forEach { ingredient ->
                                    DisplayIngredient(ingredient)
                                }
                            }

                            Column(modifier = Modifier.testTag("recipeInstructions")) {
                                selectedRecipe.instructions.forEach { instruction ->
                                    DisplayInstruction(instruction)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DisplayIngredient(ingredient: Ingredient) {
    val unit = when (ingredient.foodFacts.quantity.unit) {
        FoodUnit.GRAM -> "gr"
        FoodUnit.ML -> "ml"
        FoodUnit.COUNT -> ""
    }

    val amount = ingredient.foodFacts.quantity.amount
    val quantity = if (floor(amount) == amount) amount.toInt().toString() else amount.toString()

    Text(
        text = " - ${quantity}${unit} of ${ingredient.foodFacts.name}",
        modifier = Modifier.testTag("recipeIngredient")
    )
}

@Composable
fun DisplayInstruction(instruction: String) {
    Text(
        text = instruction,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .testTag("recipeInstruction")
    )
}