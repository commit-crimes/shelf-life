package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecipesViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

  val userRecipes = recipeRepository.recipes



  val fabExpanded = MutableStateFlow(false)

  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  private val FILTERS =
      mapOf(
          "Soon to expire" to RecipeType.USE_SOON_TO_EXPIRE,
          "Only household items" to RecipeType.USE_ONLY_HOUSEHOLD_ITEMS,
          "High protein" to RecipeType.HIGH_PROTEIN,
          "Low calories" to RecipeType.LOW_CALORIE,
          "Personal" to RecipeType.PERSONAL) // cannot make a map const
  var filters = FILTERS.keys.toList()

  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private var _query = MutableStateFlow<String>("")
  val query = _query.asStateFlow()

  val filteredRecipes: StateFlow<List<Recipe>> = combine(
    userRecipes,
    selectedFilters,
    query
  ) { recipes, filters, currentQuery ->
    recipes.filter { recipe ->
      // Check if the recipe matches selected filters
      (filters.isEmpty() || filters.any { filter -> recipe.recipeType == FILTERS[filter] }) &&
              // Check if the recipe matches the search query
              (currentQuery.isEmpty() || recipe.name.contains(currentQuery, ignoreCase = true))
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  var user = userRepository.user
  var household = houseHoldRepository.selectedHousehold

  init {
    viewModelScope.launch {
      if (user.value != null) {
        recipeRepository.getRecipes(userRepository.user.value!!.recipeUIDs)
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
    //filterRecipes()
  }

  /**
   * Updates the search query used to filter recipes and re-applies filtering logic.
   *
   * @param newQuery The new search query string.
   */
  fun changeQuery(newQuery: String) {
    _query.value = newQuery
    //filterRecipes()
  }

  /**
   * Filters the user's recipe list based on selected filters and the search query. Combines filter
   * and search criteria to generate the final filtered list.
   */
  private fun filterRecipes() {

  }
}
