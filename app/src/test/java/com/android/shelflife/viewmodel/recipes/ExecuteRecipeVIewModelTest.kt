package com.android.shelflife.viewmodel.recipes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeExecutionState
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.*
import kotlin.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class ExecuteRecipeViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var recipeRepository: RecipeRepository
  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: ExecuteRecipeViewModel

  private val testDispatcher = StandardTestDispatcher()

  // StateFlows from the repositories
  private val userFlow = MutableStateFlow<User?>(null)
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)
  private val foodItemsFlow = MutableStateFlow<List<FoodItem>>(emptyList())
  private val selectedRecipeFlow = MutableStateFlow<Recipe?>(null)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    houseHoldRepository = mock()
    foodItemRepository = mock()
    recipeRepository = mock()
    userRepository = mock()

    whenever(userRepository.user).thenReturn(userFlow)
    whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHouseholdFlow)
    whenever(foodItemRepository.foodItems).thenReturn(foodItemsFlow)
    whenever(recipeRepository.selectedRecipe).thenReturn(selectedRecipeFlow)

    // Set default user
    val currentUser =
        User(
            uid = "currentUserUID",
            username = "TestUser",
            selectedHouseholdUID = "h1",
            email = "test@example.com")
    userFlow.value = currentUser

    // Set default household
    val household =
        HouseHold(
            uid = "h1",
            name = "TestHousehold",
            sharedRecipes = emptyList(),
            members = listOf("currentUserUID", "otherUserUID"),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    selectedHouseholdFlow.value = household

    // Set default recipe
    val defaultRecipe =
        Recipe(
            uid = "r1",
            name = "Test Recipe",
            ingredients =
                listOf(
                    Ingredient(name = "Tomato", quantity = Quantity(amount = 2.0)),
                    Ingredient(name = "Cheese", quantity = Quantity(amount = 1.0))),
            instructions = listOf("Cut tomatoes", "Add cheese", "Serve"),
            servings = 2f,
            time = Duration.ZERO)
    selectedRecipeFlow.value = defaultRecipe

    // Set default food items
    val tomatoItem =
        FoodItem(
            uid = "f1",
            owner = "currentUserUID",
            foodFacts =
                FoodFacts(
                    name = "Tomato", quantity = Quantity(amount = 5.0, unit = FoodUnit.COUNT)))
    val cheeseItem =
        FoodItem(
            uid = "f2",
            owner = "otherUserUID",
            foodFacts =
                FoodFacts(
                    name = "Cheese", quantity = Quantity(amount = 3.0, unit = FoodUnit.COUNT)))
    foodItemsFlow.value = listOf(tomatoItem, cheeseItem)

    viewModel =
        ExecuteRecipeViewModel(
            houseHoldRepository, foodItemRepository, recipeRepository, userRepository)

    // Ensure all initial emissions have time to happen
    runTest { advanceUntilIdle() }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state is SelectServings`() = runTest {
    assertTrue(viewModel.state.value is RecipeExecutionState.SelectServings)
    assertEquals(2f, viewModel.servings.value)
    assertEquals("Tomato", viewModel.currentIngredientName.value)
    assertEquals(0, viewModel.currentInstructionIndex.value)
  }

  @Test
  fun `nextState transitions from SelectServings to SelectFood`() = runTest {
    viewModel.nextState()
    assertTrue(viewModel.state.value is RecipeExecutionState.SelectFood)
  }

  @Test
  fun `previousState from SelectFood returns to SelectServings`() = runTest {
    viewModel.nextState() // Now in SelectFood
    viewModel.previousState()
    assertTrue(viewModel.state.value is RecipeExecutionState.SelectServings)
  }

  @Test
  fun `updateServings changes servings value`() = runTest {
    viewModel.updateServings(4f)
    assertEquals(4f, viewModel.servings.value)
  }

  @Test
  fun `temporarilyConsumeItems reduces available food items`() = runTest {
    // Currently we have tomatoItem with 5 pieces and cheeseItem with 3 pieces
    val tomatoItem = foodItemsFlow.value.first { it.foodFacts.name == "Tomato" }
    val cheeseItem = foodItemsFlow.value.first { it.foodFacts.name == "Cheese" }

    viewModel.temporarilyConsumeItems(listOf(tomatoItem, cheeseItem), listOf(2f, 1f))
    advanceUntilIdle()

    val updatedItems = viewModel.foodItems.value
    val updatedTomato = updatedItems.find { it.foodFacts.name == "Tomato" }!!
    val updatedCheese = updatedItems.find { it.foodFacts.name == "Cheese" }!!

    assertEquals(3.0, updatedTomato.foodFacts.quantity.amount) // 5 - 2 consumed
    assertEquals(2.0, updatedCheese.foodFacts.quantity.amount) // 3 - 1 consumed
  }

  @Test
  fun `consumeSelectedItems updates Firestore and rat points`() = runTest {
    // Select some items not owned by current user to trigger rat points update
    val cheeseItem = viewModel.foodItems.value.first { it.foodFacts.name == "Cheese" }
    viewModel.selectFoodItemForIngredient("Cheese", cheeseItem, 1f)
    advanceUntilIdle()

    viewModel.consumeSelectedItems()
    advanceUntilIdle()

    // Now verify that rat points are updated here
    verify(foodItemRepository).setFoodItems(eq("h1"), eq(viewModel.foodItems.value))
    verify(houseHoldRepository).updateRatPoints(eq("h1"), argThat { this["currentUserUID"] == 1L })
  }

  @Test
  fun `nextIngredient updates current ingredient name`() = runTest {
    assertEquals("Tomato", viewModel.currentIngredientName.value)
    viewModel.nextIngredient()
    advanceUntilIdle()
    assertEquals("Cheese", viewModel.currentIngredientName.value)
    assertFalse(viewModel.hasMoreIngredients()) // only two ingredients total
  }

  @Test
  fun `selectFoodItemForIngredient updates selectedFoodItemsForIngredients`() = runTest {
    val tomatoItem = viewModel.foodItems.value.first { it.foodFacts.name == "Tomato" }
    viewModel.selectFoodItemForIngredient("Tomato", tomatoItem, 2f)
    advanceUntilIdle()

    val selectedMap = viewModel.selectedFoodItemsForIngredients.value
    assertTrue("Tomato" in selectedMap.keys)
    val itemsForTomato = selectedMap["Tomato"]!!
    assertEquals(1, itemsForTomato.size)
    assertEquals(2.0, itemsForTomato[0].foodFacts.quantity.amount)
  }

  @Test
  fun `instruction navigation works correctly`() = runTest {
    // Ensure the initial emission has occurred
    advanceUntilIdle()

    val instructions = selectedRecipeFlow.value!!.instructions
    assertEquals("Cut tomatoes", viewModel.currentInstruction.value)

    viewModel.nextInstruction()
    advanceUntilIdle()
    assertEquals("Add cheese", viewModel.currentInstruction.value)
    assertTrue(viewModel.hasMoreInstructions())

    viewModel.nextInstruction()
    advanceUntilIdle()
    assertEquals("Serve", viewModel.currentInstruction.value)
    assertFalse(viewModel.hasMoreInstructions())

    viewModel.previousInstruction()
    advanceUntilIdle()
    assertEquals("Add cheese", viewModel.currentInstruction.value)
    assertTrue(viewModel.hasPreviousInstructions())
  }
}
