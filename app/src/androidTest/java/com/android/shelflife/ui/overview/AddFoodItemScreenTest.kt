package com.android.shelflife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.FoodSearchInput
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.newhousehold.HouseholdViewModel
import com.android.shelfLife.ui.newnavigation.NavigationActions
import com.android.shelfLife.ui.newoverview.AddFoodItemScreen
import com.android.shelfLife.ui.newutils.formatTimestampToDate
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddFoodItemScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHoldViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var foodFactsViewModel: FoodFactsViewModel
  private lateinit var fakeRepository: FakeFoodFactsRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Initialize MockK
    navigationActions = mockk()
    houseHoldViewModel = mockk()
    foodItemViewModel = mockk()

    // Mock getUID() to return a valid UID
    // `when`(foodItemRepository.getNewUid()).thenReturn("testUID")
    every { foodItemViewModel.getUID() } returns "testUID"
    every { houseHoldViewModel.addFoodItem(any()) } just runs
    every { navigationActions.goBack() } just runs
    // Mock getFoodFactsSuggestions() to return a StateFlow
    val sampleFoodFactsList =
        listOf(
            FoodFacts(
                "Sample Food 1",
                "1234567890",
                Quantity(1.0, FoodUnit.COUNT),
                FoodCategory.OTHER,
                NutritionFacts(),
                DEFAULT_IMAGE_URL),
            FoodFacts(
                "Sample Food 2",
                "1234567891",
                Quantity(2.0, FoodUnit.COUNT),
                FoodCategory.OTHER,
                NutritionFacts(),
                DEFAULT_IMAGE_URL),
            FoodFacts(
                "Sample Food 3",
                "1234567892",
                Quantity(3.0, FoodUnit.COUNT),
                FoodCategory.OTHER,
                NutritionFacts(),
                DEFAULT_IMAGE_URL),
            FoodFacts(
                "Sample Food 4",
                "1234567893",
                Quantity(4.0, FoodUnit.COUNT),
                FoodCategory.OTHER,
                NutritionFacts(),
                DEFAULT_IMAGE_URL),
            FoodFacts(
                "Sample Food 5",
                "1234567894",
                Quantity(5.0, FoodUnit.COUNT),
                FoodCategory.OTHER,
                NutritionFacts(),
                DEFAULT_IMAGE_URL))

    fakeRepository = FakeFoodFactsRepository().apply { foodFactsList = sampleFoodFactsList }
    foodFactsViewModel = FoodFactsViewModel(fakeRepository)
  }

  @Test
  fun testInitialUIComponentsDisplayed() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Verify that all input fields are displayed
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodUnit"))
    composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodCategory"))
    composeTestRule.onNodeWithTag("inputFoodCategory").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodLocation"))
    composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodOpenDate"))
    composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodBuyDate"))
    composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
  }

  @Test
  fun testFoodNameFieldValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Enter invalid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Food name contains invalid characters").assertIsDisplayed()

    // Enter valid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apples")
    // Verify error message is gone
    composeTestRule.onNodeWithText("Food name contains invalid characters").assertDoesNotExist()
  }

  @Test
  fun testAmountFieldValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Enter invalid amount (letters)
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be a number").assertIsDisplayed()

    // Enter invalid amount (negative number)
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("-5")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be positive").assertIsDisplayed()

    // Enter valid amount
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("10")
    // Verify error messages are gone
    composeTestRule.onNodeWithText("Amount must be a number").assertDoesNotExist()
    composeTestRule.onNodeWithText("Amount must be positive").assertDoesNotExist()
  }

  @Test
  fun testUnitDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Open the unit dropdown
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
    // Select a unit
    composeTestRule.onNodeWithTag("dropDownItem_Ml").performClick()
    // Verify the selected unit
    composeTestRule.onNodeWithTag("dropdownMenu_Select unit").assertTextContains("Ml")
  }

  @Test
  fun testCategoryDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Open the category dropdown
    composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
    // Select a category
    composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()
    // Verify the selected category
    composeTestRule.onNodeWithTag("dropdownMenu_Select category").assertTextContains("Fruit")
  }

  @Test
  fun testLocationDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Open the location dropdown
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
    // Select a location
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    // Verify the selected location
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
  }

  @Test
  fun testDateFieldsValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Enter invalid expire date
    composeTestRule
        .onNodeWithTag("inputFoodExpireDate")
        .performTextInput("31132023") // Invalid date
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()

    // Enter valid expire date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("inputFoodExpireDate")
        .performTextInput("31122030") // Valid future date
    // Verify error message is gone
    composeTestRule.onNodeWithText("Invalid date").assertDoesNotExist()
  }

  @Test
  fun testSubmitButtonWithInvalidForm() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }
    // Scroll to the submit button
    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button without filling the form
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that error messages are displayed for required fields
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithText("Food name cannot be empty").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithText("Amount cannot be empty").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithText("Date cannot be empty").assertIsDisplayed()
  }

  @Test
  fun testSubmitButtonWithValidForm() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Count").performClick()
    composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122030") // Future date
    // Clear and re-enter buy date to ensure it's valid
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("inputFoodBuyDate")
        .performTextInput(formatTimestampToDate(Timestamp.now()))

    // Scroll to the submit button
    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that the addFoodItem function was called
    verify { houseHoldViewModel.addFoodItem(any()) }

    // Verify that navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun testOpenDateValidationAgainstBuyAndExpireDates() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Enter buy date
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01012026")

    // Enter expire date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122026")

    // Enter invalid open date (before buy date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("31122025")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertIsDisplayed()

    // Enter valid open date (after buy date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01022026")
    // Verify error message is gone
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertDoesNotExist()

    // Enter invalid open date (after expire date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01012027")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be after Expire Date").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Click the back button
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // Verify that the navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun testCancelButtonNavigatesBack() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("cancelButton"))

    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("cancelButton").performClick()

    verify { navigationActions.goBack() }
  }

  @Test
  fun testImageSelection() {

    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Scroll to the image selection section
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("selectImage"))
    composeTestRule.onNodeWithTag("selectImage").assertIsDisplayed()
    // Select the first image
    composeTestRule.onAllNodesWithTag("foodImage")[0].performClick()

    // Verify that the selected image is displayed
  }

  @Test
  fun testNoImageOptionSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Scroll to the "No Image" option
    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("noImage"))

    // Select the "No Image" option
    composeTestRule.onNodeWithTag("noImage").performClick()
    // Verify that the default image is displayed
  }

  fun testDateReValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactsViewModel)
    }

    // Enter valid buy date
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01012026")

    // Enter valid expire date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122026")

    // Enter valid open date
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01022026")

    // Change buy date to an invalid one
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("31132023")

    // Verify error messages are displayed for dependent dates
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()
  }

  inner class FakeFoodFactsRepository : FoodFactsRepository {
    var shouldReturnError = false
    var foodFactsList = listOf<FoodFacts>()

    override fun searchFoodFacts(
        searchInput: FoodSearchInput,
        onSuccess: (List<FoodFacts>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      if (shouldReturnError) {
        onFailure(Exception("Test exception"))
      } else {
        onSuccess(foodFactsList)
      }
    }
  }

  companion object {
    const val DEFAULT_IMAGE_URL =
        "https://media.istockphoto.com/id/1354776457/vector/default-image-icon-vector-missing-picture-page-for-website-design-or-mobile-app-no-photo.jpg?s=612x612&w=0&k=20&c=w3OW0wX3LyiFRuDHo9A32Q0IUMtD4yjXEvQlqyYk9O4="
  }
}
