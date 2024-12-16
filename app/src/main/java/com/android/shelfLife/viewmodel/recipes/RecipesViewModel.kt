package com.android.shelfLife.viewmodel.recipes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.household.HouseHoldRepository
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

/**
 * ViewModel responsible for managing the user's recipes, including filtering, searching, and
 * selecting recipes. Also handles UI state management for the FAB (Floating Action Button) and drawer.
 *
 * @property userRepository Repository for managing user-related data.
 * @property recipeRepository Repository for managing recipes.
 * @property houseHoldRepository Repository for managing households.
 */
@HiltViewModel
class RecipesViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    /** Flow of recipes associated with the user. */
    val userRecipes = recipeRepository.recipes

    /** State of the FAB (Floating Action Button) expansion. */
    val fabExpanded = mutableStateOf(false)

    /** State of the navigation drawer. */
    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()

    /** Available filters for recipes based on their type. */
    private val FILTERS =
        mapOf(
            "Basic" to RecipeType.BASIC,
            "High protein" to RecipeType.HIGH_PROTEIN,
            "Low calories" to RecipeType.LOW_CALORIE,
            "Personal" to RecipeType.PERSONAL)

    /** List of filter names for displaying in the UI. */
    var filters = FILTERS.keys.toList()

    /** State of the selected filters. */
    private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
    val selectedFilters = _selectedFilters.asStateFlow()

    /** State of the search query. */
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    /**
     * Flow of recipes filtered based on selected filters and search query.
     */
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

    /** Current user data. */
    val user = userRepository.user

    /** Current household data. */
    val household = houseHoldRepository.selectedHousehold

    /**
     * Selects a recipe for viewing or editing.
     *
     * @param recipe The recipe to select, or `null` to clear the selection.
     */
    fun selectRecipe(recipe: Recipe?) {
        recipeRepository.selectRecipe(recipe)
    }

    /** Expands the FAB (Floating Action Button). */
    fun expandFab() {
        fabExpanded.value = true
    }

    /** Shrinks the FAB (Floating Action Button). */
    fun shrinkFab() {
        fabExpanded.value = false
    }

    /**
     * Toggles the state of a filter (add/remove).
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
     * Updates the search query for filtering recipes.
     *
     * @param newQuery The new query string.
     */
    fun changeQuery(newQuery: String) {
        _query.value = newQuery
    }
}