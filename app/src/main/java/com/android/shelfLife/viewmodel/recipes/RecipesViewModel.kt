package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.User
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

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes = _userRecipes.asStateFlow()

    private val _filteredRecipeList = MutableStateFlow<List<Recipe>>(emptyList())
    val  filteredRecipeList = _filteredRecipeList.asStateFlow()

    val fabExpanded = MutableStateFlow(false)

    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()

    val filters = listOf("Soon to expire", "Only household items", "High protein", "Low calories", "Personal")

    private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
    val selectedFilters = _selectedFilters.asStateFlow()

    private var _query = MutableStateFlow<String>("")
    val query = _query.asStateFlow()

    var user = userRepository.user
    var household = userRepository.selectedHousehold


    init {
        if(user.value != null){
            _userRecipes.value = populateUserRecipes()
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
        _selectedFilters.value = if (selectedFilters.value.contains(filter)) {
            selectedFilters.value - filter // Remove the filter if it exists
        } else {
            selectedFilters.value + filter // Add the filter if it doesn't exist
        }
        filterRecipes()
    }

    fun changeQuery(newQuery : String){
        _query.value = newQuery
        filterRecipes()
    }

    fun filterRecipes(){
        // Combined filtering based on selected filters and search query
        _filteredRecipeList.value =
            userRecipes.value.filter { recipe ->
                // Check if recipe matches selected filters
                (selectedFilters.value.isEmpty() ||
                        selectedFilters.value.any { filter ->
                            recipe.recipeType == stringToSearchRecipeType(filter)
                        }) &&
                        // Check if recipe matches the search query
                        (query.value.isEmpty() || recipe.name.contains(query.value, ignoreCase = true))
            }
    }

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