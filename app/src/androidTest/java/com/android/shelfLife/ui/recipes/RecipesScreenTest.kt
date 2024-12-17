package com.android.shelfLife.ui.recipes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import com.android.shelfLife.model.recipe.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RecipesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavigationActions = mock(NavigationActions::class.java)
    private val mockViewModel = mock(RecipesViewModel::class.java)

    // Helper to provide dummy recipes
    private fun provideFakeRecipes(): StateFlow<List<Recipe>> {
        val recipes = listOf(
            Recipe(uid = "fakeRecipe1",
                name = "Pasta",
                servings = 2F,
                time = 15.0.minutes,
                ingredients = listOf(Ingredient("Pasta", Quantity(200.0, FoodUnit.GRAM))),
                instructions = listOf("Put the water to boil", "in it goes", "strain it", "eat up")),
            Recipe(uid = "fakeRecipe2",
                name = "Pizza",
                servings = 2F,
                time = 15.0.minutes,
                ingredients = listOf(Ingredient("pizza", Quantity(2.0, FoodUnit.COUNT))),
                instructions = listOf("Turn on oven", "in it goes", "let it rest", "eat up"))
        )
        return MutableStateFlow(recipes)
    }

    @Test
    fun testRecipeList_isDisplayedCorrectly() = runTest {
        // Mock ViewModel states
        val fakeRecipes = provideFakeRecipes()
        val fakeQuery = MutableStateFlow("")
        val fakeFilters = MutableStateFlow(emptyList<String>())

        // Stub methods
        mockViewModel.apply {
            org.mockito.Mockito.`when`(filteredRecipes).thenReturn(fakeRecipes)
            org.mockito.Mockito.`when`(query).thenReturn(fakeQuery)
            org.mockito.Mockito.`when`(selectedFilters).thenReturn(fakeFilters)
        }

        // Launch the RecipesScreen
        composeTestRule.setContent {
            RecipesScreen(navigationActions = mockNavigationActions, recipesViewModel = mockViewModel)
        }

        // Check if list items are displayed
        composeTestRule.onNodeWithTag("recipesList").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pasta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()

        // Check FAB visibility
        composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed()
    }

    @Test
    fun testNoRecipesAvailableText_isDisplayed() {
        // Mock empty recipe list
        val fakeRecipes = MutableStateFlow(emptyList<Recipe>())

        mockViewModel.apply {
            org.mockito.Mockito.`when`(filteredRecipes).thenReturn(fakeRecipes)
        }

        // Launch the RecipesScreen
        composeTestRule.setContent {
            RecipesScreen(navigationActions = mockNavigationActions, recipesViewModel = mockViewModel)
        }

        // Verify "No recipes available" text is displayed
        composeTestRule.onNodeWithTag("noRecipesAvailableText").assertIsDisplayed()
    }

    @Test
    fun testSearchBar_isDisplayedAndInteractable() {
        val fakeRecipes = provideFakeRecipes()
        val fakeQuery = MutableStateFlow("")

        mockViewModel.apply {
            org.mockito.Mockito.`when`(filteredRecipes).thenReturn(fakeRecipes)
            org.mockito.Mockito.`when`(query).thenReturn(fakeQuery)
        }

        // Launch RecipesScreen
        composeTestRule.setContent {
            RecipesScreen(navigationActions = mockNavigationActions, recipesViewModel = mockViewModel)
        }

        // Check Search Bar
        composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("searchBar").performTextInput("Pasta")

        // Verify search query change
        verify(mockViewModel).changeQuery("Pasta")
    }

    @Test
    fun testAddRecipeFAB_navigatesToAddRecipe() {
        val fakeRecipes = provideFakeRecipes()

        mockViewModel.apply {
            org.mockito.Mockito.`when`(filteredRecipes).thenReturn(fakeRecipes)
        }

        // Launch RecipesScreen
        composeTestRule.setContent {
            RecipesScreen(navigationActions = mockNavigationActions, recipesViewModel = mockViewModel)
        }

        // Click FAB
        composeTestRule.onNodeWithTag("addRecipeFab").performClick()

        // Verify navigation
        verify(mockNavigationActions).navigateTo(com.android.shelfLife.ui.navigation.Screen.ADD_RECIPE)
    }
}
