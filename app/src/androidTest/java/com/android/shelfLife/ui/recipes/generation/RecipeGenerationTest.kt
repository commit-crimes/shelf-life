package com.android.shelfLife.ui.recipes.generation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.test.core.app.ApplicationProvider
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
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.recipes.generateRecipe.CompletionStep
import com.android.shelfLife.ui.recipes.generateRecipe.FoodSelectionStep
import com.android.shelfLife.ui.recipes.generateRecipe.GenerateRecipeScreen
import com.android.shelfLife.ui.recipes.generateRecipe.RecipeInputStep
import com.android.shelfLife.ui.recipes.generateRecipe.ReviewStep
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeGenerationViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.RecipeRepositoryTestHelper
import io.mockk.mockk
import java.util.Date
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@HiltAndroidTest
class RecipeGenerationTest {

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var recipeRepository: RecipeRepository
  @Inject lateinit var recipeGeneratorRepository: RecipeGeneratorRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var recipeRepositoryTestHelper: RecipeRepositoryTestHelper

  private lateinit var recipeGenerationViewModel: RecipeGenerationViewModel
  private lateinit var overviewScreenViewModel: OverviewScreenViewModel

  private lateinit var instrumentationContext: android.content.Context

  private lateinit var houseHold: HouseHold
  private lateinit var foodItem: FoodItem

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    recipeRepositoryTestHelper = RecipeRepositoryTestHelper(recipeRepository)
    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(foodItemRepository)
    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)

    instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    whenever(navigationActions.currentRoute()).thenReturn(Route.RECIPES)

    // Create a FoodItem to be used in tests
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)

    foodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day,
            owner = "testOwner")

    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    householdRepositoryTestHelper.selectHousehold(houseHold)
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))

    recipeGenerationViewModel =
        RecipeGenerationViewModel(
            recipeRepository, recipeGeneratorRepository, foodItemRepository, userRepository)

    overviewScreenViewModel =
        OverviewScreenViewModel(
            houseHoldRepository, foodItemRepository, userRepository, instrumentationContext)
  }


  /** TESTING FIRST STEP: BASIC RECIPE OPTIONS */
  @Test
  fun recipeInputStep_renderedCorrectly() {
    composeTestRule.setContent {
      RecipeInputStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = {})
    }

    // Check for the presence of input fields and buttons
    composeTestRule.onNodeWithText("Recipe Name").assertExists()
    composeTestRule.onNodeWithText("Recipe Type").assertExists()
    composeTestRule.onNodeWithText("Servings").assertExists()
    composeTestRule.onNodeWithTag("recipeSubmitButton").assertExists()
    composeTestRule.onNodeWithTag("cancelButton").assertExists()
  }

  @Test
  fun recipeInputStep_enterValidRecipeName() {
    composeTestRule.setContent {
      RecipeInputStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = {})
    }

    val testName = "My Test Recipe"

    // Input into Recipe Name field
    composeTestRule.onNodeWithText("Recipe Name").performTextInput(testName)

    // Assert that the entered text is displayed
    composeTestRule.onNodeWithText(testName).assertExists()
  }

  @Test
  fun recipeInputStep_toggleSwitches_updatesStates() {
    composeTestRule.setContent {
      RecipeInputStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = {})
    }

    // Toggle Short/Long Recipe Duration
    composeTestRule.onNodeWithTag("shortLongDurationSwitch").assertExists().performClick()

    // Toggle Only Household Ingredients
    composeTestRule.onNodeWithTag("onlyHouseholdItemsSwitch").assertExists().performClick()

    // Toggle Prioritise Expiring Ingredients
    composeTestRule.onNodeWithTag("prioritiseExpiringItemsSwitch").assertExists().performClick()
    // Validate toggled state visually (this requires internal validation of state tracking)
  }

  @Test
  fun recipeInputStep_clickCancel_callsOnBack() {
    var backCalled = false

    composeTestRule.setContent {
      RecipeInputStep(
          viewModel = recipeGenerationViewModel, onNext = {}, onBack = { backCalled = true })
    }

    // Click cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.waitForIdle()

    // Assert that the callback was triggered
    assertTrue(backCalled)
  }

  @Test
  fun recipeInputStep_clickSubmit_callsOnNext() {
    var nextCalled = false

    // Pre-configure ViewModel state with valid default values
    recipeGenerationViewModel.updateRecipePrompt(
        RecipePrompt(
            name = "Valid Recipe", // Set a valid recipe name
            recipeType = RecipeType.BASIC,
            servings = 2f, // Valid serving value
            shortDuration = true,
            onlyHouseHoldItems = false,
            prioritiseSoonToExpire = false))

    composeTestRule.setContent {
      RecipeInputStep(
          viewModel = recipeGenerationViewModel, onNext = { nextCalled = true }, onBack = {})
    }

    // Verify state is rendered correctly
    composeTestRule.onNodeWithText("Valid Recipe").assertExists()
    composeTestRule.onNodeWithText("Servings").assertExists()

    // Click Submit Button
    composeTestRule.onNodeWithTag("recipeSubmitButton").performClick()
    composeTestRule.waitForIdle()

    // Assert that onNext was triggered
    assertTrue("onNext was not triggered", nextCalled)
  }

  /** TESTING SECOND STEP: FOOD SELECTION */
  @Test
  fun foodSelectionStep_renderedCorrectly() {
    composeTestRule.setContent {
      FoodSelectionStep(
          viewModel = recipeGenerationViewModel, onNext = {}, onBack = {}, overviewScreenViewModel)
    }

    // Verify Household header exists
    composeTestRule.onNodeWithText("Household").assertExists()

    // Verify Ingredients section with count exists
    composeTestRule.onNodeWithText("Ingredients (0)").assertExists()

    // Verify Back and Next buttons exist
    composeTestRule.onNodeWithTag("cancelButton").assertExists()
    composeTestRule.onNodeWithTag("recipeSubmitButton").assertExists()
  }

  @Test
  fun foodSelectionStep_addFoodItem_updatesSelectedList() {
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    foodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day,
            owner = "testOwner")
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    composeTestRule.setContent {
      FoodSelectionStep(
          viewModel = recipeGenerationViewModel, onNext = {}, onBack = {}, overviewScreenViewModel)
    }

    // Click on the available food item
    composeTestRule.onNodeWithText("Apple").performClick()

    // Verify the selected list now contains the item
    composeTestRule.onNodeWithText("Ingredients (1)").assertExists()
    composeTestRule.onNodeWithText("Apple").assertExists()
  }

  @Test
  fun foodSelectionStep_clickBackButton_triggersOnBack() {
    var backCalled = false

    composeTestRule.setContent {
      FoodSelectionStep(
          viewModel = recipeGenerationViewModel,
          onNext = {},
          onBack = { backCalled = true },
          overviewScreenViewModel)
    }

    // Click on the Back button
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.waitForIdle()
    // Verify the callback was triggered
    assertTrue(backCalled)
  }

  /** TESTING RECIPE REVIEW */
  @Test
  fun reviewStep_renderedCorrectly() {
    val testRecipePrompt =
        RecipePrompt(
            name = "Test Recipe",
            servings = 2f,
            recipeType = RecipeType.HIGH_PROTEIN,
            onlyHouseHoldItems = true,
            prioritiseSoonToExpire = false,
            ingredients =
                listOf(
                    FoodItem(
                        uid = "1",
                        foodFacts =
                            FoodFacts(name = "Apple", quantity = Quantity(3.0, FoodUnit.COUNT)),
                        owner = "testOwner")),
            specialInstruction = "Add cinnamon for flavor")

    recipeGenerationViewModel.updateRecipePrompt(testRecipePrompt)

    composeTestRule.setContent {
      ReviewStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = {})
    }

    // Verify recipe title
    composeTestRule.onNodeWithText("Test Recipe").assertExists()

    // Verify specified ingredients
    composeTestRule.onNodeWithText("Specified ingredients:").assertExists()
    composeTestRule.onNodeWithText("- Apple, 3 in stock").assertExists()

    // Verify options
    composeTestRule.onNodeWithText("- 2 servings").assertExists()
    composeTestRule.onNodeWithText("- High protein recipe").assertExists()
    composeTestRule.onNodeWithText("- Only household ingredients").assertExists()

    // Verify custom instructions input field
    composeTestRule.onNodeWithText("Custom instructions or comments").assertExists()

    // Verify buttons
    composeTestRule.onNodeWithTag("cancelButton2").assertExists()
    composeTestRule.onNodeWithTag("generateButton").assertExists()
  }

  @Test
  fun reviewStep_updateCustomInstructions_updatesViewModel() {
    val initialPrompt = RecipePrompt(name = "Test Recipe", specialInstruction = "")
    recipeGenerationViewModel.updateRecipePrompt(initialPrompt)

    composeTestRule.setContent {
      ReviewStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = {})
    }

    val updatedInstructions = "Add cinnamon for flavor"

    // Type into the custom instructions field
    composeTestRule
        .onNodeWithText("Custom instructions or comments")
        .performTextInput(updatedInstructions)

    // Verify the ViewModel state is updated
    assertEquals(
        updatedInstructions, recipeGenerationViewModel.recipePrompt.value.specialInstruction)
  }

  @Test
  fun reviewStep_clickBackButton_triggersOnBack() {
    var backCalled = false

    composeTestRule.setContent {
      ReviewStep(viewModel = recipeGenerationViewModel, onNext = {}, onBack = { backCalled = true })
    }

    // Click on the Back button
    composeTestRule.onNodeWithTag("cancelButton2").performClick()
    composeTestRule.waitForIdle()

    // Verify the callback was triggered
    assertTrue(backCalled)
  }

  @Test
  fun reviewStep_clickGenerateButton_TriggersOnNext() {
    var nextCalled = false

    composeTestRule.setContent {
      ReviewStep(viewModel = recipeGenerationViewModel, onNext = { nextCalled = true }, onBack = {})
    }

    // Click on the Generate button
    composeTestRule.onNodeWithTag("generateButton").performClick()
    composeTestRule.waitForIdle()

    // Verify onNext callback
    assertTrue(nextCalled)
  }

  /**
   * TESTING COMPLETION STEP
   */

  @Test
  fun completionStep_loadingState_showsSpinnerAndText() {
    // Set ViewModel state for generating recipe
    recipeGenerationViewModel._isGeneratingRecipe.value = true

    composeTestRule.setContent {
      CompletionStep(
        viewModel = recipeGenerationViewModel,
        onBack = {},
        navigationActions = mockk()
      )
    }

    // Check for "Generating..." text
    composeTestRule.onNodeWithText("Generating...").assertExists()

    // Check for CircularProgressIndicator
    composeTestRule.onNodeWithTag("loadingSpinner").assertExists()
  }

  @Test
  fun completionStep_nullGeneratedRecipe_triggersOnBack() {
    var backCalled = false

    recipeGenerationViewModel._isGeneratingRecipe.value = false

    composeTestRule.setContent {
      CompletionStep(
        viewModel = recipeGenerationViewModel,
        onBack = { backCalled = true },
        navigationActions = mockk()
      )
    }

    // Click on the Back button
    composeTestRule.onNodeWithTag("regenerateButton").performClick()
    composeTestRule.waitForIdle()


    // Verify onBack callback was triggered
    assertTrue(backCalled)
  }


  /**
   * TESTING OVERALL SCREEN
   */
  @Test
  fun generateRecipeScreen_initialState_displaysInputStep() {
    composeTestRule.setContent {
      GenerateRecipeScreen(
        navigationActions = navigationActions,
      )
    }

    // Verify the initial step (RecipeInputStep) is displayed
    composeTestRule.onNodeWithText("Recipe Name").assertExists()
    composeTestRule.onNodeWithText("Servings").assertExists()
  }


}
