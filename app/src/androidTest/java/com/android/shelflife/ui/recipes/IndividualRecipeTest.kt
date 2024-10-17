package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class IndividualRecipeTest {

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

  // Helper function to set up the IndividualRecipeScreen
  private fun setUpIndividualRecipeScreen(selectedRecipeIndex: Int? = null) {
    householdViewModel.selectHousehold(houseHold)
    selectedRecipeIndex?.let {
      listRecipesViewModel.selectRecipe(listRecipesViewModel.recipes.value[it])
    }
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }
  }

  // Helper function to check if the common UI elements are displayed
  private fun verifyCommonUIElements() {
    composeTestRule.onNodeWithTag("individualRecipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipe").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeServings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeInstructions").assertIsDisplayed()
  }

  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    setUpIndividualRecipeScreen(selectedRecipeIndex = 2)
    verifyCommonUIElements()
  }

  @Test
  fun noRecipeSelectedMessageIsDisplayedWhenNoSelectedRecipe() {
    val emptyHousehold = houseHold.copy(foodItems = emptyList())
    mockHouseHoldRepositoryGetHouseholds(listOf(emptyHousehold))

    householdViewModel.selectHousehold(emptyHousehold)
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("noRecipeSelectedMessage").assertIsDisplayed()
    composeTestRule.onNodeWithText("No recipe selected. Should not happen").assertIsDisplayed()
  }

  @Test
  fun clickGoBackArrowNavigatesBack() {
    setUpIndividualRecipeScreen(selectedRecipeIndex = 2)

    // Click on the go back arrow
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // Verify that goBack() was called
    verify(navigationActions).goBack()
  }
}
