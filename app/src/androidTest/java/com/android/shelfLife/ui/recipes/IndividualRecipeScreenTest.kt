package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
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
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltAndroidTest
class IndividualRecipeScreenTest {
    @get:Rule(order = 0)
    val hiltAndroidTestRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navigationActions: NavigationActions
    private lateinit var houseHold: HouseHold

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var recipeRepository: RecipeRepository
    @Inject lateinit var houseHoldRepository: HouseHoldRepository
    @Inject lateinit var listFoodItemsRepository: FoodItemRepository


    private lateinit var individualRecipeViewModel: IndividualRecipeViewModel

    // This section might need to be moved to it's own file
    private val selectedHousehold = MutableStateFlow<HouseHold?>(null)
    private val householdToEdit = MutableStateFlow<HouseHold?>(null)
    private val households = MutableStateFlow<List<HouseHold>>(emptyList())
    private val foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    private val user = MutableStateFlow<User?>(null)
    private val selectedRecipe = MutableStateFlow<Recipe?>(null)
    private val recipeList = MutableStateFlow<List<Recipe>>(emptyList())

    private lateinit var instrumentationContext: android.content.Context

    @Before
    fun setUp() {
        hiltAndroidTestRule.inject()
        navigationActions = mock()
        recipeRepository = mock()

        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        whenever(navigationActions.currentRoute()).thenReturn(Route.RECIPES)
        whenever(houseHoldRepository.selectedHousehold).thenReturn(selectedHousehold.asStateFlow())
        whenever(houseHoldRepository.households).thenReturn(households.asStateFlow())
        whenever(houseHoldRepository.householdToEdit).thenReturn(householdToEdit.asStateFlow())
        whenever(listFoodItemsRepository.foodItems).thenReturn(foodItems.asStateFlow())
        whenever(userRepository.user).thenReturn(user.asStateFlow())
        whenever(recipeRepository.selectedRecipe).thenReturn(selectedRecipe.asStateFlow())
        whenever(recipeRepository.recipes).thenReturn(recipeList.asStateFlow())

        // Create a FoodItem to be used in tests
        val foodFacts =
            FoodFacts(
                name = "Apple",
                barcode = "123456789",
                quantity = Quantity(5.0, FoodUnit.COUNT),
                category = FoodCategory.FRUIT)
        val foodItem =
            FoodItem(
                uid = "foodItem1",
                foodFacts = foodFacts,
                expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day,
                owner = "testOwner")



        val recipe1 =
            Recipe(
                uid = "recipe1",
                name = "Pasta Bolognese",
                instructions = listOf("Cook  meat", " boil water", "add pasta", "eat"),
                servings = 2F,
                time = 45.minutes,
                ingredients = listOf(
                    Ingredient("Pasta", Quantity(200.0, FoodUnit.GRAM)),
                    Ingredient("Ground Beef", Quantity(100.0, FoodUnit.GRAM)),
                    Ingredient("Tomato Sauce", Quantity(100.0, FoodUnit.ML)),
                    Ingredient("Onion", Quantity(2.0, FoodUnit.COUNT))
                ),
                recipeType = RecipeType.PERSONAL
            )

        val recipe2 =
            Recipe(
                uid = "recipe2",
                name = "Pizza",
                instructions = listOf("Heat up the oven", "Place pizza until cooked", "eat"),
                servings = 2F,
                time = 25.minutes,
                ingredients = listOf(
                    Ingredient("Pizza", Quantity(1.0, FoodUnit.COUNT))
                ),
                recipeType = RecipeType.BASIC
            )

        selectedRecipe.value = recipe1
        recipeList.value = listOf(recipe1, recipe2)

        // Initialize the household with the food item
        houseHold =
            HouseHold(
                uid = "1",
                name = "Test Household",
                members = listOf("John", "Doe"),
                sharedRecipes = emptyList(),
                ratPoints = emptyMap(),
                stinkyPoints = emptyMap()
            )
        households.value = listOf(houseHold)
        selectedHousehold.value = houseHold
        foodItems.value = listOf(foodItem)

        user.value = User(
            uid = "user1",
            username = "Tester",
            email = "test_email@test.com",
            selectedHouseholdUID = "1",
            householdUIDs = listOf("1"),
            recipeUIDs = listOf("recipe1", "recipe2"),
        )

        individualRecipeViewModel =
            IndividualRecipeViewModel(recipeRepository, userRepository)
    }

    @Test
    fun verifyBasicUIElements(){
        composeTestRule.setContent { IndividualRecipeScreen(navigationActions, individualRecipeViewModel) }

        composeTestRule.onNodeWithTag("individualRecipesScreen").isDisplayed()
    }

}