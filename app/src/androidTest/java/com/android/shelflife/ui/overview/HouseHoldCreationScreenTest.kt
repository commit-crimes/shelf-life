package com.android.shelflife.ui.overview

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import io.mockk.MockKAnnotations
import io.mockk.mockk
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
  private lateinit var mockedNavigationActions: NavigationActions
  private lateinit var mockedHouseholdViewModel: HouseholdViewModel

  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    houseHoldRepository = mock(HouseholdRepositoryFirestore::class.java)
    foodItemRepository = mock(FoodItemRepository::class.java)
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    householdViewModel =
        HouseholdViewModel(
            houseHoldRepository,
            listFoodItemsViewModel,
            mockk<InvitationRepositoryFirestore>(relaxed = true),
            org.mockito.kotlin.mock<DataStore<Preferences>>())

    MockKAnnotations.init(this)
    mockedNavigationActions = mockk(relaxed = true)
    mockedHouseholdViewModel = mockk(relaxed = true)

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
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()
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
    composeTestRule.onNodeWithTag("cancelDeleteHouseholdButton").performClick()
    composeTestRule.onNodeWithTag("DeleteConfirmationDialog").assertDoesNotExist()
  }

  @Test
  fun addingEmailDisplaysInMemberList() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    val email = "test@example.com"
    composeTestRule.onNodeWithTag("AddEmailFab").performClick()
    composeTestRule.onNodeWithTag("EmailInputField").performTextInput(email)
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()

    composeTestRule.onNodeWithText(email).assertIsDisplayed()
  }

  @Test
  fun addingEmailDoesNotDisplayNewTextBox() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    val email = "example@example.com"
    composeTestRule.onNodeWithTag("AddEmailFab").performClick()
    composeTestRule.onNodeWithTag("EmailInputField").performTextInput(email)
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()
    composeTestRule.onNodeWithTag("EmailInputField").assertIsNotDisplayed()
  }
}
