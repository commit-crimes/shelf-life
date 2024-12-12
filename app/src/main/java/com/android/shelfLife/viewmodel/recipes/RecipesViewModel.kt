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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
          "Personal" to RecipeType.PERSONAL)

  var filters = FILTERS.keys.toList()

  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private val _query = MutableStateFlow("")
  val query = _query.asStateFlow()

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

  val user = userRepository.user
  val household = houseHoldRepository.selectedHousehold

  fun selectRecipe(recipe: Recipe?) {
    recipeRepository.selectRecipe(recipe)
  }

  fun expandFab() {
    fabExpanded.value = true
  }

  fun shrinkFab() {
    fabExpanded.value = false
  }

  fun clickOnFilter(filter: String) {
    _selectedFilters.value =
        if (selectedFilters.value.contains(filter)) {
          selectedFilters.value - filter // Remove filter
        } else {
          selectedFilters.value + filter // Add filter
        }
  }

  fun changeQuery(newQuery: String) {
    _query.value = newQuery
  }
}
