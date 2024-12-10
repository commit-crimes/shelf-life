package com.android.shelflife.ui.invitations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.shelfLife.model.newFoodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.android.shelfLife.model.newhousehold.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.newInvitations.InvitationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class InvitationScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  @MockK private lateinit var householdViewModel: HouseholdViewModel
  @MockK private lateinit var navigationActions: NavigationActions
  // Define your test invitation here
  private val testInvitation =
      Invitation(
          householdName = "Test Household",
          invitationId = "0",
          householdId = "0",
          invitedUserId = "0",
          inviterUserId = "0")
  private val invitationRepository =
      object : InvitationRepository {
        override fun getInvitations(
            onSuccess: (List<Invitation>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
          onSuccess(listOf(testInvitation))
        }

        override fun addInvitationListener(
            onUpdate: (List<Invitation>) -> Unit,
            onError: (Exception) -> Unit
        ) {
          onUpdate(listOf(testInvitation))
        }

        override fun removeInvitationListener() {}

        override fun declineInvitation(
            invitation: Invitation,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun acceptInvitation(
            invitation: Invitation,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun sendInvitation(
            household: HouseHold,
            invitedUserEmail: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}
      }

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    navigationActions = mockk(relaxed = true)

    // Mock FirebaseAuth
    mockkStatic(FirebaseAuth::class)
    val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)

    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

    every { mockFirebaseAuth.addAuthStateListener(any()) } answers
        {
          val listener = it.invocation.args[0] as FirebaseAuth.AuthStateListener
          listener.onAuthStateChanged(mockFirebaseAuth)
        }
  }

  @After
  fun tearDown() {
    unmockkStatic(FirebaseAuth::class)
  }
  /*
  @Test
  fun invitationScreen_displaysNoInvitationsMessage_whenNoInvitations() {
    val emptyRepository =
        object : InvitationRepository {
          override fun declineInvitation(
              invitation: Invitation,
              onSuccess: () -> Unit,
              onFailure: (Exception) -> Unit
          ) {
            TODO("Not yet implemented")
          }

          override fun acceptInvitation(
              invitation: Invitation,
              onSuccess: () -> Unit,
              onFailure: (Exception) -> Unit
          ) {
            TODO("Not yet implemented")
          }

          override fun getInvitations(
              onSuccess: (List<Invitation>) -> Unit,
              onFailure: (Exception) -> Unit
          ) {
            onSuccess(emptyList())
          }

          override fun sendInvitation(
              household: HouseHold,
              invitedUserEmail: String,
              onSuccess: () -> Unit,
              onFailure: (Exception) -> Unit
          ) {
            TODO("Not yet implemented")
          }
        }
    val houseHoldRepository = mockk<HouseholdRepositoryFirestore>()
    every { houseHoldRepository.getHouseholds(any(), any()) } answers
        {
          val onSuccess = it.invocation.args[0] as (List<HouseHold>) -> Unit
          onSuccess(listOf(HouseHold("0", "Test Household", emptyList(), emptyList())))
        }
    householdViewModel =
        HouseholdViewModel(
            houseHoldRepository,
            mockk(relaxed = true),
            emptyRepository,
            mock<DataStore<Preferences>>())
    householdViewModel.setHouseholds(
        listOf(HouseHold("0", "Test Household", emptyList(), emptyList())))
    householdViewModel.selectHousehold(householdViewModel.households.value.first())
    val invitationViewModel = InvitationViewModel(emptyRepository)
    composeTestRule.setContent {
      InvitationScreen(
          invitationViewModel = invitationViewModel, navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithText("No pending invitations").assertIsDisplayed()
  }*/

  @Test
  fun invitationScreen_displaysInvitations_whenInvitationsArePresent() {

    val listFoodItemsViewModel = mockk<ListFoodItemsViewModel>(relaxed = true)
    val houseHoldRepository = mockk<HouseholdRepositoryFirestore>(relaxed = true)
    val householdViewModel =
        HouseholdViewModel(
            houseHoldRepository,
            listFoodItemsViewModel,
            invitationRepository,
            mock<DataStore<Preferences>>())

    composeTestRule.setContent {
      InvitationScreen(
          navigationActions = navigationActions, invitationRepository = invitationRepository)
    }

    // Assert
    composeTestRule.onNodeWithTag("invitationCard").assertIsDisplayed()
  }
}
