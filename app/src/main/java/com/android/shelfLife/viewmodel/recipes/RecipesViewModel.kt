package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for managing recipes.
 *
 * @property userRepository Repository for user data.
 * @property recipeRepository Repository for recipe data.
 * @property houseHoldRepository Repository for household data.
 */
@HiltViewModel
class RecipesViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

  /** Flow of user recipes. */
  val userRecipes = recipeRepository.recipes

  /** State for the Floating Action Button (FAB) expansion. */
  val fabExpanded = mutableStateOf(false)

  /** State for the drawer. */
  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  /** Map of available filters. */
  private val FILTERS =
      mapOf(
          "Basic" to RecipeType.BASIC,
          "High protein" to RecipeType.HIGH_PROTEIN,
          "Low calories" to RecipeType.LOW_CALORIE,
          "Personal" to RecipeType.PERSONAL)

  /** List of filter keys. */
  var filters = FILTERS.keys.toList()

  /** State for selected filters. */
  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  /** State for the search query. */
  private val _query = MutableStateFlow("")
  val query = _query.asStateFlow()

  /** Flow of filtered recipes based on selected filters and search query. */
  val filteredRecipes =
      combine(userRecipes, selectedFilters, query) { recipes, currentFilters, currentQuery ->
            recipes.filter { recipe ->
              // Check if the recipe matches selected filters
              (currentFilters.isEmpty() ||
                  currentFilters.any { filter -> recipe.recipeType == FILTERS[filter] }) &&
                  // Check if the recipe matches the search query
                  (currentQuery.isEmpty() || recipe.name.contains(currentQuery, ignoreCase = true))
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  /** Flow of the current user. */
  val user = userRepository.user

  /** Flow of the selected household. */
  val household = houseHoldRepository.selectedHousehold

  /**
   * Selects a recipe.
   *
   * @param recipe The recipe to select.
   */
  fun selectRecipe(recipe: Recipe?) {
    recipeRepository.selectRecipe(recipe)
  }

  /** Expands the Floating Action Button (FAB). */
  fun expandFab() {
    fabExpanded.value = true
  }

  /** Shrinks the Floating Action Button (FAB). */
  fun shrinkFab() {
    fabExpanded.value = false
  }

  /**
   * Toggles the selection of a filter.
   *
   * @param filter The filter to toggle.
   */
  fun clickOnFilter(filter: String) {
    _selectedFilters.value =
        if (selectedFilters.value.contains(filter)) {
          selectedFilters.value - filter // Remove filter
        } else {
          selectedFilters.value + filter // Add filter
        }
  }

  /**
   * Updates the search query.
   *
   * @param newQuery The new search query.
   */
  fun changeQuery(newQuery: String) {
    _query.value = newQuery
  }
}
