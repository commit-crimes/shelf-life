package com.android.shelflife.viewmodel.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class FoodItemViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var foodItemRepository: FoodItemRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: FoodItemViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val userFlow = MutableStateFlow<User?>(User(
            uid = "userId", username = "TestUser", email = "test@example.com",
            photoUrl = "",
            selectedHouseholdUID = "householdId",
            householdUIDs = listOf("householdId"),
            recipeUIDs = listOf(),
            invitationUIDs = listOf()
        ))
        `when`(userRepository.user).thenReturn(userFlow)
        val selectedFoodFlow = MutableStateFlow<FoodItem?>(null)
        `when`(foodItemRepository.selectedFoodItem).thenReturn(selectedFoodFlow)
        viewModel = FoodItemViewModel(foodItemRepository, userRepository)
    }

    @Test
    fun `validate fields when submit button clicked`() {
        viewModel.foodName = ""
        viewModel.amount = "abc"
        viewModel.buyDate = "2023-01-01"
        viewModel.expireDate = "2022-12-31"

        viewModel.validateAllFieldsWhenSubmitButton()

        assertNotNull(viewModel.foodNameErrorResId)
        assertNotNull(viewModel.amountErrorResId)
        assertNotNull(viewModel.expireDateErrorResId)
        assertNull(viewModel.openDateErrorResId)
    }

    @Test
    fun `change food name triggers validation`() {
        viewModel.changeFoodName("New Name")
        assertEquals("New Name", viewModel.foodName)
        assertNull(viewModel.foodNameErrorResId)
    }

    @Test
    fun `change amount triggers validation`() {
        viewModel.changeAmount("123")
        assertEquals("123", viewModel.amount)
        assertNull(viewModel.amountErrorResId)
    }


    @Test
    fun `reset for scanner`() {
        viewModel.resetForScanner()

        assertEquals(FoodStorageLocation.PANTRY, viewModel.location)
        assertEquals("", viewModel.expireDate)
        assertEquals("", viewModel.openDate)
        assertNotNull(viewModel.buyDate)
        assertNull(viewModel.expireDateErrorResId)
        assertNull(viewModel.openDateErrorResId)
        assertNull(viewModel.buyDateErrorResId)
    }

    @Test
    fun `submit food item fails validation`() = runTest {
        viewModel.foodName = ""
        viewModel.amount = "abc"
        viewModel.buyDate = "2023-01-01"
        viewModel.expireDate = "2022-12-31"

        val result = viewModel.submitFoodItem()

        assertFalse(result)
    }
}