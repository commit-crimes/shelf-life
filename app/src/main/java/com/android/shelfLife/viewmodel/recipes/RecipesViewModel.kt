package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class RecipesViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
): ViewModel(){

    val userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val  filteredRecipeList = MutableStateFlow<List<Recipe>>(emptyList())
    val fabExpanded = MutableStateFlow(false)
    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()
    val filters = listOf("Soon to expire", "Only household items", "High protein", "Low calories", "Personal")
    var selectedFilters = MutableStateFlow<List<String>>(emptyList())

    init {
        if(userRepository.user.value != null){
            userRecipes.value = populateUserRecipes()
        }
    }

    private fun populateUserRecipes(): List<Recipe> {
        return recipeRepository.recipes.value.filter {
            recipe -> (recipe.uid in userRepository.user.value!!.recipeUIDs) }
    }

    fun selectRecipe(recipe:Recipe){
        if(recipe != null){
            recipeRepository.selectRecipe(recipe)
        }
    }

    fun expandFab(){
        fabExpanded.value = true
    }

    fun shrinkFab(){
        fabExpanded.value = false
    }

    fun clickOnFilter(filter : String){
        selectedFilters.value = if (selectedFilters.value.contains(filter)) {
            selectedFilters.value - filter // Remove the filter if it exists
        } else {
            selectedFilters.value + filter // Add the filter if it doesn't exist
        }
    }






    /**
     * Filters a list of recipes based on selected filters and a search query.
     *
     * @param listRecipes The complete list of recipes to be filtered.
     * @param selectedFilters A list of filter criteria. Each filter is matched against the recipe's
     *   types. If the list is empty, no filter is applied based on this parameter.
     * @param query A search string used to filter recipes by their names. If the string is empty, no
     *   filtering is applied based on this parameter.
     * @return A list of recipes that match both the selected filters and the search query.
     *     - Recipes are included if they match at least one filter from `selectedFilters` (if
     *       provided).
     *     - Recipes are included if their names contain the `query` string (case-insensitive, if
     *       provided).
     *     - If both `selectedFilters` and `query` are empty, the original list is returned without any
     *       filtering.
     */
    fun filterRecipes(
        listRecipes: List<Recipe>,
        selectedFilters: List<String>,
        query: String
    ): List<Recipe> {
        // Combined filtering based on selected filters and search query
        val filteredRecipes =
            listRecipes.filter { recipe ->
                // Check if recipe matches selected filters
                (selectedFilters.isEmpty() ||
                        selectedFilters.any { filter ->
                            recipe.recipeType == stringToSearchRecipeType(filter)
                        }) &&
                        // Check if recipe matches the search query
                        (query.isEmpty() || recipe.name.contains(query, ignoreCase = true))
            }

        return filteredRecipes
    }

    /**
     * Converts a string representation of a recipe type into a corresponding
     * `RecipesRepository.SearchRecipeType` enumeration value.
     *
     * @param string A string describing the type of recipe to search for. Possible values include:
     *     - "Soon to expire": Recipes with ingredients that are nearing expiration.
     *     - "Only household items": Recipes using only ingredients available in the household.
     *     - "High protein": Recipes with a high protein content.
     *     - "Low calories": Recipes with low caloric content.
     *
     * @return The corresponding `RecipesRepository.SearchRecipeType` enum value.
     * @throws IllegalArgumentException If the input string does not match any known recipe type.
     */
    private fun stringToSearchRecipeType(string: String): RecipeType {
        return when (string) {
            "Soon to expire" -> RecipeType.USE_SOON_TO_EXPIRE
            "Only household items" -> RecipeType.USE_ONLY_HOUSEHOLD_ITEMS
            "High protein" -> RecipeType.HIGH_PROTEIN
            "Low calories" -> RecipeType.LOW_CALORIE
            "Personal" -> RecipeType.PERSONAL
            else -> throw IllegalArgumentException("Unknown filter: $string")
        }
    }


}