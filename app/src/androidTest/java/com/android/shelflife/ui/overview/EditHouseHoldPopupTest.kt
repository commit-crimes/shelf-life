package com.android.shelflife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.overview.EditHouseHoldPopUp
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditHouseHoldPopUpTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var householdRepository: HouseholdRepositoryFirestore

  @Before
  fun setUp() {
    // Create a spy of the real ViewModel
    householdRepository = HouseholdRepositoryFirestore(FirebaseFirestore.getInstance())
    householdViewModel = spyk(HouseholdViewModel(householdRepository, mockk()))

    // Set the households data
    val testHouseholds =
        listOf(
            HouseHold(uid = "1", name = "Home", emptyList(), emptyList()),
            HouseHold(uid = "2", name = "Work", emptyList(), emptyList()))
    // Assuming setHouseholds is available for testing
    householdViewModel.setHouseholds(testHouseholds)
  }

  @Test
  fun dialogIsDisplayedWithHouseholds() {
    val showDialogState = mutableStateOf(true)

    // Act
    composeTestRule.setContent {
      EditHouseHoldPopUp(
          showDialog = showDialogState.value,
          onDismiss = { showDialogState.value = false },
          householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("editHouseholdPopup").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Work").assertIsDisplayed()
  }

  @Test
  fun editingHouseholdNameUpdatesViewModel() {
    val showDialogState = mutableStateOf(true)
    val newName = "New Home"

    // Act
    composeTestRule.setContent {
      EditHouseHoldPopUp(
          showDialog = showDialogState.value,
          onDismiss = { showDialogState.value = false },
          householdViewModel = householdViewModel)
    }

    // Perform text input to change the household name
    composeTestRule.onNodeWithText("Home").performTextReplacement(newName)

    // Click "Apply"
    composeTestRule.onNodeWithText("Apply").performClick()

    // Assert
    verify { householdViewModel.updateHousehold(match { it.uid == "1" && it.name == newName }) }
  }

  @Test
  fun deletingHouseholdCallsDeleteMethod() {
    val showDialogState = mutableStateOf(true)

    // Act
    composeTestRule.setContent {
      EditHouseHoldPopUp(
          showDialog = showDialogState.value,
          onDismiss = { showDialogState.value = false },
          householdViewModel = householdViewModel)
    }

    // Click the delete icon
    composeTestRule
        .onNodeWithTag("deleteIcon_${householdViewModel.households.value[0].uid}")
        .performClick()

    // Click "Apply"
    composeTestRule.onNodeWithText("Apply").performClick()

    // Assert
    verify { householdViewModel.deleteHouseholdById("1") }
  }

  @Test
  fun cancelButtonDismissesDialogWithoutChanges() {
    val showDialogState = mutableStateOf(true)

    // Act
    composeTestRule.setContent {
      EditHouseHoldPopUp(
          showDialog = showDialogState.value,
          onDismiss = { showDialogState.value = false },
          householdViewModel = householdViewModel)
    }

    // Click "Cancel"
    composeTestRule.onNodeWithText("Cancel").performClick()

    // Assert
    assert(!showDialogState.value)
    verify(exactly = 0) { householdViewModel.deleteHouseholdById(any()) }
    verify(exactly = 0) { householdViewModel.updateHousehold(any()) }
  }

  @Test
  fun togglingDeleteButtonAddsOrRemovesFromDeletionList() {
    val showDialogState = mutableStateOf(true)

    // Act
    composeTestRule.setContent {
      EditHouseHoldPopUp(
          showDialog = showDialogState.value,
          onDismiss = { showDialogState.value = false },
          householdViewModel = householdViewModel)
    }

    // Click the delete icon to mark for deletion
    composeTestRule
        .onNodeWithTag("deleteIcon_${householdViewModel.households.value[0].uid}")
        .performClick()
    // Click the delete icon again to unmark for deletion
    composeTestRule
        .onNodeWithTag("deleteIcon_${householdViewModel.households.value[0].uid}")
        .performClick()

    // Click "Apply"
    composeTestRule.onNodeWithText("Apply").performClick()

    // Assert
    verify(exactly = 0) { householdViewModel.deleteHouseholdById(any()) }
    verify { householdViewModel.updateHousehold(match { it.uid == "1" && it.name == "Home" }) }
  }
}
