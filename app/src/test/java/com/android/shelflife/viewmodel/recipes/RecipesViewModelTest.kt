package com.android.shelflife.viewmodel.recipes

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.minutes

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
@ExperimentalCoroutinesApi
class RecipesViewModelTest {
  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher()

  @MockK private lateinit var mockHouseHoldRepository: HouseHoldRepository

  @MockK private lateinit var mockUserRepository: UserRepository

  @MockK private lateinit var mockRecipeRepository: RecipeRepository

  @MockK private lateinit var mockContext: Context

  private lateinit var viewModel: RecipesViewModel

  @Before
  fun setUp(){
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    val householdFlow =
      MutableStateFlow<HouseHold?>(
        HouseHold(
          uid = "test-household-uid",
          name = "Test Household",
          members = listOf("user1", "user2"),
          sharedRecipes = emptyList(),
          ratPoints = emptyMap(),
          stinkyPoints = emptyMap()))
    every { mockHouseHoldRepository.selectedHousehold } returns householdFlow

    every { mockHouseHoldRepository.households } returns MutableStateFlow(emptyList())

    val userFlow  =
      MutableStateFlow<User>(
        User(uid = "user1",
          username = "User 1",
          email = "testemail@test.com",
          selectedHouseholdUID = "test-household-uid",
          householdUIDs = listOf("test-household-uid"),
          recipeUIDs = listOf("recipe1", "recipe2", "recipe3"))
      )

    every{ mockUserRepository.user} returns userFlow

    val recipe1 =
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
          Ingredient("Onion", Quantity(2.0, FoodUnit.COUNT))
        ),
        recipeType = RecipeType.PERSONAL)

    val recipe2 =
      Recipe(
        uid = "recipe2",
        name = "Pizza",
        instructions = listOf("Heat up the oven", "Place pizza until cooked", "eat"),
        servings = 2F,
        time = 25.minutes,
        ingredients = listOf(Ingredient("Pizza", Quantity(1.0, FoodUnit.COUNT))),
        recipeType = RecipeType.BASIC)

    val recipe3 = Recipe(
      uid = "recipe3",
      name = "scrambled eggs",
      instructions = listOf("scramble eggs", "cook eggs", "eat"),
      servings = 1F,
      time = 5.minutes,
      ingredients = listOf(Ingredient("eggs", Quantity(2.0, FoodUnit.COUNT))),
      recipeType = RecipeType.HIGH_PROTEIN
    )

    val recipesFlow = MutableStateFlow<List<Recipe>>(
      listOf(recipe1, recipe2, recipe3)
    )

    every {mockRecipeRepository.recipes} returns recipesFlow

    viewModel = RecipesViewModel(
      mockUserRepository, mockRecipeRepository, mockHouseHoldRepository
    )
  }

  @Test
  fun `clickOnFilter adds and removes filter correctly`() = runTest {
    // Initially no filters
    assertTrue(viewModel.selectedFilters.first().isEmpty())

    // Add a filter
    viewModel.clickOnFilter("Basic")
    assertTrue(viewModel.selectedFilters.first().contains("Basic"))

    // Remove the filter
    viewModel.clickOnFilter("Basic")
    assertFalse(viewModel.selectedFilters.first().contains("Basic"))
  }

  @Test
  fun `changeQuery changes query to the new value`() = runTest{
    //initialize that the query is null
    assertTrue(viewModel.query.value == "")

    // add value Pasta to query
    viewModel.changeQuery("Pasta")
    assertTrue(viewModel.query.value == "Pasta")

    //change value back to "" (i.e. deleting query, check UI)
    viewModel.changeQuery("")
    assertTrue(viewModel.query.value == "")
  }

  @Test
  fun `expandFab and shrinkFab change the value of fabExpanded`()= runTest{
    //we check that it is initially closed
    assertTrue(!viewModel.fabExpanded.value)

    // we expand it
    viewModel.expandFab()
    assertTrue(viewModel.fabExpanded.value)

    //we shrinck it back
    viewModel.shrinkFab()
    assertTrue(!viewModel.fabExpanded.value)
  }
}
