package com.android.shelfLife.ui.recipes

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
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
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.RecipeRepositoryTestHelper
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
class IndividualRecipeScreenTest {
  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHold: HouseHold
  private lateinit var user: User

  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var recipeRepository: RecipeRepository
  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var listFoodItemsRepository: FoodItemRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var recipeRepositoryTestHelper: RecipeRepositoryTestHelper
  private lateinit var listFoodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  private lateinit var individualRecipeViewModel: IndividualRecipeViewModel

  private lateinit var instrumentationContext: android.content.Context

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    recipeRepositoryTestHelper = RecipeRepositoryTestHelper(recipeRepository)
    listFoodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)

    instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    whenever(navigationActions.currentRoute()).thenReturn(Route.RECIPES)

    // Create a FoodItem to be used in tests
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val foodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day,
            owner = "testOwner")

    val recipe1 =
        Recipe(
            uid = "recipe1",
            name = "Pasta Bolognese",
            instructions = listOf("Cook  meat", " boil water", "add pasta", "eat"),
            servings = 2F,
            time = 45.minutes,
            ingredients =
                listOf(
                    Ingredient("Pasta", Quantity(200.0, FoodUnit.GRAM)),
                    Ingredient("Ground Beef", Quantity(100.0, FoodUnit.GRAM)),
                    Ingredient("Tomato Sauce", Quantity(100.0, FoodUnit.ML)),
                    Ingredient("Onion", Quantity(2.0, FoodUnit.COUNT))),
            recipeType = RecipeType.PERSONAL)

    val recipe2 =
        Recipe(
            uid = "recipe2",
            name = "Pizza",
            instructions = listOf("Heat up the oven", "Place pizza until cooked", "eat"),
            servings = 2F,
            time = 25.minutes,
            ingredients = listOf(Ingredient("Pizza", Quantity(1.0, FoodUnit.COUNT))),
            recipeType = RecipeType.BASIC)

    recipeRepositoryTestHelper.setSelectedRecipe(recipe1)
    recipeRepositoryTestHelper.setRecipes(listOf(recipe1, recipe2))

    // Initialize the household with the food item
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    householdRepositoryTestHelper.selectHousehold(houseHold)
    listFoodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))

    user =
        User(
            uid = "user1",
            username = "Tester",
            email = "test_email@test.com",
            selectedHouseholdUID = "1",
            householdUIDs = listOf("1"),
            recipeUIDs = listOf("recipe1", "recipe2"),
        )

    individualRecipeViewModel = IndividualRecipeViewModel(recipeRepository, userRepository)
  }

  @Test
  fun verifyBasicUIElements() {
    composeTestRule.setContent {
      IndividualRecipeScreen(navigationActions, individualRecipeViewModel)
    }

    composeTestRule.onNodeWithTag("individualRecipesScreen").isDisplayed()
    composeTestRule.onNodeWithTag("individualRecipeTitle").isDisplayed()
    composeTestRule.onNodeWithTag("topBar").isDisplayed()
    composeTestRule.onNodeWithTag("goBackArrow").isDisplayed()
    composeTestRule.onNodeWithTag("deleteFoodItem").isDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").isDisplayed()
    composeTestRule.onNodeWithTag("startButton").isDisplayed()
    composeTestRule.onNodeWithTag("recipe").isDisplayed()
    composeTestRule.onNodeWithTag("recipeContent").isDisplayed()
    composeTestRule.onNodeWithTag("recipeImage").isDisplayed()
    composeTestRule.onNodeWithTag("recipeServings").isDisplayed()
    composeTestRule.onNodeWithTag("recipeTime").isDisplayed()
    composeTestRule.onNodeWithTag("recipeIngredients").isDisplayed()
    composeTestRule.onNodeWithTag("recipeInstructions").isDisplayed()
    composeTestRule.onAllNodesWithTag("recipeIngredient").onFirst().assertExists()
    composeTestRule.onAllNodesWithTag("recipeInstruction").onFirst().assertExists()
  }

  @Test
  fun arrowBackNavigateBack() {
    composeTestRule.setContent {
      IndividualRecipeScreen(navigationActions, individualRecipeViewModel)
    }

    composeTestRule.onNodeWithTag("goBackArrow").isDisplayed()
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    verify(navigationActions).goBack()
  }
}
