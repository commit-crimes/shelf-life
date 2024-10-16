package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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

  // Test that the recipe is displayed when recipe selected
  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    householdViewModel.selectHousehold(houseHold)
    listRecipesViewModel.selectRecipe(listRecipesViewModel.recipes.value.get(2))
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("individualRecipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipe").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeServings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeInstructions").assertIsDisplayed()
  }

  // Test that "No recipe selected" message is displayed when no selected recipe exist
  @Test
  fun noRecipeSelectedMessageIsDisplayedWhenNoSelectedRecipe() {
    val emptyHousehold = houseHold.copy(foodItems = emptyList())

    // Mock the repository to return the household with no food items
    mockHouseHoldRepositoryGetHouseholds(listOf(emptyHousehold))

    householdViewModel.selectHousehold(emptyHousehold)
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    // Check that the "No recipe selected. Should not happen" message is displayed
    composeTestRule.onNodeWithTag("noRecipeSelectedMessage").assertIsDisplayed()
    composeTestRule.onNodeWithText("No recipe selected. Should not happen").assertIsDisplayed()
  }

  // Test that the arrow button navigates back
  @Test
  fun clickGoBackArrowNavigatesBack() {
    householdViewModel.selectHousehold(houseHold)
    listRecipesViewModel.selectRecipe(listRecipesViewModel.recipes.value.get(2))
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          listRecipesViewModel = listRecipesViewModel,
          householdViewModel = householdViewModel)
    }

    // Click on the go back Arrow
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // Verify that goBack() was called
    verify(navigationActions).goBack()
  }
}
