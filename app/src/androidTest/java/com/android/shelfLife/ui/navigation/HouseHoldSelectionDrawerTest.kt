package com.android.shelfLife.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.DeletionConfirmationPopUp
import com.android.shelfLife.viewmodel.navigation.HouseholdSelectionDrawerViewModel
import com.android.shelfLife.viewmodel.utils.DeletionConfirmationViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class HouseHoldSelectionDrawerTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository

  lateinit var navigationActions: NavigationActions

  private lateinit var instrumentationContext: android.content.Context

  // Test flows for data
  private val householdsFlow = MutableStateFlow<List<HouseHold>>(emptyList())
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)
  private val householdToEditFlow =
      MutableStateFlow<HouseHold?>(null) // For DeletionConfirmationViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    // Mock repository flows
    whenever(houseHoldRepository.households).thenReturn(householdsFlow.asStateFlow())
    whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHouseholdFlow.asStateFlow())
    whenever(houseHoldRepository.householdToEdit).thenReturn(householdToEditFlow.asStateFlow())

    runBlocking {
      // Stubs for repository calls
      whenever(houseHoldRepository.selectHousehold(any())).thenAnswer {}
      whenever(userRepository.selectHousehold(any())).thenAnswer {}
      whenever(foodItemRepository.getFoodItems(any())).thenAnswer {}
      whenever(houseHoldRepository.selectHouseholdToEdit(anyOrNull())).thenAnswer {}

      // For deletion scenario
      whenever(houseHoldRepository.deleteHouseholdById(any())).thenAnswer {}
      whenever(userRepository.deleteHouseholdUID(any())).thenAnswer {}
    }
  }

  // Create the HouseholdSelectionDrawerViewModel after flows and mocks
  private fun createHouseholdSelectionDrawerViewModel(): HouseholdSelectionDrawerViewModel {
    return HouseholdSelectionDrawerViewModel(
        houseHoldRepository, userRepository, foodItemRepository)
  }

  // Create the DeletionConfirmationViewModel after flows and mocks
  private fun createDeletionConfirmationViewModel(): DeletionConfirmationViewModel {
    return DeletionConfirmationViewModel(houseHoldRepository, userRepository, foodItemRepository)
  }

  @Test
  fun displaysHouseholdsCorrectly(): Unit = runBlocking {
    val household1 =
        HouseHold(
            uid = "1",
            name = "Household One",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    val household2 =
        HouseHold(
            uid = "2",
            name = "Household Two",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    // Setup data before creating ViewModel
    householdsFlow.value = listOf(household1, household2)
    selectedHouseholdFlow.value = household1
    val drawerViewModel = createHouseholdSelectionDrawerViewModel()

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      // For now, no deletion pop-up here, just drawer
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = { Box {} })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().printToLog("UI-TREE")

    // Verify both households are displayed
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
  fun selectsHouseholdWhenClicked() = runTest {
    val household1 =
        HouseHold(
            uid = "1",
            name = "Household One",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    val household2 =
        HouseHold(
            uid = "2",
            name = "Household Two",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    householdsFlow.value = listOf(household1, household2)
    selectedHouseholdFlow.value = household1
    val drawerViewModel = createHouseholdSelectionDrawerViewModel()

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = { Box {} })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().printToLog("UI-TREE")

    // Perform click on the second household
    composeTestRule.onNodeWithTag("householdElement_1").performClick()

    // Verify selectHousehold was called via repository interactions
    verify(houseHoldRepository).selectHousehold(household2)
    verify(userRepository).selectHousehold("2")
    verify(foodItemRepository).getFoodItems("2")
  }

  @Test
  fun navigatesToHouseholdCreationWhenAddButtonClicked() = runTest {
    val drawerViewModel = createHouseholdSelectionDrawerViewModel()

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = { Box {} })
    }

    composeTestRule.waitForIdle()

    // Click the add household icon
    composeTestRule.onNodeWithTag("addHouseholdIcon").performClick()

    // Verify navigation
    verify(navigationActions).navigateTo(Screen.HOUSEHOLD_CREATION)
  }

  @Test
  fun togglesEditModeWhenEditButtonClicked() = runTest {
    val household1 =
        HouseHold(
            uid = "1",
            name = "Household One",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    val drawerViewModel = createHouseholdSelectionDrawerViewModel()

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = { Box {} })
    }
    composeTestRule.waitForIdle()

    // Initially, edit icons should not be displayed
    composeTestRule.onAllNodesWithTag("editHouseholdIndicatorIcon").assertCountEquals(0)

    // Click the edit household icon to enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // After enabling edit mode, edit icons should appear
    composeTestRule.onAllNodesWithTag("editHouseholdIndicatorIcon").assertCountEquals(1)
  }

  @Test
  fun showsConfirmationDialogWhenDeletingHousehold() = runTest {
    val household1 =
        HouseHold(
            uid = "1",
            name = "Household One",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    // Set the householdToEdit before creating DeletionConfirmationViewModel
    // Later, when we click delete, we show the pop-up
    val drawerViewModel = createHouseholdSelectionDrawerViewModel()

    // We'll show the pop-up in conjunction with the drawer
    // For that, we need a DeletionConfirmationViewModel and set householdToEditFlow
    // to a specific household before confirming deletion
    val deletionViewModel = createDeletionConfirmationViewModel()

    // We'll simulate showDeleteDialog state in the test by remembering it in setContent
    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      var showDeleteDialog = remember { mutableStateOf(false) }

      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = {
            // Content can be empty, just a Box
            Box {}
            // Confirmation Dialog for Deletion
            DeletionConfirmationPopUp(
                showDeleteDialog = showDeleteDialog.value,
                onDismiss = { showDeleteDialog.value = false },
                onConfirm = { showDeleteDialog.value = false },
                deletionConfirmationViewModel = deletionViewModel)
          })

      // After enabling edit mode and clicking delete icon,
      // we will set householdToEditFlow and showDeleteDialog
    }

    composeTestRule.waitForIdle()

    // Enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // Click the delete icon
    // At this moment, selectHouseholdToEdit will be called, we must set householdToEditFlow now
    runBlocking {
      // Mock that selectHouseholdToEdit sets householdToEditFlow.value
      // In real scenario, we must ensure that when selectHouseholdToEdit is called,
      // householdToEditFlow is updated. Since we're testing UI only, let's just simulate this:
      householdToEditFlow.value = household1
    }

    composeTestRule.onNodeWithTag("deleteHouseholdIcon").performClick()

    // Verify the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to delete 'Household One'?")
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelDeleteHouseholdButton").assertIsDisplayed()

    // Confirm deletion
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()

    verify(houseHoldRepository).deleteHouseholdById("1")
  }

  @Test
  fun hidesConfirmationDialogWhenDeletionCanceled() = runTest {
    val household1 =
        HouseHold(
            uid = "1",
            name = "Household One",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    householdsFlow.value = listOf(household1)
    selectedHouseholdFlow.value = household1

    val drawerViewModel = createHouseholdSelectionDrawerViewModel()
    val deletionViewModel = createDeletionConfirmationViewModel()

    composeTestRule.setContent {
      val scope = rememberCoroutineScope()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
      var showDeleteDialog = remember { mutableStateOf(false) }

      HouseHoldSelectionDrawer(
          scope = scope,
          drawerState = drawerState,
          navigationActions = navigationActions,
          householdSelectionDrawerViewModel = drawerViewModel,
          content = {
            Box {}
            DeletionConfirmationPopUp(
                showDeleteDialog = showDeleteDialog.value,
                onDismiss = { showDeleteDialog.value = false },
                onConfirm = { showDeleteDialog.value = false },
                deletionConfirmationViewModel = deletionViewModel)
          })
    }

    composeTestRule.waitForIdle()

    // Enable edit mode
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()

    // Set the householdToEditFlow for deletion
    runBlocking { householdToEditFlow.value = household1 }

    // Click the delete icon
    composeTestRule.onNodeWithTag("deleteHouseholdIcon").performClick()

    // Verify that the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()

    // Cancel deletion
    composeTestRule.onNodeWithTag("cancelDeleteHouseholdButton").performClick()

    // Verify dialog is no longer displayed
    composeTestRule.onNodeWithText("Delete Household").assertDoesNotExist()
  }
}
