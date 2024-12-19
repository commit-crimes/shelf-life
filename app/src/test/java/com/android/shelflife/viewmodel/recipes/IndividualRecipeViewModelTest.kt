package com.android.shelflife.viewmodel.recipes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
@ExperimentalCoroutinesApi
class IndividualRecipeViewModelTest {
  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher()

  @MockK private lateinit var mockUserRepository: UserRepository

  @MockK private lateinit var mockRecipeRepository: RecipeRepository

  private lateinit var viewModel: IndividualRecipeViewModel

  private val recipe1 =
      Recipe(
          uid = "recipe1",
          name = "Pasta Bolognese",
          instructions = listOf("Cook  meat", " boil water", "add pasta", "eat"),
          servings = 2F,
          time = 45.minutes,
          ingredients =
              listOf(
                  Ingredient("Pasta", Quantity(200.0, FoodUnit.GRAM)),
                  Ingredient("Ground Beef", Quantity(100.0, FoodUnit.GRAM)),
                  Ingredient("Tomato Sauce", Quantity(100.0, FoodUnit.ML)),
                  Ingredient("Onion", Quantity(2.0, FoodUnit.COUNT))),
          recipeType = RecipeType.PERSONAL)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    val userFlow =
        MutableStateFlow<User>(
            User(
                uid = "user1",
                username = "User 1",
                email = "testemail@test.com",
                selectedHouseholdUID = "test-household-uid",
                householdUIDs = listOf("test-household-uid"),
                recipeUIDs = listOf("recipe1", "recipe2", "recipe3")))

    every { mockUserRepository.user } returns userFlow

    val recipe2 =
        Recipe(
            uid = "recipe2",
            name = "Pizza",
            instructions = listOf("Heat up the oven", "Place pizza until cooked", "eat"),
            servings = 2F,
            time = 25.minutes,
            ingredients = listOf(Ingredient("Pizza", Quantity(1.0, FoodUnit.COUNT))),
            recipeType = RecipeType.BASIC)

    val recipe3 =
        Recipe(
            uid = "recipe3",
            name = "scrambled eggs",
            instructions = listOf("scramble eggs", "cook eggs", "eat"),
            servings = 1F,
            time = 5.minutes,
            ingredients = listOf(Ingredient("eggs", Quantity(2.0, FoodUnit.COUNT))),
            recipeType = RecipeType.HIGH_PROTEIN)

    val recipesFlow = MutableStateFlow<List<Recipe>>(listOf(recipe1, recipe2, recipe3))

    every { mockRecipeRepository.recipes } returns recipesFlow

    val selectedRecipe = MutableStateFlow<Recipe>(recipe1)
    every { mockRecipeRepository.selectedRecipe } returns (selectedRecipe)

    viewModel = IndividualRecipeViewModel(mockRecipeRepository, mockUserRepository)
  }

  @Test
  fun `getRecipeName get the selected recipe's name`() = runTest {
    assertTrue(viewModel.getRecipeName() == recipe1.name)
  }

  @Test
  fun `getRecipeServing gets the selected recipe's number of servings`() = runTest {
    assertTrue(viewModel.getRecipeServing() == recipe1.servings)
  }

  @Test
  fun `getRecipeTime gets the selected recipe's time`() = runTest {
    assertTrue(viewModel.getRecipeTime() == recipe1.time.inWholeMinutes)
  }

  @Test
  fun `getRecipeIngredients gets the selected recipe's list of ingredients`() = runTest {
    assertTrue(viewModel.getRecipeIngredients() == recipe1.ingredients)
  }

  @Test
  fun `getRecipeInstruction gets the selected recipe's list of instruction`() = runTest {
    assertTrue(viewModel.getRecipeInstruction() == recipe1.instructions)
  }
}
