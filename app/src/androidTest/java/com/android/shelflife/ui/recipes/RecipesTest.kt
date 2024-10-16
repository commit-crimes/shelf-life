package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RecipesTest {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var listRecipesViewModel: ListRecipesViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel

  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
    foodItemRepository = mock()
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)

    listRecipesViewModel = ListRecipesViewModel()
    houseHoldRepository = mock()
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // Expires in 1 day
            )

    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf(foodItem))

    // Mock the repository to return the initial household
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

  // Test if the RecipeScreen is displayed with all elements
  @Test
  fun recipesScreenDisplayedCorrectly() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      RecipesScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeSearchBar").assertIsDisplayed()
  }

  // Test that the recipes are displayed when recipes exist
  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      RecipesScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    // Check that the recipe list is displayed
    composeTestRule.onNodeWithTag("recipesList").assertIsDisplayed()

    // Check that 5 recipe card is displayed (only 5 fit at a time)
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(5)
  }

  @Test
  fun searchFiltersFoodItemList() {

    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      RecipesScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    // Check that 5 recipe card is displayed (only 5 fit at a time)
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(5)

    // Activate the SearchBar
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()

    // Enter search query "paella"
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Paella")

    // Only Paella should be displayed
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(1)

    // Assert that the displayed recipe contains the text "Paella"
    composeTestRule
        .onNode(hasText("Paella") and hasAnyAncestor(hasTestTag("recipeSearchBar")))
        .assertIsDisplayed()
  }

  // Test that the card navigates to the individual recipe screen
  @Test
  fun clickOnRecipeNavigatesToIndividualRecipeScreen() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      RecipesScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    // Check that 5 recipe card is displayed (only 5 fit at a time)
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(5)

    // Activate the SearchBar
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()

    // Enter search query "Tortilla de patata"
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Tortilla de patata")

    // Click on the recipe
    composeTestRule.onNodeWithTag("recipesCards").performClick()

    composeTestRule.waitForIdle()

    // Verify that navigateTo(Screen.INDIVIDUAL_RECIPE) was called
    verify(navigationActions)
        .navigateTo(com.android.shelfLife.ui.navigation.Screen.INDIVIDUAL_RECIPE)
  }
}
