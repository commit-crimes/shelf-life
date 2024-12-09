package com.android.shelfLife.viewmodel

import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import javax.inject.Inject

@HiltAndroidTest
class FoodItemViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var foodItemRepository: FoodItemRepository

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var viewModel: FoodItemViewModel

    private val mockUserFlow = MutableStateFlow<User?>(null)
    private val mockSelectedFoodFlow = MutableStateFlow<FoodItem?>(null)

    @Before
    fun setUp() {
        hiltRule.inject()

        // Mock dependencies
        `when`(userRepository.user).thenReturn(mockUserFlow)
        `when`(foodItemRepository.selectedFoodItem).thenReturn(mockSelectedFoodFlow)

        viewModel = FoodItemViewModel(foodItemRepository, userRepository)
    }

    @Test
    fun `submitFoodItem adds new food item`() = runTest {
        // Arrange
        viewModel.foodName = "Apple"
        viewModel.amount = "1"
        viewModel.expireDate = "2024-12-31"

        // Act
        viewModel.submitFoodItem()

        // Assert
        verify(foodItemRepository, times(1)).addFoodItem(anyString(), any(FoodItem::class.java))
    }
}