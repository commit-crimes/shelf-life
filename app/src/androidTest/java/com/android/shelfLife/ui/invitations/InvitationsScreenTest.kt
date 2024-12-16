package com.android.shelfLife.ui.invitations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.invitations.InvitationViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InvitationScreenTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController = mockk<NavHostController>(relaxed = true)
    private val navigationActions = NavigationActions(navController)
    private val mockViewModel: InvitationViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
    }

    @Test
    fun testNoInvitationsMessageDisplayed() {
        // Mock an empty list of invitations
        val invitationsFlow = MutableStateFlow(emptyList<Invitation>())
        coEvery { mockViewModel.invitations } returns invitationsFlow

        composeTestRule.setContent {
            InvitationScreen(navigationActions = navigationActions, viewModel = mockViewModel)
        }

        // Verify "No pending invitations" message is displayed
        composeTestRule.onNodeWithText("No pending invitations").assertIsDisplayed()
    }

    @Test
    fun testInvitationsAreDisplayed() {
        // Mock a list of invitations
        val mockInvitations = listOf(
            Invitation(
                invitationId = "1",
                householdId = "household_1",
                householdName = "Household Alpha",
                invitedUserId = "user_1",
                inviterUserId = "inviter_1"
            ),
            Invitation(
                invitationId = "2",
                householdId = "household_2",
                householdName = "Household Beta",
                invitedUserId = "user_2",
                inviterUserId = "inviter_2"
            )
        )
        val invitationsFlow = MutableStateFlow(mockInvitations)
        coEvery { mockViewModel.invitations } returns invitationsFlow

        composeTestRule.setContent {
            InvitationScreen(navigationActions = navigationActions, viewModel = mockViewModel)
        }

        // Verify each invitation is displayed
        mockInvitations.forEach { invitation ->
            composeTestRule.onNodeWithText("You have been invited to join household: ${invitation.householdName}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun testAcceptAndDeclineButtonsAreDisplayed() {
        // Mock a list with a single invitation
        val mockInvitation = Invitation(
            invitationId = "1",
            householdId = "household_1",
            householdName = "Household Alpha",
            invitedUserId = "user_1",
            inviterUserId = "inviter_1"
        )
        val invitationsFlow = MutableStateFlow(listOf(mockInvitation))
        coEvery { mockViewModel.invitations } returns invitationsFlow

        composeTestRule.setContent {
            InvitationScreen(navigationActions = navigationActions, viewModel = mockViewModel)
        }

        // Verify "Accept" and "Decline" buttons are displayed
        composeTestRule.onNodeWithText("Accept").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decline").assertIsDisplayed()
    }
}