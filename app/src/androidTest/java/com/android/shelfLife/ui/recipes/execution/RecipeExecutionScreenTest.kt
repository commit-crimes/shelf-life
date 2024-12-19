package com.android.shelfLife.ui.recipes.execution

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class RecipeExecutionScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  // Compose rule for testing UI
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var recipeRepository: RecipeRepository

  private lateinit var instrumentationContext: android.content.Context
  private lateinit var navigationActions: NavigationActions

  // Mocked Flows
  private val userFlow = MutableStateFlow<User?>(null)
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)
  private val foodItemsFlow = MutableStateFlow<List<FoodItem>>(emptyList())
  private val selectedRecipeFlow = MutableStateFlow<Recipe?>(null)

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    // Provide a user
    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "user@example.com",
            photoUrl = null,
            householdUIDs = listOf("household123"),
            selectedHouseholdUID = "household123",
            recipeUIDs = emptyList())
    userFlow.value = realUser
    whenever(userRepository.user).thenReturn(userFlow.asStateFlow())

    // Provide a selected household
    val exampleSelectedHousehold =
        HouseHold(
            uid = "household123",
            name = "Example Household",
            members = listOf("currentUserId", "member2"),
            sharedRecipes = emptyList(),
            ratPoints = mapOf("currentUserId" to 10),
            stinkyPoints = mapOf("member2" to 5))
    selectedHouseholdFlow.value = exampleSelectedHousehold
    whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHouseholdFlow.asStateFlow())

    // Provide food items
    val testFoodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(5.0, FoodUnit.COUNT),
                    category = FoodCategory.FRUIT,
                    nutritionFacts = NutritionFacts()),
            expiryDate = null,
            owner = "currentUserId")
    foodItemsFlow.value = listOf(testFoodItem)
    whenever(foodItemRepository.foodItems).thenReturn(foodItemsFlow.asStateFlow())

    val testRecipe =
        Recipe(
            uid = "recipe123",
            name = "Test Recipe",
            instructions = listOf("Step 1: Do something", "Step 2: Do something else"),
            servings = 2f,
            time = Duration.ZERO, // Provide a valid Duration
            ingredients =
                listOf(
                    Ingredient(
                        name = "Apple",
                        quantity = Quantity(2.0, FoodUnit.COUNT) // Provide Quantity and FoodUnit
                        )),
            recipeType = RecipeType.PERSONAL, // optional since it defaults to PERSONAL
            workInProgress = false)
    selectedRecipeFlow.value = testRecipe
    whenever(recipeRepository.selectedRecipe).thenReturn(selectedRecipeFlow.asStateFlow())

    // Since we use hiltViewModel in the UI,
    // the ExecuteRecipeViewModel will be constructed with these flows
  }

  private fun setContent() {
    composeTestRule.setContent {
      // The UI we want to test is RecipeExecutionScreen
      RecipeExecutionScreen(navigationActions = navigationActions)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun recipeExecutionScreen_initiallyShowsServingsScreen() {
    setContent()

    // Initially we should be in RecipeExecutionState.SelectServings
    // Check that the ServingsScreen is displayed
    composeTestRule.onNodeWithTag("servingsScreen").assertIsDisplayed()
    // There's a Next FAB to proceed
    composeTestRule.onNodeWithTag("nextFab").assertIsDisplayed()
  }

  @Test
  fun recipeExecutionScreen_canNavigateToSelectFoodItems() {
    setContent()

    // Click Next on ServingsScreen
    composeTestRule.onNodeWithTag("nextFab").performClick()

    // Now we should be on SelectFoodItemsForIngredientScreen
    // It shows a top bar with "Select Items for Apple" (the ingredient)
    composeTestRule.onNodeWithText("Select Items for Apple").assertIsDisplayed()
    // "Done" button is the FAB to move on
    composeTestRule.onNodeWithText("Done").assertIsDisplayed()
  }

  @Test
  fun recipeExecutionScreen_selectFoodItemsAndGoToInstructions() {
    setContent()

    // Move from ServingsScreen to SelectFoodItemsForIngredientScreen
    composeTestRule.onNodeWithTag("nextFab").performClick()

    // On SelectFoodItemsForIngredientScreen, we have one Apple item:
    composeTestRule.onNodeWithText("Apple").assertIsDisplayed()

    // Expand Apple card by clicking on it
    composeTestRule.onNodeWithText("Apple").performClick()

    // A slider for amount should appear if expanded, but no test tags provided.
    // We'll assume clicking on Apple toggles expansion and we trust that logic.
    // For now, just click "Done"
    composeTestRule.onNodeWithText("Done").performClick()

    // Now we should be on InstructionScreen
    composeTestRule.onNodeWithTag("instructionScreen").assertIsDisplayed()

    // The first instruction "Step 1: Do something" should appear
    composeTestRule.onNodeWithText("Step 1: Do something").assertIsDisplayed()
  }

  @Test
  fun recipeExecutionScreen_instructionsCanNavigateNextAndFinish() {
    setContent()

    // Move to SelectFoodItemsForIngredientScreen
    composeTestRule.onNodeWithTag("nextFab").performClick()
    // Move to InstructionScreen by "Done" on SelectFood
    composeTestRule.onNodeWithText("Done").performClick()

    // Initially on Step 1
    composeTestRule.onNodeWithText("Step 1: Do something").assertIsDisplayed()

    // Click Next to go to Step 2
    composeTestRule.onNodeWithTag("nextButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithText("Step 2: Do something else").assertIsDisplayed()

    // Click Finish to complete
    composeTestRule.onNodeWithTag("finishButton").assertIsDisplayed().performClick()

    // On finish, it navigates back twice and goes to OVERVIEW
    verify(navigationActions, times(2)).goBack()
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }
}
