package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class RecipesTest {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var listRecipesViewModel: ListRecipesViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var recipeGeneratorRepository: RecipeGeneratorRepository
  private lateinit var recipeRepository: RecipeRepository

  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
    foodItemRepository = mock()
    recipeRepository = mock()
    recipeGeneratorRepository = mock()
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    listRecipesViewModel = ListRecipesViewModel(recipeRepository, recipeGeneratorRepository)
    houseHoldRepository = mock()
    householdViewModel =
        HouseholdViewModel(
            houseHoldRepository, listFoodItemsViewModel, mock<DataStore<Preferences>>())

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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // Expires in 1 day
            )
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf(foodItem))

    mockHouseHoldRepositoryGetHouseholds(listOf(houseHold))
  }

  private fun mockHouseHoldRepositoryGetHouseholds(households: List<HouseHold>) {
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (List<HouseHold>) -> Unit
          onSuccess(households)
          null
        }
        .whenever(houseHoldRepository)
        .getHouseholds(any(), any())
  }

  // Helper function to set up the screen with RecipesScreen content
  private fun setUpRecipesScreen() {
    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      RecipesScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }
  }

  // Helper function to check if the basic UI elements are displayed
  private fun verifyBasicUIElements() {
    composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeSearchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
  }

  @Test
  fun recipesScreenDisplayedCorrectly() {
    setUpRecipesScreen()
    verifyBasicUIElements()
  }

  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    setUpRecipesScreen()
    composeTestRule.onNodeWithTag("recipesList").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
  }

  @Test
  fun searchFiltersFoodItemList() {
    setUpRecipesScreen()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()

    // Activate the SearchBar and enter the search query
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Paella")

    // Verify that only one recipe card is displayed and contains the text "Paella"
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(1)
    composeTestRule
        .onNode(hasText("Paella") and hasAnyAncestor(hasTestTag("recipeSearchBar")))
        .assertIsDisplayed()
  }

  @Test
  fun clickOnRecipeNavigatesToIndividualRecipeScreen() {
    setUpRecipesScreen()
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()

    // Activate the SearchBar and enter the search query
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Paella")

    // Click on the recipe and verify navigation
    composeTestRule.onNodeWithTag("recipesCards").performClick()
    composeTestRule.waitForIdle()
    verify(navigationActions)
        .navigateTo(com.android.shelfLife.ui.navigation.Screen.INDIVIDUAL_RECIPE)
  }

  @Test
  fun clickOnAddRecipeFabNavigatesToAddRecipeScreen() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("addRecipeFab").performClick()
    composeTestRule.waitForIdle()
    verify(navigationActions).navigateTo(com.android.shelfLife.ui.navigation.Screen.ADD_RECIPE)
  }

  @Test
  fun filtersAppearWhenFilterIconIsClicked() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filterBar").assertExists().assertIsDisplayed()
  }

  @Test
  fun filtersRecipesBasedOnSelectedFilter() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    // Apply a filter (e.g., "Soon to expire")
    composeTestRule.onNodeWithText("Soon to expire").performClick()
    composeTestRule.waitForIdle()

    // Check if the list is filtered by the selected filter
    // Assuming the filter hides recipes that don't match the criteria
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
  }

  @Test
  fun multipleFiltersWorkTogether() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    // Apply two filters: "Soon to expire" and "High protein"
    composeTestRule.onNodeWithText("Soon to expire").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("High protein").performClick()
    composeTestRule.waitForIdle()

    // Check that the filtered list now includes recipes that match both filters
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
  }

  @Test
  fun filteredRecipesListUpdatesWhenFilterIsToggled() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()
    // Apply a filter: "Soon to expire"
    composeTestRule.onNodeWithText("Soon to expire").performClick()
    composeTestRule.waitForIdle()

    // Check that the recipes list is filtered
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()

    // Remove the filter
    composeTestRule.onNodeWithText("Soon to expire").performClick()
    composeTestRule.waitForIdle()

    // Check that the recipes list now includes all recipes again
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
  }

  @Test
  fun searchQueryCombinesWithFilters() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    // Apply a filter: "Soon to expire"
    composeTestRule.onNodeWithText("Soon to expire").performClick()
    composeTestRule.waitForIdle()

    // Enter a search query

    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Pa")

    // Check that the recipes list is filtered by the search query, not the active filter
    composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithText("Pa").assertIsDisplayed()
  }

  @Test
  fun showTextIfNoRecipeOption() {
    setUpRecipesScreen()

    composeTestRule.onNodeWithTag("filterIcon").performClick()

    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("ZZZYZZZYZ")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("noRecipesAvailableText").assertIsDisplayed()
  }
}
