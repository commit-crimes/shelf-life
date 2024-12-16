package com.android.shelfLife.viewmodel.recipes

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the recipe generation process. It interacts with the
 * repositories to fetch food items, create recipe prompts, and save generated recipes.
 *
 * @property recipeRepository Repository for managing recipes.
 * @property recipeGeneratorRepository Repository for generating recipes based on prompts.
 * @property foodItemRepository Repository for managing food items.
 * @property userRepository Repository for managing user-related data.
 */
@HiltViewModel
open class RecipeGenerationViewModel
@Inject
constructor(
  private val recipeRepository: RecipeRepository,
  private val recipeGeneratorRepository: RecipeGeneratorRepository,
  private val foodItemRepository: FoodItemRepository,
  private val userRepository: UserRepository
) : ViewModel() {

  companion object {
    private const val CREATION_STEP_COUNT = 3
  }

  private val _recipePrompt = MutableStateFlow<RecipePrompt>(RecipePrompt(name = ""))
  open val recipePrompt: StateFlow<RecipePrompt> = _recipePrompt.asStateFlow()

  private val _currentGeneratedRecipe = MutableStateFlow<Recipe?>(null)
  open val currentGeneratedRecipe: StateFlow<Recipe?> = _currentGeneratedRecipe.asStateFlow()

  private val _selectedFoodItemsUids = MutableStateFlow<List<String>>(emptyList())

  private val _availableFoodItems =
    MutableStateFlow<List<FoodItem>>(foodItemRepository.foodItems.value)
  open val availableFoodItems: StateFlow<List<FoodItem>> = _availableFoodItems.asStateFlow()

  private val _selectedFoodItems =
    MutableStateFlow<List<FoodItem>>(emptyList())
  open val selectedFoodItems: StateFlow<List<FoodItem>> = _selectedFoodItems.asStateFlow()

  val _isGeneratingRecipe = MutableStateFlow(false)
  open val isGeneratingRecipe: StateFlow<Boolean> = _isGeneratingRecipe.asStateFlow()

  /**
   * Adds a food item to the selected list.
   *
   * @param foodItem The food item to select.
   */
  fun selectFoodItem(foodItem: FoodItem) {
    _selectedFoodItemsUids.value += listOf(foodItem.uid)
    _updateFoodItemSelection()
  }

  /**
   * Removes a food item from the selected list.
   *
   * @param foodItem The food item to deselect.
   */
  fun deselectFoodItem(foodItem: FoodItem) {
    _selectedFoodItemsUids.value = _selectedFoodItemsUids.value.filter { it != foodItem.uid }
    _updateFoodItemSelection()
  }

  /**
   * Updates the lists of selected and available food items and updates the recipe prompt.
   */
  private fun _updateFoodItemSelection() {
    _availableFoodItems.value =
      foodItemRepository.foodItems.value.filter { it.uid !in _selectedFoodItemsUids.value }
    _selectedFoodItems.value =
      foodItemRepository.foodItems.value.filter { it.uid in _selectedFoodItemsUids.value }
    _recipePrompt.value =
      _recipePrompt.value.copy(ingredients = _selectedFoodItems.value.toMutableStateList())
  }

  /**
   * Updates the recipe prompt with the given prompt data.
   *
   * @param prompt The new recipe prompt data.
   */
  fun updateRecipePrompt(prompt: RecipePrompt) {
    _recipePrompt.value = prompt
  }

  /**
   * Generates a recipe based on the current prompt. Updates the state with the generated recipe
   * or an error message.
   *
   * @param onSuccess Callback invoked with the generated recipe on success.
   * @param onFailure Callback invoked with an error message on failure.
   */
  fun generateRecipe(onSuccess: (Recipe) -> Unit, onFailure: (String) -> Unit) {
    _isGeneratingRecipe.value = true
    val prompt = _recipePrompt.value
    viewModelScope.launch {
      val recipe = recipeGeneratorRepository.generateRecipe(prompt)
      if (recipe == null) {
        onFailure("Failed to generate recipe")
        _isGeneratingRecipe.value = false
        return@launch
      }
      recipeRepository.selectRecipe(recipe)
      _currentGeneratedRecipe.value = recipe
      _isGeneratingRecipe.value = false
      onSuccess(recipe)
    }
  }

  /**
   * Accepts the current generated recipe and saves it to the repository.
   *
   * @param onSuccess Callback invoked after successfully saving the recipe.
   */
  fun acceptGeneratedRecipe(onSuccess: () -> Unit) {
    val recipe = _currentGeneratedRecipe.value
    if (recipe != null) {
      viewModelScope.launch {
        val newRecipe = recipe.copy(uid = recipeRepository.getUid(), workInProgress = false)
        recipeRepository.addRecipe(newRecipe)
        userRepository.addRecipeUID(newRecipe.uid)
        onSuccess()
      }
    } else {
      Log.e("RecipeGenerationViewModel", "No generated recipe to accept")
    }
  }
}