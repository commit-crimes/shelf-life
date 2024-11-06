package com.android.shelflife.ui.overview

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

class HouseHoldCreationScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel

  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    houseHoldRepository = mock(HouseHoldRepository::class.java)
    foodItemRepository = mock(FoodItemRepository::class.java)
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf())

    householdViewModel.finishedLoading.value = true
  }

  @Test
  fun houseHoldCreationScreenDisplaysAllComponentsInAddMode() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CloseButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeleteButton").assertDoesNotExist()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldMembersText").assertIsDisplayed()
  }

  @Test
  fun houseHoldCreationScreenDisplaysAllComponentsInEditMode() {
    householdViewModel.selectHouseholdToEdit(houseHold)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CloseButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeleteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assert(hasText(houseHold.name))
    composeTestRule.onNodeWithTag("HouseHoldMembersText").assertIsDisplayed()
  }

  @Test
  fun clickingCancelClosesScreen() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("CancelButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun clickingCloseButtonClosesScreen() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("CloseButton").performClick()
    verify(navigationActions).goBack()
  }

  /*
  // Not that great of a test, but it's a start
  @Test
  fun clickingConfirmVerifiesHouseHoldName(){
      householdViewModel.selectHouseholdToEdit(null)
      composeTestRule.setContent {
          HouseHoldCreationScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
      }
      composeTestRule.onNodeWithTag("HouseHoldNameTextField").performClick()
      composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput(houseHold.name)
      composeTestRule.onNodeWithTag("ConfirmButton").performClick()
      composeTestRule.onNodeWithTag("HouseHoldNameTextField").assert(hasText(houseHold.name))
  }

   */

  /*
  @Test
  fun clickingConfirmButtonSavesHouseHold(){
      householdViewModel.selectHouseholdToEdit(null)
      composeTestRule.setContent {
          HouseHoldCreationScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
      }
      composeTestRule.onNodeWithTag("HouseHoldNameTextField").performClick()
      composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New Household")
      composeTestRule.onNodeWithTag("ConfirmButton").performClick()
  }

   */

  @Test
  fun clickingDeleteButtonShowsConfirmationDialog() {
    householdViewModel.selectHouseholdToEdit(houseHold)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("DeleteButton").performClick()
    composeTestRule.onNodeWithTag("DeleteConfirmationDialog").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteButtonConfirmationDeletesHouseHold() {
    householdViewModel.selectHouseholdToEdit(houseHold)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("DeleteButton").performClick()
    composeTestRule.onNodeWithTag("ConfirmDeleteButton").performClick()
    verify(houseHoldRepository).deleteHouseholdById(any(), any(), any())
  }

  @Test
  fun clickingDeleteButtonCancelClosesDialog() {
    householdViewModel.selectHouseholdToEdit(houseHold)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("DeleteButton").performClick()
    composeTestRule.onNodeWithTag("CancelDeleteButton").performClick()
    composeTestRule.onNodeWithTag("DeleteConfirmationDialog").assertDoesNotExist()
  }
}
