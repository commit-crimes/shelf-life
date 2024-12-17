package com.android.shelflife.viewmodel.navigation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.navigation.HouseholdSelectionDrawerViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class HouseholdSelectionDrawerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var houseHoldRepository: HouseHoldRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var foodItemRepository: FoodItemRepository

    private lateinit var viewModel: HouseholdSelectionDrawerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(houseHoldRepository.households).thenReturn(mock())
        `when`(houseHoldRepository.selectedHousehold).thenReturn(mock())
        viewModel = HouseholdSelectionDrawerViewModel(
            houseHoldRepository,
            userRepository,
            foodItemRepository
        )
    }

    @Test
    fun `households returns data from repository`() {
        assertEquals(houseHoldRepository.households, viewModel.households)
    }

    @Test
    fun `selectedHousehold returns data from repository`() {
        assertEquals(houseHoldRepository.selectedHousehold, viewModel.selectedHousehold)
    }

    @Test
    fun `selectHousehold invokes repository methods`() = runTest {
        val household = HouseHold("house1", "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        viewModel.selectHousehold(household)

        verify(houseHoldRepository).selectHousehold(household)
        verify(userRepository).selectHousehold(household.uid)
        verify(foodItemRepository).getFoodItems(household.uid)
    }

    @Test
    fun `selectHousehold does nothing when household is null`() = runTest {
        viewModel.selectHousehold(null)

        verifyNoInteractions(userRepository)
        verifyNoInteractions(foodItemRepository)
    }

    @Test
    fun `selectHouseholdToEdit invokes repository methods`() = runTest {
        val household = HouseHold("house2", "Editable Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        viewModel.selectHouseholdToEdit(household)

        verify(houseHoldRepository).selectHouseholdToEdit(household)
    }

    @Test
    fun `selectHouseholdToEdit does nothing when household is null`() = runTest {
        viewModel.selectHouseholdToEdit(null)

        verify(houseHoldRepository).selectHouseholdToEdit(null)
    }
}