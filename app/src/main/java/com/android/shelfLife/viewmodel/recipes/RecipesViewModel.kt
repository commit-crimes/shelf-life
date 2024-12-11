package com.android.shelfLife.viewmodel.recipes

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecipesViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

  private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
  val userRecipes = _userRecipes.asStateFlow()

  private val _filteredRecipeList = MutableStateFlow<List<Recipe>>(emptyList())
  val filteredRecipeList = _filteredRecipeList.asStateFlow()

  val fabExpanded = MutableStateFlow(false)

  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  val filters =
      listOf("Soon to expire", "Only household items", "High protein", "Low calories", "Personal")

  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private var _query = MutableStateFlow<String>("")
  val query = _query.asStateFlow()

  var user = userRepository.user
  var household = userRepository.selectedHousehold

  init {
    viewModelScope.launch {
      if (user.value != null) {
        recipeRepository.getRecipes(userRepository.user.value!!.recipeUIDs)
        _userRecipes.value = recipeRepository.recipes.value
        filterRecipes()
      }
    }
  }

  /**
   * Selects a recipe to be highlighted or worked on.
   *
   * @param recipe The recipe to select.
   */
  fun selectRecipe(recipe: Recipe) {
    if (recipe != null) {
      recipeRepository.selectRecipe(recipe)
    }
  }

  /** Expands the floating action button (FAB) to indicate an expanded state. */
  fun expandFab() {
    fabExpanded.value = true
  }

  /** Shrinks the floating action button (FAB) to indicate a collapsed state. */
  fun shrinkFab() {
    fabExpanded.value = false
  }

  /**
   * Toggles a filter in the list of selected filters.
   *
   * @param filter The filter to toggle. If it is already selected, it is removed. Otherwise, it is
   *   added.
   */
  fun clickOnFilter(filter: String) {
    _selectedFilters.value =
        if (selectedFilters.value.contains(filter)) {
          selectedFilters.value - filter // Remove the filter if it exists
        } else {
          selectedFilters.value + filter // Add the filter if it doesn't exist
        }
    filterRecipes()
  }

  /**
   * Updates the search query used to filter recipes and re-applies filtering logic.
   *
   * @param newQuery The new search query string.
   */
  fun changeQuery(newQuery: String) {
    _query.value = newQuery
    filterRecipes()
  }

  /**
   * Filters the user's recipe list based on selected filters and the search query. Combines filter
   * and search criteria to generate the final filtered list.
   */
  private fun filterRecipes() {
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

  /**
   * Converts a filter string into a corresponding RecipeType enum.
   *
   * @param string The filter string to convert.
   * @return The corresponding RecipeType for the given string.
   * @throws IllegalArgumentException If the string does not match a valid filter.
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
