package com.android.shelflife.viewmodel.recipes

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
@ExperimentalCoroutinesApi
class RecipesViewModelTest{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var mockHouseHoldRepository: HouseHoldRepository

    @MockK
    private lateinit var mockFoodItemRepository: FoodItemRepository

    @MockK
    private lateinit var mockUserRepository: UserRepository

    @MockK
    private lateinit var mockRecipeRepository: RecipeRepository

    @MockK
    private lateinit var mockContext: Context

    private lateinit var viewModel : RecipesViewModel


}