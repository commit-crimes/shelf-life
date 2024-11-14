package com.android.shelflife.ui.overview

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
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

  @Test
  fun clickingConfirmWithExistingHouseholdNameShowsError() {
    householdViewModel.selectHouseholdToEdit(null)
    `when`(householdViewModel.checkIfHouseholdNameExists(anyString())).thenReturn(true)

    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    val householdName = "Existing Household"
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput(householdName)
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    composeTestRule.onNodeWithText("Household name already exists").assertIsDisplayed()
  }

  @Test
  fun clickingConfirmWithNewHouseholdNameAddsHouseholdAndNavigatesBack() {
    householdViewModel.selectHouseholdToEdit(null)
    // Set up the Composable screen for the test
    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel
      )
    }

    // Act
    val householdName = "New Household"
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput(householdName)
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    // Assert
    assertTrue(householdViewModel.households.value.isNotEmpty())
    assertEquals(householdName, householdViewModel.households.value.first().name)
  }

  @Test
  fun clickingConfirmInEditModeUpdatesHousehold() {
    householdViewModel.selectHouseholdToEdit(houseHold)

    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Act
    val newHouseholdName = "Updated Household"
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput(newHouseholdName)
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    verify(householdViewModel).updateHousehold(any())
    verify(navigationActions).goBack()
  }

  @Test
  fun clickingConfirmWithMissingEmailsHandlesMissingEmails() {
    householdViewModel.selectHouseholdToEdit(null)
    `when`(householdViewModel.checkIfHouseholdNameExists(anyString())).thenReturn(false)
    doAnswer { invocation ->
      val emails = invocation.getArgument<List<String>>(0)
      val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
      callback(emptyMap())
    }.`when`(householdViewModel).getUserIdsByEmails(any(), any())

    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    val householdName = "New Household"
    val missingEmail = "missing@example.com"
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput(householdName)
    composeTestRule.onNodeWithText("Enter email").performTextInput(missingEmail)
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    verify(householdViewModel).addNewHousehold(eq(householdName), any())
    verify(navigationActions).goBack()
  }

  @Test
  fun addingEmailDisplaysInMemberList() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    val email = "test@example.com"
    composeTestRule.onNodeWithText("Enter email").performTextInput(email)
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()

    composeTestRule.onNodeWithText(email).assertIsDisplayed()
  }

  @Test
  fun removingEmailRemovesFromMemberList() {
    householdViewModel.selectHouseholdToEdit(null)
    composeTestRule.setContent {
      HouseHoldCreationScreen(
        navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    val email = "test@example.com"
    composeTestRule.onNodeWithText("Enter email").performTextInput(email)
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()
    composeTestRule.onAllNodesWithTag("RemoveEmailButton").onFirst().performClick()

    composeTestRule.onNodeWithText(email).assertDoesNotExist()
  }

}
