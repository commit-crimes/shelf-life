package com.android.shelfLife.ui.recipes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
class RecipesScreenTest {
  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHold: HouseHold

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var recipeRepository: RecipeRepository
  @Inject lateinit var listFoodItemsRepository: FoodItemRepository

  private lateinit var recipesViewModel: RecipesViewModel

  // This section might need to be moved to it's own file
  private val selectedHousehold = MutableStateFlow<HouseHold?>(null)
  private val householdToEdit = MutableStateFlow<HouseHold?>(null)
  private val households = MutableStateFlow<List<HouseHold>>(emptyList())
  private val foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  private val user = MutableStateFlow<User?>(null)
  private val recipeList = MutableStateFlow<List<Recipe>>(emptyList())

  private lateinit var instrumentationContext: android.content.Context

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()
    recipeRepository = mock()

    instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    whenever(navigationActions.currentRoute()).thenReturn(Route.RECIPES)
    whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHousehold.asStateFlow())
    whenever(houseHoldRepository.households).thenReturn(households.asStateFlow())
    whenever(houseHoldRepository.householdToEdit).thenReturn(householdToEdit.asStateFlow())
    whenever(listFoodItemsRepository.foodItems).thenReturn(foodItems.asStateFlow())
    whenever(userRepository.user).thenReturn(user.asStateFlow())
    whenever(recipeRepository.recipes).thenReturn(recipeList.asStateFlow())

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

    recipeList.value = listOf(recipe1, recipe2)

    // Initialize the household with the food item
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    households.value = listOf(houseHold)
    selectedHousehold.value = houseHold
    foodItems.value = listOf(foodItem)

    user.value =
        User(
            uid = "user1",
            username = "Tester",
            email = "test_email@test.com",
            selectedHouseholdUID = "1",
            householdUIDs = listOf("1"),
            recipeUIDs = listOf("recipe1", "recipe2"),
        )

    recipesViewModel = RecipesViewModel(userRepository, recipeRepository, houseHoldRepository)
  }

  @Test
  fun verifyBasicUIElements() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("recipesScreen").isDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NavigationTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
  }

  @Test
  fun recipeListIsDisplayedWhenRecipesExist() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("recipesList").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsDisplayed()
  }

  @Test
  fun searchFilersRecipesList() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("NavigationTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    composeTestRule.onNodeWithText("Basic").assertIsDisplayed()
    composeTestRule.onNodeWithText("Basic").performClick()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()

    composeTestRule.onNodeWithText("Basic").performClick()

    composeTestRule.onNodeWithText("Personal").assertIsDisplayed()
    composeTestRule.onNodeWithText("Personal").performClick()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsDisplayed()
  }

  @Test
  fun clickOnRecipeNavigatesToIndividualRecipeScreen() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pizza").performClick()

    verify(navigationActions).navigateTo(Screen.INDIVIDUAL_RECIPE)
  }

  @Test
  fun clickOnAddRecipeFabExtendsFab() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").performClick()

    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generateRecipeFab").assertIsDisplayed()
  }

  @Test
  fun clickOnAddRecipeFabNavigatesToAddRecipeScreen() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").performClick()

    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_RECIPE)
  }

  @Test
  fun clickOnGenerateRecipeFabNavigatesToGenerateRecipeScreen() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").performClick()

    composeTestRule.onNodeWithTag("generateRecipeFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generateRecipeFab").performClick()

    verify(navigationActions).navigateTo(Screen.GENERATE_RECIPE)
  }

  @Test
  fun filtersAppearAndDisappearWhenButtonIsClicked() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("filterBar").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("NavigationTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    composeTestRule.onNodeWithTag("filterBar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    composeTestRule.onNodeWithTag("filterBar").assertIsNotDisplayed()
  }

  @Test
  fun emptyScreenAppearsWhenThereAreNoRecipes() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Kfrjknfejeojogsrjgiowi")

    composeTestRule.onNodeWithTag("noRecipesAvailableText").assertIsDisplayed()
  }

  @Test
  fun searchBarWorks() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("P")

    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("i")

    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsNotDisplayed()
  }

  @Test
  fun searchBarWorksWithFilters() {
    composeTestRule.setContent { RecipesScreen(navigationActions, recipesViewModel) }

    composeTestRule.onNodeWithTag("NavigationTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    composeTestRule.onNodeWithText("Basic").assertIsDisplayed()
    composeTestRule.onNodeWithText("Basic").performClick()

    composeTestRule.onNodeWithText("Personal").assertIsDisplayed()
    composeTestRule.onNodeWithText("Personal").performClick()

    composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Pa")

    composeTestRule.onNodeWithText("Pizza").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Pasta Bolognese").assertIsDisplayed()
  }
}
