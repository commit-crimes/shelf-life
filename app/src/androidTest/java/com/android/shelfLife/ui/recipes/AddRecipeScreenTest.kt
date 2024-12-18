package com.android.shelfLife.ui.recipes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.viewmodel.recipes.AddRecipeViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class AddRecipeScreenTest {
  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var recipeRepository: RecipeRepository

  private lateinit var addRecipeViewModel: AddRecipeViewModel

  // This section might need to be moved to it's own file
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
    whenever(userRepository.user).thenReturn(user.asStateFlow())
    whenever(recipeRepository.recipes).thenReturn(recipeList.asStateFlow())

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

    user.value =
        User(
            uid = "user1",
            username = "Tester",
            email = "test_email@test.com",
            selectedHouseholdUID = "1",
            householdUIDs = listOf("1"),
            recipeUIDs = listOf("recipe1", "recipe2"),
        )

    addRecipeViewModel = AddRecipeViewModel(recipeRepository, userRepository)
  }

  @Test
  fun verifyBasicUIElementsNoErrors() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addRecipeScreen").isDisplayed()
    composeTestRule.onNodeWithTag("addRecipeTitle").isDisplayed()
    composeTestRule.onNodeWithTag("topBar").isDisplayed()
    composeTestRule.onNodeWithTag("goBackArrow").isDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeTitle").isDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeServings").isDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeTime").isDisplayed()
    composeTestRule.onNodeWithTag("ingredientSection").isDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton").isDisplayed()
    composeTestRule.onNodeWithTag("instructionSection").isDisplayed()
    composeTestRule.onNodeWithTag("addInstructionButton").isDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").isDisplayed()
    composeTestRule.onNodeWithTag("addButton").isDisplayed()
  }

  @Test
  fun clickingOnAddIngredientButtonMakesPopUpAppear() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addIngredientButton").isDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()

    composeTestRule.onNodeWithTag("instructionDialog").isDisplayed()
    composeTestRule.onNodeWithTag("addIngredientPopUp").isDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientName").isDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientQuantity").isDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientUnit").isDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton2").isDisplayed()
    composeTestRule.onNodeWithTag("cancelIngredientButton").isDisplayed()
  }

  @Test
  fun instructionAppearWhenAddInstructionIsClickedAndThenDisappearsWhenDeleted() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertExists()
    composeTestRule.onNodeWithTag("deleteInstructionButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertDoesNotExist()
  }

  @Test
  fun ingredientPopUpAddsIngredient() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

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
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

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
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

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
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addIngredientButton2").performClick()

    // checks we have not left the pop up
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
  }

  @Test
  fun addRecipeScreenDoesNotAllowYouToAddRecipeWithErrors() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addButton").performClick()

    // checks we have not left the pop up
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
  }

  @Test
  fun popUpCancelButtonClosesPopUp() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelIngredientButton").performClick()

    // checks we have left the pop up
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertDoesNotExist()
  }

  @Test
  fun addRecipeScreenCancelButtonNavigatesBack() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // checks we have navigated back
    verify(navigationActions).goBack()
  }

  @Test
  fun arrowInAddRecipeScreenNavigatesBack() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // checks we have navigated back
    verify(navigationActions).goBack()
  }

  @Test
  fun addInstructionButtonAddsInstructionInput() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }

    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule.onAllNodesWithTag("inputRecipeInstruction").onFirst().assertExists()
  }

  @Test
  fun addRecipe() {
    composeTestRule.setContent { AddRecipeScreen(navigationActions, addRecipeViewModel) }
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
