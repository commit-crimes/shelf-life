package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newFoodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.android.shelfLife.model.newhousehold.HouseholdViewModel
import com.android.shelfLife.model.newInvitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.newRecipe.ListRecipesViewModel
import com.android.shelfLife.model.newRecipe.RecipeGeneratorRepository
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.ui.newnavigation.NavigationActions
import com.android.shelfLife.ui.recipes.addRecipe.AddRecipeScreen
import com.google.firebase.Timestamp
import io.mockk.mockk
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddRecipesTest {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var listRecipesViewModel: ListRecipesViewModel
  private lateinit var houseHoldRepository: HouseholdRepositoryFirestore
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

  // Helper function to set up the screen with RecipesScreen content
  private fun setUpAddRecipesScreen() {
    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      AddRecipeScreen(
          navigationActions = navigationActions, listRecipesViewModel = listRecipesViewModel)
    }
  }

  @Test
  fun initialAddRecipeScreenDisplayedCorrectly() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackArrow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeServings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ingredientSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("instructionSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addInstructionButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addButton").assertIsDisplayed()
  }

  @Test
  fun instructionAppearWhenAddInstructionIsClickedAndThenDisappearsWhenDeleted() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertExists()
    composeTestRule.onNodeWithTag("deleteInstructionButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertDoesNotExist()
  }

  @Test
  fun ingredientPopUpAppearsWhenAddIngredientButtonIsPressed() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientQuantity").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("ingredientUnitButton").onFirst().assertExists()
    composeTestRule.onNodeWithTag("addIngredientButton2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelIngredientButton").assertIsDisplayed()
  }

  @Test
  fun ingredientPopUpAddsIngredient() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientName").performTextInput("Egg")
    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextInput("5")
    composeTestRule.onNodeWithTag("addIngredientButton2").performClick()

    // check we have left the popUp
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertDoesNotExist()

    composeTestRule.onAllNodesWithTag("ingredientItem").onFirst().assertExists()
  }

  @Test
  fun textFieldsAreLeftEmptyMakesErrorMessageAppearInAddRecipeScreen() {
    setUpAddRecipesScreen()

    composeTestRule.onNodeWithTag("inputRecipeTitle").performTextInput("Smoked salmon")
    composeTestRule.onNodeWithTag("inputRecipeTitle").performTextClearance()
    composeTestRule.onNodeWithTag("titleErrorMessage").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputRecipeServings").performTextInput("5")
    composeTestRule.onNodeWithTag("inputRecipeServings").performTextClearance()
    composeTestRule.onNodeWithTag("servingsErrorMessage").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputRecipeTime").performTextInput("5")
    composeTestRule.onNodeWithTag("inputRecipeTime").performTextClearance()
    composeTestRule.onNodeWithTag("timeErrorMessage").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule
        .onNodeWithTag("inputRecipeInstruction")
        .performTextInput("Place the salmon into a smoker")
    composeTestRule.onNodeWithTag("inputRecipeInstruction").performTextClearance()
    composeTestRule.onNodeWithTag("instructionErrorMessage").assertIsDisplayed()
  }

  @Test
  fun textFieldsAreLeftEmptyMakesErrorMessageAppearInIngredientPopUP() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()

    composeTestRule.onNodeWithTag("inputIngredientName").performTextInput("Salmon")
    composeTestRule.onNodeWithTag("inputIngredientName").performTextClearance()
    composeTestRule.onNodeWithTag("ingredientNameErrorMessage").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextInput("5")
    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextClearance()
    composeTestRule.onNodeWithTag("ingredientQuantityErrorMessage").assertIsDisplayed()
  }

  @Test
  fun popUpDoesNotAllowYouToAddIngredientWithErrors() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton2").performClick()

    // checks we have not left the pop up
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
  }

  @Test
  fun addRecipeScreenDoesNotAllowYouToAddRecipeWithErrors() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addButton").performClick()

    // checks we have not left the pop up
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
  }

  @Test
  fun popUpCancelButtonClosesPopUp() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelIngredientButton").performClick()

    // checks we have left the pop up
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertDoesNotExist()
  }

  @Test
  fun addRecipeScreenCancelButtonNavigatesBack() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // checks we have navigated back
    verify(navigationActions).goBack()
  }

  @Test
  fun arrowInAddRecipeScreenNavigatesBack() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // checks we have navigated back
    verify(navigationActions).goBack()
  }

  @Test
  fun addInstructionButtonAddsInstructionInput() {
    setUpAddRecipesScreen()
    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertExists()
  }

  @Test
  fun addRecipe() {
    setUpAddRecipesScreen()
    // val listRecipesViewModelSizeStart = listRecipesViewModel.toArray().size

    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputRecipeTitle").performTextInput("Smoked salmon")
    composeTestRule.onNodeWithTag("inputRecipeServings").performTextInput("5")
    composeTestRule.onNodeWithTag("inputRecipeTime").performTextInput("360")

    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("inputIngredientName").performTextInput("Salmon")
    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextInput("5")
    composeTestRule.onNodeWithTag("addIngredientButton2").performClick()

    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule
        .onNodeWithTag("inputRecipeInstruction")
        .performTextInput("Add the salmon into the smoker")

    // composeTestRule.onNodeWithTag("addButton").performClick()
    // verify(navigationActions).goBack()
  }
}
