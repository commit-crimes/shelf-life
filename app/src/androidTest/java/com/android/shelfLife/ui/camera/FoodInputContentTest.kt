package com.android.shelfLife.ui.camera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class FoodInputContentTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var instrumentationContext: android.content.Context
  private lateinit var navigationActions: NavigationActions

  // We'll mock a user and set userRepository.user flow
  private val userFlow = MutableStateFlow<User?>(null)

  // Mock for selectedFood flow
  private val selectedFoodFlow = MutableStateFlow<FoodItem?>(null)

  @Before
  fun setUp() {
    hiltRule.inject()
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    navigationActions = mock()

    // Provide a user
    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "user@example.com",
            photoUrl = null,
            householdUIDs = listOf("household123"),
            selectedHouseholdUID = "household123",
            recipeUIDs = emptyList())
    userFlow.value = realUser
    whenever(userRepository.user).thenReturn(userFlow.asStateFlow())

    // Mock selectedFood flow in foodItemRepository
    whenever(foodItemRepository.selectedFoodItem).thenReturn(selectedFoodFlow.asStateFlow())

    // By default, stub foodItemRepository calls
    runBlocking {
      whenever(foodItemRepository.getNewUid()).thenReturn("newFoodItemUid")
      whenever(foodItemRepository.addFoodItem(any(), any())).thenAnswer {}
      whenever(foodItemRepository.updateFoodItem(any(), any())).thenAnswer {}
      whenever(foodItemRepository.selectFoodItem(anyOrNull())).thenAnswer {}
    }
  }

  private fun createViewModel(): FoodItemViewModel {
    // After all flows and mocks are set, create the viewModel
    return FoodItemViewModel(foodItemRepository, userRepository)
  }

  // Helper to set content and return callbacks spies
  private fun setContent(
      viewModel: FoodItemViewModel,
      foodFacts: FoodFacts,
      onSubmit: () -> Unit = {},
      onCancel: () -> Unit = {},
      onExpandRequested: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      FoodInputContent(
          foodItemViewModel = viewModel,
          foodFacts = foodFacts,
          onSubmit = onSubmit,
          onCancel = onCancel,
          onExpandRequested = onExpandRequested)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun foodInputContent_displaysBasicInfo() {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Test Apple",
            barcode = "12345",
            quantity = Quantity(1.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    setContent(viewModel, testFoodFacts)

    // Verify that the basic info is displayed
    composeTestRule.onNodeWithText("Test Apple").assertIsDisplayed()
    composeTestRule.onNodeWithText(FoodCategory.FRUIT.name).assertIsDisplayed()
  }

  @Test
  fun foodInputContent_tapToExpandCallsOnExpandRequested() {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Test Apple",
            barcode = "12345",
            quantity = Quantity(1.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    var expandCalled = false
    setContent(viewModel, testFoodFacts, onExpandRequested = { expandCalled = true })

    // The entire column is clickable, click on text
    composeTestRule.onNodeWithText("Test Apple").performClick()

    assertTrue(expandCalled)
  }

  @Test
  fun foodInputContent_submitWithInvalidData_showsError_andNoSubmit() = runBlocking {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Non-Scanned Fruit",
            barcode = "",
            quantity = Quantity(1.0, FoodUnit.COUNT),
            category = FoodCategory.OTHER,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    // Not scanned scenario and invalid data
    viewModel.foodName = ""
    viewModel.amount = ""
    viewModel.changeBuyDate("abcd") // invalid date

    var submitCalled = false
    setContent(viewModel, testFoodFacts, onSubmit = { submitCalled = true })

    // Click submit
    composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed().performClick()

    // No add or update calls
    verify(foodItemRepository, never()).addFoodItem(any(), any())
    verify(foodItemRepository, never()).updateFoodItem(any(), any())

    assert(!submitCalled)
  }

  @Test
  fun foodInputContent_cancelButton_callsOnCancel() {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Grapes",
            barcode = "99999",
            quantity = Quantity(500.0, FoodUnit.GRAM),
            category = FoodCategory.OTHER,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    var cancelCalled = false
    setContent(viewModel, testFoodFacts, onCancel = { cancelCalled = true })

    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed().performClick()

    assertTrue(cancelCalled)
  }

  @Test
  fun foodInputContent_changeLocation_changesViewModelState() {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Melon",
            barcode = "54321",
            quantity = Quantity(2.0, FoodUnit.COUNT),
            category = FoodCategory.OTHER,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    setContent(viewModel, testFoodFacts)

    // Print the UI tree in the unmerged tree to confirm the node structure
    composeTestRule.onRoot(useUnmergedTree = true).printToLog("UI-TREE")

    // Use useUnmergedTree = true to find the node
    composeTestRule
        .onNodeWithTag("locationDropdown", useUnmergedTree = true)
        .assertExists()
        .performClick()

    // After clicking the dropdown, try selecting "Fridge"
    // It's possible you need useUnmergedTree here as well, depending on how the menu is structured
    composeTestRule.onNodeWithText("Fridge", useUnmergedTree = true).assertExists().performClick()

    assert(viewModel.location.name == "FRIDGE")
  }

  @Test
  fun foodInputContent_changeDatesAndValidate() {
    val viewModel = createViewModel()
    val testFoodFacts =
        FoodFacts(
            name = "Canned Beans",
            barcode = "00000",
            quantity = Quantity(1.0, FoodUnit.COUNT),
            category = FoodCategory.OTHER,
            nutritionFacts = NutritionFacts(),
            imageUrl = FoodFacts.DEFAULT_IMAGE_URL)

    setContent(viewModel, testFoodFacts)

    // Set buy date, expire date, open date
    composeTestRule
        .onNodeWithTag("buyDateTextField")
        .assertIsDisplayed()
        .performTextInput("20230715")
    composeTestRule
        .onNodeWithTag("expireDateTextField")
        .assertIsDisplayed()
        .performTextInput("20231231")
    composeTestRule
        .onNodeWithTag("openDateTextField")
        .assertIsDisplayed()
        .performTextInput("20230801")

    // Check if viewModel fields updated
    assert(viewModel.buyDate.contains("20230715"))
    assert(viewModel.expireDate.contains("20231231"))
    assert(viewModel.openDate.contains("20230801"))
  }
}
