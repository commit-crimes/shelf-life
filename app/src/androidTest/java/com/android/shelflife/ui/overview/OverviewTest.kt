package com.android.shelflife.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.overview.OverviewScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class OverviewTest {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel

  private val houseHold =
      HouseHold(uid = "1", name = "Test", members = listOf("John", "Doe"), foodItems = listOf())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    foodItemRepository = mock(FoodItemRepository::class.java)
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)

    houseHoldRepository = mock(HouseHoldRepository::class.java)
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
  }

  // Test if the FirstTimeWelcomeScreen and all its elements are displayed correctly
  @Test
  fun firstTimeWelcomeScreenDisplayedCorrectly() {
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("firstTimeWelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdNameSaveButton").assertIsDisplayed()
  }

  // Test if the OverviewScreen is displayed with all elements
  @Test
  fun overviewScreenDisplayedCorrectly() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed()
  }

  // Clicking on hamburger icon opens the household selection drawer
  @Test
  fun clickHamburgerIconOpensHouseholdSelectionDrawer() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
  }

  // Clicking on the filter icon opens the filter bar
  @Test
  fun clickFilterIconOpensFilterBar() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("filterIcon").performClick()
    composeTestRule.onNodeWithTag("filterBar").assertIsDisplayed()
  }

  // Clicking on edit icon in the drawer opens the edit household popup
  @Test
  fun clickEditInDrawerOpensEditHouseholdPopup() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
    composeTestRule.onNodeWithTag("editHouseholdPopup").assertIsDisplayed()
  }

  // Clicking on add icon in the drawer opens the add household popup
  @Test
  fun clickAddInDrawerOpensAddHouseholdPopup() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("addHouseholdIcon").performClick()
    composeTestRule.onNodeWithTag("addHouseholdPopup").assertIsDisplayed()
  }

  // Does not work for now because the add food screen is not finished
  /*
  // Clicking on the add food fab opens the add food screen
  @Test
  fun clickAddFoodFabOpensAddFoodScreen() {
      householdViewModel.selectHousehold(houseHold)
      composeTestRule.setContent { OverviewScreen(
          navigationActions = navigationActions,
          listFoodItemsViewModel = listFoodItemsViewModel,
          householdViewModel = householdViewModel
      ) }

      composeTestRule.onNodeWithTag("addFoodFab").performClick()
      verify(navigationActions).navigateTo(Route.ADD_FOOD)
  }
   */
}
