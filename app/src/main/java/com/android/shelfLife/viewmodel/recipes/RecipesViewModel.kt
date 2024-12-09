package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.recipes.stringToSearchRecipeType
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
    val fabExpanded = MutableStateFlow(false)
    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()
    val filters = listOf("Soon to expire", "Only household items", "High protein", "Low calories", "Personal")

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



}