package com.android.shelflife.ui.recipes

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.google.firebase.Timestamp
import io.mockk.mockk
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
  private lateinit var houseHoldRepository: HouseholdRepositoryFirestore
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var recipeRepository: RecipeRepository
  private lateinit var recipeGeneratorRepository: RecipeGeneratorRepository

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
            houseHoldRepository,
            listFoodItemsViewModel,
            mockk<InvitationRepositoryFirestore>(relaxed = true),
            mock<DataStore<Preferences>>())

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
  @Composable
  private fun setUpIndividualRecipeScreen(selectedRecipeIndex: Int? = null) {
    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
    selectedRecipeIndex?.let {
      listRecipesViewModel.selectRecipe(listRecipesViewModel.recipes.value[it])
    }
      val individualRecipeViewModel = viewModel{
          IndividualRecipeViewModel(recipeRepository)
      }
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          individualRecipeViewModel = individualRecipeViewModel)
    }
  }

  // Helper function to check if the common UI elements are displayed
  private fun verifyCommonUIElements() {
    composeTestRule.onNodeWithTag("individualRecipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("individualRecipeTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeServings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("recipeIngredients").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("recipeIngredient").onFirst().assertExists()
    composeTestRule.onNodeWithTag("recipeInstructions").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("recipeInstruction").onFirst().assertExists()
  }

  @Composable
  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    setUpIndividualRecipeScreen(selectedRecipeIndex = 0)
    verifyCommonUIElements()
  }

  @Composable
  @Test
  fun noRecipeSelectedMessageIsDisplayedWhenNoSelectedRecipe() {
    val emptyHousehold = houseHold.copy(foodItems = emptyList())
    mockHouseHoldRepositoryGetHouseholds(listOf(emptyHousehold))

    householdViewModel.setHouseholds(listOf(emptyHousehold))
    householdViewModel.selectHousehold(emptyHousehold)
      val individualRecipeViewModel = viewModel{
          IndividualRecipeViewModel(recipeRepository)
      }
    composeTestRule.setContent {
      IndividualRecipeScreen(
          navigationActions = navigationActions,
          individualRecipeViewModel = individualRecipeViewModel)
    }

    composeTestRule.onNodeWithTag("noRecipeSelectedMessage").assertIsDisplayed()
    composeTestRule.onNodeWithText("No recipe selected. Should not happen").assertIsDisplayed()
  }

  @Composable
  @Test
  fun clickGoBackArrowNavigatesBack() {
    setUpIndividualRecipeScreen(selectedRecipeIndex = 0)

    // Click on the go back arrow
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // Verify that goBack() was called
    verify(navigationActions).goBack()
  }
}
