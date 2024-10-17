package com.android.shelflife.ui.recipes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class RecipesTest {

    private lateinit var foodItemRepository: FoodItemRepository
    private lateinit var navigationActions: NavigationActions
    private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
    private lateinit var listRecipesViewModel: ListRecipesViewModel
    private lateinit var houseHoldRepository: HouseHoldRepository
    private lateinit var householdViewModel: HouseholdViewModel

    private lateinit var houseHold: HouseHold

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navigationActions = mock()
        foodItemRepository = mock()
        listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
        listRecipesViewModel = ListRecipesViewModel()
        houseHoldRepository = mock()
        householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

        val foodFacts = FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT
        )
        val foodItem = FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // Expires in 1 day
        )
        houseHold = HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf(foodItem)
        )

        mockHouseHoldRepositoryGetHouseholds(listOf(houseHold))
    }

    private fun mockHouseHoldRepositoryGetHouseholds(households: List<HouseHold>) {
        doAnswer { invocation ->
            val onSuccess = invocation.arguments[0] as (List<HouseHold>) -> Unit
            onSuccess(households)
            null
        }.whenever(houseHoldRepository).getHouseholds(any(), any())
    }

    // Helper function to set up the screen with RecipesScreen content
    private fun setUpRecipesScreen() {
        householdViewModel.selectHousehold(houseHold)
        composeTestRule.setContent {
            RecipesScreen(
                navigationActions = navigationActions,
                listRecipesViewModel = listRecipesViewModel,
                householdViewModel = householdViewModel
            )
        }
    }

    // Helper function to check if the basic UI elements are displayed
    private fun verifyBasicUIElements() {
        composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recipeSearchBar").assertIsDisplayed()
    }

    @Test
    fun recipesScreenDisplayedCorrectly() {
        setUpRecipesScreen()
        verifyBasicUIElements()
    }

    @Test
    fun foodItemListIsDisplayedWhenFoodItemsExist() {
        setUpRecipesScreen()
        composeTestRule.onNodeWithTag("recipesList").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()
    }

    @Test
    fun searchFiltersFoodItemList() {
        setUpRecipesScreen()
        composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()

        // Activate the SearchBar and enter the search query
        composeTestRule.onNodeWithTag("searchBar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
            .performTextInput("Paella")

        // Verify that only one recipe card is displayed and contains the text "Paella"
        composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(1)
        composeTestRule
            .onNode(hasText("Paella") and hasAnyAncestor(hasTestTag("recipeSearchBar")))
            .assertIsDisplayed()
    }

    @Test
    fun clickOnRecipeNavigatesToIndividualRecipeScreen() {
        setUpRecipesScreen()
        composeTestRule.onAllNodesWithTag("recipesCards").onFirst().assertExists()

        // Activate the SearchBar and enter the search query
        composeTestRule.onNodeWithTag("searchBar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
            .performTextInput("Tortilla de patata")

        // Click on the recipe and verify navigation
        composeTestRule.onNodeWithTag("recipesCards").performClick()
        composeTestRule.waitForIdle()
        verify(navigationActions)
            .navigateTo(com.android.shelfLife.ui.navigation.Screen.INDIVIDUAL_RECIPE)
    }
}
