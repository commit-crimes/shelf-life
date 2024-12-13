package com.android.shelfLife.viewmodel.recipes

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
open class RecipeGenerationViewModel
@Inject
constructor(
    private val recipeRepository: RecipeRepository,
    private val recipeGeneratorRepository: RecipeGeneratorRepository,
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {

  companion object {
    private const val CREATION_STEP_COUNT = 3
  }

  private val _recipePrompt = MutableStateFlow<RecipePrompt>(RecipePrompt(name = ""))
  open val recipePrompt: StateFlow<RecipePrompt> = _recipePrompt.asStateFlow()

  private val _currentGeneratedRecipe = MutableStateFlow<Recipe?>(null)
  open val currentGeneratedRecipe: StateFlow<Recipe?> = _currentGeneratedRecipe.asStateFlow()

  private val _currentStep = MutableStateFlow(0)
  open val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

  private val _selectedFoodItemsUids = MutableStateFlow<List<String>>(emptyList())

  private val _availableFoodItems = MutableStateFlow<List<FoodItem>>(foodItemRepository.foodItems.value) //food items that are still available to be selected
  open val availableFoodItems: StateFlow<List<FoodItem>> = _availableFoodItems.asStateFlow()

  private val _selectedFoodItems = MutableStateFlow<List<FoodItem>>(emptyList()) //food items that have been selected
  open val selectedFoodItems: StateFlow<List<FoodItem>> = _selectedFoodItems.asStateFlow()

  val _isGeneratingRecipe = MutableStateFlow(false)
  open val isGeneratingRecipe: StateFlow<Boolean> = _isGeneratingRecipe.asStateFlow()

  fun selectFoodItem(foodItem: FoodItem) {
    _selectedFoodItemsUids.value += listOf(foodItem.uid)
    _updateFoodItemSelection()
  }

  fun deselectFoodItem(foodItem: FoodItem) {
    _selectedFoodItemsUids.value = _selectedFoodItemsUids.value.filter { it != foodItem.uid }
    _updateFoodItemSelection()
  }

  fun _updateFoodItemSelection() { //update our lists of selected and available food items, and update the recipe prompt aswell
    _availableFoodItems.value = foodItemRepository.foodItems.value.filter { it.uid !in _selectedFoodItemsUids.value }
    _selectedFoodItems.value = foodItemRepository.foodItems.value.filter { it.uid in _selectedFoodItemsUids.value }
    _recipePrompt.value = _recipePrompt.value.copy(ingredients = _selectedFoodItems.value.toMutableStateList())
  }

  fun updateRecipePrompt(prompt: RecipePrompt) {
    _recipePrompt.value = prompt
  }

  fun nextStep() {
    _currentStep.value += 1
  }

  fun previousStep() {
    _currentStep.value -= 1
  }

  fun resetSteps() {
    _currentStep.value = 0
  }

  fun isLastStep(): Boolean {
    return _currentStep.value == (CREATION_STEP_COUNT-1)
  }

  /** Generates a recipe based on the current prompt. */
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
      // Update the state with the generated recipe
      recipeRepository.selectRecipe(recipe) //select the recipe so individual recipe view can show it
      _currentGeneratedRecipe.value = recipe
      _isGeneratingRecipe.value = false
      onSuccess(recipe)
    }
  }

  /** Accepts the current generated recipe and saves it to the repository. */
  fun acceptGeneratedRecipe(onSuccess: () -> Unit) {
    val recipe = _currentGeneratedRecipe.value
    if (recipe != null) {
      viewModelScope.launch {
        recipeRepository.addRecipe(recipe.copy(uid = recipeRepository.getUid(), workInProgress = false))
        onSuccess()
      }
    } else {
      Log.e("RecipeGenerationViewModel", "No generated recipe to accept")
    }
  }
}
