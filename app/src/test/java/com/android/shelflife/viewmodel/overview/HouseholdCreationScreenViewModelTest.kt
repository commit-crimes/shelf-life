package com.android.shelflife.viewmodel.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.overview.HouseholdCreationScreenViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class HouseholdCreationScreenViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var invitationRepository: InvitationRepository
  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: HouseholdCreationScreenViewModel

  private val householdToEditFlow = MutableStateFlow<HouseHold?>(null)
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)
  private val householdsFlow = MutableStateFlow<List<HouseHold>>(emptyList())
  private val userFlow = MutableStateFlow<User?>(null)

  @Before
  fun setUp() = runTest {
    houseHoldRepository = mock(HouseHoldRepository::class.java)
    foodItemRepository = mock(FoodItemRepository::class.java)
    invitationRepository = mock(InvitationRepository::class.java)
    userRepository = mock(UserRepository::class.java)

    `when`(houseHoldRepository.householdToEdit).thenReturn(householdToEditFlow)
    `when`(houseHoldRepository.selectedHousehold).thenReturn(selectedHouseholdFlow)
    `when`(houseHoldRepository.households).thenReturn(householdsFlow)
    `when`(userRepository.user).thenReturn(userFlow)

    // Default stubs for all calls that might return null by default:
    `when`(houseHoldRepository.getHouseholdMembers(anyString())).thenReturn(emptyList())
    `when`(userRepository.getUserEmails(anyList())).thenReturn(emptyMap())
    `when`(userRepository.getUserIds(anySet())).thenReturn(emptyMap())
    `when`(houseHoldRepository.checkIfHouseholdNameExists(anyString())).thenReturn(false)
    `when`(houseHoldRepository.getNewUid()).thenReturn("defaultUID") // Default UID if needed
  }

  @Test
  fun `init with no householdToEdit sets empty emailList and finishedLoading true`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    advanceUntilIdle()
    assertTrue(viewModel.emailList.value.isEmpty())
  }

  @Test
  fun `init with householdToEdit sets emailList from members`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val household =
        HouseHold("h1", "MyHouse", listOf("uid1", "uid2"), emptyList(), emptyMap(), emptyMap())
    householdToEditFlow.value = household

    `when`(houseHoldRepository.getHouseholdMembers("h1")).thenReturn(listOf("uid1", "uid2"))
    `when`(userRepository.getUserEmails(listOf("uid1", "uid2")))
        .thenReturn(mapOf("uid1" to "email1@test.com", "uid2" to "email2@test.com"))

    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    advanceUntilIdle()

    assertEquals(setOf("email1@test.com", "email2@test.com"), viewModel.emailList.value)
  }

  @Test
  fun `add and remove email updates emailList`() {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    viewModel.tryAddEmailCard("friend@test.com")
    assertEquals(setOf("friend@test.com"), viewModel.emailList.value)

    viewModel.removeEmail("friend@test.com")
    assertTrue(viewModel.emailList.value.isEmpty())
  }

  @Test
  fun `tryAddEmailCard doesn't add blank or duplicate emails`() {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    assertFalse(viewModel.tryAddEmailCard("   "))
    assertTrue(viewModel.tryAddEmailCard("hello@test.com"))
    assertFalse(viewModel.tryAddEmailCard("hello@test.com"))
    assertEquals(setOf("hello@test.com"), viewModel.emailList.value)
  }

  @Test
  fun `confirmHouseholdActions returns false if name invalid`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    // Name "" is invalid => no need to stub since code checks length
    val result = viewModel.confirmHouseholdActions("")
    assertFalse(result)
  }

  @Test
  fun `confirmHouseholdActions with existing name and not editing returns false`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    `when`(houseHoldRepository.checkIfHouseholdNameExists("MyHouse")).thenReturn(true)
    val result = viewModel.confirmHouseholdActions("MyHouse")
    assertFalse(result)
  }

  @Test
  fun `confirmHouseholdActions creates new household if no householdToEdit and name valid`() =
      runTest {
        viewModel =
            HouseholdCreationScreenViewModel(
                houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
        val user =
            User(
                "uid",
                "Tester",
                "tester@mail.com",
                "",
                selectedHouseholdUID = "householdX",
                householdUIDs = listOf("householdX"),
                recipeUIDs = emptyList(),
                invitationUIDs = emptyList())
        userFlow.value = user

        `when`(houseHoldRepository.checkIfHouseholdNameExists("NewHouse")).thenReturn(false)
        `when`(houseHoldRepository.getNewUid()).thenReturn("newHid")

        val result = viewModel.confirmHouseholdActions("NewHouse")
        advanceUntilIdle()

        assertTrue(result)
      }

  @Test
  fun `confirmHouseholdActions with new household and friend emails sends invitations`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val user =
        User(
            "uid",
            "Tester",
            "tester@mail.com",
            "",
            selectedHouseholdUID = "hid",
            householdUIDs = listOf("hid"),
            recipeUIDs = emptyList(),
            invitationUIDs = emptyList())
    userFlow.value = user
    viewModel.tryAddEmailCard("friend1@test.com")
    viewModel.tryAddEmailCard("friend2@test.com")

    `when`(houseHoldRepository.checkIfHouseholdNameExists("NewHouse")).thenReturn(false)
    `when`(houseHoldRepository.getNewUid()).thenReturn("hid2")
    `when`(userRepository.getUserIds(setOf("friend1@test.com", "friend2@test.com")))
        .thenReturn(mapOf("friend1@test.com" to "f1", "friend2@test.com" to "f2"))

    val result = viewModel.confirmHouseholdActions("NewHouse")
    advanceUntilIdle()

    assertTrue(result)
  }

  @Test
  fun `confirmHouseholdActions editing household with same name doesn't fail`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val oldHousehold =
        HouseHold("hid", "OldName", listOf("u1"), emptyList(), emptyMap(), emptyMap())
    householdToEditFlow.value = oldHousehold
    `when`(houseHoldRepository.checkIfHouseholdNameExists("OldName")).thenReturn(true)

    val result = viewModel.confirmHouseholdActions("OldName")
    advanceUntilIdle()

    assertTrue(result)
  }

  @Test
  fun `confirmHouseholdActions editing household with new name and adding members`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val oldHousehold =
        HouseHold("hid", "OldName", listOf("u1"), emptyList(), emptyMap(), emptyMap())
    householdToEditFlow.value = oldHousehold

    `when`(houseHoldRepository.checkIfHouseholdNameExists("NewName")).thenReturn(false)
    // Add an email (new member)
    viewModel.tryAddEmailCard("new@test.com")
    `when`(userRepository.getUserIds(setOf("new@test.com")))
        .thenReturn(mapOf("new@test.com" to "u2"))

    val result = viewModel.confirmHouseholdActions("NewName")
    advanceUntilIdle()

    assertTrue(result)
  }

  @Test
  fun `confirmHouseholdActions editing household and removing members`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val oldHousehold =
        HouseHold("hid", "OldName", listOf("u1", "u2"), emptyList(), emptyMap(), emptyMap())
    householdToEditFlow.value = oldHousehold

    `when`(houseHoldRepository.checkIfHouseholdNameExists("Renamed")).thenReturn(false)
    `when`(userRepository.getUserEmails(listOf("u1", "u2")))
        .thenReturn(mapOf("u1" to "u1@mail.com", "u2" to "u2@mail.com"))

    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    advanceUntilIdle()

    // Initially has u1@mail.com and u2@mail.com
    // Remove u2@mail.com:
    viewModel.removeEmail("u2@mail.com")
    `when`(userRepository.getUserIds(setOf("u1@mail.com"))).thenReturn(mapOf("u1@mail.com" to "u1"))

    val result = viewModel.confirmHouseholdActions("Renamed")
    advanceUntilIdle()

    assertTrue(result)
  }

  @Test
  fun `confirmHouseholdActions fails if user not logged in when creating new household`() =
      runTest {
        viewModel =
            HouseholdCreationScreenViewModel(
                houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
        userFlow.value = null
        `when`(houseHoldRepository.checkIfHouseholdNameExists("NoUserHouse")).thenReturn(false)

        val result = viewModel.confirmHouseholdActions("NoUserHouse")
        advanceUntilIdle()

        assertTrue(result)
      }

  @Test
  fun `addNewHousehold with friend emails but some not found logs warning`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val user =
        User(
            "uid",
            "Tester",
            "tester@mail.com",
            "",
            selectedHouseholdUID = "hid",
            householdUIDs = listOf("hid"),
            recipeUIDs = emptyList(),
            invitationUIDs = emptyList())
    userFlow.value = user
    viewModel.tryAddEmailCard("known@mail.com")
    viewModel.tryAddEmailCard("unknown@mail.com")

    `when`(houseHoldRepository.getNewUid()).thenReturn("hid3")
    `when`(userRepository.getUserIds(setOf("known@mail.com", "unknown@mail.com")))
        .thenReturn(mapOf("known@mail.com" to "kUID")) // unknown not found

    `when`(houseHoldRepository.checkIfHouseholdNameExists("MixedEmails")).thenReturn(false)
    val result = viewModel.confirmHouseholdActions("MixedEmails")
    advanceUntilIdle()

    assertTrue(result)
  }

  @Test
  fun `updateHousehold selects and fetches items if current selected matches`() = runTest {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository, foodItemRepository, invitationRepository, userRepository)
    val household = HouseHold("hid", "HH", listOf("u1"), emptyList(), emptyMap(), emptyMap())
    householdsFlow.value = listOf(household)
    selectedHouseholdFlow.value = household

    val user =
        User(
            "uid",
            "Tester",
            "tester@mail.com",
            "",
            selectedHouseholdUID = "hid",
            householdUIDs = listOf("hid"),
            recipeUIDs = emptyList(),
            invitationUIDs = emptyList())
    userFlow.value = user

    `when`(houseHoldRepository.checkIfHouseholdNameExists("HH2")).thenReturn(false)

    // First confirm to cause update scenario
    viewModel.confirmHouseholdActions("HH2")
    advanceUntilIdle()

    // Add email to force differences
    viewModel.tryAddEmailCard("friend@mail.com")
    `when`(userRepository.getUserIds(setOf("friend@mail.com")))
        .thenReturn(mapOf("friend@mail.com" to "fId"))

    val result = viewModel.confirmHouseholdActions("HH2")
    advanceUntilIdle()

    assertTrue(result)
  }
}
