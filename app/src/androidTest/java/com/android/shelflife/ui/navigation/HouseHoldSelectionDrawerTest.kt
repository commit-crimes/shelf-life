package com.android.shelflife.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.newFoodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.android.shelfLife.model.newhousehold.HouseholdViewModel
import com.android.shelfLife.model.newInvitations.InvitationRepositoryFirestore
import com.android.shelfLife.ui.newnavigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class HouseHoldSelectionDrawerTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockHouseholdRepository: HouseholdRepositoryFirestore
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var mockFoodItemViewModel: ListFoodItemsViewModel

  private val householdsFlow = MutableStateFlow<List<HouseHold>>(emptyList())
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)

  @Before
  fun setUp() {
    // Mock the NavigationActions
    mockNavigationActions = mockk(relaxed = true)

    // Mock the HouseholdRepository
    mockHouseholdRepository = mockk(relaxed = true)
    mockFoodItemViewModel = mockk(relaxed = true)
    // Use real MutableStateFlow instances in the ViewModel
    householdViewModel =
        HouseholdViewModel(
            mockHouseholdRepository,
            mockFoodItemViewModel,
            mockk<InvitationRepositoryFirestore>(relaxed = true),
            mock<DataStore<Preferences>>())

    // Replace the ViewModel's flows with our test flows
    householdViewModel.households = householdsFlow
    householdViewModel.selectedHousehold = selectedHouseholdFlow
  }

  @Test
  fun displaysHouseholdsCorrectly() {
    val household1 =
        HouseHold(uid = "1", name = "Household One", members = emptyList(), foodItems = emptyList())
    val household2 =
        HouseHold(uid = "2", name = "Household Two", members = emptyList(), foodItems = emptyList())
    householdsFlow.value = listOf(household1, household2)
    selectedHouseholdFlow.value = household1

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }

    // Verify that both households are displayed
    composeTestRule
        .onNodeWithTag("householdElement_0")
        .assertIsDisplayed()
        .assertTextContains("Household One")

    composeTestRule
        .onNodeWithTag("householdElement_1")
        .assertIsDisplayed()
        .assertTextContains("Household Two")
  }

  @Test
  fun selectsHouseholdWhenClicked() {
    val household1 =
        HouseHold(uid = "1", name = "Household One", members = emptyList(), foodItems = emptyList())
    val household2 =
        HouseHold(uid = "2", name = "Household Two", members = emptyList(), foodItems = emptyList())
    householdsFlow.value = listOf(household1, household2)
    selectedHouseholdFlow.value = household1

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }

    // Perform click on the second household
    composeTestRule.onNodeWithTag("householdElement_1").performClick()

    // Verify that selectHousehold was called with household2
    verify { householdViewModel.selectHousehold(household2) }
  }

  @Test
  fun navigatesToHouseholdCreationWhenAddButtonClicked() {
    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }

    // Click the add household icon
    composeTestRule.onNodeWithTag("addHouseholdIcon").performClick()

    // Verify that navigation to household creation screen occurred
    verify { mockNavigationActions.navigateTo(Screen.HOUSEHOLD_CREATION) }
  }

  @Test
  fun togglesEditModeWhenEditButtonClicked() {
    val household1 =
        HouseHold(uid = "1", name = "Household One", members = emptyList(), foodItems = emptyList())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }
    // Initially, edit icons should not be displayed
    composeTestRule.onAllNodesWithTag("editHouseholdIndicatorIcon").assertCountEquals(0)

    // Click the edit household icon to enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // Now, edit icons should be displayed
    composeTestRule.onAllNodesWithTag("editHouseholdIndicatorIcon").assertCountEquals(1)
  }

  @Test
  fun showsConfirmationDialogWhenDeletingHousehold() {
    val household1 =
        HouseHold(uid = "1", name = "Household One", members = emptyList(), foodItems = emptyList())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }

    // Enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // Click the delete icon
    composeTestRule.onNodeWithTag("deleteHouseholdIcon").performClick()

    // Verify that the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelDeleteHouseholdButton").assertIsDisplayed()

    // Confirm deletion
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()
  }

  @Test
  fun hidesConfirmationDialogWhenDeletionCanceled() {
    val household1 =
        HouseHold(uid = "1", name = "Household One", members = emptyList(), foodItems = emptyList())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = mockNavigationActions,
          householdViewModel = householdViewModel,
          content = { Box {} } // Empty content
          )
    }

    // Enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // Click the delete icon
    composeTestRule.onNodeWithTag("deleteHouseholdIcon").performClick()

    // Verify that the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()

    // Cancel deletion
    composeTestRule.onNodeWithTag("cancelDeleteHouseholdButton").performClick()

    // Verify that the confirmation dialog is no longer displayed
    composeTestRule.onNodeWithText("Delete Household").assertDoesNotExist()
  }
}
