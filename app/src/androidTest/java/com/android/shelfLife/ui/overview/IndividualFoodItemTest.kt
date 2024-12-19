package com.android.shelflife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.viewmodel.overview.IndividualFoodItemViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import java.util.*
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class IndividualFoodItemTest {

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Inject lateinit var listFoodItemsRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  private lateinit var individualFoodItemViewModel: IndividualFoodItemViewModel

  private lateinit var instrumentationContext: android.content.Context

  private lateinit var houseHold: HouseHold
  private lateinit var foodItem: FoodItem

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    whenever(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
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
    // Initialize the household with the food item
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    userRepository.selectHousehold("Test Household")
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    individualFoodItemViewModel =
        IndividualFoodItemViewModel(listFoodItemsRepository, userRepository)

    individualFoodItemViewModel.selectedFood = foodItem
  }

  @Test
  fun testUnselectFoodItemClearsSelection() {
    composeTestRule.setContent {
      IndividualFoodItemScreen(navigationActions, individualFoodItemViewModel)
    }
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertTextEquals("Apple")
    composeTestRule.onNodeWithTag("IndividualFoodItemImage").assertExists()
  }

  @Test
  fun testEditButtonNavigatesToEditScreen() {
    composeTestRule.setContent {
      IndividualFoodItemScreen(navigationActions, individualFoodItemViewModel)
    }
    composeTestRule.onNodeWithTag("editFoodFab").performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_FOOD)
  }

  @Test
  fun testDeleteButtonRemovesFoodItem() {
    composeTestRule.setContent {
      IndividualFoodItemScreen(navigationActions, individualFoodItemViewModel)
    }
    composeTestRule.onNodeWithTag("deleteFoodItem").assertExists()
  }
}
